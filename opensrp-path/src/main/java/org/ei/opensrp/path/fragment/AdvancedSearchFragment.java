package org.ei.opensrp.path.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.clientandeventmodel.DateUtil;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.event.Listener;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildSmartRegisterActivity;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.provider.ChildSmartClientsProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.GlobalSearchUtils;
import util.JsonFormUtils;
import util.Utils;

public class AdvancedSearchFragment extends BaseSmartRegisterFragment {
    private View mView;
    private final ClientActionHandler clientActionHandler = new ClientActionHandler();
    private Button search;
    private RadioGroup searchLimits;
    private CheckBox active;
    private CheckBox inactive;
    private CheckBox lostToFollowUp;
    private EditText zeirId;
    private EditText firstName;
    private EditText lastName;
    private EditText motherGuardianName;
    private EditText motherGuardianNrc;
    private EditText motherGuardianPhoneNumber;
    private EditText startDate;
    private EditText endDate;

    private TextView searchCriteria;
    private View listViewLayout;
    private View advancedSearchForm;

    private TextView filterCount;

    private List<Integer> editedList = new ArrayList<>();
    private Map<String, String> editMap = new HashMap<>();
    private boolean listMode = false;
    private int overdueCount = 0;
    private boolean outOfArea = false;
    private AdvancedMatrixCursor matrixCursor;

    private static final String INACTIVE = "inactive";
    private static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";

    private static final String ZEIR_ID = "zeir_id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String NRC_NUMBER = "nrc_number";

    private static final String CONTACT_PHONE_NUMBER = "contact_phone_number";
    private static final String BIRTH_DATE = "birth_date";

