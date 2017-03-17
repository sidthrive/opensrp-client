package org.ei.opensrp.path.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.repository.VaccineRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

import util.JsonFormUtils;
import util.VaccinatorUtils;


/**
 * Created by keyman on 3/01/2017.
 */
public class VaccineIntentService extends IntentService {
    private static final String TAG = VaccineIntentService.class.getCanonicalName();
    private final VaccineRepository vaccineRepository;
    private final JSONArray availableVaccines;

    public VaccineIntentService() throws JSONException {

        super("VaccineService");
        vaccineRepository = Context.getInstance().vaccineRepository();
        availableVaccines = new JSONArray(VaccinatorUtils.getSupportedVaccines(this));
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final String entityId = "1410AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final String calId = "1418AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final String dateDataType = "date";
        final String calculationDataType = "calculate";
        final String concept = "concept";

        try {
            List<Vaccine> vaccines = vaccineRepository.findUnSyncedBeforeTime(24);
            if (!vaccines.isEmpty()) {
                for (Vaccine vaccine : vaccines) {
                    String eventType = "Vaccination";
                    String entityType = "vaccination";

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = simpleDateFormat.format(vaccine.getDate());

                    JSONArray jsonArray = new JSONArray();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JsonFormUtils.KEY, vaccine.getName().toLowerCase().replace(" ", "_"));
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY, concept);
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_ID, entityId);
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_PARENT, getParentId(vaccine.getName()));
                    jsonObject.put(JsonFormUtils.OPENMRS_DATA_TYPE, dateDataType);
                    jsonObject.put(JsonFormUtils.VALUE, formattedDate);
                    jsonArray.put(jsonObject);

                    if (vaccine.getCalculation() != null && vaccine.getCalculation().intValue() >= 0) {
                        jsonObject = new JSONObject();
                        jsonObject.put(JsonFormUtils.KEY, vaccine.getName().toLowerCase().replace(" ", "_") + "_dose");
                        jsonObject.put(JsonFormUtils.OPENMRS_ENTITY, concept);
                        jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_ID, calId);
                        jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_PARENT, getParentId(vaccine.getName()));
                        jsonObject.put(JsonFormUtils.OPENMRS_DATA_TYPE, calculationDataType);
                        jsonObject.put(JsonFormUtils.VALUE, vaccine.getCalculation());
                        jsonArray.put(jsonObject);
                    }
                    JsonFormUtils.createVaccineEvent(getApplicationContext(), vaccine, eventType, entityType, jsonArray);
                    vaccineRepository.close(vaccine.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private String getParentId(String name) {
        try {
            for (int i = 0; i < availableVaccines.length(); i++) {
                JSONObject curVaccineGroup = availableVaccines.getJSONObject(i);
                for (int j = 0; j < curVaccineGroup.getJSONArray("vaccines").length(); j++) {
                    if (curVaccineGroup.getJSONArray("vaccines").getJSONObject(j).getString("name")
                            .equals(name)) {
                        return curVaccineGroup.getJSONArray("vaccines").getJSONObject(j)
                                .getJSONObject("openmrs_date").getString("parent_entity");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return "";
    }
}
