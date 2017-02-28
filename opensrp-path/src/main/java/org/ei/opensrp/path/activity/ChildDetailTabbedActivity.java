package org.ei.opensrp.path.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.RegisterClickables;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.path.view.VaccineGroup;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import util.DateUtils;
import util.Utils;


public class ChildDetailTabbedActivity extends BaseActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //////////////////////////////////////////////////
    private static final String TAG = "ChildImmunoActivity";
    private static final String VACCINES_FILE = "vaccines.json";
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    // Data
    private CommonPersonObjectClient childDetails;

    ////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_detail_activity_simple_tabs);
        initiallization(savedInstanceState);




        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);
    }

    private void initiallization(Bundle savedInstanceState) {
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }
        profileWidget();
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_simple_tabs, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected int getContentView() {
        return R.layout.child_detail_activity_simple_tabs;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }

    @Override
    protected Class onBackActivity() {
        return ChildImmunizationActivity.class;
    }

    private void profileWidget() {
        TextView profilename = (TextView)findViewById(R.id.name);
        TextView profileZeirID = (TextView)findViewById(R.id.idforclient);
        TextView profileage = (TextView)findViewById(R.id.ageforclient);
        String name = "";
        String childId = "";
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";
        if (isDataOk()) {
            name = Utils.getValue(childDetails.getColumnmaps(), "first_name", true)
                    + " " + Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
            childId = Utils.getValue(childDetails.getColumnmaps(), "program_client_id", false);
            if (StringUtils.isNotBlank(childId)) {
                childId = childId.replace("-", "");
            }
            dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                Date dob = dateTime.toDate();
                formattedDob = DATE_FORMAT.format(dob);
                long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

                if (timeDiff >= 0) {
                    formattedAge = DateUtils.getDuration(timeDiff);
                }
            }
        }


        profileage.setText(String.format("%s: %s", getString(R.string.age), formattedAge));
        profileZeirID.setText(String.format("%s: %s", getString(R.string.label_zeir), childId));
        profilename.setText(name);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(new OneFragment(), "Registration Data");
//        adapter.addFragment(new TwoFragment(), "Under Five History");
        viewPager.setAdapter(adapter);
    }
    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