    private static final String MOTHER_GUARDIAN_FIRST_NAME = "mother_first_name";
    private static final String MOTHER_GUARDIAN_LAST_NAME = "mother_last_name";
    private static final String MOTHER_GUARDIAN_NRC_NUMBER = "mother_nrc_number";
    private static final String MOTHER_GUARDIAN_PHONE_NUMBER = "mother_contact_phone_number";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.smart_register_activity_advanced_search, container, false);
        mView = view;
        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    protected void onCreation() {
    }

    @Override
    protected void onResumption() {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            switchViews(false);
            updateLocationText();
            updateSeachLimits();
            resetForm();
        }
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        listViewLayout = view.findViewById(R.id.list_view_layout);
        advancedSearchForm = view.findViewById(R.id.advanced_search_form);

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.global_search);
        imageButton.setBackgroundColor(getResources().getColor(R.color.transparent_dark_blue));
        imageButton.setOnClickListener(clientActionHandler);


        final View filterSection = view.findViewById(R.id.filter_selection);
        filterSection.setOnClickListener(clientActionHandler);

        filterCount = (TextView) view.findViewById(R.id.filter_count);
        if (overdueCount > 0) {
            filterCount.setText(String.valueOf(overdueCount));
        }
        filterCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterSection.performClick();
            }
        });

        if (titleLabelView != null) {
            titleLabelView.setText(getString(R.string.advanced_search));
        }

        View nameInitials = view.findViewById(R.id.name_inits);
        nameInitials.setVisibility(View.GONE);

        ImageView backButton = (ImageView) view.findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE);

        populateFormViews(view);
    }

    @Override
    public void setupSearchView(View view) {
    }

    @Override
    protected void startRegistration() {
        ((ChildSmartRegisterActivity) getActivity()).startFormActivity("child_enrollment", null, null);
    }

    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.global_search:
                    goBack();
                    break;
                case R.id.filter_selection:
                    ((ChildSmartRegisterActivity) getActivity()).filterSelection();
                    break;
                case R.id.search:
                    search();
                    break;
            }
        }
    }

    private void populateFormViews(View view) {
        searchCriteria = (TextView) view.findViewById(R.id.search_criteria);
        search = (Button) view.findViewById(R.id.search);
        searchLimits = (RadioGroup) view.findViewById(R.id.search_limits);

        active = (CheckBox) view.findViewById(R.id.active);
        inactive = (CheckBox) view.findViewById(R.id.inactive);
        lostToFollowUp = (CheckBox) view.findViewById(R.id.lost_to_follow_up);

        zeirId = (EditText) view.findViewById(R.id.zeir_id);
        firstName = (EditText) view.findViewById(R.id.first_name);
        lastName = (EditText) view.findViewById(R.id.last_name);
        motherGuardianName = (EditText) view.findViewById(R.id.mother_guardian_name);
        motherGuardianNrc = (EditText) view.findViewById(R.id.mother_guardian_nrc);
        motherGuardianPhoneNumber = (EditText) view.findViewById(R.id.mother_guardian_phone_number);

        startDate = (EditText) view.findViewById(R.id.start_date);
        endDate = (EditText) view.findViewById(R.id.end_date);

        resetForm();
    }

    private void resetForm() {
        clearSearchCriteria();

        search.setOnClickListener(clientActionHandler);
        search.setClickable(false);
        search.setEnabled(false);

        active.setChecked(true);
        inactive.setChecked(false);
        lostToFollowUp.setChecked(false);

        zeirId.setText("");
        zeirId.addTextChangedListener(advancedSearchWatcher);

        firstName.setText("");
        firstName.addTextChangedListener(advancedSearchWatcher);

        lastName.setText("");
        lastName.addTextChangedListener(advancedSearchWatcher);

        motherGuardianName.setText("");
        motherGuardianName.addTextChangedListener(advancedSearchWatcher);

        motherGuardianNrc.setText("");
        motherGuardianNrc.addTextChangedListener(advancedSearchWatcher);

        motherGuardianPhoneNumber.setText("");
        motherGuardianPhoneNumber.addTextChangedListener(advancedSearchWatcher);

        startDate.setText("");
        startDate.addTextChangedListener(advancedSearchWatcher);
        setDatePicker(startDate);

        endDate.setText("");
        endDate.addTextChangedListener(advancedSearchWatcher);
        setDatePicker(endDate);
    }

    private void clearSearchCriteria() {
        searchCriteria.setVisibility(View.GONE);
        searchCriteria.setText("");
    }

    private void updateSeachLimits() {
        if (searchLimits != null) {
            if (Utils.isConnectedToNetwork(getActivity())) {
                searchLimits.check(R.id.out_and_inside);
            } else {
                searchLimits.check(R.id.my_catchment);
            }
        }
    }

    private void search() {

        if (!search.isEnabled()) {
            return;
        }

        String tableName = "ec_child";
        String parentTableName = "ec_mother";

        editMap.clear();

        String searchCriteriaString = "Search criteria: Include: ";

        if (searchLimits.getCheckedRadioButtonId() == R.id.out_and_inside) {
            outOfArea = true;
            searchCriteriaString += " \"Outside and Inside My Catchment Area\", ";
        } else {
            outOfArea = false;
            searchCriteriaString += " \"My Catchment Area\", ";
        }

        boolean isActive = active.isChecked();
        boolean isInactive = inactive.isChecked();

        if (!(isActive && isInactive)) {
            boolean inActiveStatus = false;
            if (isActive) {
                inActiveStatus = false;
            } else if (isInactive) {
                inActiveStatus = true;
            }

            String inActiveKey = INACTIVE;
            if (!outOfArea) {
                inActiveKey = tableName + "." + INACTIVE;
            }
            editMap.put(inActiveKey, Boolean.toString(inActiveStatus));
        }

        boolean isLostToFollowUp = lostToFollowUp.isChecked();
        if (isLostToFollowUp) {
            String lostToFollowUpKey = LOST_TO_FOLLOW_UP;
            if (!outOfArea) {
                lostToFollowUpKey = tableName + "." + LOST_TO_FOLLOW_UP;
            }
            editMap.put(lostToFollowUpKey, Boolean.toString(isLostToFollowUp));
        }

        if (isActive || isInactive || isLostToFollowUp) {
            String statusString = " \" ";
            if (isActive) {
                statusString += "Active";
            }
            if (isInactive) {
                if (statusString.contains("ctive")) {
                    statusString += ", Inactive";
                } else {
                    statusString += "Inactive";
                }
            }
            if (isLostToFollowUp) {
                if (statusString.contains("ctive")) {
                    statusString += ", Lost to Follow-up";
                } else {
                    statusString += "Lost to Follow-up";
                }
            }
            statusString += "\"; ";

            searchCriteriaString += statusString;
        }


        String zeirIdString = zeirId.getText().toString();
        if (StringUtils.isNotBlank(zeirIdString)) {
            searchCriteriaString += " ZEIR ID: \"" + zeirIdString + "\",";
            String key = ZEIR_ID;
            if (!outOfArea) {
                key = tableName + "." + ZEIR_ID;
            }
            editMap.put(key, zeirIdString.trim());
        }

        String firstNameString = firstName.getText().toString();
        if (StringUtils.isNotBlank(firstNameString)) {
            searchCriteriaString += " First name: \"" + firstNameString + "\",";
            String key = FIRST_NAME;
            if (!outOfArea) {
                key = tableName + "." + FIRST_NAME;
            }
            editMap.put(key, firstNameString.trim());
        }

        String lastNameString = lastName.getText().toString();
        if (StringUtils.isNotBlank(lastNameString)) {
            searchCriteriaString += " Last name: \"" + lastNameString + "\",";
            String key = LAST_NAME;
            if (!outOfArea) {
                key = tableName + "." + LAST_NAME;
            }
            editMap.put(key, lastNameString.trim());
        }

        String motherGuardianNameString = motherGuardianName.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianNameString)) {
            searchCriteriaString += " Mother/Guardian name: \"" + motherGuardianNameString + "\",";
            String key = MOTHER_GUARDIAN_FIRST_NAME;
            if (!outOfArea) {
                key = parentTableName + "." + FIRST_NAME;
            }
            editMap.put(key, motherGuardianNameString.trim());

            key = MOTHER_GUARDIAN_LAST_NAME;
            if (!outOfArea) {
                key = parentTableName + "." + LAST_NAME;
            }
            editMap.put(key, motherGuardianNameString.trim());
        }

        String motherGuardianNrcString = motherGuardianNrc.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianNrcString)) {
            searchCriteriaString += " Mother/Guardian nrc: \"" + motherGuardianNrcString + "\",";
            String key = MOTHER_GUARDIAN_NRC_NUMBER;
            if (!outOfArea) {
                key = parentTableName + "." + NRC_NUMBER;
            }
            editMap.put(key, motherGuardianNrcString.trim());
        }

        String motherGuardianPhoneNumberString = motherGuardianPhoneNumber.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianPhoneNumberString)) {
            searchCriteriaString += " Mother/Guardian phone number: \"" + motherGuardianPhoneNumberString + "\",";
            String key = MOTHER_GUARDIAN_PHONE_NUMBER;
            if (!outOfArea) {
                key = parentTableName + "." + CONTACT_PHONE_NUMBER;
            }
            editMap.put(key, motherGuardianPhoneNumberString.trim());
        }

        String startDateString = startDate.getText().toString();
        if (StringUtils.isNotBlank(startDateString)) {
            searchCriteriaString += " Start date: \"" + startDateString + "\",";
            editMap.put(START_DATE, startDateString.trim());
        }

        String endDateString = endDate.getText().toString();
        if (StringUtils.isNotBlank(endDateString)) {
            searchCriteriaString += " End date: \"" + endDateString + "\",";
            editMap.put(END_DATE, endDateString.trim());
        }

        if (searchCriteria != null) {
            searchCriteria.setText(removeLastComma(searchCriteriaString));
            searchCriteria.setVisibility(View.VISIBLE);
        }

        if (outOfArea) {
            globalSearch();
        } else {
            localSearch();
        }

    }

    private void initListMode() {
        switchViews(true);

        String tableName = "ec_child";
        setTablename(tableName);
        ChildSmartClientsProvider hhscp = new ChildSmartClientsProvider(getActivity(),
                clientActionHandler, context().alertService(), VaccinatorApplication.getInstance().vaccineRepository(), VaccinatorApplication.getInstance().weightRepository());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, Context.getInstance().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

    }

    private void localSearch() {

        initListMode();

        filterandSortInInitializeQueries();
    }

    private void globalSearch() {

        initListMode();

        final AdvancedSearchFragment advancedSearchFragment = this;


        final Listener<JSONArray> listener = new Listener<JSONArray>() {
            public void onEvent(final JSONArray jsonArray) {


                String[] columns = new String[]{"_id", "relationalid", FIRST_NAME, "middle_name", LAST_NAME, "gender", "dob", ZEIR_ID, "epi_card_number", MOTHER_GUARDIAN_FIRST_NAME, MOTHER_GUARDIAN_LAST_NAME};
                matrixCursor = new AdvancedMatrixCursor(columns);

                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject client = getJsonObject(jsonArray, i);

                        String entityId = getJsonString(client, "baseEntityId");
                        String firstName = getJsonString(client, "firstName");
                        String middleName = getJsonString(client, "middleName");
                        String lastName = getJsonString(client, "lastName");

                        String gender = getJsonString(client, "gender");
                        String dob = getJsonString(client, "birthdate");
                        if (StringUtils.isNotBlank(dob) && StringUtils.isNumeric(dob)) {
                            try {
                                Long dobLong = Long.valueOf(dob);
                                Date date = new Date(dobLong);
                                dob = DateUtil.yyyyMMddTHHmmssSSSZ.format(date);
                            } catch (Exception e) {
                                Log.e(getClass().getName(), e.toString(), e);
                            }
                        }
                        String zeirId = getJsonString(getJsonObject(client, "identifiers"), JsonFormUtils.ZEIR_ID);
                        String epiCardNumber = getJsonString(getJsonObject(client, "attributes"), "Child_Register_Card_Number");

                        String motherFirstName = "";
                        String motherLastName = "";

                        if (client.has("mother")) {
                            JSONObject mother = getJsonObject(client, "mother");
                            motherFirstName = getJsonString(mother, "firstName");
                            motherLastName = getJsonString(mother, "lastName");
                        }

                        matrixCursor.addRow(new Object[]{entityId, null, firstName, middleName, lastName, gender, dob, zeirId, epiCardNumber, motherFirstName, motherLastName});
                    }

                    advancedSearchFragment.filterandSortInInitializeQueries();
                }
            }
        };

        globalSearch(listener, clientsProgressView, null);
    }

    private void globalSearch(Listener<JSONArray> listener, ProgressBar progressBar, Button button) {
        Date date0 = new Date(0);
        String startDate = DateUtil.yyyyMMdd.format(date0);

        Date now = new Date();
        String endDate = DateUtil.yyyyMMdd.format(now);

        if (editMap.containsKey(START_DATE)) {
            startDate = editMap.remove(START_DATE);
        }
        if (editMap.containsKey(END_DATE)) {
            endDate = editMap.remove(END_DATE);
        }

        String bDate = startDate + ":" + endDate;
        editMap.put(BIRTH_DATE, bDate);

        GlobalSearchUtils.backgroundSearch(editMap, listener, progressBar, button);
    }

    @Override
    public void CountExecute() {
        Cursor c = null;

        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);
            String query = "";
            sqb.addCondition("");
            query = sqb.orderbyCondition("");
            query = sqb.Endquery(query);

            Log.i(getClass().getName(), query);
            c = commonRepository().RawCustomQueryForAdapter(query);
            c.moveToFirst();
            totalcount = c.getInt(0);
            Log.v("total count here", "" + totalcount);
            currentlimit = 20;
            currentoffset = 0;

        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String filterandSortQuery() {
        String tableName = "ec_child";
        String parentTableName = "ec_mother";

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, new String[]

                        {
                                tableName + ".relationalid",
                                tableName + ".details",
                                tableName + ".zeir_id",
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
                                tableName + ".client_reg_date",
                                tableName + ".last_interacted_with"
                        }

        );
        queryBUilder.customJoin("LEFT JOIN " + parentTableName + " ON  " + tableName + ".relational_id =  " + parentTableName + ".id");
        return queryBUilder.mainCondition(getMainConditionString(tableName));
    }

    @Override
    public boolean onBackPressed() {
        if (listMode) {
            switchViews(false);
            return true;
        }
        return false;
    }

    @Override
    protected void goBack() {
        if (listMode) {
            switchViews(false);
        } else {
            ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment(null);
        }
    }

    private String getMainConditionString(String tableName) {

        String startDateKey = START_DATE;
        String endDateKey = END_DATE;

        String mainConditionString = "";
        for (Map.Entry<String, String> entry : editMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals(startDateKey) && !key.equals(endDateKey)) {
                if (StringUtils.isBlank(mainConditionString)) {
                    if (key.contains(LOST_TO_FOLLOW_UP)) {
                        mainConditionString += " " + key + " = '" + value + "'";
                    } else if (key.contains(INACTIVE)) {
                        if (value.equalsIgnoreCase("true")) {
                            mainConditionString += " " + key + " = '" + value + "'";
                        } else {
                            mainConditionString += " (" + key + " = '" + value + "' OR " + key + " is null OR " + key + " = '')";
                        }
                    } else {
                        mainConditionString += " " + key + " Like '%" + value + "%'";
                    }
                } else {
                    if (key.contains(LOST_TO_FOLLOW_UP)) {
                        mainConditionString += " AND " + key + " = '" + value + "'";
                    } else if (key.contains(INACTIVE)) {
                        if (value.equalsIgnoreCase("true")) {
                            mainConditionString += " AND " + key + " = '" + value + "'";
                        } else {
                            mainConditionString += " AND (" + key + " = '" + value + "' OR " + key + " is null OR " + key + " = '')";
                        }
                    } else {
                        mainConditionString += " AND " + key + " Like '%" + value + "%'";
                    }
                }
            }
        }

        if (StringUtils.isBlank(mainConditionString)) {
            if (editMap.containsKey(startDateKey) && editMap.containsKey(endDateKey)) {
                mainConditionString += " " + tableName + ".dob BETWEEN '" + editMap.get(startDateKey) + "' AND '" + editMap.get(endDateKey) + "'";
            } else if (editMap.containsKey(startDateKey)) {
                mainConditionString += " " + tableName + ".dob >= '" + editMap.get(startDateKey) + "'";

            } else if (editMap.containsKey(startDateKey)) {
                mainConditionString += " " + tableName + ".dob <= '" + editMap.get(endDateKey) + "'";
            }
        } else {
            if (editMap.containsKey(startDateKey) && editMap.containsKey(endDateKey)) {
                mainConditionString += " AND " + tableName + ".dob BETWEEN '" + editMap.get(startDateKey) + "' AND '" + editMap.get(endDateKey) + "'";
            } else if (editMap.containsKey(startDateKey)) {
                mainConditionString += " AND " + tableName + ".dob >= '" + editMap.get(startDateKey) + "'";

            } else if (editMap.containsKey(startDateKey)) {
                mainConditionString += " AND " + tableName + ".dob <= '" + editMap.get(endDateKey) + "'";
            }
        }

        return mainConditionString;

    }

    private void switchViews(boolean showList) {
        if (showList) {
            advancedSearchForm.setVisibility(View.GONE);
            listViewLayout.setVisibility(View.VISIBLE);
            clientsView.setVisibility(View.VISIBLE);
            clientsProgressView.setVisibility(View.INVISIBLE);

            showProgressView();
            listMode = true;
        } else {
            clearSearchCriteria();
            advancedSearchForm.setVisibility(View.VISIBLE);
            listViewLayout.setVisibility(View.GONE);
            clientsView.setVisibility(View.INVISIBLE);
            clientsProgressView.setVisibility(View.VISIBLE);
            listMode = false;
        }
    }


    private void setDatePicker(final EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //To show current date in the datepicker
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, selectedyear);
                        calendar.set(Calendar.MONTH, selectedmonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedday);

                        String dateString = DateUtil.yyyyMMdd.format(calendar.getTime());
                        editText.setText(dateString);

                    }
                }, mYear, mMonth, mDay);
                mDatePicker.getDatePicker().setCalendarViewShown(false);
                mDatePicker.show();
            }
        });

    }

    private String removeLastComma(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        if (!outOfArea) {
                            String query = filterandSortQuery();
                            Cursor cursor = commonRepository().RawCustomQueryForAdapter(query);
                            totalcount = cursor.getCount();
                            Log.v("total count here", "" + totalcount);
                            currentlimit = 20;
                            currentoffset = 0;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                    hideProgressView();
                                }

                                ;
                            });

                            return cursor;
                        } else {
                            return matrixCursor;
                        }
                    }
                };
            default:
                // An invalid id was passed in
                return null;
        }
    }

    protected TextWatcher advancedSearchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(final CharSequence cs, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (zeirId.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, zeirId.getId());
            } else if (firstName.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, firstName.getId());
            } else if (lastName.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, lastName.getId());
            } else if (motherGuardianName.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, motherGuardianName.getId());
            } else if (motherGuardianNrc.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, motherGuardianNrc.getId());
            } else if (motherGuardianPhoneNumber.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, motherGuardianPhoneNumber.getId());
            } else if (startDate.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, startDate.getId());
            } else if (endDate.getText().hashCode() == editable.hashCode()) {
                updateSearchButton(editable, endDate.getId());
            }
        }
    };

    private void updateSearchButton(Editable editable, Integer editTextId) {
        String textChanged = editable.toString();
        if (StringUtils.isNotBlank(textChanged)) {
            if (!editedList.contains(editTextId)) {
                editedList.add(editTextId);
            }

            if (search != null && !search.isEnabled()) {
                search.setEnabled(true);
                search.setClickable(true);
            }
        } else {
            if (editedList.contains(editTextId)) {
                editedList.remove(editTextId);
            }

            if (editedList.isEmpty() && search != null && search.isEnabled()) {
                search.setEnabled(false);
                search.setClickable(false);
            }
        }
    }

    public void updateFilterCount(int count) {
        if (filterCount != null) {
            if (count > 0) {
                filterCount.setText(String.valueOf(count));
                filterCount.setVisibility(View.VISIBLE);
                filterCount.setClickable(true);
            } else {
                filterCount.setVisibility(View.GONE);
                filterCount.setClickable(false);
            }
        }
        overdueCount = count;
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

    private class AdvancedMatrixCursor extends net.sqlcipher.MatrixCursor {
        public AdvancedMatrixCursor(String[] columnNames) {
            super(columnNames);
        }

        @Override
        public long getLong(int column) {
            try {
                return super.getLong(column);
            } catch (NumberFormatException e) {
                return (new Date()).getTime();
            }
        }
    }

    public EditText getZeirId() {
        return this.zeirId;
    }

    ;

}
