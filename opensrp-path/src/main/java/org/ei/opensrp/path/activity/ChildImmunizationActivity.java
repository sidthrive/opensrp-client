package org.ei.opensrp.path.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.domain.Weight;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.Photo;
import org.ei.opensrp.path.domain.RegisterClickables;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.domain.WeightWrapper;
import org.ei.opensrp.path.fragment.RecordWeightDialogFragment;
import org.ei.opensrp.path.fragment.UndoVaccinationDialogFragment;
import org.ei.opensrp.path.fragment.VaccinationDialogFragment;
import org.ei.opensrp.path.listener.VaccinationActionListener;
import org.ei.opensrp.path.listener.WeightActionListener;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.path.view.ExpandableHeightGridView;
import org.ei.opensrp.path.view.VaccineGroup;
import org.ei.opensrp.repository.VaccineRepository;
import org.ei.opensrp.repository.WeightRepository;
import org.ei.opensrp.util.OpenSRPImageLoader;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.api.constants.Gender;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import util.DateUtils;
import util.ImageUtils;
import util.JsonFormUtils;
import util.Utils;

import static util.Utils.getValue;

/**
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */

public class ChildImmunizationActivity extends BaseActivity
        implements LocationSwitcherToolbar.OnLocationChangeListener, WeightActionListener, VaccinationActionListener {

    private static final String TAG = "ChildImmunoActivity";
    private static final String VACCINES_FILE = "vaccines.json";
    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private static final String EXTRA_REGISTER_CLICKABLES = "register_clickables";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private ArrayList<VaccineGroup> vaccineGroups;

    // Views
    private LocationSwitcherToolbar toolbar;

    // Data
    private CommonPersonObjectClient childDetails;
    private RegisterClickables registerClickables;
    private List<Vaccine> vaccineList;
    private Weight weight;

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
                finish();
            }
        });
        toolbar.setOnLocationChangeListener(this);
