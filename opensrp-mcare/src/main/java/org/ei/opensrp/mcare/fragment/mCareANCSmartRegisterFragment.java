package org.ei.opensrp.mcare.fragment;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.commonregistry.CommonRepository;
import org.ei.opensrp.commonregistry.ControllerFilterMap;
import org.ei.opensrp.cursoradapter.CursorCommonObjectFilterOption;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.cursoradapter.CursorFilterOption;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.mcare.LoginActivity;
import org.ei.opensrp.mcare.R;
import org.ei.opensrp.mcare.anc.mCareANCServiceModeOption;
import org.ei.opensrp.mcare.anc.mCareANCSmartClientsProvider;
import org.ei.opensrp.mcare.anc.mCareANCSmartRegisterActivity;
import org.ei.opensrp.mcare.anc.mCareAncDetailActivity;
import org.ei.opensrp.mcare.elco.ElcoMauzaCommonObjectFilterOption;
import org.ei.opensrp.mcare.elco.ElcoPSRFDueDateSort;
import org.ei.opensrp.mcare.elco.ElcoSmartRegisterActivity;
import org.ei.opensrp.mcare.elco.PSRFHandler;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.util.StringUtil;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.ECClient;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.controller.VillageController;
import org.ei.opensrp.view.customControls.CustomFontTextView;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.ArrayList;
import java.util.Map;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by koros on 11/2/15.
 */
public class mCareANCSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    private SmartRegisterClientsProvider clientProvider = null;
    private CommonPersonObjectController controller;
    private VillageController villageController;
    private DialogOptionMapper dialogOptionMapper;

    private final ClientActionHandler clientActionHandler = new ClientActionHandler();

    @Override
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(clientsProvider());
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {

            @Override
            public ServiceModeOption serviceMode() {
                return new mCareANCServiceModeOption(clientsProvider());
            }

            @Override
            public FilterOption villageFilter() {
                return new AllClientsFilter();
            }

            @Override
            public SortOption sortOption() {
                return new ElcoPSRFDueDateSort();

            }

            @Override
            public String nameInShortFormForTitle() {
                return getResources().getString(R.string.mcare_ANC_register_title_in_short);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                ArrayList<DialogOption> dialogOptionslist = new ArrayList<DialogOption>();
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_all_label),""));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc1),filterStringForANCRV1()));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc2),filterStringForANCRV2()));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc3),filterStringForANCRV3()));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc4),filterStringForANCRV4()));

                String locationjson = context().anmLocationController().get();
                LocationTree locationTree = EntityUtils.fromJson(locationjson, LocationTree.class);

                Map<String,TreeNode<String, Location>> locationMap =
                        locationTree.getLocationsHierarchy();
                addChildToList(dialogOptionslist,locationMap);
                DialogOption[] dialogOptions = new DialogOption[dialogOptionslist.size()];
                for (int i = 0;i < dialogOptionslist.size();i++){
                    dialogOptions[i] = dialogOptionslist.get(i);
                }

                return  dialogOptions;
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
//                        new ElcoPSRFDueDateSort(),
                        new CursorCommonObjectSort(getString(R.string.due_status),sortByAlertmethod()),
                        new CursorCommonObjectSort(Context.getInstance().applicationContext().getString(R.string.elco_alphabetical_sort),sortByFWWOMFNAME()),
                        new CursorCommonObjectSort(Context.getInstance().applicationContext().getString(R.string.hh_fwGobhhid_sort),sortByGOBHHID()),
                        new CursorCommonObjectSort( Context.getInstance().applicationContext().getString(R.string.hh_fwJivhhid_sort),sortByJiVitAHHID()),
                        new CursorCommonObjectSort( Context.getInstance().applicationContext().getString(R.string.sortbyLmp),sortByLmp())

