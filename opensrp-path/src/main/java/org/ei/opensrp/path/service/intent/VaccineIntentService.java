package org.ei.opensrp.path.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.repository.VaccineRepository;
import org.ei.opensrp.path.repository.WeightRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

import util.JsonFormUtils;


/**
 * Created by keyman on 3/01/2017.
 */
public class VaccineIntentService extends IntentService {
    private static final String TAG = VaccineIntentService.class.getCanonicalName();
    private VaccineRepository vaccineRepository;


    public VaccineIntentService() {
        super("VaccineService");
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
                    String formatedDate = simpleDateFormat.format(vaccine.getDate());

                    JSONArray jsonArray = new JSONArray();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JsonFormUtils.KEY, vaccine.getName().toLowerCase().replace(" ", "_"));
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY, concept);
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_ID, entityId);
                    jsonObject.put(JsonFormUtils.OPENMRS_ENTITY_PARENT, getParentId(vaccine.getName()));
                    jsonObject.put(JsonFormUtils.OPENMRS_DATA_TYPE, dateDataType);
                    jsonObject.put(JsonFormUtils.VALUE, formatedDate);
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
        name = name.split("\\s+")[0];
        switch (name) {
            case "BCG":
                return "886AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "OPV":
                return "783AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "PCV":
                return "162342AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "Penta":
                return "1685AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "Rota":
                return "159698AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "Measles":
                return "36AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            case "MR":
                return "162586AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        }
        return "";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vaccineRepository = VaccinatorApplication.getInstance().vaccineRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
