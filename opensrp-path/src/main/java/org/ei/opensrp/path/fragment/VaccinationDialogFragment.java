package org.ei.opensrp.path.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.path.activity.ChildDetailActivity;
import org.ei.opensrp.path.db.VaccineRepo;
import org.ei.opensrp.path.domain.Photo;
import org.ei.opensrp.path.domain.VaccinateFormSubmissionWrapper;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.activity.WomanDetailActivity;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.listener.VaccinationActionListener;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

import util.Utils;

@SuppressLint("ValidFragment")
public class VaccinationDialogFragment extends DialogFragment {
    private final Context context;
    private final List<VaccineWrapper> tags;
    private final View viewGroup;
    private VaccinationActionListener listener;
    public static final String DIALOG_TAG = "VaccinationDialogFragment";

    private VaccinationDialogFragment(Context context,
                                      List<VaccineWrapper> tags, View viewGroup) {
        this.context = context;
        this.tags = tags;
        this.viewGroup = viewGroup;
    }

    public static VaccinationDialogFragment newInstance(
            Context context,
            List<VaccineWrapper> tags, View viewGroup) {
        return new VaccinationDialogFragment(context, tags, viewGroup);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        if (tags == null || tags.isEmpty()) {
            return null;
        }

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.vaccination_dialog_view, container, false);
        TextView nameView = (TextView) dialogView.findViewById(R.id.name);
        nameView.setText(tags.get(0).getPatientName());
        TextView numberView = (TextView) dialogView.findViewById(R.id.number);
        numberView.setText(tags.get(0).getPatientNumber());

        LinearLayout vaccinationNameLayout = (LinearLayout) dialogView.findViewById(R.id.vaccination_name_layout);

        if (tags.size() == 1) {
            View vaccinationName = inflater.inflate(R.layout.vaccination_name, null);
            TextView vaccineView = (TextView) vaccinationName.findViewById(R.id.vaccine);

            VaccineWrapper vaccineWrapper = tags.get(0);
            VaccineRepo.Vaccine vaccine = vaccineWrapper.getVaccine();
            if(vaccine != null) {
                vaccineView.setText(vaccine.display());
            }else {
                vaccineView.setText(vaccineWrapper.getName());
            }
            CheckBox select = (CheckBox) vaccinationName.findViewById(R.id.select);
            select.setVisibility(View.GONE);

            vaccinationNameLayout.addView(vaccinationName);
        } else {
            for (VaccineWrapper vaccineWrapper : tags) {

                View vaccinationName = inflater.inflate(R.layout.vaccination_name, null);
                TextView vaccineView = (TextView) vaccinationName.findViewById(R.id.vaccine);

                VaccineRepo.Vaccine vaccine = vaccineWrapper.getVaccine();
                if(vaccineWrapper.getVaccine() != null) {
                    vaccineView.setText(vaccine.display());
                }else {
                    vaccineView.setText(vaccineWrapper.getName());
                }

                vaccinationNameLayout.addView(vaccinationName);
            }
        }

        Photo photo = tags.get(0).getPhoto();
        if (photo != null) {
            ImageView mImageView = (ImageView) dialogView.findViewById(R.id.child_profilepic);
            if (StringUtils.isNotBlank(photo.getFilePath())) {
                Utils.setProfiePicFromPath(context, mImageView, photo.getFilePath(), null);
            } else if (photo.getResourceId() > 0) {
                Utils.setProfiePic(context, mImageView, photo.getResourceId(), null);
            }
        }

        final DatePicker earlierDatePicker = (DatePicker) dialogView.findViewById(R.id.earlier_date_picker);

        String color = tags.get(0).getColor();
        Button status = (Button) dialogView.findViewById(R.id.status);
        if (status != null) {
            status.setBackgroundColor(StringUtils.isBlank(color) ? Color.WHITE : Color.parseColor(color));
        }

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
                for(VaccineWrapper tag: tags) {
                    tag.setUpdatedVaccineDate(new DateTime(calendar.getTime()), false);
                }
                //updateFormSubmission();

                listener.onVaccinateEarlier(tags, viewGroup);

            }
        });

        final Button vaccinateToday = (Button) dialogView.findViewById(R.id.vaccinate_today);
        vaccinateToday.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                Calendar calendar = Calendar.getInstance();
                for(VaccineWrapper tag: tags) {
                    tag.setUpdatedVaccineDate(new DateTime(calendar.getTime()), true);
                }
                //updateFormSubmission();

                listener.onVaccinateToday(tags, viewGroup);

            }
        });

        final Button vaccinateEarlier = (Button) dialogView.findViewById(R.id.vaccinate_earlier);
        vaccinateEarlier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                vaccinateEarlier.setVisibility(View.GONE);
                earlierDatePicker.setVisibility(View.VISIBLE);
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
        VaccinateFormSubmissionWrapper vaccinateFormSubmissionWrapper = null;
        if (tags.get(0).getVaccine().category().equals("child") && listener instanceof ChildDetailActivity) {
            vaccinateFormSubmissionWrapper = ((ChildDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        } else if (tags.get(0).getVaccine().category().equals("woman") && listener instanceof WomanDetailActivity) {
            vaccinateFormSubmissionWrapper = ((WomanDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        }

        if (vaccinateFormSubmissionWrapper != null) {
            //vaccinateFormSubmissionWrapper.add(tag);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (VaccinationActionListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement VaccinationActionListener");
        }
    }


}
