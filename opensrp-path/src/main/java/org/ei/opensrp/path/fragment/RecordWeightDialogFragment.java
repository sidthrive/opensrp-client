package org.ei.opensrp.path.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildDetailActivity;
import org.ei.opensrp.path.activity.WomanDetailActivity;
import org.ei.opensrp.path.domain.VaccinateFormSubmissionWrapper;
import org.ei.opensrp.path.domain.WeightWrapper;
import org.ei.opensrp.path.listener.WeightActionListener;
import org.joda.time.DateTime;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class RecordWeightDialogFragment extends DialogFragment {
    private final Context context;
    private final WeightWrapper tag;
    private WeightActionListener listener;
    public static final String DIALOG_TAG = "RecordWeightDialogFragment";

    private RecordWeightDialogFragment(Context context,
                                       WeightWrapper tag) {
        this.context = context;
        if(tag == null){
            tag = new WeightWrapper();
        }
        this.tag = tag;
    }

    public static RecordWeightDialogFragment newInstance(
            Context context,
            WeightWrapper tag) {
        return new RecordWeightDialogFragment(context, tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.record_weight_dialog_view, container, false);

        final EditText editWeight = (EditText) dialogView.findViewById(R.id.edit_weight);
        formatEditWeightView(editWeight, "");
        editWeight.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("^(\\d{1,3}(\\d{3})*|(\\d+))(\\.\\d)$")) {
                    String userInput = "" + s.toString().replaceAll("[^\\d]", "");
                    formatEditWeightView(editWeight, userInput);
                }

            }
        });

        final DatePicker earlierDatePicker = (DatePicker) dialogView.findViewById(R.id.earlier_date_picker);

        /*
        TextView nameView = (TextView) dialogView.findViewById(R.id.name);
        nameView.setText(tag.getPatientName());
        TextView numberView = (TextView) dialogView.findViewById(R.id.number);
        numberView.setText(tag.getPatientNumber());

        LinearLayout vaccinationNameLayout = (LinearLayout) dialogView.findViewById(R.id.vaccination_name_layout);

        List<VaccineRepo.Vaccine> vaccines = tag.vaccines();
        if (vaccines.size() == 1) {
            VaccineRepo.Vaccine vaccine = tag.vaccines().get(0);

            View vaccinationName = inflater.inflate(R.layout.vaccination_name, null);
            TextView vaccineView = (TextView) vaccinationName.findViewById(R.id.vaccine);
            vaccineView.setText(vaccine.display());
            CheckBox select = (CheckBox) vaccinationName.findViewById(R.id.select);
            select.setVisibility(View.GONE);

            vaccinationNameLayout.addView(vaccinationName);
        } else if (vaccines.size() > 1) {
            for (VaccineRepo.Vaccine vaccine : vaccines) {

                View vaccinationName = inflater.inflate(R.layout.vaccination_name, null);
                TextView vaccineView = (TextView) vaccinationName.findViewById(R.id.vaccine);
                vaccineView.setText(vaccine.display());

                vaccinationNameLayout.addView(vaccinationName);
            }
        }

        Photo photo = tag.getPhoto();
        if (photo != null) {
            ImageView mImageView = (ImageView) dialogView.findViewById(R.id.child_profilepic);
            if (StringUtils.isNotBlank(photo.getFilePath())) {
                Utils.setProfiePicFromPath(context, mImageView, photo.getFilePath(), null);
            } else if (photo.getResourceId() > 0) {
                Utils.setProfiePic(context, mImageView, photo.getResourceId(), null);
            }
        }



        String color = tag.getColor();
        Button status = (Button) dialogView.findViewById(R.id.status);
        if (status != null) {
            status.setBackgroundColor(StringUtils.isBlank(color) ? Color.WHITE : Color.parseColor(color));
        } */

        final Button set = (Button) dialogView.findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                int day = earlierDatePicker.getDayOfMonth();
                int month = earlierDatePicker.getMonth();
                int year = earlierDatePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                tag.setUpdatedVaccineDate(new DateTime(calendar.getTime()), false);

                //updateFormSubmission();

                listener.onWeightTakenEarlier(tag);

            }
        });

        final Button weightTakenToday = (Button) dialogView.findViewById(R.id.weight_taken_today);
        weightTakenToday.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                Calendar calendar = Calendar.getInstance();
                tag.setUpdatedVaccineDate(new DateTime(calendar.getTime()), true);

                //updateFormSubmission();

                listener.onWeightTakenToday(tag);

            }
        });

        final Button weightTakenEarlier = (Button) dialogView.findViewById(R.id.weight_taken_earlier);
        weightTakenEarlier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                weightTakenEarlier.setVisibility(View.GONE);
                earlierDatePicker.setVisibility(View.VISIBLE);
                earlierDatePicker.requestFocus();
                set.setVisibility(View.VISIBLE);
            }
        });

        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        return dialogView;
    }

    private void updateFormSubmission() {
        /*VaccinateFormSubmissionWrapper vaccinateFormSubmissionWrapper = null;
        if (tag.vaccines().get(0).category().equals("child") && listener instanceof ChildDetailActivity) {
            vaccinateFormSubmissionWrapper = ((ChildDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        } else if (tag.vaccines().get(0).category().equals("woman") && listener instanceof WomanDetailActivity) {
            vaccinateFormSubmissionWrapper = ((WomanDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        }

        if (vaccinateFormSubmissionWrapper != null) {
            vaccinateFormSubmissionWrapper.add(tag);
        }*/
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the WeightActionListener so we can send events to the host
            listener = (WeightActionListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement WeightActionListener");
        }
    }

    private void formatEditWeightView(EditText editWeight, String userInput){
        StringBuilder stringBuilder = new StringBuilder(userInput);

        while (stringBuilder.length() > 2 && stringBuilder.charAt(0) == '0') {
            stringBuilder.deleteCharAt(0);
        }
        while (stringBuilder.length() < 2) {
            stringBuilder.insert(0, '0');
        }
        stringBuilder.insert(stringBuilder.length() - 1, '.');

        editWeight.setText(stringBuilder.toString());
        // keeps the cursor always to the right
        Selection.setSelection(editWeight.getText(), stringBuilder.toString().length());
    }

}