//                        new CommonObjectSort(true,false,true,"age")
                };
            }

            @Override
            public String searchHint() {
                return getString(R.string.str_ec_search_hint);
            }
        };
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {

        return null;
    }


    @Override
    protected void onInitialization() {

        context().formSubmissionRouter().getHandlerMap().put("psrf_form",new PSRFHandler());
    }

    @Override
    protected void startRegistration() {
        ((ElcoSmartRegisterActivity)getActivity()).startRegistration();
    }

    @Override
    protected void onCreation() {
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        if(isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        try{
            LoginActivity.setLanguage();
        }catch (Exception e){

        }

    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(INVISIBLE);

        ImageButton startregister = (ImageButton)view.findViewById(org.ei.opensrp.R.id.register_client);
        startregister.setVisibility(View.GONE);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
//        list.setBackgroundColor(Color.RED);
        initializeQueries();
    }

    private DialogOption[] getEditOptions() {
        return ((mCareANCSmartRegisterActivity)getActivity()).getEditOptions();
    }
    private DialogOption[] getEditOptionsforanc(String ancvisittext,String ancvisitstatus) {
        return ((mCareANCSmartRegisterActivity)getActivity()).getEditOptionsforanc(ancvisittext,ancvisitstatus);
    }



    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout:
                    mCareAncDetailActivity.ancclient = (CommonPersonObjectClient)view.getTag();
                    Intent intent = new Intent(getActivity(),mCareAncDetailActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nbnf_due_date:
                    showFragmentDialog(new EditDialogOptionModelfornbnf(), view.getTag(R.id.clientobject));
                    break;
                case R.id.anc_reminder_due_date:
                    CustomFontTextView ancreminderDueDate = (CustomFontTextView)view.findViewById(R.id.anc_reminder_due_date);
                    Log.v("do as you will", (String) view.getTag(R.id.textforAncRegister));
                    showFragmentDialog(new EditDialogOptionModelForANC((String)view.getTag(R.id.textforAncRegister),(String)view.getTag(R.id.AlertStatustextforAncRegister)), view.getTag(R.id.clientobject));
                    break;
            }
        }

        private void showProfileView(ECClient client) {
            navigationController.startEC(client.entityId());
        }
    }
    private class EditDialogOptionModelfornbnf implements DialogOptionModel {
        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptions();
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }
    }
    private class EditDialogOptionModelForANC implements DialogOptionModel {
        String ancvisittext ;;
        String Ancvisitstatus;
        public EditDialogOptionModelForANC(String text,String status) {
            ancvisittext = text;
            Ancvisitstatus = status;
        }

        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptionsforanc(ancvisittext,Ancvisitstatus);
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }
    }



    public void updateSearchView(){
        getSearchView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(final CharSequence cs, int start, int before, int count) {

                if(cs.toString().equalsIgnoreCase("")){
                    filters = "";
                }else {
                    //filters = "and FWWOMFNAME Like '%" + cs.toString() + "%' or GOBHHID Like '%" + cs.toString() + "%'  or JiVitAHHID Like '%" + cs.toString() + "%' ";
                    filters = cs.toString();
                }
                joinTable = "";
                mainCondition = " is_closed=0 AND FWWOMFNAME not null and FWWOMFNAME != \"\" ";

                getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                CountExecute();
                filterandSortExecute();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    public void addChildToList(ArrayList<DialogOption> dialogOptionslist,Map<String,TreeNode<String, Location>> locationMap){
        for(Map.Entry<String, TreeNode<String, Location>> entry : locationMap.entrySet()) {

            if(entry.getValue().getChildren() != null) {
                addChildToList(dialogOptionslist,entry.getValue().getChildren());

            }else{
                StringUtil.humanize(entry.getValue().getLabel());
                String name = StringUtil.humanize(entry.getValue().getLabel());
                dialogOptionslist.add(new ElcoMauzaCommonObjectFilterOption(name,"location_name", name, "ec_elco"));

            }
        }
    }
    class ancControllerfiltermap extends ControllerFilterMap{

        @Override
        public boolean filtermapLogic(CommonPersonObject commonPersonObject) {
            boolean returnvalue = false;
            if(commonPersonObject.getDetails().get("FWWOMVALID") != null){
                if(commonPersonObject.getDetails().get("FWWOMVALID").equalsIgnoreCase("1")){
                    returnvalue = true;
                    if(commonPersonObject.getDetails().get("Is_PNC")!=null){
                        if(commonPersonObject.getDetails().get("Is_PNC").equalsIgnoreCase("1")){
                            returnvalue = false;
                        }

                    }
                }
            }
            Log.v("the filter",""+returnvalue);
            return returnvalue;
        }
    }
    public String ancMainSelectWithJoins(){
        return "Select ec_elco.id as _id, ec_mcaremother.relationalid,ec_elco.FWWOMNID,ec_elco.FWWOMBID, ec_elco.FWWOMFNAME, ec_elco.FWWOMMAUZA_PARA as mauza, ec_mcaremother.FWPSRLMP, ec_elco.JiVitAHHID, ec_elco.GOBHHID \n" +
                "from ec_mcaremother Left Join ec_elco on  ec_mcaremother.id = ec_elco.id \n";
    }
    public String ancMainCountWithJoins(){
        return "Select Count(*) \n" +
                "from ec_mcaremother Left Join ec_elco on  ec_mcaremother.id = ec_elco.id \n";
    }
    public void initializeQueries(){
        try {
            mCareANCSmartClientsProvider hhscp = new mCareANCSmartClientsProvider(getActivity(),clientActionHandler,context().alertService());
            clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, new CommonRepository("ec_mcaremother",new String []{"FWWOMFNAME","FWWOMNID","mauza","FWPSRLMP","JiVitAHHID","GOBHHID"}));
            clientsView.setAdapter(clientAdapter);

            setTablename("ec_mcaremother");
            SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder(ancMainCountWithJoins());
            mainCondition = " is_closed=0 AND FWWOMFNAME not null and FWWOMFNAME != \"\" ";
            joinTable = "";
            countSelect = countqueryBUilder.mainCondition(mainCondition);
            super.CountExecute();

            SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder(ancMainSelectWithJoins());
            mainSelect = queryBUilder.mainCondition(" ec_mcaremother.is_closed=0 AND ec_elco.FWWOMFNAME not null and ec_elco.FWWOMFNAME != \"\" ");
            Sortqueries = sortByFWWOMFNAME();

            currentlimit = 20;
            currentoffset = 0;

            super.filterandSortInInitializeQueries();

            updateSearchView();
            refresh();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {

        }

    }
    private String sortBySortValue(){
        return " FWSORTVALUE ASC";
    }
    private String sortByFWWOMFNAME(){
        return " FWWOMFNAME ASC";
    }
    private String sortByJiVitAHHID(){
        return " JiVitAHHID ASC";
    }
    private String sortByLmp(){
        return " FWPSRLMP ASC";
    }
    private String filterStringForANCRV1(){
        return "ancrv_1";
    }
    private String filterStringForANCRV2(){
        return "ancrv_2";
    }
    private String filterStringForANCRV3(){
        return "ancrv_3";
    }
    private String filterStringForANCRV4(){
        return "ancrv_4";
    }
    private String sortByGOBHHID(){
        return " GOBHHID ASC";
    }
    private String sortByAlertmethod() {
        return " CASE WHEN Ante_Natal_Care_Reminder_Visit = 'urgent' and BirthNotificationPregnancyStatusFollowUp = 'urgent' THEN 1 "
                +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'upcoming' and BirthNotificationPregnancyStatusFollowUp = 'urgent' THEN  2\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'normal' and BirthNotificationPregnancyStatusFollowUp = 'urgent' THEN 3\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'expired' and BirthNotificationPregnancyStatusFollowUp = 'urgent' THEN 4\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit is null and BirthNotificationPregnancyStatusFollowUp = 'urgent' THEN 5\n" +

                "WHEN Ante_Natal_Care_Reminder_Visit = 'urgent' and BirthNotificationPregnancyStatusFollowUp = 'upcoming' THEN 6\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'upcoming' and BirthNotificationPregnancyStatusFollowUp = 'upcoming' THEN 7\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'normal' and BirthNotificationPregnancyStatusFollowUp = 'upcoming' THEN 8\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'expired' and BirthNotificationPregnancyStatusFollowUp = 'upcoming' THEN 9\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit is null and BirthNotificationPregnancyStatusFollowUp = 'upcoming' THEN 10\n" +

                "WHEN Ante_Natal_Care_Reminder_Visit = 'urgent' and BirthNotificationPregnancyStatusFollowUp = 'normal' THEN 11\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'upcoming' and BirthNotificationPregnancyStatusFollowUp = 'normal' THEN 12\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'normal' and BirthNotificationPregnancyStatusFollowUp = 'normal' THEN 13\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'expired' and BirthNotificationPregnancyStatusFollowUp = 'normal' THEN 14\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit is null and BirthNotificationPregnancyStatusFollowUp = 'normal' THEN 15\n" +

                "WHEN Ante_Natal_Care_Reminder_Visit = 'urgent' and BirthNotificationPregnancyStatusFollowUp = 'expired' THEN 16\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'upcoming' and BirthNotificationPregnancyStatusFollowUp = 'expired' THEN 17\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'normal' and BirthNotificationPregnancyStatusFollowUp = 'expired' THEN 18\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = 'expired' and BirthNotificationPregnancyStatusFollowUp = 'expired' THEN 19\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit is null and BirthNotificationPregnancyStatusFollowUp = 'expired' THEN 20\n" +

                "WHEN BirthNotificationPregnancyStatusFollowUp is null THEN 9999\n" +
                "WHEN Ante_Natal_Care_Reminder_Visit = \"\" THEN 99999\n" +
//                "WHEN BirthNotificationPregnancyStatusFollowUp is null THEN '18'\n" +
                "Else Ante_Natal_Care_Reminder_Visit END ASC";
    }

    /**
     * Override filter to capture fts filter by location
     * @param filter
     */
    @Override
    public void onFilterSelection(FilterOption filter) {
        super.onFilterSelection(filter);
    }
}
