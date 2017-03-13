package org.ei.opensrp.path.viewComponents;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.view.ExpandableHeightGridView;
import org.ei.opensrp.path.view.VaccineCard;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import util.Utils;

/**
 * Created by Jason Rogena - jrogena@ona.io on 21/02/2017.
 */

public class ImmunizationRowGroup extends LinearLayout implements View.OnClickListener,
        ImmunizationRowCard.OnVaccineStateChangeListener {
    private static final String TAG = "VaccineGroup";
    private Context context;
    private TextView nameTV;
    private TextView recordAllTV;
    private ExpandableHeightGridView vaccinesGV;
    private ImmunizationRowAdapter vaccineCardAdapter;
    private JSONObject vaccineData;
    private CommonPersonObjectClient childDetails;
    private List<Vaccine> vaccineList;
    private State state;
    public boolean editmode;
    private OnRecordAllClickListener onRecordAllClickListener;
    private OnVaccineClickedListener onVaccineClickedListener;
    private OnVaccineUndoClickListener onVaccineUndoClickListener;
    private SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("dd MMMM, yyyy", Locale.US);

    private static enum State {
        IN_PAST,
        CURRENT,
        IN_FUTURE
    }

    public ImmunizationRowGroup(Context context,boolean editmode) {
        super(context);
        this.editmode = editmode;
        init(context);
    }

    public ImmunizationRowGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImmunizationRowGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CommonPersonObjectClient getChildDetails() {
        return this.childDetails;
    }

    public JSONObject getVaccineData() {
        return this.vaccineData;
    }

    public List<Vaccine> getVaccineList() {
        return this.vaccineList;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImmunizationRowGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_immunization_row_group, this, true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        nameTV = (TextView) findViewById(R.id.name_tv);
        nameTV.setVisibility(GONE);
        vaccinesGV = (ExpandableHeightGridView) findViewById(R.id.vaccines_gv);
        vaccinesGV.setExpanded(true);
        recordAllTV = (TextView) findViewById(R.id.record_all_tv);
        recordAllTV.setOnClickListener(this);
        recordAllTV.setVisibility(GONE);
    }

    public void setData(JSONObject vaccineData, CommonPersonObjectClient childDetails, List<Vaccine> vaccines) {
        this.vaccineData = vaccineData;
        this.childDetails = childDetails;
        this.vaccineList = vaccines;
        updateViews();
    }

    public void setOnVaccineUndoClickListener(OnVaccineUndoClickListener onVaccineUndoClickListener) {
        this.onVaccineUndoClickListener = onVaccineUndoClickListener;
    }

    public void updateViews() {
        this.state = State.IN_PAST;
        if (this.vaccineData != null) {
            String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            DateTime dateTime = new DateTime(dobString);
            Date dob = dateTime.toDate();
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
                    DateTime dateTime = new DateTime(dobString);
                    dobCalender.setTime(dateTime.toDate());
                    dobCalender.add(Calendar.DATE, vaccineData.getInt("days_after_birth_due"));
                    nameTV.setText(String.format(context.getString(R.string.due_),
                            vaccineData.getString("name"),
                            READABLE_DATE_FORMAT.format(dobCalender.getTime())));
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void updateVaccineCards() {
        if (vaccineCardAdapter == null) {
            try {
                vaccineCardAdapter = new ImmunizationRowAdapter(context, this,editmode);
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

    public void toggleRecordAllTV() {
        if (vaccineCardAdapter.getDueVaccines().size() > 0) {
            recordAllTV.setVisibility(GONE);
        } else {
            recordAllTV.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(recordAllTV)) {
            if (onRecordAllClickListener != null && vaccineCardAdapter != null) {
                onRecordAllClickListener.onClick(this, vaccineCardAdapter.getDueVaccines());
            }
        } else if (v instanceof ImmunizationRowCard) {
            if (onVaccineClickedListener != null) {
                onVaccineClickedListener.onClick(this, ((ImmunizationRowCard) v).getVaccineWrapper());
            }
        } else if (v.getId() == R.id.undo_b) {
            if (v.getParent().getParent() instanceof ImmunizationRowCard) {
                ImmunizationRowCard vaccineCard = (ImmunizationRowCard) v.getParent().getParent();
                onUndoClick(vaccineCard);
            }
        }
    }

    @Override
    public void onStateChanged(ImmunizationRowCard.State newState) {
        updateViews();
    }

    public void onUndoClick(ImmunizationRowCard vaccineCard) {
        if (this.onVaccineUndoClickListener != null) {
            this.onVaccineUndoClickListener.onUndoClick(this, vaccineCard.getVaccineWrapper());
        }
    }

    public void setOnRecordAllClickListener(OnRecordAllClickListener onRecordAllClickListener) {
        this.onRecordAllClickListener = onRecordAllClickListener;
    }

    public void setOnVaccineClickedListener(OnVaccineClickedListener onVaccineClickedListener) {
        this.onVaccineClickedListener = onVaccineClickedListener;
    }

    public static interface OnRecordAllClickListener {
        void onClick(ImmunizationRowGroup vaccineGroup, ArrayList<VaccineWrapper> dueVaccines);
    }

    public static interface OnVaccineClickedListener {
        void onClick(ImmunizationRowGroup vaccineGroup, VaccineWrapper vaccine);
    }

    public static interface OnVaccineUndoClickListener {
        void onUndoClick(ImmunizationRowGroup vaccineGroup, VaccineWrapper vaccine);
    }

    public ArrayList<VaccineWrapper> getDueVaccines() {
        if (vaccineCardAdapter != null) {
            return vaccineCardAdapter.getDueVaccines();
        }
        return new ArrayList<VaccineWrapper>();
    }
}
