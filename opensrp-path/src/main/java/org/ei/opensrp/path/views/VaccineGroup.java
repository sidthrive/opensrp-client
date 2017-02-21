package org.ei.opensrp.path.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildImmunizationActivity;
import org.ei.opensrp.path.db.VaccineRepo;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import util.Utils;

/**
 * Created by Jason Rogena - jrogena@ona.io on 21/02/2017.
 */

public class VaccineGroup extends LinearLayout implements View.OnClickListener, VaccineCard.OnVaccineStateChangeListener {
    private static final String TAG = "VaccineGroup";
    private Context context;
    private TextView nameTV;
    private TextView recordAllTV;
    private GridView vaccinesGV;
    private VaccineCardAdapter vaccineCardAdapter;
    private JSONObject vaccineData;
    private CommonPersonObjectClient childDetails;
    private State state;
    private OnRecordAllClickListener onRecordAllClickListener;
    private SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("dd MMMM, yyyy", Locale.US);

    private static enum State {
        IN_PAST,
        CURRENT,
        IN_FUTURE
    }

    public VaccineGroup(Context context) {
        super(context);
        init(context);
    }

    public VaccineGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VaccineGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VaccineGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_vaccine_group, this, true);
        nameTV = (TextView) findViewById(R.id.name_tv);
        vaccinesGV = (GridView) findViewById(R.id.vaccines_gv);
        recordAllTV = (TextView) findViewById(R.id.record_all_tv);
        recordAllTV.setOnClickListener(this);
    }

    public void setData(JSONObject vaccineData, CommonPersonObjectClient childDetails) {
        this.vaccineData = vaccineData;
        this.childDetails = childDetails;
        updateViews();
    }

    public void updateViews() {
        this.state = State.IN_PAST;
        if (this.vaccineData != null) {
            String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            try {
                Date dob = ChildImmunizationActivity.DATE_FORMAT.parse(dobString);
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                long timeDiff = today.getTimeInMillis() - dob.getTime();

                if (timeDiff < today.getTimeInMillis()) {
                    this.state = State.IN_PAST;
                } else if (timeDiff > (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))) {
                    this.state = State.IN_FUTURE;
                } else {
                    this.state = State.CURRENT;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            updateStatusViews();
            updateVaccineCards();
        }
    }

    private void updateStatusViews() {
        try {
            switch (this.state) {
                case IN_PAST:
                    nameTV.setText(vaccineData.getString("name"));
                    break;
                case CURRENT:
                    nameTV.setText(String.format(context.getString(R.string.due_),
                            vaccineData.getString("name"), context.getString(R.string.today)));
                    break;
                case IN_FUTURE:
                    String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
                    Calendar dobCalender = Calendar.getInstance();
                    dobCalender.setTime(ChildImmunizationActivity.DATE_FORMAT.parse(dobString));
                    dobCalender.add(Calendar.DATE, vaccineData.getInt("days_after_birth_due"));
                    nameTV.setText(String.format(context.getString(R.string.due_),
                            vaccineData.getString("name"),
                            READABLE_DATE_FORMAT.format(dobCalender)));
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ParseException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void updateVaccineCards() {
        if (vaccineCardAdapter == null) {
            try {
                vaccineCardAdapter = new VaccineCardAdapter(context, this);
                vaccinesGV.setAdapter(vaccineCardAdapter);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        if (vaccineCardAdapter != null) {
            vaccineCardAdapter.update();
            toggleRecordAllTV();
        }
    }

    private void toggleRecordAllTV() {
        if (vaccineCardAdapter.getDueVaccines().size() > 0) {
            recordAllTV.setVisibility(VISIBLE);
        } else {
            recordAllTV.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(recordAllTV)) {
            if (onRecordAllClickListener != null && vaccineCardAdapter != null) {
                onRecordAllClickListener.onClick(vaccineCardAdapter.getDueVaccines());
            }
        }
    }

    @Override
    public void onStateChanged(VaccineCard.State newState) {
        updateViews();
    }

    private static class VaccineCardAdapter extends BaseAdapter {
        private final Context context;
        private HashMap<String, VaccineCard> vaccineCards;
        private final VaccineGroup vaccineGroup;

        public VaccineCardAdapter(Context context, VaccineGroup vaccineGroup) throws JSONException {
            this.context = context;
            this.vaccineGroup = vaccineGroup;
            vaccineCards = new HashMap<>();
        }

        @Override
        public int getCount() {
            try {
                return vaccineGroup.vaccineData.getJSONArray("vaccines").length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return vaccineCards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 231231 + position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                JSONObject vaccineData = vaccineGroup.vaccineData.getJSONArray("vaccines")
                        .getJSONObject(position);
                String vaccineName = vaccineData.getString("name");
                if(!vaccineCards.containsKey(vaccineName)) {
                    VaccineCard vaccineCard = new VaccineCard(context);
                    vaccineCard.setOnVaccineStateChangeListener(vaccineGroup);
                    vaccineCard.setId((int) getItemId(position));
                    VaccineWrapper vaccineWrapper = new VaccineWrapper();
                    vaccineWrapper.setName(vaccineName);

                    String dobString = Utils.getValue(vaccineGroup.childDetails.getColumnmaps(), "dob", false);
                    Calendar dobCalender = Calendar.getInstance();
                    dobCalender.setTime(ChildImmunizationActivity.DATE_FORMAT.parse(dobString));
                    dobCalender.add(Calendar.DATE, vaccineGroup.vaccineData.getInt("days_after_birth_due"));
                    vaccineWrapper.setVaccineDate(new DateTime(dobCalender.getTime()));
                    // TODO: get date vaccination was done
                    vaccineCard.setVaccineWrapper(vaccineWrapper);

                    vaccineCards.put(vaccineName, vaccineCard);
                    vaccineGroup.toggleRecordAllTV();
                }

                return vaccineCards.get(vaccineName);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (ParseException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            return null;
        }

        public void update() {
            if (vaccineCards != null) {
                for (VaccineCard curCard : vaccineCards.values()) {
                    if (curCard != null) curCard.updateState();
                }
            }
        }

        public ArrayList<VaccineWrapper> getDueVaccines() {
            ArrayList<VaccineWrapper> dueVaccines = new ArrayList<>();
            if (vaccineCards != null) {
                for (VaccineCard curCard : vaccineCards.values()) {
                    if (curCard != null && (curCard.getState().equals(VaccineCard.State.DUE)
                            || curCard.getState().equals(VaccineCard.State.OVERDUE))) {
                        dueVaccines.add(curCard.getVaccineWrapper());
                    }
                }
            }

            return dueVaccines;
        }
    }

    public void setOnRecordAllClickListener(OnRecordAllClickListener onRecordAllClickListener) {
        this.onRecordAllClickListener = onRecordAllClickListener;
    }

    public static interface OnRecordAllClickListener {
        void onClick(ArrayList<VaccineWrapper> dueVaccines);
    }
}
