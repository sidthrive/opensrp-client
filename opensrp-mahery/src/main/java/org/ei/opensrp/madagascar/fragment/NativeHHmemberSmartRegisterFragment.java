package org.ei.opensrp.madagascar.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.commonregistry.CommonRepository;
import org.ei.opensrp.cursoradapter.CursorCommonObjectFilterOption;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.madagascar.HH.HHClientsProvider;
import org.ei.opensrp.madagascar.HH.HHServiceMode;
import org.ei.opensrp.madagascar.HH.HHSmartRegisterActivity;
import org.ei.opensrp.madagascar.HH.HouseHoldDetailActivity;
import org.ei.opensrp.madagascar.HH.KICommonObjectFilterOption;
import org.ei.opensrp.madagascar.HH.RCCDetailActivity;
import org.ei.opensrp.madagascar.HHmember.HHMemberServiceMode;
import org.ei.opensrp.madagascar.HHmember.HHmemberClientsProvider;
import org.ei.opensrp.madagascar.HHmember.HHmemberDetailActivity;
import org.ei.opensrp.madagascar.HHmember.HHmemberSmartRegisterActivity;
import org.ei.opensrp.madagascar.LoginActivity;
import org.ei.opensrp.madagascar.R;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.util.StringUtil;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.ECClient;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.controller.VillageController;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.LocationSelectorDialogFragment;
import org.ei.opensrp.view.dialog.NameSort;
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
 * Created by Dimas Ciputra on 2/18/15.
 */
public class NativeHHmemberSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    private SmartRegisterClientsProvider clientProvider = null;
    private CommonPersonObjectController controller;
    private VillageController villageController;
    private DialogOptionMapper dialogOptionMapper;
    public static CommonPersonObjectClient HHid;
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private String locationDialogTAG = "locationDialogTAG";
    @Override
    protected void onCreation() {
        //
    }

//    @Override
//    protected SmartRegisterPaginatedAdapter adapter() {
//        return new SmartRegisterPaginatedAdapter(clientsProvider());
//    }

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {

            @Override
            public ServiceModeOption serviceMode() {
                return new HHMemberServiceMode(clientsProvider());
            }

            @Override
            public FilterOption villageFilter() {
                return new AllClientsFilter();
            }

            @Override
            public SortOption sortOption() {
                return new NameSort();

            }

            @Override
            public String nameInShortFormForTitle() {
                return Context.getInstance().getStringResource(R.string.hh_members);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
               // FlurryFacade.logEvent("click_filter_option_on_kohort_ibu_dashboard");
                ArrayList<DialogOption> dialogOptionslist = new ArrayList<DialogOption>();

                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_all_label),filterStringForAll()));
           //     dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_no_mwra),filterStringForNoElco()));
          //      dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_has_mwra),filterStringForOneOrMoreElco()));

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
           //     FlurryFacade.logEvent("click_sorting_option_on_kohort_ibu_dashboard");
                return new DialogOption[]{
//                        new HouseholdCensusDueDateSort(),


                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_name_label),KiSortByNameAZ()),
                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_name_label_reverse),KiSortByNameZA()),
                    //    new CursorCommonObjectSort(getResources().getString(R.string.sort_by_wife_age_label),KiSortByAge()),
                    //  new CursorCommonObjectSort(getResources().getString(R.string.sort_by_wife_age_label_rev),KiSortByAgeRev()),
                    //     new CursorCommonObjectSort(getResources().getString(R.string.short_by_gender),shortbygenderFM()),
                    //    new CursorCommonObjectSort(getResources().getString(R.string.short_by_gendermf),shortbygenderMF()),
                 };
            }

            @Override
            public String searchHint() {
                return getResources().getString(R.string.hh_search_hint);
            }
        };
    }



    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
//        if (clientProvider == null) {
//            clientProvider = new HouseHoldSmartClientsProvider(
//                    getActivity(),clientActionHandler , context.alertService());
//        }
        return null;
    }

    private DialogOption[] getEditOptions() {
        return ((HHmemberSmartRegisterActivity)getActivity()).getEditOptions();
    }

    @Override
    protected void onInitialization() {
      //  context.formSubmissionRouter().getHandlerMap().put("census_enrollment_form", new CensusEnrollmentHandler());
    }

    @Override
    public void setupViews(View view) {
        getDefaultOptionsProvider();

        super.setupViews(view);
        view.findViewById(R.id.register_client).setVisibility(View.GONE);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(View.GONE);
        view.findViewById(R.id.filter_selection).setVisibility(View.GONE);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
//        list.setBackgroundColor(Color.RED);
        initializeQueries(getCriteria());
    }
    private String filterStringForAll(){
        return "";
    }
    private String sortByAlertmethod() {
        return " CASE WHEN alerts.status = 'urgent' THEN '1'"
                +
                "WHEN alerts.status = 'upcoming' THEN '2'\n" +
                "WHEN alerts.status = 'normal' THEN '3'\n" +
                "WHEN alerts.status = 'expired' THEN '4'\n" +
                "WHEN alerts.status is Null THEN '5'\n" +
                "Else alerts.status END ASC";
    }
    public void initializeQueries(String s){
        HHmemberClientsProvider kiscp = new HHmemberClientsProvider(getActivity(),clientActionHandler,
                context().alertService());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, kiscp, new CommonRepository("HHMember",new String []{ "Name_family_member","Ethnic_Group","Sex","Education","Profession","isClosed"}));
        clientsView.setAdapter(clientAdapter);

        setTablename("HHMember");
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts("HHMember");


        countSelect = countqueryBUilder.mainCondition(" Name_family_member != '' ");
       // mainCondition = " Name_family_member != '' ";
       // mainCondition = " ";
                if (s == null) {
                        mainCondition = " Name_family_member != '' ";
                     //  Log.e(TAG, "initializeQueries: Not Initialized" );
                            } else {
                     //   Log.e(TAG, "initializeQueries: id " + s); details not LIKE '%"jenisKontrasepsi":""%'
                                mainCondition = " Name_family_member != '' AND details LIKE '%" + s + "%'";
                   }
        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable("HHMember", new String[]{"isClosed", "details","Name_family_member","Ethnic_Group","Sex","Education","Profession"});
        // queryBUilder.customJoin("LEFT JOIN ibu on HH.id = ibu.kartuIbuId LEFT JOIN anak ON ibu.id = anak.ibuCaseId ");
        //    countqueryBUilder.joinwithchilds("ibu");
        mainSelect = queryBUilder.mainCondition(" Name_family_member != '' ");
        Sortqueries = KiSortByNameAZ();
        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

