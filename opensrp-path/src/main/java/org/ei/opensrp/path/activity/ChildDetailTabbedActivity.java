package org.ei.opensrp.path.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonRepository;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.RegisterClickables;
import org.ei.opensrp.path.tabfragments.child_registration_data_fragment;
import org.ei.opensrp.path.tabfragments.child_under_five_fragment;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.path.view.VaccineGroup;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.repository.UniqueIdRepository;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.util.OpenSRPImageLoader;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DateUtils;
import util.ImageUtils;
import util.JsonFormUtils;
import util.Utils;
import util.barcode.BarcodeIntentIntegrator;
import util.barcode.BarcodeIntentResult;


public class ChildDetailTabbedActivity extends BaseActivity {

    private Toolbar detailtoolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    static final int REQUEST_TAKE_PHOTO = 1;
    public static Gender gender;
    //////////////////////////////////////////////////
    private static final String TAG = "ChildImmunoActivity";
    private static final String VACCINES_FILE = "vaccines.json";
    public static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private child_registration_data_fragment child_data_fragment;
    private child_under_five_fragment child_under_five_Fragment;
    private File currentfile;

    public CommonPersonObjectClient getChildDetails() {
        return childDetails;
    }

    // Data
    private CommonPersonObjectClient childDetails;
    private Map<String,String> detailmaps;

    ////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }
        setContentView(R.layout.child_detail_activity_simple_tabs);

        child_data_fragment = new child_registration_data_fragment();

        child_data_fragment.setArguments(this.getIntent().getExtras());

        child_under_five_Fragment = new child_under_five_fragment();
        child_under_five_Fragment.setArguments(this.getIntent().getExtras());


        detailtoolbar = (Toolbar) findViewById(R.id.child_detail_toolbar);

        ((TextView)detailtoolbar.findViewById(R.id.title)).setText(updateActivityTitle());

        detailtoolbar.setNavigationIcon(R.drawable.back_button);
//        detailtoolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.ic_menu));


        detailtoolbar.showOverflowMenu();

        setSupportActionBar(detailtoolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        getSupportActionBar().
        initiallization(savedInstanceState);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        detailtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        detailtoolbar.setTitle(updateActivityTitle());


        tabLayout.setupWithViewPager(viewPager);
    }

    private void initiallization(Bundle savedInstanceState) {

        DetailsRepository detailsRepository = org.ei.opensrp.Context.getInstance().detailsRepository();
        detailmaps  = detailsRepository.getAllDetailsForClient(childDetails.entityId());
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
                String formmetadata = getmetaDataForEditForm();
                startFormActivity("child_enrollment", childDetails.entityId(), formmetadata);
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

    private String getmetaDataForEditForm() {
        Context context = Context.getInstance();
        try{
        JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson("child_enrollment");
        JsonFormUtils.addChildRegLocHierarchyQuestions(form, context);
        if (form != null) {
            form.put("entity_id",childDetails.entityId());
            Intent intent = new Intent(getApplicationContext(), JsonFormActivity.class);
            //inject zeir id into the form
            JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("First_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"first_name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Last_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"last_name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Sex")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"gender",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.ZEIR_ID)) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"program_client_id",true).replace("-", ""));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Child_Register_Card_Number")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Child_Register_Card_Number",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Child_Birth_Certificate")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Child_Birth_Certificate",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_First_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"mother_first_name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Last_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(childDetails.getColumnmaps(),"mother_last_name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_NRC")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Mother_Guardian_NRC",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Mother_Guardian_Number")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Mother_Guardian_Number",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Father_Guardian_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Father_Guardian_Name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Father_Guardian_NRC")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Father_Guardian_NRC",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("First_Health_Facility_Contact")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"First_Health_Facility_Contact",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Date_Birth")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    DateTime dateTime = new DateTime(Utils.getValue(childDetails.getColumnmaps(),"dob",true));
                    Date dob = dateTime.toDate();
                    jsonObject.put(JsonFormUtils.VALUE,Utils.getValue(childDetails.getColumnmaps(),"dob",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Birth_Weight")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Birth_Weight",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Place_Birth")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Place_Birth",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Birth_Facility_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Birth_Facility_Name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Residential_Area")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Residential_Area",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Residential_Address")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Residential_Address",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Physical_Landmark")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"Physical_Landmark",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("CHW_Name")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"CHW_Name",true));
                }
                if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("PMTCT_Status")) {
                    jsonObject.remove(JsonFormUtils.VALUE);
                    jsonObject.put(JsonFormUtils.VALUE, Utils.getValue(detailmaps,"PMTCT_Status",true));
                }
            }
//            intent.putExtra("json", form.toString());
//            startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            return form.toString();
        }
    } catch (Exception e) {
        Log.e(TAG, e.getMessage());
    }

    return "";
    }

    public void startFormActivity(String formName, String entityId, String metaData) {
        Context context = Context.getInstance();

        Intent intent = new Intent(getApplicationContext(), JsonFormActivity.class);

        intent.putExtra("json", metaData);
        startActivityForResult(intent, REQUEST_CODE_GET_JSON);



    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON) {
            if (resultCode == RESULT_OK) {

                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JsonFormUtils.save(this, jsonString, allSharedPreferences.fetchRegisteredANM(), "Child_Photo", "child", "mother");
            }
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            String imageBitmap = (String) extras.get(MediaStore.EXTRA_OUTPUT);
//            Toast.makeText(this,imageBitmap,Toast.LENGTH_LONG).show();
            String imageLocation = currentfile.getAbsolutePath();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

            JsonFormUtils.saveImage(this, allSharedPreferences.fetchRegisteredANM(), childDetails.entityId(), imageLocation);
            updateProfilePicture(gender);
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
        Gender gender = Gender.UNKNOWN;
        if (isDataOk()) {
            String genderString = Utils.getValue(childDetails, "gender", false);
            if (genderString != null && genderString.toLowerCase().equals("female")) {
                gender = Gender.FEMALE;
            } else if (genderString != null && genderString.toLowerCase().equals("male")) {
                gender = Gender.MALE;
            }
        }
        updateProfilePicture(gender);
    }
    private String updateActivityTitle() {
        String name = "";
        if (isDataOk()) {
            name = Utils.getValue(childDetails.getColumnmaps(), "first_name", true)
                    + " " + Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        }
        return String.format("%s's %s",  name,"Health Details");
    }
    private void updateProfilePicture(Gender gender) {
        this.gender = gender;
        if (isDataOk()) {
            ImageView profileImageIV = (ImageView) findViewById(R.id.profile_image_iv);

            if(childDetails.entityId()!=null){//image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                profileImageIV.setTag(org.ei.opensrp.R.id.entity_id, childDetails.entityId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(childDetails.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));

            }
            profileImageIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(child_data_fragment, "Registration Data");
        adapter.addFragment(child_under_five_Fragment, "Under Five History");
        viewPager.setAdapter(adapter);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentfile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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
        tabLayout.setTabTextColors(getResources().getColor(R.color.dark_grey),getResources().getColor(normalShade));
//        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(normalShade));
        try {
            Field field = TabLayout.class.getDeclaredField("mTabStrip");
            field.setAccessible(true);
            Object ob = field.get(tabLayout);
            Class<?> c = Class.forName("android.support.design.widget.TabLayout$SlidingTabStrip");
            Method method = c.getDeclaredMethod("setSelectedIndicatorColor", int.class);
            method.setAccessible(true);
            method.invoke(ob, getResources().getColor(normalShade));//now its ok
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


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
