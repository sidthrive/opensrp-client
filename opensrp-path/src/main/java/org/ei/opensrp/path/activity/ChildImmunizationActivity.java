package org.ei.opensrp.path.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.toolbars.LocationSwitcherToolbar;
import org.ei.opensrp.repository.ImageRepository;
import org.opensrp.api.constants.Gender;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import util.DateUtils;
import util.Utils;

/**
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */

public class ChildImmunizationActivity extends BaseActivity
        implements LocationSwitcherToolbar.OnLocationChangeListener {

    private static final String TAG = "ChildImmunoActivity";
    private static final String EXTA_CHILD_DETAILS = "child_details";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    // Views
    private LocationSwitcherToolbar toolbar;

    // Data
    private CommonPersonObjectClient childDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar = (LocationSwitcherToolbar) getToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChildImmunizationActivity.this, ChildSmartRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        toolbar.setOnLocationChangeListener(this);

        // Get child details from bundled data
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }

    private void updateViews() {
        // TODO: update all views using child data
        updateGenderViews();
        setTitle(updateActivityTitle());
        updateAgeViews();
        updateChildIdViews();
    }

    private void updateProfilePicture(Gender gender) {
        if (isDataOk()) {
            ImageView profileImageIV = (ImageView) findViewById(R.id.profile_image_iv);
            ProfileImage photo = ((ImageRepository) org.ei.opensrp.Context
                    .getInstance().imageRepository()).findByEntityId(childDetails.entityId());
            if (photo != null) {
                Utils.setProfiePicFromPath(this, profileImageIV, photo.getFilepath(), org.ei.opensrp.R.drawable.ic_pencil);
            } else {
                int defaultProfileImg = R.drawable.child_transgender_inflant;
                if (gender.equals(Gender.FEMALE)) {
                    defaultProfileImg = R.drawable.child_girl_infant;
                } else if (gender.equals(Gender.MALE)) {
                    defaultProfileImg = R.drawable.child_boy_infant;
                }

                Utils.setProfiePic(this, profileImageIV, defaultProfileImg, org.ei.opensrp.R.drawable.ic_pencil);
            }
        }
    }

    private void updateChildIdViews() {
        String name = "";
        String childId = "";
        if (isDataOk()) {
            name = Utils.getValue(childDetails.getColumnmaps(), "first_name", true)
                    + " " + Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
            childId = Utils.getValue(childDetails.getColumnmaps(), "zeir", false);
        }

        TextView nameTV = (TextView) findViewById(R.id.name_tv);
        nameTV.setText(name);
        TextView childIdTV = (TextView) findViewById(R.id.child_id_tv);
        childIdTV.setText(String.format("%s: %s", getString(R.string.label_zeir), childId));
    }

    private void updateAgeViews() {
        String dobString = "";
        String formattedAge = "";
        if (isDataOk()) {
            dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                try {
                    Date dob = DATE_FORMAT.parse(dobString);
                    long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

                    if (timeDiff >= 0) {
                        formattedAge = DateUtils.getDuration(timeDiff);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
        TextView dobTV = (TextView) findViewById(R.id.dob_tv);
        dobTV.setText(String.format("%s: %s", getString(R.string.birthdate), dobString));
        TextView ageTV = (TextView) findViewById(R.id.age_tv);
        ageTV.setText(String.format("%s: %s", getString(R.string.age), formattedAge));
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
        updateGenderViews(gender);
    }

    @Override
    protected int[] updateGenderViews(Gender gender) {
        int[] selectedColor = super.updateGenderViews(gender);

        String identifier = getString(R.string.neutral_sex_id);
        if (gender.equals(Gender.FEMALE)) {
            identifier = getString(R.string.female_sex_id);
        } else if (gender.equals(Gender.MALE)) {
            identifier = getString(R.string.male_sex_id);
        }

        TextView childSiblingsTV = (TextView) findViewById(R.id.child_siblings_tv);
        childSiblingsTV.setText(
                String.format(getString(R.string.child_siblings), identifier).toUpperCase());

        updateProfilePicture(gender);

        return selectedColor;
    }

    public static void launchActivity(Context fromContext, CommonPersonObjectClient childDetails) {
        Intent intent = new Intent(fromContext, ChildImmunizationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTA_CHILD_DETAILS, childDetails);
        intent.putExtras(bundle);

        fromContext.startActivity(intent);
    }

    private String updateActivityTitle() {
        String name = "";
        if (isDataOk()) {
            name = Utils.getValue(childDetails.getColumnmaps(), "first_name", true)
                    + " " + Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
        }
        return String.format("%s > %s", getString(R.string.app_name), name);
    }

    @Override
    public void onLocationChanged(String newLocation) {
        // TODO: Do whatever needs to be done when the location is changed
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
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }
}
