package org.ei.opensrp.indonesia.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import org.ei.opensrp.indonesia.LoginActivity;
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.anc.ANCDetailActivity;
import org.ei.opensrp.indonesia.anc.KIANCClientsProvider;
import org.ei.opensrp.indonesia.anc.KIANCOverviewServiceMode;
import org.ei.opensrp.indonesia.anc.NativeKIANCSmartRegisterActivity;
import org.ei.opensrp.indonesia.face.camera.SmartShutterActivity;
import org.ei.opensrp.indonesia.kartu_ibu.KICommonObjectFilterOption;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
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
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.NameSort;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import util.AsyncTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by koros on 10/29/15.
 */
public class NativeKIANCSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    private static final String TAG = NativeKIANCSmartRegisterFragment.class.getSimpleName();
    private SmartRegisterClientsProvider clientProvider = null;
    private CommonPersonObjectController controller;
    private VillageController villageController;
    private DialogOptionMapper dialogOptionMapper;

    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private String locationDialogTAG = "locationDialogTAG";

    public static String criteria;

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
                return new KIANCOverviewServiceMode(clientsProvider());
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
                return Context.getInstance().getStringResource(R.string.anc_register_title_in_short);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                FlurryFacade.logEvent("click_filter_option_on_kohort_anc_dashboard");
                ArrayList<DialogOption> dialogOptionslist = new ArrayList<DialogOption>();

                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_all_label),filterStringForAll()));
                //     dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_no_mwra),filterStringForNoElco()));
                //      dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.hh_has_mwra),filterStringForOneOrMoreElco()));

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
                FlurryFacade.logEvent("click_sorting_option_on_kohort_anc_dashboard");
                return new DialogOption[]{
//                        new HouseholdCensusDueDateSort(),

                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_name_label),KiSortByNameAZ()),
                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_name_label_reverse),KiSortByNameZA()),
                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_wife_age_label),KiSortByAge()),
                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_edd_label),KiSortByEdd()),
                        new CursorCommonObjectSort(getResources().getString(R.string.sort_by_no_ibu_label),KiSortByNoIbu()),
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
        return ((NativeKIANCSmartRegisterActivity)getActivity()).getEditOptions();
    }

    @Override
    protected void onInitialization() {
        //  context.formSubmissionRouter().getHandlerMap().put("census_enrollment_form", new CensusEnrollmentHandler());
    }

    @Override
    public void setupViews(View view) {
        getDefaultOptionsProvider();

        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.register_client).setVisibility(View.GONE);
        view.findViewById(R.id.service_mode_selection).setVisibility(View.GONE);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
//        list.setBackgroundColor(Color.RED);
        initializeQueries(getCriteria());
    }
    private String filterStringForAll(){
        return "";
    }
    private String sortByAlertmethod() {
        return "CASE WHEN alerts.status = 'urgent' THEN '1'" +
                "WHEN alerts.status = 'upcoming' THEN '2'\n" +
                "WHEN alerts.status = 'normal' THEN '3'\n" +
                "WHEN alerts.status = 'expired' THEN '4'\n" +
                "WHEN alerts.status is Null THEN '5'\n" +
                "Else alerts.status END ASC";
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void initializeQueries(String s){
        KIANCClientsProvider kiscp = new KIANCClientsProvider(getActivity(),clientActionHandler,context.alertService());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, kiscp, new CommonRepository("ibu",new String []{"ibu.isClosed", "ibu.ancDate", "ibu.ancKe","kartu_ibu.namalengkap","kartu_ibu.umur","kartu_ibu.namaSuami"}));
        clientsView.setAdapter(clientAdapter);

        setTablename("ibu");
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts("ibu");
        countqueryBUilder.customJoin("LEFT JOIN kartu_ibu ON ibu.kartuIbuId = kartu_ibu.id");
        countSelect = countqueryBUilder.mainCondition(" ibu.isClosed != 'true' AND ibu.type = 'anc'");
//        mainCondition = " isClosed !='true' and type = 'anc'";

        if (s == null || Objects.equals(s, "!")) {
            Log.e(TAG, "initializeQueries: "+"Not Initialized" );

            mainCondition = "isClosed !='true' AND type = 'anc'";
//        mainCondition = " isClosed !='true' and type = 'anc'";
        } else {
            Log.e(TAG, "initializeQueries: init " + s);
            mainCondition = "isClosed !='true' and type = 'anc' AND kartuIbuId LIKE '%" + s + "%'";
        }

        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable("ibu", new String[]{"ibu.isClosed", "ibu.details", "ibu.ancDate", "ibu.ancKe","kartu_ibu.namalengkap","kartu_ibu.umur","kartu_ibu.namaSuami"});
        queryBUilder.customJoin("LEFT JOIN kartu_ibu ON ibu.kartuIbuId = kartu_ibu.id");
        mainSelect = queryBUilder.mainCondition("ibu.isClosed !='true' AND ibu.type = 'anc'");
     //   Sortqueries = KiSortByNameAZ();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

//        setServiceModeViewDrawableRight(null);
        updateSearchView();
        refresh();
    }


    @Override
    public void startRegistration() {
     //   FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
     //   Fragment prev = getActivity().getFragmentManager().findFragmentByTag(locationDialogTAG);
     //   if (prev != null) {
    //        ft.remove(prev);
    //    }
    //    ft.addToBackStack(null);
    //    BidanLocationSelectorDialogFragment
    //            .newInstance((NativeKIAnakSmartRegisterActivity) getActivity(), new EditDialogOptionModel(), context.anmLocationController().get(), KOHORT_KB_REGISTER)
   //             .show(ft, locationDialogTAG);
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout:
                    FlurryFacade.logEvent("click_detail_view_on_kohort_anc_dashboard");
                    ANCDetailActivity.ancclient = (CommonPersonObjectClient)view.getTag();
                    Intent intent = new Intent(getActivity(),ANCDetailActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                 case R.id.btn_edit:
                    FlurryFacade.logEvent("click_visit_button_on_kohort_anc_dashboard");
                    showFragmentDialog(new EditDialogOptionModel(), view.getTag());
                    break;
            }
        }

        private void showProfileView(ECClient client) {
            navigationController.startEC(client.entityId());
        }
    }



    private String KiSortByName() {
        return " namalengkap ASC";
    }
    private String KiSortByNameAZ() {
        return " namalengkap ASC";
    }
    private String KiSortByNameZA() {
        return " namalengkap DESC";
    }

    private String KiSortByAge() {
        return " umur DESC";
    }
    private String KiSortByNoIbu() {
        return " noIbu ASC";
    }

    private String KiSortByEdd() {
        return " htp IS NULL, htp";
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
            initializeQueries("!");
        }
        //     updateSearchView();
        //   checkforNidMissing(mView);
