package util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.path.db.VaccineRepo;
import org.ei.opensrp.service.ZiggyService;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.domain.VaccinateFormSubmissionWrapper;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.fragment.VaccinationDialogFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

import static org.ei.opensrp.AllConstants.ENTITY_ID_PARAM;
import static org.ei.opensrp.AllConstants.FORM_NAME_PARAM;
import static org.ei.opensrp.AllConstants.INSTANCE_ID_PARAM;
import static org.ei.opensrp.AllConstants.SYNC_STATUS;
import static org.ei.opensrp.AllConstants.VERSION_PARAM;
import static org.ei.opensrp.domain.SyncStatus.PENDING;
import static org.ei.opensrp.util.EasyMap.create;
import static util.Utils.convertDateFormat;

/**
 * Created by keyman on 17/11/2016.
 */
public class VaccinateActionUtils {

    public static String formData(Context context, String entityId, String formName, String metaData) {
        try {
            return FormUtils.getInstance(context).generateXMLInputForFormWithEntityId(entityId, formName, metaData);
        } catch (Exception e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
            return null;
        }
    }

    public static void updateJson(JSONObject jsonObject, String field, String value) {
        try {
            if (jsonObject.has(field)) {
                JSONObject fieldJson = jsonObject.getJSONObject(field);
                fieldJson.put("content", value);
            }
        } catch (JSONException e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
        }
    }

    public static JSONObject find(JSONObject jsonObject, String field) {
        try {
            if (jsonObject.has(field)) {
                return jsonObject.getJSONObject(field);

            }
        } catch (JSONException e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
        }

        return null;
    }


    public static TableRow findRow(Set<TableLayout> tables, String tag) {
        for (TableLayout table : tables) {
            View view = table.findViewWithTag(tag);
            if (view != null && view instanceof TableRow) {
                return (TableRow) view;
            }
        }
        return null;
    }

    public static TableRow findRow(TableLayout table, String tag) {
        View view = table.findViewWithTag(tag);
        if (view != null && view instanceof TableRow) {
            return (TableRow) view;
        }
        return null;
    }

    public static void vaccinateToday(TableRow tableRow, VaccineWrapper tag) {
        TextView textView = (TextView) tableRow.findViewById(R.id.date);
        textView.setText(R.string.done_today);

        Button button = (Button) tableRow.findViewById(R.id.undo);
        button.setVisibility(View.VISIBLE);

        String color = "#31B404";
        Button status = (Button) tableRow.findViewById(R.id.status);
        status.setBackgroundColor(Color.parseColor(color));

        tableRow.setOnClickListener(null);
    }

    public static void vaccinateEarlier(TableRow tableRow, VaccineWrapper tag) {
        String vaccineDate = convertDateFormat(tag.getUpdatedVaccineDateAsString(), true);

        TextView textView = (TextView) tableRow.findViewById(R.id.date);
        textView.setText(vaccineDate);

        Button button = (Button) tableRow.findViewById(R.id.undo);
        button.setVisibility(View.VISIBLE);

        String color = "#31B404";
        Button status = (Button) tableRow.findViewById(R.id.status);
        status.setBackgroundColor(Color.parseColor(color));

        tableRow.setOnClickListener(null);
    }

    public static void undoVaccination(final Context context, TableRow tableRow, final VaccineWrapper tag) {
        Button button = (Button) tableRow.findViewById(R.id.undo);
        button.setVisibility(View.GONE);

        String color = tag.getColor();
        Button status = (Button) tableRow.findViewById(R.id.status);
        status.setBackgroundColor(Color.parseColor(color));

        TextView v = (TextView) tableRow.findViewById(R.id.date);
        v.setText(tag.getFormattedVaccineDate());

        if (tag.getStatus().equalsIgnoreCase("due")) {
            tableRow.setOnClickListener(new TableRow.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction ft = ((Activity) context).getFragmentManager().beginTransaction();
                    Fragment prev = ((Activity) context).getFragmentManager().findFragmentByTag(VaccinationDialogFragment.DIALOG_TAG);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    VaccinationDialogFragment vaccinationDialogFragment = VaccinationDialogFragment.newInstance(context, Arrays.asList(tag), null);
                    vaccinationDialogFragment.show(ft, VaccinationDialogFragment.DIALOG_TAG);

                }
            });
        }

    }

    public static void saveFormSubmission(Context appContext, final String formSubmission, String id, final String formName, JSONObject fieldOverrides) {

        Log.v("fieldoverride", fieldOverrides.toString());

        // save the form
        try {
            FormUtils formUtils = FormUtils.getInstance(appContext);
            final FormSubmission submission = formUtils.generateFormSubmisionFromXMLString(id, formSubmission, formName, fieldOverrides);

            org.ei.opensrp.Context context = org.ei.opensrp.Context.getInstance();
            ZiggyService ziggyService = context.ziggyService();
            ziggyService.saveForm(getParams(submission), submission.instance());

            // Update Fts Tables
            CommonFtsObject commonFtsObject = context.commonFtsObject();
            if (commonFtsObject != null) {
                String[] ftsTables = commonFtsObject.getTables();
                for (String ftsTable : ftsTables) {
                    AllCommonsRepository allCommonsRepository = context.allCommonsRepositoryobjects(ftsTable);
                    boolean updated = allCommonsRepository.updateSearch(submission.entityId());
                    if (updated) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
            e.printStackTrace();
        }
    }

    private static String getParams(FormSubmission submission) {
        return new Gson().toJson(
                create(INSTANCE_ID_PARAM, submission.instanceId())
                        .put(ENTITY_ID_PARAM, submission.entityId())
                        .put(FORM_NAME_PARAM, submission.formName())
                        .put(VERSION_PARAM, submission.version())
                        .put(SYNC_STATUS, PENDING.value())
                        .map());
    }

    public static JSONObject retrieveFieldOverides(String overrides) {
        try {
            //get the field overrides map
            if (overrides != null) {
                JSONObject json = new JSONObject(overrides);
                String overridesStr = json.getString("fieldOverrides");
                return new JSONObject(overridesStr);
            }
        } catch (Exception e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
        }
        return new JSONObject();

    }

    public static String retrieveExistingAge(VaccinateFormSubmissionWrapper vaccinateFormSubmissionWrapper) {
        try {
            if (vaccinateFormSubmissionWrapper != null) {
                JSONObject fieldOverrides = vaccinateFormSubmissionWrapper.getOverrides();
                if (fieldOverrides.has("existing_age")) {
                    return fieldOverrides.getString("existing_age");
                }
            }
        } catch (JSONException e) {
            Log.e(VaccinateActionUtils.class.getName(), "", e);
        }
        return null;
    }

    public static boolean addDialogHookCustomFilter(VaccineWrapper tag) {
        boolean addHook = false;

        int age = 0;
        String existingAge = tag.getExistingAge();
        if (StringUtils.isNumeric(existingAge)) {
            age = Integer.valueOf(existingAge);
        }

        VaccineRepo.Vaccine vaccine = tag.getVaccine();
            switch (vaccine) {
                case penta1:
                case pcv1:
                case opv1:
                    if (age > 35)
                        addHook = true;
                    break;
                case penta2:
                case pcv2:
                case opv2:
                    if (age > 63)
                        addHook = true;
                    break;
                case penta3:
                case pcv3:
                case opv3:
                case ipv:
                    if (age > 91)
                        addHook = true;
                    break;
                case measles1:
                    if (age > 250)
                        addHook = true;
                    break;
                case measles2:
                    if (age > 340)
                        addHook = true;
                    break;
                default:
                    addHook = true;
                    break;
            }

        return addHook;

    }

}
