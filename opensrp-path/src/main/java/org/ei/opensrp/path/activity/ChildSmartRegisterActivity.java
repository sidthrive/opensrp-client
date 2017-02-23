package org.ei.opensrp.path.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.adapter.BaseRegisterActivityPagerAdapter;
import org.ei.opensrp.path.fragment.ChildSmartRegisterFragment;
import org.ei.opensrp.path.receiver.ServiceReceiver;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.UniqueIdRepository;
import org.ei.opensrp.service.FormSubmissionService;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.ei.opensrp.view.viewpager.OpenSRPViewPager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import util.JsonFormUtils;
import util.barcode.Barcode;
import util.barcode.BarcodeIntentIntegrator;
import util.barcode.BarcodeIntentResult;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class ChildSmartRegisterActivity extends SecuredNativeSmartRegisterActivity {
private static String TAG=ChildSmartRegisterActivity.class.getCanonicalName();

    @Bind(R.id.view_pager)
    OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private int currentPage;

    private String[] formNames = new String[]{};
    private android.support.v4.app.Fragment mBaseFragment = null;

    private ServiceReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        formNames = this.buildFormNameList();
        mBaseFragment = new ChildSmartRegisterFragment();

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

    }

    private String[] buildFormNameList() {
        List<String> formNames = new ArrayList<String>();
        formNames.add("child_enrollment");
        formNames.add("child_followup");
        formNames.add("offsite_child_followup");

        return formNames.toArray(new String[formNames.size()]);
    }


    public void onPageChanged(int page) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(clientsProvider());
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected void setupViews() {
    }

    @Override
    protected void onResumption() {
    }

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {
        return null;
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {
    }

    @Override
    public void startRegistration() {
    }

    @Override
    public void showFragmentDialog(DialogOptionModel dialogOptionModel, Object tag) {
        try {
            LoginActivity.setLanguage();
        } catch (Exception e) {

        }
        super.showFragmentDialog(dialogOptionModel, tag);
    }


    public DialogOption[] getEditOptions(HashMap<String, String> overridemap) {

        return new DialogOption[]{
                new OpenFormOption(getResources().getString(R.string.child_followup), "child_followup", formController, overridemap, OpenFormOption.ByColumnAndByDetails.bydefault)
        };
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
//        Log.v("fieldoverride", metaData);
        try {
            UniqueIdRepository uniqueIdRepo = Context.getInstance().uniqueIdRepository();
            String openmrsId=uniqueIdRepo.getNextUniqueId()!=null?uniqueIdRepo.getNextUniqueId().getOpenmrsId():"";
            if(openmrsId.isEmpty()){
                Toast.makeText(this,getString(R.string.no_openmrs_id),Toast.LENGTH_SHORT).show();
                return;
            }
            int formIndex = FormUtils.getIndexForFormName(formName, formNames) + 1; // add the offset
            /*if (entityId != null || metaData != null){
                String data = null;
                //check if there is previously saved data for the form
                data = getPreviouslySavedDataForForm(formName, metaData, entityId);
                if (data == null){
                    data = FormUtils.getInstance(getApplicationContext()).generateXMLInputForFormWithEntityId(entityId, formName, metaData);
                }

                DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(formIndex);
                if (displayFormFragment != null) {
                    displayFormFragment.setFormPartialSaving(false);
                    displayFormFragment.setFormData(data);
                    displayFormFragment.setRecordId(entityId);
                    displayFormFragment.setFieldOverides(metaData);
                }
            }

            mPager.setCurrentItem(formIndex, false); //Don't animate the view on orientation
            change the view disapears*/
            JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson(formName);
            setLocationHierarchyQuestions(form);
            if (form != null ) {
                Intent intent = new Intent(getApplicationContext(), JsonFormActivity.class);
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    if(jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.ZEIRD)){
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, openmrsId.replace("-",""));
                        continue;
                    }
                }
                intent.putExtra("json", form.toString());
                startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

    }

    private void setLocationHierarchyQuestions(JSONObject form) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            JSONArray treeWithoutOther = JsonFormUtils.generateLocationHierarchyTree(context(), false);
            JSONArray treeWithOther = JsonFormUtils.generateLocationHierarchyTree(context(), true);

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")
                        || questions.getJSONObject(i).getString("key").equals("Birth_Facility_Name")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(treeWithoutOther.toString()));
                } else if (questions.getJSONObject(i).getString("key").equals("Residential_Area")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(treeWithOther.toString()));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON) {
            if (resultCode == RESULT_OK) {

                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JsonFormUtils.save(this, jsonString, allSharedPreferences.fetchRegisteredANM(), "ec_child", "Child_Photo");
                refreshBaseFragment(true);
            }
        } else if (requestCode == BarcodeIntentIntegrator.REQUEST_CODE) {
            BarcodeIntentResult res = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (StringUtils.isNotBlank(res.getContents())) {
                onQRCodeSucessfullyScanned(res.getContents());
            } else Log.i("", "NO RESULT FOR QR CODE");
        }
    }

    @Override
    public void saveFormSubmission(String formSubmission, String id, String formName, JSONObject fieldOverrides) {
        // save the form
        try {
            FormUtils formUtils = FormUtils.getInstance(getApplicationContext());
            FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            org.ei.opensrp.Context context = org.ei.opensrp.Context.getInstance();
            ZiggyService ziggyService = context.ziggyService();
            ziggyService.saveForm(getParams(submission), submission.instance());

            FormSubmissionService formSubmissionService = context.formSubmissionService();
            formSubmissionService.updateFTSsearch(submission);

            Log.v("we are here", "hhregister");
            //switch to forms list fragmentstregi
            switchToBaseFragment(formSubmission); // Unnecessary!! passing on data

        } catch (Exception e) {
            DisplayFormFragment displayFormFragment = getDisplayFormFragmentAtIndex(currentPage);
            if (displayFormFragment != null) {
                displayFormFragment.setFormPartialSaving(false);
                displayFormFragment.hideTranslucentProgressDialog();
            }
            e.printStackTrace();
        }
    }

    public void switchToBaseFragment(final String data) {
        Log.v("we are here", "switchtobasegragment");
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
                    displayFormFragment.setFormPartialSaving(false);
                    displayFormFragment.hideTranslucentProgressDialog();
                    displayFormFragment.setFormData(null);
                }

                displayFormFragment.setRecordId(null);
            }
        });

    }

    public void refreshBaseFragment(final boolean result) {
        SecuredNativeSmartRegisterFragment registerFragment = (SecuredNativeSmartRegisterFragment) findFragmentByPosition(0);
        if (registerFragment != null && result) {
            registerFragment.refreshListView();
        }
    }


    @Override
    public void onBackPressed() {
        if (currentPage != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(org.ei.opensrp.path.R.string.form_back_confirm_dialog_message)
                    .setTitle(org.ei.opensrp.path.R.string.form_back_confirm_dialog_title)
                    .setCancelable(false)
                    .setPositiveButton(org.ei.opensrp.path.R.string.yes_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    switchToBaseFragment(null);
                                }
                            })
                    .setNegativeButton(org.ei.opensrp.path.R.string.no_button_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                    .show();
        } else if (currentPage == 0) {
            super.onBackPressed(); // allow back key only if we are
        }
    }

    public android.support.v4.app.Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }

    public DisplayFormFragment getDisplayFormFragmentAtIndex(int index) {
        return (DisplayFormFragment) findFragmentByPosition(index);
    }

    public void retrieveAndSaveUnsubmittedFormData() {
        if (currentActivityIsShowingForm()) {
            DisplayFormFragment formFragment = getDisplayFormFragmentAtIndex(currentPage);
            formFragment.saveCurrentFormData();
        }
    }

    private boolean currentActivityIsShowingForm() {
        return currentPage != 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        retrieveAndSaveUnsubmittedFormData();
    }

    public void startQrCodeScanner() {
        BarcodeIntentIntegrator integ = new BarcodeIntentIntegrator(this);
        integ.addExtra(Barcode.SCAN_MODE, Barcode.QR_MODE);
        integ.initiateScan();
    }

    private void onQRCodeSucessfullyScanned(String qrCode) {
        Log.i(getClass().getName(), "QR code: "+ qrCode);
        //TODO Update qr code
    }
}