//
        try{
            LoginActivity.setLanguage();
        }catch (Exception e){

        }

    }

//    @Override
//    public void setupSearchView(View view) {
//        searchView = (EditText) view.findViewById(org.ei.opensrp.R.id.edt_search);
//        searchView.setHint(getNavBarOptionsProvider().searchHint());
//        searchView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            }
//
//            @Override
//            public void onTextChanged(final CharSequence cs, int start, int before, int count) {
//
//                (new AsyncTask() {
//                    SmartRegisterClients filteredClients;
//
//                    @Override
//                    protected Object doInBackground(Object[] params) {
////                        currentSearchFilter =
////                        setCurrentSearchFilter(new HHSearchOption(cs.toString()));
////                        filteredClients = getClientsAdapter().getListItemProvider()
////                                .updateClients(getCurrentVillageFilter(), getCurrentServiceModeOption(),
////                                        getCurrentSearchFilter(), getCurrentSortOption());
////
//                        filters = cs.toString();
//                        joinTable = "";
//                        mainCondition = " isClosed !='true' and type = 'anc' ";
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Object o) {
////                        clientsAdapter
////                                .refreshList(currentVillageFilter, currentServiceModeOption,
////                                        currentSearchFilter, currentSortOption);
////                        getClientsAdapter().refreshClients(filteredClients);
////                        getClientsAdapter().notifyDataSetChanged();
//                        getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
//                        CountExecute();
//                        filterandSortExecute();
//                        super.onPostExecute(o);
//                    }
//                }).execute();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//            }
//        });
//        searchCancelView = view.findViewById(org.ei.opensrp.R.id.btn_search_cancel);
//        searchCancelView.setOnClickListener(searchCancelHandler);
//    }

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
                        filters = cs.toString();
                        joinTable = "";
                        mainCondition = " isClosed !='true' and type = 'anc' ";
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
                dialogOptionslist.add(new KICommonObjectFilterOption(name,"dusun", name));

            }
        }
    }

    //    WD
    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public static String getCriteria(){
        return criteria;
    }

    //    WD
    @Override
    public void setupSearchView(final View view) {
        searchView = (EditText) view.findViewById(org.ei.opensrp.R.id.edt_search);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence selections[] = new CharSequence[]{"Name", "Photo"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Please Choose one, Search by");
                builder.setItems(selections, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int opt) {
                        if (opt == 0) searchTextChangeListener("");
                        else getFacialRecord(view);
                    }
                });
                builder.show();
            }
        });

        searchCancelView = view.findViewById(org.ei.opensrp.R.id.btn_search_cancel);
        searchCancelView.setOnClickListener(searchCancelHandler);
    }

    public void getFacialRecord(View view) {
        Log.e(TAG, "getFacialRecord: ");
        SmartShutterActivity.kidetail = (CommonPersonObjectClient) view.getTag();

        Intent intent = new Intent(getActivity(), SmartShutterActivity.class);
        intent.putExtra("org.sid.sidface.ImageConfirmation.origin", NativeKIANCSmartRegisterFragment.class.getSimpleName());
        intent.putExtra("org.sid.sidface.ImageConfirmation.identify", true);
        intent.putExtra("org.sid.sidface.ImageConfirmation.kidetail", (Parcelable) SmartShutterActivity.kidetail);
        startActivity(intent);
    }

    public void searchTextChangeListener(String s) {
        Log.e(TAG, "searchTextChangeListener: " + s);
        if (s != null) {
            filters = s;
        } else {
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(final CharSequence cs, int start, int before, int count) {

                    Log.e(TAG, "onTextChanged: " + searchView.getText());
                    (new AsyncTask() {
//                    SmartRegisterClients filteredClients;

                        @Override
                        protected Object doInBackground(Object[] params) {
//                        currentSearchFilter =
//                        setCurrentSearchFilter(new HHSearchOption(cs.toString()));
//                        filteredClients = getClientsAdapter().getListItemProvider()
//                                .updateClients(getCurrentVillageFilter(), getCurrentServiceModeOption(),
//                                        getCurrentSearchFilter(), getCurrentSortOption());
//
                            filters = cs.toString();
                            joinTable = "";
                            mainCondition = " isClosed !='true' and type = 'anc' ";
                            return null;
                        }
//
//                    @Override
//                    protected void onPostExecute(Object o) {
////                        clientsAdapter
////                                .refreshList(currentVillageFilter, currentServiceModeOption,
////                                        currentSearchFilter, currentSortOption);
////                        getClientsAdapter().refreshClients(filteredClients);
////                        getClientsAdapter().notifyDataSetChanged();
//                        getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
//                        CountExecute();
//                        filterandSortExecute();
//                        super.onPostExecute(o);
//                    }
                    }).execute();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }



}