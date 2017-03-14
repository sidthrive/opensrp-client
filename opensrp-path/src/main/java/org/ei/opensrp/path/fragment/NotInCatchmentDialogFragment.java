package org.ei.opensrp.path.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.ei.opensrp.path.R;

/**
 * Created by Jason Rogena - jrogena@ona.io on 14/03/2017.
 */

public class NotInCatchmentDialogFragment extends DialogFragment implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.dialog_not_in_catchment, container, false);
        Button cancelB = (Button) dialogView.findViewById(R.id.cancel_b);
        cancelB.setOnClickListener(this);
        Button recordB = (Button) dialogView.findViewById(R.id.record_b);
        recordB.setOnClickListener(this);
        Button searchB = (Button) dialogView.findViewById(R.id.search_b);
        searchB.setOnClickListener(this);
        return dialogView;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_b) {

        } else if (v.getId() == R.id.record_b) {

        } else if (v.getId() == R.id.cancel_b) {
            this.dismiss();
        }
    }
}
