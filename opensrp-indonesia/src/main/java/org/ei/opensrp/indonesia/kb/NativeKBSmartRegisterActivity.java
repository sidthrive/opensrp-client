package org.ei.opensrp.indonesia.kb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.ei.opensrp.domain.form.FieldOverrides;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.indonesia.LoginActivity;
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.fragment.NativeKBSmartRegisterFragment;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
import org.ei.opensrp.indonesia.pageradapter.BaseRegisterActivityPagerAdapter;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.LocationSelectorDialogFragment;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.OpenSRPViewPager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_CLOSE;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_EDIT;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_REGISTER;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_UPDATE;

/**
 * Created by Dimas Ciputra on 2/18/15.
 */
public class NativeKBSmartRegisterActivity extends SecuredNativeSmartRegisterActivity implements LocationSelectorDialogFragment.OnLocationSelectedListener{

    public static final String TAG = "KBActivity";
    @Bind(R.id.view_pager)
    OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private int currentPage;

    private String[] formNames = new String[]{};
    private android.support.v4.app.Fragment mBaseFragment = null;


    ZiggyService ziggyService;

    NativeKBSmartRegisterFragment nf = new NativeKBSmartRegisterFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        FlurryFacade.logEvent("kb_dashboard");
        formNames = this.buildFormNameList();

        //        WD
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            boolean mode_face = extras.getBoolean("org.ei.opensrp.indonesia.face.face_mode");
            String base_id = extras.getString("org.ei.opensrp.indonesia.face.base_id");
            double proc_time = extras.getDouble("org.ei.opensrp.indonesia.face.proc_time");
//            Log.e(TAG, "onCreate: "+proc_time );

