package org.ei.opensrp.path.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildSmartRegisterActivity;

public class AdvancedSearchFragment extends BaseSmartRegisterFragment {
    private View mView;
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.smart_register_activity_advanced_search, container, false);
        mView = view;
        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    protected void onCreation() {
    }

    @Override
    protected void onResumption() {
        updateLocationText();
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.global_search);
        imageButton.setBackgroundColor(getResources().getColor(R.color.transparent_dark_blue));
        imageButton.setOnClickListener(clientActionHandler);


        final View filterSection = view.findViewById(R.id.filter_selection);
        filterSection.setOnClickListener(clientActionHandler);

        TextView filterCount = (TextView) view.findViewById(R.id.filter_count);
        filterCount.setVisibility(View.GONE);
        filterCount.setClickable(false);
    }

    @Override
    public void setupSearchView(View view) {
    }

    @Override
    protected void startRegistration() {
        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("child_enrollment", null, null);
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.global_search:
                    ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment(null);
                break;

                case R.id.filter_selection:
                    ((ChildSmartRegisterActivity) getActivity()).startQrCodeScanner();
                    break;
            }
        }
    }
}
