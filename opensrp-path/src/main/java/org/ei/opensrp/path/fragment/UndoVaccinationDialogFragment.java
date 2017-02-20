package org.ei.opensrp.path.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ei.opensrp.path.activity.ChildDetailActivity;
import org.ei.opensrp.path.domain.VaccinateFormSubmissionWrapper;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.activity.WomanDetailActivity;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.listener.VaccinationActionListener;

@SuppressLint("ValidFragment")
public class UndoVaccinationDialogFragment extends DialogFragment {
    private final Context context;
    private final VaccineWrapper tag;
    private VaccinationActionListener listener;
    public static final String DIALOG_TAG = "UndoVaccinationDialogFragment";

    private UndoVaccinationDialogFragment(Context context,
                                          VaccineWrapper tag) {
        this.context = context;
        this.tag = tag;
    }

    public static UndoVaccinationDialogFragment newInstance(
            Context context,
            VaccineWrapper tag) {
        return new UndoVaccinationDialogFragment(context, tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.undo_vaccination_dialog_view, container, false);
        TextView nameView = (TextView) dialogView.findViewById(R.id.name);
        nameView.setText(tag.getPatientName());
        TextView numberView = (TextView) dialogView.findViewById(R.id.number);
        numberView.setText(tag.getPatientNumber());

        TextView vaccineView = (TextView) dialogView.findViewById(R.id.vaccine);
        vaccineView.setText(tag.vaccines().get(0).display());

        Button vaccinateToday = (Button) dialogView.findViewById(R.id.yes_undo);
        vaccinateToday.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                updateFormSubmission();

                listener.onUndoVaccination(tag);
            }
        });

        Button cancel = (Button) dialogView.findViewById(R.id.no_go_back);
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
        if (tag.vaccines().get(0).category().equals("child") && listener instanceof ChildDetailActivity) {
            vaccinateFormSubmissionWrapper = ((ChildDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        } else if (tag.vaccines().get(0).category().equals("woman") && listener instanceof WomanDetailActivity) {
            vaccinateFormSubmissionWrapper = ((WomanDetailActivity) listener).getVaccinateFormSubmissionWrapper();
        }

        if (vaccinateFormSubmissionWrapper != null) {
            vaccinateFormSubmissionWrapper.remove(tag);
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
