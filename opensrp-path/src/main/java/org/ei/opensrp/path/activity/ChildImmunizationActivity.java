package org.ei.opensrp.path.activity;

import android.os.Bundle;

import org.ei.opensrp.path.R;

/**
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */

public class ChildImmunizationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getToolbar().setTitle(getActivityTitle());
    }

    private String getActivityTitle() {
        //TODO: get the child's name
        return String.format("? > ?", getString(R.string.app_name), "Child Name");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_child_immunization;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }
}
