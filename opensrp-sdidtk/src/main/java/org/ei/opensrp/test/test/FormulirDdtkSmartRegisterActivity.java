package org.ei.opensrp.test.test;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.test.LoginActivity;
import org.ei.opensrp.test.R;
//import org.ei.opensrp.test.fragment.HouseHoldSmartRegisterFragment;
import org.ei.opensrp.test.fragment.FormulirDdtkSmartRegisterFragment;
import org.ei.opensrp.test.pageradapter.BaseRegisterActivityPagerAdapter;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.OpenSRPViewPager;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FormulirDdtkSmartRegisterActivity extends SecuredNativeSmartRegisterActivity {

    public static final String TAG = "TestActivity";
    @Bind(R.id.view_pager)
    OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private int currentPage;

    private String[] formNames = new String[]{};
    private android.support.v4.app.Fragment mBaseFragment = null;


    ZiggyService ziggyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        formNames = this.buildFormNameList();
        mBaseFragment = new FormulirDdtkSmartRegisterFragment();

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
    protected void setupViews() {}

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
                    new OpenFormOption("Antropometri", "antropometri", formController),
                    new OpenFormOption("Kpsp Bayi 1 Tahun", "kpsp_bayi_1thn", formController),
                    new OpenFormOption("Kpsp Balita 2 Tahun", "kpsp_balita_2thn", formController),
                    new OpenFormOption("Kpsp Balita 3 Tahun", "kpsp_balita_3thn", formController),
                    new OpenFormOption("Kpsp Balita 4 Tahun", "kpsp_balita_4thn", formController),
                    new OpenFormOption("Kpsp Balita 5 Tahun", "kpsp_balita_5thn", formController),
                    new OpenFormOption("Kpsp Balita 6 Tahun", "kpsp_balita_6thn", formController),
                    new OpenFormOption("Tes Daya Dengar", "tes_daya_dengar", formController),
                    new OpenFormOption("Tes Daya Lihat", "tes_daya_lihat", formController),
                    new OpenFormOption("Masalah Mental Emosional", "masalah_mental_emosional", formController),
                    new OpenFormOption("Deteksi Dini Autis", "deteksi_dini_autis", formController),
                    new OpenFormOption("GGPH", "gangguan_konsentrasi_hiperaktivitas", formController)

            };
    }


    private String getalertstateforcensus(CommonPersonObjectClient pc) {
        try {
            List<Alert> alertlist_for_client = Context.getInstance().alertService().findByEntityIdAndAlertNames(pc.entityId(), "FW CENSUS");
            String alertstate = "";
            if (alertlist_for_client.size() == 0) {

            } else {
                for (int i = 0; i < alertlist_for_client.size(); i++) {
//           psrfdue.setText(alertlist_for_client.get(i).expiryDate());
                    Log.v("printing alertlist", alertlist_for_client.get(i).status().value());
                    alertstate = alertlist_for_client.get(i).status().value();

                }
            }
            return alertstate;
        }catch (Exception e){
            return "";
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
       // Log.v("fieldoverride", metaData);
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
                    displayFormFragment.loadFormData();
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
                    displayFormFragment.loadFormData();
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

        formNames.add("formulir_ddtk");
        formNames.add("antropometri");
        formNames.add("kpsp_bayi_1thn");
        formNames.add("kpsp_balita_2thn");
        formNames.add("kpsp_balita_3thn");
        formNames.add("kpsp_balita_4thn");
        formNames.add("kpsp_balita_5thn");
        formNames.add("kpsp_balita_6thn");
        formNames.add("tes_daya_dengar");
        formNames.add("tes_daya_lihat");
        formNames.add("gangguan_konsentrasi_hiperaktivitas");
        formNames.add("masalah_mental_emosional");
        formNames.add("deteksi_dini_autis");
     //   formNames.add("census_enrollment_form");
//        DialogOption[] options = getEditOptions();
//        for (int i = 0; i < options.length; i++){
//            formNames.add(((OpenFormOption) options[i]).getFormName());
//        }
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
}
