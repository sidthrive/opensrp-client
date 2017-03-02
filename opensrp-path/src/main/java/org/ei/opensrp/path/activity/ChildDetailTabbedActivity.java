package org.ei.opensrp.path.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.RegisterClickables;
import org.ei.opensrp.path.tabfragments.child_registration_data_fragment;
import org.ei.opensrp.path.tabfragments.child_under_five_fragment;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.path.view.VaccineGroup;
import org.ei.opensrp.repository.UniqueIdRepository;
import org.ei.opensrp.util.FormUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import util.DateUtils;
import util.JsonFormUtils;
import util.Utils;


public class ChildDetailTabbedActivity extends BaseActivity {

    private Toolbar detailtoolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    //////////////////////////////////////////////////
    private static final String TAG = "ChildImmunoActivity";
    private static final String VACCINES_FILE = "vaccines.json";
    public static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private child_registration_data_fragment child_data_fragment;
    private child_under_five_fragment child_under_five_Fragment;

    public CommonPersonObjectClient getChildDetails() {
        return childDetails;
    }

    // Data
    private CommonPersonObjectClient childDetails;

    ////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_detail_activity_simple_tabs);

        child_data_fragment = new child_registration_data_fragment();

        child_data_fragment.setArguments(this.getIntent().getExtras());

        child_under_five_Fragment = new child_under_five_fragment();
        child_under_five_Fragment.setArguments(savedInstanceState);


        detailtoolbar = (Toolbar) findViewById(R.id.child_detail_toolbar);
        detailtoolbar.showOverflowMenu();
        setSupportActionBar(detailtoolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().
        initiallization(savedInstanceState);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_child_detail_settings, menu);
        return true;
    }
    @Override
    public void initViews(){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.registration_data:
                startFormActivity("child_enrollment", childDetails.entityId(), null);
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.immunization_data:
                viewPager.setCurrentItem(1);
                child_under_five_Fragment.loadview(true);
                return  true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public void startFormActivity(String formName, String entityId, String metaData) {
        Context context = Context.getInstance();
        try {
            if (StringUtils.isBlank(entityId)) {
                UniqueIdRepository uniqueIdRepo = context.uniqueIdRepository();
                entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                if (entityId.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson(formName);
            JsonFormUtils.addChildRegLocHierarchyQuestions(form,context);
            if (form != null) {
                Intent intent = new Intent(getApplicationContext(), JsonFormActivity.class);
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.ZEIR_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId.replace("-", ""));
                        continue;
                    }
                }
                intent.putExtra("json", form.toString());
                startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
        return R.id.child_detail_toolbar;
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
        updateGenderViews();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(child_data_fragment, "Registration Data");
        adapter.addFragment(child_under_five_Fragment, "Under Five History");
        viewPager.setAdapter(adapter);
    }
    private void updateGenderViews() {
        Gender gender = Gender.UNKNOWN;
        if (isDataOk()) {
            String genderString = Utils.getValue(childDetails, "gender", false);
            if (genderString != null && genderString.toLowerCase().equals("female")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                gender = Gender.MALE;
            }
        }
       int [] colors = updateGenderViews(gender);
        int darkShade = colors[0];
        int normalShade = colors[1];
        int lightSade = colors[2];
        detailtoolbar.setBackground(new ColorDrawable(getResources().getColor(normalShade)));

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