//       View view= toolbar.findViewById(R.id.immunization_separator);
//        view.setBackground(R.drawable.vertical_seperator_female);

        // Get child details from bundled data
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }

            serializable = extras.getSerializable(EXTRA_REGISTER_CLICKABLES);
            if (serializable != null && serializable instanceof RegisterClickables) {
                registerClickables = (RegisterClickables) serializable;
            }
        }

        toolbar.init(this);
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
        WeightRepository weightRepository = getOpenSRPContext().weightRepository();
        weight = weightRepository.findUnSyncedByEntityId(childDetails.entityId());

        VaccineRepository vaccineRepository = getOpenSRPContext().vaccineRepository();
        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());

        updateGenderViews();
        toolbar.setTitle(updateActivityTitle());
        updateAgeViews();
        updateChildIdViews();
        updateVaccinationViews();
        updateRecordWeightView();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        performRegisterActions();
    }

    private void updateProfilePicture(Gender gender) {
        if (isDataOk()) {
            ImageView profileImageIV = (ImageView) findViewById(R.id.profile_image_iv);

            if (childDetails.entityId() != null) {//image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                profileImageIV.setTag(org.ei.opensrp.R.id.entity_id, childDetails.entityId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(childDetails.entityId(), OpenSRPImageLoader.getStaticImageListener((ImageView) profileImageIV, ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));

            }
        }
    }

    private void updateChildIdViews() {
        String name = "";
        String childId = "";
        if (isDataOk()) {
            name = Utils.getValue(childDetails.getColumnmaps(), "first_name", true)
                    + " " + Utils.getValue(childDetails.getColumnmaps(), "last_name", true);
            childId = Utils.getValue(childDetails.getColumnmaps(), "zeir_id", false);
        }

        TextView nameTV = (TextView) findViewById(R.id.name_tv);
        nameTV.setText(name);
        TextView childIdTV = (TextView) findViewById(R.id.child_id_tv);
        childIdTV.setText(String.format("%s: %s", getString(R.string.label_zeir), childId));
    }

    private void updateAgeViews() {
        String dobString = "";
        String formattedAge = "";
        String formattedDob = "";
        if (isDataOk()) {
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
        TextView dobTV = (TextView) findViewById(R.id.dob_tv);
        dobTV.setText(String.format("%s: %s", getString(R.string.birthdate), formattedDob));
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
        int toolbarResource = 0;
        if (gender.equals(Gender.FEMALE)) {
            toolbarResource = R.drawable.vertical_separator_female;
            identifier = getString(R.string.female_sex_id);
        } else if (gender.equals(Gender.MALE)) {
            toolbarResource = R.drawable.vertical_separator_male;
            identifier = getString(R.string.male_sex_id);
        }
        toolbar.updateSeparatorView(toolbarResource);

        TextView childSiblingsTV = (TextView) findViewById(R.id.child_siblings_tv);
        childSiblingsTV.setText(
                String.format(getString(R.string.child_siblings), identifier).toUpperCase());
        updateProfilePicture(gender);

        return selectedColor;
    }

    private void updateVaccinationViews() {
        if (vaccineGroups == null) {
            vaccineGroups = new ArrayList<>();
            LinearLayout vaccineGroupCanvasLL = (LinearLayout) findViewById(R.id.vaccine_group_canvas_ll);
            String supportedVaccinesString = readAssetContents(VACCINES_FILE);
            try {
                JSONArray supportedVaccines = new JSONArray(supportedVaccinesString);
                for (int i = 0; i < supportedVaccines.length(); i++) {
                    VaccineGroup curGroup = new VaccineGroup(this);
                    curGroup.setData(supportedVaccines.getJSONObject(i), childDetails, vaccineList);
                    curGroup.setOnRecordAllClickListener(new VaccineGroup.OnRecordAllClickListener() {
                        @Override
                        public void onClick(VaccineGroup vaccineGroup, ArrayList<VaccineWrapper> dueVaccines) {
                            addVaccinationDialogFragment(dueVaccines, vaccineGroup);
                        }
                    });
                    curGroup.setOnVaccineClickedListener(new VaccineGroup.OnVaccineClickedListener() {
                        @Override
                        public void onClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
                            addVaccinationDialogFragment(Arrays.asList(vaccine), vaccineGroup);
                        }
                    });
                    curGroup.setOnVaccineUndoClickListener(new VaccineGroup.OnVaccineUndoClickListener() {
                        @Override
                        public void onUndoClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
                            addVaccineUndoDialogFragment(vaccineGroup, vaccine);
                        }
                    });
                    vaccineGroupCanvasLL.addView(curGroup);
                    vaccineGroups.add(curGroup);
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    private void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, VaccineWrapper vaccineWrapper) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(UndoVaccinationDialogFragment.DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        UndoVaccinationDialogFragment undoVaccinationDialogFragment = UndoVaccinationDialogFragment.newInstance(this, vaccineWrapper, vaccineGroup);
        undoVaccinationDialogFragment.show(ft, UndoVaccinationDialogFragment.DIALOG_TAG);
    }

    private void updateRecordWeightView() {

        String childName = getValue(childDetails.getColumnmaps(), "first_name", true) + " " + getValue(childDetails, "last_name", true);
        String gender = getValue(childDetails.getColumnmaps(), "gender", true) + " " + getValue(childDetails, "gender", true);
        String motherFirstName = getValue(childDetails.getColumnmaps(), "mother_first_name", true);
        if (childName.trim().isEmpty() && !motherFirstName.isEmpty()) {
            childName = "B/o " + motherFirstName;
        }

        String zeirId = getValue(childDetails.getColumnmaps(), "zeir_id", false);
        String duration = "";
        String dobString = getValue(childDetails.getColumnmaps(), "dob", false);
        if (StringUtils.isNotBlank(dobString)) {
            DateTime dateTime = new DateTime(getValue(childDetails.getColumnmaps(), "dob", false));
            duration = DateUtils.getDuration(dateTime);
        }

        Photo photo = ImageUtils.profilePhotoByClient(childDetails);

        WeightWrapper weightWrapper = new WeightWrapper();
        weightWrapper.setId(childDetails.entityId());
        weightWrapper.setGender(gender);
        weightWrapper.setPatientName(childName);
        weightWrapper.setPatientNumber(zeirId);
        weightWrapper.setPatientAge(duration);
        weightWrapper.setPhoto(photo);
        weightWrapper.setPmtctStatus(getValue(childDetails.getColumnmaps(), "pmtct_status", false));

        if (weight != null) {
            weightWrapper.setWeight(weight.getKg());
            weightWrapper.setDbKey(weight.getId());
        }

        updateRecordWeightView(weightWrapper);
    }

    private void updateRecordWeightView(WeightWrapper weightWrapper) {
        Button recordWeightButton = (Button) findViewById(R.id.record_weight);
        if (weightWrapper.getDbKey() != null) {
            recordWeightButton.setBackgroundResource(R.drawable.record_weight_update_bg);
            recordWeightButton.setTextColor(Color.WHITE);
            recordWeightButton.setText("Update weight");
        }

        recordWeightButton.setTag(weightWrapper);
        recordWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWeightDialog(view);
            }
        });

    }

    private void showWeightDialog(View view) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(RecordWeightDialogFragment.DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        WeightWrapper weightWrapper = (WeightWrapper) view.getTag();
        RecordWeightDialogFragment recordWeightDialogFragment = RecordWeightDialogFragment.newInstance(this, weightWrapper);
        recordWeightDialogFragment.show(ft, RecordWeightDialogFragment.DIALOG_TAG);

    }

    private String readAssetContents(String path) {
        String fileContents = null;
        try {
            InputStream is = getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            fileContents = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            android.util.Log.e(TAG, ex.toString(), ex);
        }

        return fileContents;
    }

    public static void launchActivity(Context fromContext, CommonPersonObjectClient childDetails, RegisterClickables registerClickables) {
        Intent intent = new Intent(fromContext, ChildImmunizationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_CHILD_DETAILS, childDetails);
        bundle.putSerializable(EXTRA_REGISTER_CLICKABLES, registerClickables);
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
    public void onLocationChanged(final String newLocation) {
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

    @Override
    protected Class onBackActivity() {
        return ChildSmartRegisterActivity.class;
    }

    @Override
    public void onWeightTaken(final WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = getOpenSRPContext().weightRepository();
            Weight weight = new Weight();
            if (tag.getDbKey() != null) {
                weight = weightRepository.find(tag.getDbKey());
            }
            weight.setBaseEntityId(childDetails.entityId());
            weight.setKg(tag.getWeight());
            weight.setDate(tag.getUpdatedWeightDate().toDate());
            weight.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            try {
                weight.setLocationId(JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(),
                        toolbar.getCurrentLocation()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final Handler handler = new Handler(Looper.getMainLooper());

            final Weight weightToSave = weight;
            processInThread(new Runnable() {
                @Override
                public void run() {
                    weightRepository.add(weightToSave);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tag.setDbKey(weightToSave.getId());
                            updateRecordWeightView(tag);
                        }
                    });
                }
            });
        }

    }

    @Override
    public void onVaccinateToday(List<VaccineWrapper> tags, View view) {
        if (tags != null && !tags.isEmpty()) {
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onVaccinateEarlier(List<VaccineWrapper> tags, View view) {
        if (tags != null && !tags.isEmpty()) {
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onUndoVaccination(final VaccineWrapper tag, final View view) {
        if (tag != null) {

            if (tag.getDbKey() != null) {
                final VaccineRepository vaccineRepository = getOpenSRPContext().vaccineRepository();
                final Long dbKey = tag.getDbKey();
                tag.setUpdatedVaccineDate(null, false);
                tag.setRecordedDate(null);
                tag.setDbKey(null);


                processInThread(new Runnable() {
                    @Override
                    public void run() {
                        vaccineRepository.deleteVaccine(dbKey);
                    }
                });

                updateVaccineGroupViews(view);
            }
        }
    }

    public void addVaccinationDialogFragment(List<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(VaccinationDialogFragment.DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        VaccinationDialogFragment vaccinationDialogFragment = VaccinationDialogFragment.newInstance(this, vaccineWrappers, vaccineGroup);
        vaccinationDialogFragment.show(ft, VaccinationDialogFragment.DIALOG_TAG);
    }

    public void performRegisterActions() {
        if (this.registerClickables != null) {
            if (this.registerClickables.isRecordWeight()) {
                Button recordWeightButton = (Button) findViewById(R.id.record_weight);
                recordWeightButton.performClick();
            } else if (this.registerClickables.isRecordAll()) {
                // TODO get the right vaccineCard/recordAll
                if (vaccineGroups != null) {
                    final VaccineGroup vaccineGroup = vaccineGroups.get(0);
                    ExpandableHeightGridView gridView = vaccineGroup.getGridView();
                    gridView.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView recordAllTV = (TextView) vaccineGroup.findViewById(R.id.record_all_tv);
                            recordAllTV.performClick();
                        }
                    });
                }
            }
        }
    }

    private void saveVaccine(final List<VaccineWrapper> tags, final View view) {
        final VaccineRepository vaccineRepository = getOpenSRPContext().vaccineRepository();
        final List<Vaccine> toSave = new ArrayList<>();

        for (VaccineWrapper tag : tags) {
            Vaccine vaccine = new Vaccine();
            if (tag.getDbKey() != null) {
                vaccine = vaccineRepository.find(tag.getDbKey());
            }
            vaccine.setBaseEntityId(childDetails.entityId());
            vaccine.setName(tag.getName());
            vaccine.setDate(tag.getUpdatedVaccineDate().toDate());
            vaccine.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
            try {
                vaccine.setLocationId(JsonFormUtils.getOpenMrsLocationId(getOpenSRPContext(),
                        toolbar.getCurrentLocation()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
            if (StringUtils.isNumeric(lastChar)) {
                vaccine.setCalculation(Integer.valueOf(lastChar));
            } else {
                vaccine.setCalculation(-1);
            }
            toSave.add(vaccine);
        }

        processInThread(new Runnable() {
            @Override
            public void run() {
                if (!toSave.isEmpty()) {
                    for (Vaccine vaccine : toSave) {
                        vaccineRepository.add(vaccine);
                        updateTagsDbKey(tags, vaccine);
                    }
                }
            }
        });

        updateVaccineGroupViews(view);
    }

    private void updateVaccineGroupViews(View view) {
        if (view == null || !(view instanceof VaccineGroup)) {
            return;
        }
        final VaccineGroup vaccineGroup = (VaccineGroup) view;

        if (Looper.myLooper() == Looper.getMainLooper()) {
            vaccineGroup.updateViews();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    vaccineGroup.updateViews();
                }
            });
        }
    }

    private void updateTagsDbKey(List<VaccineWrapper> tags, Vaccine vaccine) {
        if (!tags.isEmpty()) {
            for (VaccineWrapper tag : tags) {
                if (tag.getName().equals(vaccine.getName())) {
                    tag.setDbKey(vaccine.getId());
                }
            }
        }

    }
}
