package org.ei.opensrp.mcare.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonObjectFilterOption;
import org.ei.opensrp.commonregistry.CommonObjectSort;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectClients;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.commonregistry.CommonRepository;
import org.ei.opensrp.cursoradapter.CursorCommonObjectFilterOption;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.cursoradapter.CursorFilterOption;
import org.ei.opensrp.cursoradapter.CursorSortOption;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.mcare.LoginActivity;
import org.ei.opensrp.mcare.NativeHomeActivity;
import org.ei.opensrp.mcare.R;
import org.ei.opensrp.mcare.household.CensusEnrollmentHandler;
import org.ei.opensrp.mcare.household.HHMWRAEXISTFilterOption;
import org.ei.opensrp.mcare.household.HHMauzaCommonObjectFilterOption;
import org.ei.opensrp.mcare.household.HHSearchOption;
import org.ei.opensrp.mcare.household.HouseHoldDetailActivity;
import org.ei.opensrp.mcare.household.HouseHoldServiceModeOption;
import org.ei.opensrp.mcare.household.HouseHoldSmartClientsProvider;
import org.ei.opensrp.mcare.household.HouseHoldSmartRegisterActivity;
import org.ei.opensrp.mcare.household.HouseholdCensusDueDateSort;
import org.ei.opensrp.mcare.household.NOHHMWRAEXISTFilterOption;
import org.ei.opensrp.mcare.household.noNIDFilter;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.util.StringUtil;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.ECClient;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.controller.VillageController;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.ECSearchOption;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.LocationSelectorDialogFragment;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.fragment.SecuredNativeSmartRegisterFragment;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.AsyncTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by koros on 10/12/15.
 */
public class HouseHoldSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    private SmartRegisterClientsProvider clientProvider = null;
    private CommonPersonObjectController controller;
    private VillageController villageController;
    private DialogOptionMapper dialogOptionMapper;

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
                return new HouseHoldServiceModeOption(clientsProvider());
            }

            @Override
            public FilterOption villageFilter() {
                return new AllClientsFilter();
            }

            @Override
            public SortOption sortOption() {
                return new HouseholdCensusDueDateSort();

            }

            @Override
            public String nameInShortFormForTitle() {
                return Context.getInstance().getStringResource(R.string.hh_register_title_in_short);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {

                ArrayList<DialogOption> dialogOptionslist = new ArrayList<DialogOption>();
                //CEK DISINI
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_all_label),filterStringForAll()));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_no_mwra),filterStringForNoElco()));
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_has_mwra),filterStringForOneOrMoreElco()));

                String locationjson = context.anmLocationController().get();
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
//                        new HouseholdCensusDueDateSort(),

                        new CursorCommonObjectSort(getResources().getString(R.string.due_status),sortByAlertmethod()),
                        new CursorCommonObjectSort(getResources().getString(R.string.hh_alphabetical_sort),householdSortByName()),
                        new CursorCommonObjectSort(getResources().getString(R.string.hh_fwGobhhid_sort),householdSortByFWGOBHHID()),
                        new CursorCommonObjectSort(getResources().getString(R.string.hh_fwJivhhid_sort),householdSortByFWJIVHHID())
//""
//                        new CommonObjectSort(true,false,true,"age")
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
        return ((HouseHoldSmartRegisterActivity)getActivity()).getEditOptions();
    }

    @Override
    protected void onInitialization() {
        context.formSubmissionRouter().getHandlerMap().put("census_enrollment_form", new CensusEnrollmentHandler());
    }

    @Override
    public void setupViews(View view) {
        getDefaultOptionsProvider();

        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(View.GONE);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
//        list.setBackgroundColor(Color.RED);
        initializeQueries();
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
    public void initializeQueries(){
        CommonRepository commonRepository = context.commonrepository("household");
        setTablename("household");
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts("household");
        countqueryBUilder.joinwithALerts("household","FW CENSUS");
        countSelect = countqueryBUilder.mainCondition(" FWHOHFNAME is not null ");
        CountExecute();


        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable("household", new String[]{"relationalid", "details", "FWHOHFNAME", "FWGOBHHID", "FWJIVHHID"});
        queryBUilder.joinwithALerts("household","FW CENSUS");
        mainSelect = queryBUilder.mainCondition(" FWHOHFNAME is not null ");
        queryBUilder.addCondition(filters);
        Sortqueries = sortByAlertmethod();
        currentquery  = queryBUilder.orderbyCondition(Sortqueries);


//        queryBUilder.queryForRegisterSortBasedOnRegisterAndAlert("household", new String[]{"relationalid" ,"details","FWHOHFNAME", "FWGOBHHID","FWJIVHHID"}, null, "FW CENSUS");
//        Cursor c = commonRepository.CustomQueryForAdapter(new String[]{"id as _id","relationalid","details"},"household",""+currentlimit,""+currentoffset);
        Cursor c = commonRepository.RawCustomQueryForAdapter(queryBUilder.Endquery(queryBUilder.addlimitandOffset(currentquery, 20, 0)));
        HouseHoldSmartClientsProvider hhscp = new HouseHoldSmartClientsProvider(getActivity(),clientActionHandler,context.alertService());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), c, hhscp, new CommonRepository("household",new String []{"FWHOHFNAME", "FWGOBHHID","FWJIVHHID"}));
        clientsView.setAdapter(clientAdapter);
