package org.ei.opensrp.path.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.cursoradapter.CursorCommonObjectFilterOption;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.cursoradapter.CursorSortOption;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.event.Listener;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildDetailActivity;
import org.ei.opensrp.path.activity.ChildImmunizationActivity;
import org.ei.opensrp.path.activity.ChildSmartRegisterActivity;
import org.ei.opensrp.path.activity.LoginActivity;
import org.ei.opensrp.path.db.Client;
import org.ei.opensrp.path.domain.RegisterClickables;
import org.ei.opensrp.path.domain.WeightWrapper;
import org.ei.opensrp.path.listener.WeightActionListener;
import org.ei.opensrp.path.option.BasicSearchOption;
import org.ei.opensrp.path.option.DateSort;
import org.ei.opensrp.path.option.StatusSort;
import org.ei.opensrp.path.provider.ChildSmartClientsProvider;
import org.ei.opensrp.path.servicemode.VaccinationServiceModeOption;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.customControls.CustomFontTextView;
import org.ei.opensrp.view.customControls.FontVariant;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.GlobalSearchUtils;
import util.JsonFormUtils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static util.Utils.getValue;
import static util.Utils.nonEmptyValue;
import static util.VaccinatorUtils.providerDetails;

public class ChildSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private LocationPickerDialogFragment locationPickerDialogFragment;


    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {
            // FIXME path_conflict
            //@Override
            public FilterOption searchFilterOption() {
                return new BasicSearchOption("", BasicSearchOption.Type.getByRegisterName(getDefaultOptionsProvider().nameInShortFormForTitle()));
            }

            @Override
            public ServiceModeOption serviceMode() {
                return new VaccinationServiceModeOption(null, "Linda Clinic", new int[]{
                        R.string.child_profile, R.string.birthdate_age, R.string.epi_number, R.string.child_contact_number,
                        R.string.child_next_vaccine
                }, new int[]{5, 2, 2, 3, 3});
            }

            @Override
            public FilterOption villageFilter() {
                return new CursorCommonObjectFilterOption("no village filter", "");
            }

            @Override
            public SortOption sortOption() {
                return new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "first_name");
            }

            @Override
            public String nameInShortFormForTitle() {
                return Context.getInstance().getStringResource(R.string.zeir);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                        new CursorCommonObjectSort(getResources().getString(R.string.woman_alphabetical_sort), "first_name"),
                        new DateSort("Age", "dob"),
                        new StatusSort("Due Status"),
                        new CursorCommonObjectSort(getResources().getString(R.string.id_sort), "program_client_id")
                };
            }

            @Override
            public String searchHint() {
                return Context.getInstance().getStringResource(R.string.str_search_hint);
            }
        };
    }


    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {
    }

    @Override
    protected void startRegistration() {
        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("child_enrollment", null, null);
    }

    @Override
    protected void onCreation() {
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        updateGlobalSearchView();
        try {
            LoginActivity.setLanguage();
        } catch (Exception e) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.smart_register_activity_customized, container, false);
        mView = view;
        onInitialization();
        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(INVISIBLE);

        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
        setServiceModeViewDrawableRight(null);
        initializeQueries();
        updateSearchView();
        populateClientListHeaderView(view);


        View viewParent = (View) appliedSortView.getParent();
        viewParent.setVisibility(View.GONE);

        final CustomFontTextView clinicSelection = (CustomFontTextView) view.findViewById(R.id.clinic_selection);

        locationPickerDialogFragment = new LocationPickerDialogFragment(getActivity(),
                context(),
                context().anmLocationController().get());
        locationPickerDialogFragment.setOnLocationChangeListener(new LocationSwitcherToolbar.OnLocationChangeListener() {
            @Override
            public void onLocationChanged(ArrayList<String> newLocation) {
               updateLocationText(clinicSelection, newLocation);
            }
        });
        updateLocationText(clinicSelection, locationPickerDialogFragment.getValue());

        clinicSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                Fragment prev = getActivity().getFragmentManager()
                        .findFragmentByTag(DIALOG_TAG);

                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                locationPickerDialogFragment.show(ft, DIALOG_TAG);
            }
        });


        View qrCode = view.findViewById(R.id.scan_qr_code);
        TextView nameInitials = (TextView) view.findViewById(R.id.name_inits);
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQrCodeScanner();
            }
        });
        AllSharedPreferences allSharedPreferences = Context.getInstance().allSharedPreferences();
        String preferredName = allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM());
        if (!preferredName.isEmpty()) {
            String[] preferredNameArray = preferredName.split(" ");
            String initials = "";
            if (preferredNameArray.length > 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0)) + String.valueOf(preferredNameArray[1].charAt(0));
            } else if (preferredNameArray.length == 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0));
            }
            nameInitials.setText(initials);
        }

    }

    private void updateLocationText(CustomFontTextView clinicSelection, ArrayList<String> newLocation) {
        String lastName = getActivity().getString(R.string.select_location);
        if(newLocation != null && newLocation.size() > 0) {
            lastName = newLocation.get(newLocation.size() - 1);
        }

        clinicSelection.setText(lastName);
    }

    public void initializeQueries() {
        String tableName = "ec_child";
        String parentTableName = "ec_mother";

        ChildSmartClientsProvider hhscp = new ChildSmartClientsProvider(getActivity(),
                clientActionHandler, context().alertService());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, Context.getInstance().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        setTablename(tableName);
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts(tableName);
        countSelect = countqueryBUilder.mainCondition("");
        mainCondition = "";
        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]{
                tableName + ".relationalid",
                tableName + ".details",
                tableName + ".program_client_id",
                tableName + ".relational_id",
                tableName + ".first_name",
                tableName + ".last_name",
                tableName + ".gender",
                parentTableName + ".first_name as mother_first_name",
                parentTableName + ".last_name as mother_last_name",
                tableName + ".father_name",
                tableName + ".dob",
                tableName + ".epi_card_number",
                tableName + ".contact_phone_number",
                tableName + ".pmtct_status",
                tableName + ".provider_uc",
                tableName + ".provider_town",
                tableName + ".provider_id",
                tableName + ".provider_location_id",
                tableName + ".client_reg_date"
        });
        queryBUilder.customJoin("LEFT JOIN " + parentTableName + " ON  " + tableName + ".relational_id =  " + parentTableName + ".id");
        mainSelect = queryBUilder.mainCondition("");
        Sortqueries = ((CursorSortOption) getDefaultOptionsProvider().sortOption()).sort();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

        updateSearchView();
        refresh();
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
            HashMap<String, String> map = new HashMap<>();
            map.putAll(followupOverrides(client));
            map.putAll(providerDetails());

            RegisterClickables registerClickables = new RegisterClickables();

            switch (view.getId()) {
                case R.id.child_profile_info_layout:

                    ChildImmunizationActivity.launchActivity(getActivity(), client, null);
                    break;
                case R.id.record_weight:
                    registerClickables.setRecordWeight(true);
                    ChildImmunizationActivity.launchActivity(getActivity(), client, registerClickables);
                    break;

                case R.id.record_vaccination:
                    registerClickables.setRecordAll(true);
                    ChildImmunizationActivity.launchActivity(getActivity(), client, registerClickables);
                    break;

            }
        }
    }

    public void updateSearchView() {
        getSearchView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(final CharSequence cs, int start, int before, int count) {

                if (cs.toString().equalsIgnoreCase("")) {
                    filters = "";
                } else {
                    filters = cs.toString();
                }
                joinTable = "";
                mainCondition = "";
                getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                CountExecute();
                filterandSortExecute();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void populateClientListHeaderView(View view) {
        LinearLayout clientsHeaderLayout = (LinearLayout) view.findViewById(org.ei.opensrp.R.id.clients_header_layout);
        clientsHeaderLayout.setVisibility(View.GONE);

        LinearLayout headerLayout = (LinearLayout) getLayoutInflater(null).inflate(R.layout.smart_register_child_header, null);
        clientsView.addHeaderView(headerLayout);
    }

    private class EditDialogOptionModel implements DialogOptionModel {
        HashMap<String, String> map;

        EditDialogOptionModel(HashMap<String, String> map) {
            this.map = map;
        }

        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptions(map);
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }

    }

    private DialogOption[] getEditOptions(HashMap<String, String> map) {
        return ((ChildSmartRegisterActivity) getActivity()).getEditOptions(map);
    }

    private HashMap<String, String> getPreloadVaccineData(CommonPersonObjectClient client) {
        HashMap<String, String> map = new HashMap<>();
        map.put("e_bcg", nonEmptyValue(client.getColumnmaps(), true, false, "bcg", "bcg_retro"));
        map.put("e_opv0", nonEmptyValue(client.getColumnmaps(), true, false, "opv0", "opv0_retro"));
        map.put("e_penta1", nonEmptyValue(client.getColumnmaps(), true, false, "penta1", "penta1_retro"));
        map.put("e_opv1", nonEmptyValue(client.getColumnmaps(), true, false, "opv1", "opv1_retro"));
        map.put("e_pcv1", nonEmptyValue(client.getColumnmaps(), true, false, "pcv1", "pcv1_retro"));
        map.put("e_penta2", nonEmptyValue(client.getColumnmaps(), true, false, "penta2", "penta2_retro"));
        map.put("e_opv2", nonEmptyValue(client.getColumnmaps(), true, false, "opv2", "opv2_retro"));
        map.put("e_pcv2", nonEmptyValue(client.getColumnmaps(), true, false, "pcv2", "pcv2_retro"));
        map.put("e_penta3", nonEmptyValue(client.getColumnmaps(), true, false, "penta3", "penta3_retro"));
        map.put("e_opv3", nonEmptyValue(client.getColumnmaps(), true, false, "opv3", "opv3_retro"));
        map.put("e_pcv3", nonEmptyValue(client.getColumnmaps(), true, false, "pcv3", "pcv3_retro"));
        map.put("e_ipv", nonEmptyValue(client.getColumnmaps(), true, false, "ipv", "ipv_retro"));
        map.put("e_measles1", nonEmptyValue(client.getColumnmaps(), true, false, "measles1", "measles1_retro"));
        map.put("e_measles2", nonEmptyValue(client.getColumnmaps(), true, false, "measles2", "measles2_retro"));
        return map;
    }

    //TODO EC model
    /*
    private HashMap<String, String> getPreloadVaccineData(Client client) throws JSONException, ParseException {
        HashMap<String, String> map = new HashMap<>();
        map.put("e_bcg", getObsValue(getClientEventDb(), client, true, "bcg", "bcg_retro"));
        map.put("e_opv0", getObsValue(getClientEventDb(), client, true, "opv0", "opv0_retro"));
        map.put("e_penta1", getObsValue(getClientEventDb(), client, true, "penta1", "penta1_retro"));
        map.put("e_opv1", getObsValue(getClientEventDb(), client, true, "opv1", "opv1_retro"));
        map.put("e_pcv1", getObsValue(getClientEventDb(), client, true, "pcv1", "pcv1_retro"));
        map.put("e_penta2", getObsValue(getClientEventDb(), client, true, "penta2", "penta2_retro"));
        map.put("e_opv2", getObsValue(getClientEventDb(), client, true, "opv2", "opv2_retro"));
        map.put("e_pcv2", getObsValue(getClientEventDb(), client, true, "pcv2", "pcv2_retro"));
        map.put("e_penta3", getObsValue(getClientEventDb(), client, true, "penta3", "penta3_retro"));
        map.put("e_opv3", getObsValue(getClientEventDb(), client, true, "opv3", "opv3_retro"));
        map.put("e_pcv3", getObsValue(getClientEventDb(), client, true, "pcv3", "pcv3_retro"));
        map.put("e_ipv", getObsValue(getClientEventDb(), client, true, "ipv", "ipv_retro"));
        map.put("e_measles1", getObsValue(getClientEventDb(), client, true, "measles1", "measles1_retro"));
        map.put("e_measles2", getObsValue(getClientEventDb(), client, true, "measles2", "measles2_retro"));

        return map;
    }*/

    //TODO path_conflict
    //@Override
    protected String getRegistrationForm(HashMap<String, String> overrides) {
        return "child_enrollment";
    }

    //@Override
    protected String getOAFollowupForm(Client client, HashMap<String, String> overridemap) {
        overridemap.putAll(followupOverrides(client));
        return "offsite_child_followup";
    }

    //@Override
    protected Map<String, String> customFieldOverrides() {
        Map<String, String> m = new HashMap<>();
        return m;
    }

    private Map<String, String> followupOverrides(CommonPersonObjectClient client) {
        Map<String, String> map = new HashMap<>();
        map.put("existing_full_address", getValue(client.getColumnmaps(), "address1", true)
                + ", UC: " + getValue(client.getColumnmaps(), "union_council", true)
                + ", Town: " + getValue(client.getColumnmaps(), "town", true)
                + ", City: " + getValue(client, "city_village", true)
                + ", Province: " + getValue(client, "province", true));
        map.put("existing_program_client_id", getValue(client.getColumnmaps(), "program_client_id", false));
        map.put("program_client_id", getValue(client.getColumnmaps(), "program_client_id", false));

        int days = 0;
        try {
            days = Days.daysBetween(new DateTime(getValue(client.getColumnmaps(), "dob", false)), DateTime.now()).getDays();
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.put("existing_first_name", getValue(client.getColumnmaps(), "first_name", true));
        map.put("existing_last_name", getValue(client.getColumnmaps(), "last_name", true));
        map.put("existing_gender", getValue(client.getColumnmaps(), "gender", true));
        map.put("existing_mother_first_name", getValue(client.getColumnmaps(), "mother_first_name", true));
        map.put("existing_mother_last_name", getValue(client.getColumnmaps(), "mother_last_name", true));
        map.put("existing_father_name", getValue(client.getColumnmaps(), "father_name", true));
        map.put("existing_birth_date", getValue(client.getColumnmaps(), "dob", false));
        map.put("existing_age", days + "");
        map.put("existing_epi_card_number", getValue(client.getColumnmaps(), "epi_card_number", false));
        map.put("reminders_approval", getValue(client.getColumnmaps(), "reminders_approval", false));
        map.put("contact_phone_number", getValue(client.getColumnmaps(), "contact_phone_number", false));

        map.putAll(getPreloadVaccineData(client));

        return map;
    }

    private Map<String, String> followupOverrides(Client client) {
        Map<String, String> map = new HashMap<>();
        map.put("existing_full_address", client.getAddress("usual_residence").getAddressField("address1") +
                ", UC: " + client.getAddress("usual_residence").getSubTown() +
                ", Town: " + client.getAddress("usual_residence").getTown() +
                ", City: " + client.getAddress("usual_residence").getCityVillage() +
                " - " + client.getAddress("usual_residence").getAddressField("landmark"));

        map.put("existing_program_client_id", client.getIdentifier("Program Client ID"));
        map.put("program_client_id", client.getIdentifier("Program Client ID"));

        map.put("existing_first_name", client.getFirstName());
        map.put("existing_last_name", client.getLastName());
        map.put("existing_gender", client.getGender());
        map.put("existing_birth_date", client.getBirthdate().toString("yyyy-MM-dd"));
        int days = 0;
        try {
            days = Days.daysBetween(client.getBirthdate(), DateTime.now()).getDays();
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("existing_age", days + "");
        Object epi = client.getAttribute("EPI Card Number");
        map.put("existing_epi_card_number", epi == null ? "" : epi.toString());

        //TODO EC model
        /* try {
            map.put("existing_father_name", getObsValue(getClientEventDb(), client, true, "father_name", "1594AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
            map.put("existing_mother_name", getObsValue(getClientEventDb(), client, true, "mother_name", "1593AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));

            map.put("reminders_approval", getObsValue(getClientEventDb(), client, true, "reminders_approval", "163089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
            map.put("contact_phone_number", getObsValue(getClientEventDb(), client, true, "contact_phone_number", "159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));

            map.putAll(getPreloadVaccineData(client));
        } catch (Exception e) {
            e.printStackTrace();
        } */
        return map;
    }

    private void updateGlobalSearchView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.global_search, null);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        final EditText txtSearch = (EditText) view.findViewById(R.id.text_search);
        final ListView listView = (ListView) view.findViewById(R.id.list_view);
        final TextView emptyView = (TextView) view.findViewById(R.id.empty);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setPositiveButton(R.string.search, null).setNegativeButton(R.string.cancel, null);
        final AlertDialog alertDialog = builder.create();

        final Listener<JSONArray> listener = new Listener<JSONArray>() {
            public void onEvent(final JSONArray jsonArray) {

                if (jsonArray != null) {
                    List<JSONObject> list = new ArrayList<JSONObject>();
                    int len = jsonArray.length();
                    for (int i = 0; i < len; i++) {
                        list.add(getJsonObject(jsonArray, i));
                    }

                    ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, list) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                            JSONObject jsonObject = getJsonObject(jsonArray, position);

                            String name = getJsonString(jsonObject, "firstName") + " " +
                                    getJsonString(jsonObject, "middleName") + " " +
                                    getJsonString(jsonObject, "lastName");

                            String other = "Gender: " + getJsonString(jsonObject, "gender") + " Birthday: " +
                                    getJsonString(jsonObject, "birthdate") + " Program Id: " +
                                    getJsonString(getJsonObject(jsonObject, "identifiers"), "Program Client ID");


                            text1.setText(name);
                            text2.setText(other);
                            return view;
                        }
                    };
                    listView.setAdapter(adapter);
                } else {
                    listView.setAdapter(null);
                }
                listView.setEmptyView(emptyView);
                listView.setVisibility(View.VISIBLE);
            }
        };

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View searchButton) {
                        if (StringUtils.isNotBlank(txtSearch.getText().toString())) {
                            search(txtSearch, listener, progressBar, button);

                        }
                    }
                });

                txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            search(txtSearch, listener, progressBar, button);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });


        ImageButton globalSearchButton = ((ImageButton) mView.findViewById(R.id.global_search));
        globalSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });
    }

    private void search(EditText txtSearch, Listener<JSONArray> listener, ProgressBar progressBar, Button button) {
        button.setEnabled(false);
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);

        GlobalSearchUtils.backgroundSearch(txtSearch.getText().toString(), listener, progressBar, button);
    }

    private String getJsonString(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                String string = jsonObject.getString(field);
                if (string.equals("null")) {
                    return "";
                } else {
                    return string;
                }
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), "", e);
        }
        return "";

    }

    private JSONObject getJsonObject(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                return jsonObject.getJSONObject(field);
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), "", e);
        }
        return null;

    }

    private JSONObject getJsonObject(JSONArray jsonArray, int position) {
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                return jsonArray.getJSONObject(position);
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), "", e);
        }
        return null;

    }

    private void startQrCodeScanner() {
        ((ChildSmartRegisterActivity) getActivity()).startQrCodeScanner();
    }

}
