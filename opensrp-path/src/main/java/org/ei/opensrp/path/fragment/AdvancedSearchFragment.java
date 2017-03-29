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
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.clientandeventmodel.DateUtil;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildSmartRegisterActivity;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.provider.ChildSmartClientsProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private List<Integer> editedList = new ArrayList<>();
    Map<String, String> editMap = new HashMap<>();
    boolean listMode = false;

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

        TextView filterCount = (TextView) view.findViewById(R.id.filter_count);
        filterCount.setClickable(false);
        filterCount.setText("1");
        filterCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterSection.performClick();
            }
        });

        View backToHome = view.findViewById(R.id.btn_back_to_home);
        backToHome.setOnClickListener(clientActionHandler);

        View titleLayout = view.findViewById(R.id.title_layout);
        titleLayout.setOnClickListener(clientActionHandler);
        titleLayout.setPadding(1, 0, 0, 0);

        TextView titleView = (TextView) view.findViewById(R.id.txt_title_label);
        titleView.setText(getString(R.string.advanced_search));

        View nameInitials = view.findViewById(R.id.name_inits);
        nameInitials.setVisibility(View.GONE);

        ImageView backButton = (ImageView) view.findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE);

        searchCriteria = (TextView) view.findViewById(R.id.search_criteria);
        searchCriteria.setVisibility(View.GONE);

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
                case R.id.btn_back_to_home:
                case R.id.title_layout:
                    if (listMode) {
                        switchViews(false);
                    } else {
                        ((ChildSmartRegisterActivity) getActivity()).switchToBaseFragment(null);
                    }
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
        search = (Button) view.findViewById(R.id.search);
        search.setOnClickListener(clientActionHandler);
        search.setClickable(false);
        search.setEnabled(false);

        searchLimits = (RadioGroup) view.findViewById(R.id.search_limits);

        active = (CheckBox) view.findViewById(R.id.active);
        inactive = (CheckBox) view.findViewById(R.id.inactive);
        lostToFollowUp = (CheckBox) view.findViewById(R.id.lost_to_follow_up);

        zeirId = (EditText) view.findViewById(R.id.zeir_id);
        zeirId.addTextChangedListener(advancedSearchWatcher);

        firstName = (EditText) view.findViewById(R.id.first_name);
        firstName.addTextChangedListener(advancedSearchWatcher);

        lastName = (EditText) view.findViewById(R.id.last_name);
        lastName.addTextChangedListener(advancedSearchWatcher);

        motherGuardianName = (EditText) view.findViewById(R.id.mother_guardian_name);
        motherGuardianName.addTextChangedListener(advancedSearchWatcher);

        motherGuardianNrc = (EditText) view.findViewById(R.id.mother_guardian_nrc);
        motherGuardianNrc.addTextChangedListener(advancedSearchWatcher);

        motherGuardianPhoneNumber = (EditText) view.findViewById(R.id.mother_guardian_phone_number);
        motherGuardianPhoneNumber.addTextChangedListener(advancedSearchWatcher);

        startDate = (EditText) view.findViewById(R.id.start_date);
        startDate.addTextChangedListener(advancedSearchWatcher);
        setDatePicker(startDate);

        endDate = (EditText) view.findViewById(R.id.end_date);
        endDate.addTextChangedListener(advancedSearchWatcher);
        setDatePicker(endDate);

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
        String searchCriteriaString = "Search criteria: Include: ";

        boolean outOfArea = false;
        if (searchLimits.getCheckedRadioButtonId() == R.id.out_and_inside) {
            outOfArea = true;
            searchCriteriaString += " \"Outside and Inside My Catchment Area\", ";
        } else {
            searchCriteriaString += " \"My Catchment Area\", ";
        }

        boolean isActive = active.isChecked();
        boolean isInactive = inactive.isChecked();
        boolean isLostToFollowUp = lostToFollowUp.isChecked();

        if (isActive || isInactive || isLostToFollowUp) {
            searchCriteriaString += " \" ";
            if (isActive) {
                searchCriteriaString += "Active";
            }
            if (isInactive) {
                searchCriteriaString += ", Inactive";
            }
            if (isLostToFollowUp) {
                searchCriteriaString += ", Lost to Follow-up";
            }
            searchCriteriaString += "\"; ";

        }

        String tableName = "ec_child";
        String parentTableName = "ec_mother";

        String zeirIdString = zeirId.getText().toString();
        if (StringUtils.isNotBlank(zeirIdString)) {
            searchCriteriaString += " ZEIR ID: \"" + zeirIdString + "\",";
            editMap.put(tableName + ".zeir_id", zeirIdString.trim());
        }

        String firstNameString = firstName.getText().toString();
        if (StringUtils.isNotBlank(firstNameString)) {
            searchCriteriaString += " First name: \"" + firstNameString + "\",";
            editMap.put(tableName + ".first_name", firstNameString.trim());
        }

        String lastNameString = lastName.getText().toString();
        if (StringUtils.isNotBlank(lastNameString)) {
            searchCriteriaString += " Last name: \"" + lastNameString + "\",";
            editMap.put(tableName + ".last_name", lastNameString.trim());
        }

        String motherGuardianNameString = motherGuardianName.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianNameString)) {
            searchCriteriaString += " Mother/Guardian name: \"" + motherGuardianNameString + "\",";
            editMap.put(parentTableName + ".first_name", motherGuardianNameString.trim());
        }

        String motherGuardianNrcString = motherGuardianNrc.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianNrcString)) {
            searchCriteriaString += " Mother/Guardian nrc: \"" + motherGuardianNrcString + "\",";
            editMap.put(parentTableName + ".nrc_number", motherGuardianNrcString.trim());
        }

        String motherGuardianPhoneNumberString = motherGuardianPhoneNumber.getText().toString();
        if (StringUtils.isNotBlank(motherGuardianPhoneNumberString)) {
            searchCriteriaString += " Mother/Guardian phone number: \"" + motherGuardianPhoneNumberString + "\",";
            editMap.put(parentTableName + ".contact_phone_number", motherGuardianPhoneNumberString.trim());
        }

        String startDateString = startDate.getText().toString();
        if (StringUtils.isNotBlank(startDateString)) {
            searchCriteriaString += " Start date: \"" + startDateString + "\",";
            editMap.put("start_date", startDateString.trim());
        }

        String endDateString = endDate.getText().toString();
        if (StringUtils.isNotBlank(endDateString)) {
            searchCriteriaString += " End date: \"" + endDateString + "\",";
            editMap.put("end_date", endDateString.trim());
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

    private void localSearch() {
        switchViews(true);

        String tableName = "ec_child";
        setTablename(tableName);
        ChildSmartClientsProvider hhscp = new ChildSmartClientsProvider(getActivity(),
                clientActionHandler, context().alertService(), VaccinatorApplication.getInstance().vaccineRepository(), VaccinatorApplication.getInstance().weightRepository());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, Context.getInstance().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        /*SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder();
        countqueryBUilder.SelectInitiateMainTableCounts(tableName);
        countSelect = countqueryBUilder.mainCondition(getMainConditionString(tableName));
        mainCondition = "";
        CountExecute();*/

        super.filterandSortInInitializeQueries();
        //refresh();
    }

    private void globalSearch() {

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

    private String getMainConditionString(String tableName) {

        String startDateKey = "start_date";
        String endDateKey = "end_date";

        String mainConditionString = "";
        for (Map.Entry<String, String> entry : editMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals(startDateKey) && !key.equals(endDateKey)) {
                if (StringUtils.isBlank(mainConditionString)) {
                    mainConditionString += " " + key + " Like '%" + value + "%'";
                } else {
                    mainConditionString += " AND " + key + " Like '%" + value + "%'";
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

}