//        setServiceModeViewDrawableRight(null);
        updateSearchView();
        refresh();
//        checkforNidMissing(view);

    }


    @Override
    public void startRegistration() {
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag(locationDialogTAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        McareLocationSelectorDialogFragment
                .newInstance((HouseHoldSmartRegisterActivity) getActivity(), new EditDialogOptionModel(), context.anmLocationController().get(), "new_household_registration")
                .show(ft, locationDialogTAG);
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout:
                    HouseHoldDetailActivity.householdclient = (CommonPersonObjectClient)view.getTag();
                    Intent intent = new Intent(getActivity(),HouseHoldDetailActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                case R.id.hh_due_date:
                    HouseHoldDetailActivity.householdclient = (CommonPersonObjectClient)view.getTag();

                    showFragmentDialog(new EditDialogOptionModel(), view.getTag());
                    break;
            }
        }

        private void showProfileView(ECClient client) {
            navigationController.startEC(client.entityId());
        }
    }


    private String filterStringForOneOrMoreElco(){
        return "and details not LIKE '%\"ELCO\":\"0\"%'";
    }
    private String filterStringForNoElco(){
        return " and details LIKE '%\"ELCO\":\"0\"%'";
    }
    private String filterStringForAll(){
        return "";
    }
    private String householdSortByName() {
        return " FWHOHFNAME COLLATE NOCASE ASC";
    }
    private String householdSortByFWGOBHHID(){
        return " FWGOBHHID ASC";
    }
    private String householdSortByFWJIVHHID(){
      return " FWJIVHHID ASC";
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
        initializeQueries();
//        updateSearchView();
        checkforNidMissing(mView);
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

                (new AsyncTask() {
                    SmartRegisterClients filteredClients;

                    @Override
                    protected Object doInBackground(Object[] params) {
//                        currentSearchFilter =
//                        setCurrentSearchFilter(new HHSearchOption(cs.toString()));
//                        filteredClients = getClientsAdapter().getListItemProvider()
//                                .updateClients(getCurrentVillageFilter(), getCurrentServiceModeOption(),
//                                        getCurrentSearchFilter(), getCurrentSortOption());
//
                        filters = "and FWHOHFNAME Like '%" + cs.toString() + "%' or FWGOBHHID Like '%" + cs.toString() + "%'  or FWJIVHHID Like '%" + cs.toString() + "%' ";
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
//                        clientsAdapter
//                                .refreshList(currentVillageFilter, currentServiceModeOption,
//                                        currentSearchFilter, currentSortOption);
//                        getClientsAdapter().refreshClients(filteredClients);
//                        getClientsAdapter().notifyDataSetChanged();
                        getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                        CountExecute();
                        filterandSortExecute();
                        super.onPostExecute(o);
                    }
                }).execute();
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
                (new AsyncTask() {
                    SmartRegisterClients filteredClients;

                    @Override
                    protected Object doInBackground(Object[] params) {
//                        currentSearchFilter =
//                        setCurrentSearchFilter(new HHSearchOption(cs.toString()));
//                        filteredClients = getClientsAdapter().getListItemProvider()
//                                .updateClients(getCurrentVillageFilter(), getCurrentServiceModeOption(),
//                                        getCurrentSearchFilter(), getCurrentSortOption());
//
                        filters = "and FWHOHFNAME Like '%"+cs.toString()+"%' or FWGOBHHID Like '%"+cs.toString()+"%'  or FWJIVHHID Like '%"+cs.toString()+"%' or household.id in (Select elco.relationalid from elco where FWWOMFNAME Like '%"+cs.toString()+"%' )";
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
//                        clientsAdapter
//                                .refreshList(currentVillageFilter, currentServiceModeOption,
//                                        currentSearchFilter, currentSortOption);
//                        getClientsAdapter().refreshClients(filteredClients);
//                        getClientsAdapter().notifyDataSetChanged();
                        getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                        filterandSortExecute();
                        super.onPostExecute(o);
                    }
                }).execute();
//                currentSearchFilter = new HHSearchOption(cs.toString());
//                clientsAdapter
//                        .refreshList(currentVillageFilter, currentServiceModeOption,
//                                currentSearchFilter, currentSortOption);
//
//                searchCancelView.setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);


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
                dialogOptionslist.add(new HHMauzaCommonObjectFilterOption(name,"existing_Mauzapara", name));

            }
        }
    }

    private void checkforNidMissing(View view) {
        RelativeLayout titlelayout = (RelativeLayout)view.findViewById(org.ei.opensrp.R.id.register_nav_bar_container);
        if(anyNIdmissing(controller)) {
            try {
                titlelayout.removeView(getActivity().findViewById(R.id.warnid)) ;

            }catch(Exception e){

            }
            ImageView border = new ImageView(getActivity());
            border.setImageDrawable(getResources().getDrawable(R.drawable.separator));
            RelativeLayout.LayoutParams layoutParams_separator = new RelativeLayout.LayoutParams((int)getResources().getDimension(R.dimen.smart_register_nav_bar_separator), RelativeLayout.LayoutParams.FILL_PARENT);
            border.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageButton warn = new ImageButton(getActivity());
            warn.setImageDrawable(getResources().getDrawable(R.mipmap.warning));
            warn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            warn.setBackground(null);
            warn.setId(R.id.warnid);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.LEFT_OF,R.id.sort_selection);
            layoutParams_separator.addRule(RelativeLayout.RIGHT_OF,warn.getId());
            titlelayout.addView(warn, layoutParams);
            titlelayout.addView(border, layoutParams_separator);
            warn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getClientsAdapter()
//                            .refreshList(new noNIDFilter(), getCurrentServiceModeOption(),
//                                    getCurrentSearchFilter(), getCurrentSortOption());
                    filters = "and household.id in (Select elco.relationalid from elco where details not Like '%nidImage%' and details LIKE '%\"FWELIGIBLE\":\"1\"%' )";

                    CountExecute();
                    filterandSortExecute();

                }
            });
        }else{
            titlelayout.removeView(getActivity().findViewById(R.id.warnid));
        }
    }

    private boolean anyNIdmissing(CommonPersonObjectController controller) {
        boolean toreturn = false;
//        List<CommonPersonObject> allchildelco = null;
//        CommonPersonObjectClients clients = controller.getClients();
//        ArrayList<String> list = new ArrayList<String>();
//        AllCommonsRepository allElcoRepository = Context.getInstance().allCommonsRepositoryobjects("elco");
//
//        for(int i = 0;i <clients.size();i++) {
//
//            list.add((clients.get(i).entityId()));
//
//        }
//        allchildelco = allElcoRepository.findByRelationalIDs(list);
//
//        if(allchildelco != null) {
//            for (int i = 0; i < allchildelco.size(); i++) {
//                if (allchildelco.get(i).getDetails().get("FWELIGIBLE").equalsIgnoreCase("1")) {
//                    if (allchildelco.get(i).getDetails().get("nidImage") == null) {
//                        toreturn = true;
//                    }
//                }
//            }
//        }
        CommonRepository commonRepository = context.commonrepository("household");
        setTablename("household");
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts("household");
        countqueryBUilder.joinwithALerts("household", "FW CENSUS");
        countqueryBUilder.mainCondition(" FWHOHFNAME is not null ");
        String nidfilters = "and household.id in (Select elco.relationalid from elco where details not Like '%nidImage%' and details LIKE '%\"FWELIGIBLE\":\"1\"%' )";

        countqueryBUilder.addCondition(nidfilters);
        Cursor c = commonRepository.RawCustomQueryForAdapter(countqueryBUilder.Endquery(countqueryBUilder.toString()));
        c.moveToFirst();
        int missingnidCount = c.getInt(0);
        c.close();
        if(missingnidCount>0){
            toreturn = true;
        }
        return toreturn;
//        return false;
    }
}