            if (mode_face){
                nf.setCriteria(base_id);
                mBaseFragment = new NativeKBSmartRegisterFragment();

                Log.e(TAG, "onCreate: " + base_id);
                AlertDialog.Builder builder= new AlertDialog.Builder(this);
                builder.setTitle("Is it Right Clients ?");
                builder.setMessage("Process Time : " + proc_time + " s");
                builder.setNegativeButton("CANCEL", listener);
                builder.setPositiveButton("YES",                         new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do all your stuff here
                                nf.setCriteria("!");
                                currentPage = 0;
                            }
                        }
                );
                builder.show();
            }
        } else {
            mBaseFragment = new NativeKBSmartRegisterFragment();
        }

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new BaseRegisterActivityPagerAdapter(getSupportFragmentManager(), formNames, mBaseFragment);
        mPager.setOffscreenPageLimit(formNames.length);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                onPageChanged(position);
            }
        });

        ziggyService = context.ziggyService();

    }
    public void onPageChanged(int page){
        setRequestedOrientation(page == 0 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        LoginActivity.setLanguage();
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {return null;}

    @Override
    protected void setupViews() {


    }

    @Override
    protected void onResumption(){}

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {return null;}

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {return null;}

    @Override
    protected void onInitialization() {}

    @Override
    public void startRegistration() {
    }

    public DialogOption[] getEditOptions() {
        return new DialogOption[]{
                new OpenFormOption("Update KB ", KOHORT_KB_UPDATE, formController),
                new OpenFormOption("Edit KB ", KOHORT_KB_EDIT, formController),
                new OpenFormOption("Tutup KB ", KOHORT_KB_CLOSE, formController),

        };


    }

    @Override
    public void OnLocationSelected(String locationJSONString) {
        JSONObject combined = null;

        try {
            JSONObject locationJSON = new JSONObject(locationJSONString);
            //   JSONObject uniqueId = new JSONObject(context.uniqueIdController().getUniqueIdJson());

            combined = locationJSON;
            //   Iterator<String> iter = uniqueId.keys();

            //  while (iter.hasNext()) {
            //      String key = iter.next();
            //       combined.put(key, uniqueId.get(key));
            //    }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (combined != null) {
            FieldOverrides fieldOverrides = new FieldOverrides(combined.toString());
            startFormActivity(KOHORT_KB_REGISTER, null, fieldOverrides.getJSONString());
        }
    }
    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, JSONObject fieldOverrides){
        Log.v("fieldoverride", fieldOverrides.toString());
        // save the form
        try{
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            ziggyService.saveForm(getParams(submission), submission.instance());

            context.formSubmissionService().updateFTSsearch(submission);

            //switch to forms list fragment
            switchToBaseFragment(formSubmission); // Unnecessary!! passing on data

        }catch (Exception e){
            // TODO: show error dialog on the formfragment if the submission fails
            DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(currentPage);
            if (displayFormFragment != null) {
                displayFormFragment.hideTranslucentProgressDialog();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        FlurryFacade.logEvent(formName);
      //  Log.v("fieldoverride", metaData);
        try {
            int formIndex = FormUtils.getIndexForFormName(formName, formNames) + 1; // add the offset
            if (entityId != null || metaData != null){
                String data = null;
                //check if there is previously saved data for the form
                data = getPreviouslySavedDataForForm(formName, metaData, entityId);
                if (data == null){
                    data = FormUtils.getInstance(getApplicationContext()).generateXMLInputForFormWithEntityId(entityId, formName, metaData);
                }

                DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(formIndex);
                if (displayFormFragment != null) {
                    displayFormFragment.setFormData(data);
                    displayFormFragment.setRecordId(entityId);
                    displayFormFragment.setFieldOverides(metaData);
                }
            }

            mPager.setCurrentItem(formIndex, false); //Don't animate the view on orientation change the view disapears

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void switchToBaseFragment(final String data){
        final int prevPageIndex = currentPage;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(0, false);
                SecuredNativeSmartRegisterFragment registerFragment = (SecuredNativeSmartRegisterFragment) findFragmentByPosition(0);
                if (registerFragment != null && data != null) {
                    registerFragment.refreshListView();
                }

                //hack reset the form
                DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(prevPageIndex);
                if (displayFormFragment != null) {
                    displayFormFragment.hideTranslucentProgressDialog();
                    displayFormFragment.setFormData(null);

                }

                displayFormFragment.setRecordId(null);
            }
        });

    }

    public android.support.v4.app.Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }

    public DisplayFormFragment getDisplayFormFragmentAtIndex(int index) {
        return  (DisplayFormFragment)findFragmentByPosition(index);
    }

    @Override
    public void onBackPressed() {
        if (currentPage != 0) {
            switchToBaseFragment(null);
        } else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }

    private String[] buildFormNameList(){
        List<String> formNames = new ArrayList<String>();
        formNames.add(KOHORT_KB_REGISTER);
        formNames.add(KOHORT_KB_UPDATE);
        formNames.add(KOHORT_KB_EDIT);
        formNames.add(KOHORT_KB_CLOSE);
        DialogOption[] options = getEditOptions();
      //  for (int i = 0; i < options.length; i++) {
     //       formNames.add(((OpenFormOption) options[i]).getFormName());
     //   }
        return formNames.toArray(new String[formNames.size()]);
    }

    @Override
    protected void onPause() {
        super.onPause();
        retrieveAndSaveUnsubmittedFormData();
    }

    public void retrieveAndSaveUnsubmittedFormData(){
        if (currentActivityIsShowingForm()){
            DisplayFormFragment formFragment = getDisplayFormFragmentAtIndex(currentPage);
            formFragment.saveCurrentFormData();
        }
    }

    private boolean currentActivityIsShowingForm(){
        return currentPage != 0;
    }

    private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
//            mBaseFragment = new NativeKISmartRegisterFragment();

//            nf.setCriteria("");
//            onBackPressed();
            Log.e(TAG, "onClick: Cancel");

            Intent intent= new Intent(NativeKBSmartRegisterActivity.this, NativeKBSmartRegisterActivity.class);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//            Toast.makeText(NativeKISmartRegisterActivity.this, mBaseFragment.toString(), Toast.LENGTH_SHORT).show();

        }
    };

}
