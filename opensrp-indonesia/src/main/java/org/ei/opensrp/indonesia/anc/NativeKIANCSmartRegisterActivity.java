package org.ei.opensrp.indonesia.anc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.indonesia.LoginActivity;
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.fragment.NativeKBSmartRegisterFragment;
import org.ei.opensrp.indonesia.fragment.NativeKIANCSmartRegisterFragment;
import org.ei.opensrp.indonesia.kartu_ibu.KIDetailActivity;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
import org.ei.opensrp.indonesia.pageradapter.BaseRegisterActivityPagerAdapter;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.FormSubmissionService;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.activity.*;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.OpenSRPViewPager;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_CLOSE;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_EDIT;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_RENCANA_PERSALINAN;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_VISIT;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_VISIT_INTEGRASI;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_ANC_VISIT_LABTEST;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_PNC_REGISTRATION;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_CLOSE;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_EDIT;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_REGISTER;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KOHORT_KB_UPDATE;

/**
 * Created by Dimas Ciputra on 3/5/15.
 */
public class NativeKIANCSmartRegisterActivity extends SecuredNativeSmartRegisterActivity {
    SimpleDateFormat timer = new SimpleDateFormat("hh:mm:ss");
    public static final String TAG = "ANCActivity";
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

        //flurry log start
        String ANCstart = timer.format(new Date());
        Map<String, String> ANC = new HashMap<String, String>();
        ANC.put("start", ANCstart);
        FlurryAgent.logEvent("anc_dashboard",ANC, true);
      //  FlurryFacade.logEvent("anc_dashboard");
        formNames = this.buildFormNameList();
        mBaseFragment = new NativeKIANCSmartRegisterFragment();

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
                new OpenFormOption("Kunjungan ANC ", KARTU_IBU_ANC_VISIT, formController),
                new OpenFormOption("Kunjungan ANC Integrasi ", KARTU_IBU_ANC_VISIT_INTEGRASI, formController),
                new OpenFormOption("Kunjungan ANC Tes Lab ", KARTU_IBU_ANC_VISIT_LABTEST, formController),
                new OpenFormOption("Rencana Persalinan", KARTU_IBU_ANC_RENCANA_PERSALINAN, formController),
                new OpenFormOption("Dokumentasi Persalinan (Daftar PNC) ", KARTU_IBU_PNC_REGISTRATION, formController),
                new OpenFormOption("Edit ANC ", KARTU_IBU_ANC_EDIT, formController),
                new OpenFormOption("Penutupan ANC ", KARTU_IBU_ANC_CLOSE, formController),


        };


    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, JSONObject fieldOverrides){
        Log.v("fieldoverride", fieldOverrides.toString());
        // save the form
        try{
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            ziggyService.saveForm(getParams(submission), submission.instance());

            FormSubmissionService formSubmissionService = context.formSubmissionService();
            formSubmissionService.updateFTSsearch(submission);

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
        //end capture flurry log for FS
        String end = timer.format(new Date());
        Map<String, String> FS = new HashMap<String, String>();
        FS.put("end", end);
        FlurryAgent.logEvent(formName,FS, true);
    }

    @Override
    public void startFormActivity(final String formName, final String entityId, final String metaData) {

        if(formName != null) {
            final int choice = new java.util.Random().nextInt(3);
            CharSequence[] selections = selections(choice, entityId);

            final AlertDialog.Builder builder = new AlertDialog.Builder(NativeKIANCSmartRegisterActivity.this);
            builder.setTitle(context.getStringResource(R.string.reconfirmChildName));
            builder.setItems(selections, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    if (which == choice) {
                        activatingForm(formName,entityId,metaData);
                    }
                }
            });
            builder.show();
        }
        else{
            activatingForm(formName,entityId,metaData);
        }


    }

     private void activatingForm(String formName, String entityId, String metaData) {
        //Start capture flurry log for FS
        String start = timer.format(new Date());
        Map<String, String> FS = new HashMap<String, String>();
        FS.put("start", start);
        FlurryAgent.logEvent(formName, FS, true);
        // FlurryFacade.logEvent(formName);
//        Log.v("fieldoverride", metaData);
        try {
            int formIndex = FormUtils.getIndexForFormName(formName, formNames) + 1; // add the offset
            if (entityId != null || metaData != null) {
                String data = null;
                //check if there is previously saved data for the form
                data = getPreviouslySavedDataForForm(formName, metaData, entityId);
                if (data == null) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CharSequence[] selections(int choice, String entityId){
        AllCommonsRepository kiRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ibu");

        CommonPersonObject kiobject = kiRepository.findByCaseID(entityId);

        AllCommonsRepository iburep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("kartu_ibu");
        final CommonPersonObject ibuparent = iburep.findByCaseID(kiobject.getColumnmaps().get("kartuIbuId"));

        String name = ibuparent.getColumnmaps().get("namalengkap");

        System.out.println("start form activity / nama = " + name);
        CharSequence selections[] = new CharSequence[]{name, name, name};

        selections[choice] = (CharSequence) name;

        String query = "SELECT namalengkap FROM kartu_ibu where kartu_ibu.isClosed !='true'";
        Cursor cursor = context.commonrepository("kartu_ibu").RawCustomQueryForAdapter(query);
        cursor.moveToFirst();

        for (int i = 0; i < selections.length; i++) {
            if (i != choice) {
                cursor.move(new java.util.Random().nextInt(cursor.getCount()));
                String temp = cursor.getString(cursor.getColumnIndex("namalengkap"));
                System.out.println("start form activity / temp = " + temp);
                if(temp==null)
                    i--;
                else if (temp.equals(name)) {
                    System.out.println("equals");
                    i--;
                } else {
                    selections[i] = (CharSequence) temp;
                    System.out.println("char sequence of temp = " + selections[i]);
                }
                cursor.moveToFirst();
            }
        }
        cursor.close();

        return selections;
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
        if (currentPage != 0){

            new AlertDialog.Builder(this)
                    .setMessage(R.string.form_back_confirm_dialog_message)
                    .setTitle(R.string.form_back_confirm_dialog_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    switchToBaseFragment(null);
                                }
                            })
                    .setNegativeButton(R.string.no_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                }
                            })
                    .show();


        }else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }

    private String[] buildFormNameList(){
        List<String> formNames = new ArrayList<String>();
        formNames.add(KARTU_IBU_ANC_VISIT);
        formNames.add(KARTU_IBU_ANC_VISIT_INTEGRASI);
        formNames.add(KARTU_IBU_ANC_VISIT_LABTEST);
        formNames.add(KARTU_IBU_ANC_RENCANA_PERSALINAN);
        formNames.add(KARTU_IBU_PNC_REGISTRATION);
        formNames.add(KARTU_IBU_ANC_EDIT);
        formNames.add(KARTU_IBU_ANC_CLOSE);

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
        String ANCend = timer.format(new Date());
        Map<String, String> ANC = new HashMap<String, String>();
        ANC.put("end", ANCend);
        FlurryAgent.logEvent("anc_dashboard",ANC, true);
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