//        setServiceModeViewDrawableRight(null);
        updateSearchView();
        refresh();


    }

//    String HHids = NativeHHmemberSmartRegisterFragment.HHid.getCaseId();

/*            public void setCriteria(CommonPersonObjectClient HHid) {
                String HHids = NativeHHmemberSmartRegisterFragment.HHid.getCaseId();
                HHids = HHid;
            }*/

    public static String getCriteria() {
        if (NativeHHmemberSmartRegisterFragment.HHid == null){
            return "";
        }
        else {
            String HHids = NativeHHmemberSmartRegisterFragment.HHid.getCaseId();

            return HHids;
        }
    }

    @Override
    public void startRegistration() {
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(locationDialogTAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        LocationSelectorDialogFragment
                .newInstance((HHmemberSmartRegisterActivity) getActivity(), new
                        EditDialogOptionModel(), context().anmLocationController().get(),
                        "unique_identifier")
                .show(ft, locationDialogTAG);
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout:
                 //   FlurryFacade.logEvent("click_detail_view_on_kohort_ibu_dashboard");
                    HHmemberDetailActivity.memberclient = (CommonPersonObjectClient)view.getTag();
                    Intent intent = new Intent(getActivity(),HHmemberDetailActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            //    case R.id.id_detail_layout:
            //        HouseHoldDetailActivity.householdclient = (CommonPersonObjectClient)view.getTag();
//
            //        showFragmentDialog(new EditDialogOptionModel(), view.getTag());
            //        break;
                case R.id.btn_edit:
                    RCCDetailActivity.kiclient = (CommonPersonObjectClient)view.getTag();
                    showFragmentDialog(new EditDialogOptionModel(), view.getTag());
                    break;

            }
        }

        private void showProfileView(ECClient client) {
            navigationController.startEC(client.entityId());
        }
    }



    private String KiSortByNameAZ() {
        return " Name_family_member ASC";
    }
    private String KiSortByNameZA() {
        return " Name_family_member DESC";
    }

    private String KiSortByAge() {
        return " respondent_age DESC";
    }

    private String KiSortByAgeRev() {
        return " respondent_age ASC";
    }
    private String KiSortByNoIbu() {
        return " noIbu ASC";
    }

    private String shortbygenderFM() {
         return "  CASE\n" +
                 "        WHEN relation_to_child LIKE '%mother' OR relation_to_child LIKE '%female-care_giver' THEN 1\n" +
                 "        WHEN relation_to_child LIKE '%father' OR relation_to_child LIKE '%male-care_giver' THEN 2\n" +
                 "    END ASC";

    }
    private String shortbygenderMF() {
        return "  CASE\n" +
                "        WHEN relation_to_child LIKE '%father' OR relation_to_child LIKE '%male-care_giver' THEN 1\n" +
                "        WHEN relation_to_child LIKE '%mother' OR relation_to_child LIKE '%female-care_giver' THEN 2\n" +
                "    END ASC";

    }


    private class EditDialogOptionModel implements DialogOptionModel {
        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptions();
        }
        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {

            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }
    }

    @Override
    protected void onResumption() {
//        super.onResumption();
        getDefaultOptionsProvider();
        if(isPausedOrRefreshList()) {
            initializeQueries(getCriteria());
        }
   //     updateSearchView();
//
        try{
            LoginActivity.setLanguage();
        }catch (Exception e){

        }

    }
    @Override
    public void setupSearchView(View view) {
        searchView = (EditText) view.findViewById(org.ei.opensrp.R.id.edt_search);
        searchView.setHint(getNavBarOptionsProvider().searchHint());
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(final CharSequence cs, int start, int before, int count) {

                filters = cs.toString();
                joinTable = "";
                mainCondition = " Name_family_member != '' ";

                getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                CountExecute();
                filterandSortExecute();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        searchCancelView = view.findViewById(org.ei.opensrp.R.id.btn_search_cancel);
        searchCancelView.setOnClickListener(searchCancelHandler);
    }

    public void updateSearchView(){
        getSearchView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(final CharSequence cs, int start, int before, int count) {

                filters = cs.toString();
                joinTable = "";
                mainCondition = " Name_family_member != '' ";

                getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
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
                dialogOptionslist.add(new KICommonObjectFilterOption(name,"desa", name));


            }
        }
    }


}
