package org.ei.opensrp.path.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jason Rogena - jrogena@ona.io on 21/02/2017.
 */

public class VaccineCard extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "VaccineCard";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM");
    private Context context;
    private ImageView statusIV;
    private TextView nameTV;
    private Button undoB;
    private State state;
    private OnVaccineStateChangeListener onVaccineStateChangeListener;
    private OnUndoButtonClickListener onUndoButtonClickListener;
    private VaccineWrapper vaccineWrapper;

    public static enum State {
        DONE_CAN_BE_UNDONE,
        DONE_CAN_NOT_BE_UNDONE,
        DUE,
        NOT_DUE,
        OVERDUE
    }

    public VaccineCard(Context context) {
        super(context);
        init(context);
    }

    public VaccineCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VaccineCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VaccineCard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_vaccination_card, this, true);
        statusIV = (ImageView) findViewById(R.id.status_iv);
        nameTV = (TextView) findViewById(R.id.name_tv);
        undoB = (Button) findViewById(R.id.undo_b);
        undoB.setOnClickListener(this);
        setOnClickListener(this);
    }

    public void setVaccineWrapper(VaccineWrapper vaccineWrapper) {
        this.vaccineWrapper = vaccineWrapper;
        updateState();
    }

    public VaccineWrapper getVaccineWrapper() {
        return this.vaccineWrapper;
    }

    public void updateState() {
        this.state = State.NOT_DUE;
        if (vaccineWrapper != null) {
            Date dateDue = getDateDue();
            Date dateDone = getDateDone();

            if (dateDone != null) {// Vaccination was done
                Calendar today = Calendar.getInstance();
                long timeDiff = today.getTimeInMillis() - dateDone.getTime();
                if (timeDiff >= 0 && timeDiff < TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
                    // Less than one day since vaccination was done
                    this.state = State.DONE_CAN_BE_UNDONE;
                } else {
                    this.state = State.DONE_CAN_NOT_BE_UNDONE;
                }
            } else {// Vaccination has not been done
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                if (dateDue.getTime() > (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))) {
                    // Vaccination due more than one day from today
                    this.state = State.NOT_DUE;
                } else if (dateDue.getTime() < (today.getTimeInMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))) {
                    // Vaccination overdue
                    this.state = State.OVERDUE;
                } else {
                    this.state = State.DUE;
                }
            }
        }

        updateStateUi();
    }

    public void setOnVaccineStateChangeListener(OnVaccineStateChangeListener onVaccineStateChangeListener) {
        this.onVaccineStateChangeListener = onVaccineStateChangeListener;
    }

    public State getState() {
        updateState();
        return this.state;
    }

    private void updateStateUi() {
        switch (state) {
            case NOT_DUE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.vaccine_card_background_white));
                statusIV.setVisibility(GONE);
                undoB.setVisibility(GONE);
                nameTV.setVisibility(VISIBLE);
                nameTV.setTextColor(context.getResources().getColor(R.color.silver));
                nameTV.setText(getVaccineName());
                setClickable(false);
                break;
            case DUE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.vaccine_card_background_blue));
                statusIV.setVisibility(GONE);
                undoB.setVisibility(GONE);
                nameTV.setVisibility(VISIBLE);
                nameTV.setTextColor(context.getResources().getColor(android.R.color.white));
                nameTV.setText(String.format(context.getString(R.string.record_), getVaccineName()));
                setClickable(true);
                break;
            case DONE_CAN_BE_UNDONE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.vaccine_card_background_white));
                statusIV.setVisibility(VISIBLE);
                undoB.setVisibility(VISIBLE);
                nameTV.setVisibility(VISIBLE);
                nameTV.setTextColor(context.getResources().getColor(R.color.silver));
                nameTV.setText(getVaccineName());
                setClickable(false);
                break;
            case DONE_CAN_NOT_BE_UNDONE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.vaccine_card_background_white));
                statusIV.setVisibility(VISIBLE);
                undoB.setVisibility(GONE);
                nameTV.setVisibility(VISIBLE);
                nameTV.setTextColor(context.getResources().getColor(R.color.silver));
                nameTV.setText(getVaccineName());
                setClickable(false);
                break;
            case OVERDUE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.vaccine_card_background_red));
                statusIV.setVisibility(GONE);
                undoB.setVisibility(GONE);
                nameTV.setVisibility(VISIBLE);
                nameTV.setTextColor(context.getResources().getColor(android.R.color.white));
                nameTV.setText(String.format(context.getString(R.string.record_due_),
                        getVaccineName(), DATE_FORMAT.format(getDateDue())));
                setClickable(true);
                break;
        }
    }

    private String getVaccineName() {
        if (vaccineWrapper != null) {
            return vaccineWrapper.getName();
        }
        return null;
    }

    private Date getDateDue() {
        if (vaccineWrapper != null) {
            DateTime vaccineDate = vaccineWrapper.getVaccineDate();
            if (vaccineDate != null) return vaccineDate.toDate();
        }
        return null;
    }

    private Date getDateDone() {
        if (vaccineWrapper != null) {
            DateTime dateDone = vaccineWrapper.getUpdatedVaccineDate();
            if (dateDone != null) return dateDone.toDate();
        }

        return null;
    }

    public static interface OnVaccineStateChangeListener {
        void onStateChanged(final State newState);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(undoB)) {
            if(onUndoButtonClickListener != null) {
                onUndoButtonClickListener.onUndoClick(this);
            }
        }
    }

    public void setOnUndoButtonClickListener(OnUndoButtonClickListener onUndoButtonClickListener) {
        this.onUndoButtonClickListener = onUndoButtonClickListener;
    }

    private void recordVaccinated(Date date) {
        vaccineWrapper.setUpdatedVaccineDate(new DateTime(date), true);
        updateState();
        if (onVaccineStateChangeListener != null) {
            onVaccineStateChangeListener.onStateChanged(this.state);
        }
    }

    private void undoVaccinationDate() {
        vaccineWrapper.setUpdatedVaccineDate(null, false);
        updateState();
        if (onVaccineStateChangeListener != null) {
            onVaccineStateChangeListener.onStateChanged(this.state);
        }
    }

    public static interface OnUndoButtonClickListener {
        void onUndoClick(VaccineCard vaccineCard);
    }
}
