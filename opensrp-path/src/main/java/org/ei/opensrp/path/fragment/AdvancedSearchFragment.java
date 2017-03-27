package org.ei.opensrp.path.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildSmartRegisterActivity;

import util.Utils;

public class AdvancedSearchFragment extends BaseSmartRegisterFragment {
    private View mView;
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private RadioGroup searchLimits;

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
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateLocationText();
            updateSeachLimits();
        }
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
        filterCount.setClickable(false);
        filterCount.setText("1");
        filterCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterSection.performClick();
            }
        });

        View backToHome = view.findViewById(R.id.btn_back_to_home);
        backToHome.setOnClickListener(clientActionHandler);

        View titleLayout = view.findViewById(R.id.title_layout);
        titleLayout.setOnClickListener(clientActionHandler);
        titleLayout.setPadding(1, 0, 0, 0);

        TextView titleView = (TextView) view.findViewById(R.id.txt_title_label);
        titleView.setText(getString(R.string.advanced_search));

        View nameInitials = view.findViewById(R.id.name_inits);
        nameInitials.setVisibility(View.GONE);

        ImageView backButton = (ImageView) view.findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE);

        Button search = (Button) view.findViewById(R.id.search);
        search.setOnClickListener(clientActionHandler);
        search.setEnabled(false);

        searchLimits = (RadioGroup) view.findViewById(R.id.search_limits);
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
                case R.id.btn_back_to_home:
                case R.id.title_layout:
                    ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment(null);
                    break;
                case R.id.filter_selection:
                    ((ChildSmartRegisterActivity) getActivity()).filterSelection();
                    break;
            }
        }
    }

    private void updateSeachLimits() {
        if (searchLimits != null) {
            if (Utils.isConnectedToNetwork(getActivity())) {
                searchLimits.check(R.id.out_and_inside);
            } else {
                searchLimits.check(R.id.my_catchment);
            }
        }
    }
}
