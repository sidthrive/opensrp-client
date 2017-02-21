package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ei.opensrp.clientandeventmodel.Address;
import org.ei.opensrp.clientandeventmodel.Client;
import org.ei.opensrp.clientandeventmodel.DateUtil;
import org.ei.opensrp.clientandeventmodel.Event;
import org.ei.opensrp.clientandeventmodel.FormEntityConstants;
import org.ei.opensrp.clientandeventmodel.Obs;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.sync.ClientProcessor;
import org.ei.opensrp.sync.CloudantDataHandler;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

/**
 * Created by keyman on 08/02/2017.
 */
public class JsonFormUtils {
    private static final String TAG = "JsonFormUtils";

    private static final String OPENMRS_ENTITY = "openmrs_entity";
    private static final String OPENMRS_ENTITY_ID = "openmrs_entity_id";
    private static final String OPENMRS_ENTITY_PARENT = "openmrs_entity_parent";
    private static final String OPENMRS_CHOICE_IDS = "openmrs_choice_ids";
    private static final String OPENMRS_DATA_TYPE = "openmrs_data_type";

    private static final String PERSON_ATTRIBUTE = "person_attribute";
    private static final String PERSON_INDENTIFIER = "person_identifier";
    private static final String PERSON_ADDRESS = "person_address";

    private static final String CONCEPT = "concept";
    private static final String ENCOUNTER = "encounter";
    public static final String VALUE = "value";
    private static final String VALUES = "values";
    public static final String FIELDS = "fields";
    public static final String KEY = "key";
    private static final String ENTITY_ID = "entity_id";
    private static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String STEP1 = "step1";
    private static final String METADATA = "metadata";
    public static final String ZEIRD = "ZEIR_ID";

    public static final SimpleDateFormat FORM_DATE = new SimpleDateFormat("dd-MM-yyyy");

    public static void save(Context context, String jsonString, String providerId, String bindType, String imageKey) {
        if (context == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(jsonString)) {
            return;
        }

        try {

            JSONObject jsonForm = new JSONObject(jsonString);

            String entityId = getString(jsonForm, ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }

            JSONArray fields = fields(jsonForm);
            if (fields == null) {
                return;
            }

            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);

            JSONObject metadata = getJSONObject(jsonForm, METADATA);

            Client c = JsonFormUtils.createBaseClient(fields, entityId);
            Event e = JsonFormUtils.createEvent(fields, metadata, entityId, encounterType, providerId, bindType);

            CloudantDataHandler cloudantDataHandler = CloudantDataHandler.getInstance(context.getApplicationContext());

            org.ei.opensrp.cloudant.models.Event event = new org.ei.opensrp.cloudant.models.Event(e);
            cloudantDataHandler.createEventDocument(event);
            if (c != null) {
                org.ei.opensrp.cloudant.models.Client client = new org.ei.opensrp.cloudant.models.Client(c);
                cloudantDataHandler.createClientDocument(client);
            }

            String zeirId=c.getIdentifier(ZEIRD);
            //mark zeir id as used
            org.ei.opensrp.Context.uniqueIdRepository().close(zeirId);

            String imageLocation = getFieldValue(fields, imageKey);
            saveImage(context, providerId, entityId, imageLocation);

            ClientProcessor.getInstance(context).processClient();

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void saveImage(Context context, String providerId, String entityId, String imageLocation) {
        if (StringUtils.isBlank(imageLocation)) {
            return;
        }


        File file = new File(imageLocation);

        if (!file.exists()) {
            return;
        }

        Bitmap compressedImageFile = Compressor.getDefault(context).compressToBitmap(file);
        saveStaticImageToDisk(compressedImageFile, providerId, entityId);

    }

    public static void saveStaticImageToDisk(Bitmap image, String providerId, String entityId) {
        if (image == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
            return;
        }
        OutputStream os = null;
        try {

            if (entityId != null && !entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = new File(absoluteFileName);
                os = new FileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                if (compressFormat != null) {
                    image.compress(compressFormat, 100, os);
                } else {
                    throw new IllegalArgumentException("Failed to save static image, could not retrieve image compression format from name "
                            + absoluteFileName);
                }
                // insert into the db
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImageid(UUID.randomUUID().toString());
                profileImage.setAnmId(providerId);
                profileImage.setEntityID(entityId);
                profileImage.setFilepath(absoluteFileName);
                profileImage.setFilecategory("profilepic");
                profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
                imageRepo.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to save static image to disk");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                }
            }
        }

    }

    public static Client createBaseClient(JSONArray fields, String entityId) {

        String firstName = getFieldValue(fields, FormEntityConstants.Person.first_name.entity(), FormEntityConstants.Person.first_name.name());
        String middleName = getFieldValue(fields, FormEntityConstants.Person.middle_name.entity(), FormEntityConstants.Person.middle_name.name());
        String lastName = getFieldValue(fields, FormEntityConstants.Person.last_name.entity(), FormEntityConstants.Person.last_name.name());
        String bd = getFieldValue(fields, FormEntityConstants.Person.birthdate.entity(), FormEntityConstants.Person.birthdate.name());
        DateTime birthdate = formatDate(bd, true);
        String dd = getFieldValue(fields, FormEntityConstants.Person.deathdate.entity(), FormEntityConstants.Person.deathdate.name());
        DateTime deathdate = formatDate(dd, true);
        String aproxbd = getFieldValue(fields, FormEntityConstants.Person.birthdate_estimated.entity(), FormEntityConstants.Person.birthdate_estimated.name());
        Boolean birthdateApprox = false;
        if (!StringUtils.isEmpty(aproxbd) && NumberUtils.isNumber(aproxbd)) {
            int bde = 0;
            try {
                bde = Integer.parseInt(aproxbd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            birthdateApprox = bde > 0 ? true : false;
        }
        String aproxdd = getFieldValue(fields, FormEntityConstants.Person.deathdate_estimated.entity(), FormEntityConstants.Person.deathdate_estimated.name());
        Boolean deathdateApprox = false;
        if (!StringUtils.isEmpty(aproxdd) && NumberUtils.isNumber(aproxdd)) {
            int dde = 0;
            try {
                dde = Integer.parseInt(aproxdd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            deathdateApprox = dde > 0 ? true : false;
        }
        String gender = getFieldValue(fields, FormEntityConstants.Person.gender.entity(), FormEntityConstants.Person.gender.name());

        List<Address> addresses = new ArrayList<>(extractAddresses(fields).values());

        Client c = (Client) new Client(entityId)
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withBirthdate((birthdate != null ? birthdate.toDate() : null), birthdateApprox)
                .withDeathdate(deathdate != null ? deathdate.toDate() : null, deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withAddresses(addresses)
                .withAttributes(extractAttributes(fields))
                .withIdentifiers(extractIdentifiers(fields));
        return c;

    }

    public static Event createEvent(JSONArray fields, JSONObject metadata, String entityId, String encounterType, String providerId, String bindType) {

        String encounterDateField = getFieldValue(fields, FormEntityConstants.Encounter.encounter_date.entity(), FormEntityConstants.Encounter.encounter_date.name());
        String encounterLocation = getFieldValue(fields, FormEntityConstants.Encounter.location_id.entity(), FormEntityConstants.Encounter.location_id.name());

        Date encounterDate = new Date();
        if (StringUtils.isNotBlank(encounterDateField)) {
            DateTime dateTime = formatDate(encounterDateField, false);
            if (dateTime != null) {
                encounterDate = dateTime.toDate();
            }
        }

        Event e = (Event) new Event()
                .withBaseEntityId(entityId)//should be different for main and subform
                .withEventDate(encounterDate)
                .withEventType(encounterType)
                .withLocationId(encounterLocation)
                .withProviderId(providerId)
                .withEntityType(bindType)
                .withFormSubmissionId(generateRandomUUIDString())
                .withDateCreated(new Date());

        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            String value = getString(jsonObject, VALUE);
            if (StringUtils.isNotBlank(value)) {
                addObservation(e, jsonObject);
            }
        }

        if (metadata != null) {
            Iterator<?> keys = metadata.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = getJSONObject(metadata, key);
                String value = getString(jsonObject, VALUE);
                if (StringUtils.isNotBlank(value)) {
                    String entityVal = getString(jsonObject, OPENMRS_ENTITY);
                    if (entityVal != null) {
                        if (entityVal.equals(CONCEPT)) {
                            addToJSONObject(jsonObject, KEY, key);
                            addObservation(e, jsonObject);
                        } else if (entityVal.equals(ENCOUNTER)) {
                            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                            if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                DateTime eDate = formatDate(value, false);
                                if (eDate != null) {
                                    e.setEventDate(eDate.toDate());
                                }
                            }
                        }
                    }
                }
            }
        }

        return e;

    }

    private static void addObservation(Event e, JSONObject jsonObject) {
        String value = getString(jsonObject, VALUE);
        String entity = CONCEPT;
        if (StringUtils.isNotBlank(value)) {
            List<Object> vall = new ArrayList<>();

            String formSubmissionField = getString(jsonObject, KEY);

            String dataType = getString(jsonObject, OPENMRS_DATA_TYPE);
            if (StringUtils.isBlank(dataType)) {
                dataType = "text";
            }

            String entityVal = getString(jsonObject, OPENMRS_ENTITY);

            if (entityVal != null && entityVal.equals(entity)) {
                String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                String entityParentVal = getString(jsonObject, OPENMRS_ENTITY_PARENT);

                List<Object> humanReadableValues = new ArrayList<>();

                JSONArray values = getJSONArray(jsonObject, VALUES);
                if (values != null && values.length() > 0) {
                    JSONObject choices = getJSONObject(jsonObject, OPENMRS_CHOICE_IDS);
                    String chosenConcept = getString(choices, value);
                    vall.add(chosenConcept);
                    humanReadableValues.add(value);
                } else {
                    vall.add(value);
                }

                e.addObs(new Obs(CONCEPT, dataType, entityIdVal,
                        entityParentVal, vall, humanReadableValues, null, formSubmissionField));
            } else if (StringUtils.isBlank(entityVal)) {
                vall.add(value);

                e.addObs(new Obs("formsubmissionField", dataType, formSubmissionField,
                        "", vall, new ArrayList<>(), null, formSubmissionField));
            }
        }
    }


    private static Map<String, String> extractIdentifiers(JSONArray fields) {
        Map<String, String> pids = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillIdentifiers(pids, jsonObject);
        }
        return pids;
    }

    private static Map<String, Object> extractAttributes(JSONArray fields) {
        Map<String, Object> pattributes = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillAttributes(pattributes, jsonObject);
        }

        return pattributes;
    }

    private static Map<String, Address> extractAddresses(JSONArray fields) {
        Map<String, Address> paddr = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillAddressFields(jsonObject, paddr);
        }
        return paddr;
    }


    private static void fillIdentifiers(Map<String, String> pids, JSONObject jsonObject) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }
        String entity = PERSON_INDENTIFIER;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);

            //FIXME hack unique identifiers
            if(entityIdVal.equals("ZEIR_ID") && value.equals("0")){
                value = generateRandomUUIDString();
            }

            pids.put(entityIdVal, value);
        }


    }


    private static void fillAttributes(Map<String, Object> pattributes, JSONObject jsonObject) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }
        String entity = PERSON_ATTRIBUTE;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
            pattributes.put(entityIdVal, value);
        }
    }


    private static void fillAddressFields(JSONObject jsonObject, Map<String, Address> addresses) {

        if (jsonObject == null) {
            return;
        }

        try {

            String value = getString(jsonObject, VALUE);
            if (StringUtils.isBlank(value)) {
                return;
            }

            String entity = PERSON_ADDRESS;
            String entityVal = getString(jsonObject, OPENMRS_ENTITY);

            if (entityVal != null && entityVal.equalsIgnoreCase(entity)) {
                String addressType = getString(jsonObject, OPENMRS_ENTITY_PARENT);
                String addressField = getString(jsonObject, OPENMRS_ENTITY_ID);

                Address ad = addresses.get(addressType);
                if (ad == null) {
                    ad = new Address(addressType, null, null, null, null, null, null, null, null);
                }

                if (addressField.equalsIgnoreCase("startDate") || addressField.equalsIgnoreCase("start_date")) {
                    ad.setStartDate(DateUtil.parseDate(value));
                } else if (addressField.equalsIgnoreCase("endDate") || addressField.equalsIgnoreCase("end_date")) {
                    ad.setEndDate(DateUtil.parseDate(value));
                } else if (addressField.equalsIgnoreCase("latitude")) {
                    ad.setLatitude(value);
                } else if (addressField.equalsIgnoreCase("longitute")) {
                    ad.setLongitude(value);
                } else if (addressField.equalsIgnoreCase("geopoint")) {
                    // example geopoint 34.044494 -84.695704 4 76 = lat lon alt prec
                    String geopoint = value;
                    if (!StringUtils.isEmpty(geopoint)) {
                        String[] g = geopoint.split(" ");
                        ad.setLatitude(g[0]);
                        ad.setLongitude(g[1]);
                        ad.setGeopoint(geopoint);
                    }
                } else if (addressField.equalsIgnoreCase("postal_code") || addressField.equalsIgnoreCase("postalCode")) {
                    ad.setPostalCode(value);
                } else if (addressField.equalsIgnoreCase("sub_town") || addressField.equalsIgnoreCase("subTown")) {
                    ad.setSubTown(value);
                } else if (addressField.equalsIgnoreCase("town")) {
                    ad.setTown(value);
                } else if (addressField.equalsIgnoreCase("sub_district") || addressField.equalsIgnoreCase("subDistrict")) {
                    ad.setSubDistrict(value);
                } else if (addressField.equalsIgnoreCase("district") || addressField.equalsIgnoreCase("county")
                        || addressField.equalsIgnoreCase("county_district") || addressField.equalsIgnoreCase("countyDistrict")) {
                    ad.setCountyDistrict(value);
                } else if (addressField.equalsIgnoreCase("city") || addressField.equalsIgnoreCase("village")
                        || addressField.equalsIgnoreCase("cityVillage") || addressField.equalsIgnoreCase("city_village")) {
                    ad.setCityVillage(value);
                } else if (addressField.equalsIgnoreCase("state") || addressField.equalsIgnoreCase("state_province") || addressField.equalsIgnoreCase("stateProvince")) {
                    ad.setStateProvince(value);
                } else if (addressField.equalsIgnoreCase("country")) {
                    ad.setCountry(value);
                } else {
                    ad.addAddressField(addressField, value);
                }
                addresses.put(addressType, ad);
            }
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
    }

    // Helper functions

    private static JSONArray fields(JSONObject jsonForm) {
        try {

            JSONObject step1 = jsonForm.has(STEP1) ? jsonForm.getJSONObject(STEP1) : null;
            if (step1 == null) {
                return null;
            }

            return step1.has(FIELDS) ? step1.getJSONArray(FIELDS) : null;

        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    private static String getFieldValue(JSONArray jsonArray, String entity, String entityId) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            String entityVal = getString(jsonObject, OPENMRS_ENTITY);
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
            if (entityVal != null && entityVal.equals(entity) && entityIdVal != null && entityIdVal.equals(entityId)) {
                return getString(jsonObject, VALUE);
            }
        }
        return null;
    }

    private static String getFieldValue(JSONArray jsonArray, String key) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            String keyVal = getString(jsonObject, KEY);
            if (keyVal != null && keyVal.equals(key)) {
                return getString(jsonObject, VALUE);
            }
        }
        return null;
    }

    private static JSONObject getJSONObject(JSONArray jsonArray, int index) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        try {
            return jsonArray.getJSONObject(index);
        } catch (JSONException e) {
            return null;

        }
    }

    private static JSONArray getJSONArray(JSONObject jsonObject, String field) {
        if (jsonObject == null || jsonObject.length() == 0) {
            return null;
        }

        try {
            return jsonObject.getJSONArray(field);
        } catch (JSONException e) {
            return null;

        }
    }

    private static JSONObject getJSONObject(JSONObject jsonObject, String field) {
        if (jsonObject == null || jsonObject.length() == 0) {
            return null;
        }

        try {
            return jsonObject.getJSONObject(field);
        } catch (JSONException e) {
            return null;

        }
    }

    private static String getString(JSONObject jsonObject, String field) {
        if (jsonObject == null) {
            return null;
        }

        try {
            return jsonObject.has(field) ? jsonObject.getString(field) : null;
        } catch (JSONException e) {
            return null;

        }
    }

    private static DateTime formatDate(String dateString, boolean startOfToday) {
        try {
            if (StringUtils.isBlank(dateString)) {
                return null;
            }
            DateTime date = new DateTime(FORM_DATE.parse(dateString));
            if (startOfToday) {
                date.withTimeAtStartOfDay();
            }
            return date;
        } catch (ParseException e) {
            Log.e(TAG, "", e);
            return null;
        }
    }

    private static String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static void addToJSONObject(JSONObject jsonObject, String key, String value) {
        try {
            if (jsonObject == null) {
                return;
            }

            jsonObject.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
    }

/*    // TODO ADD Subform
    public Client createSubformClient(SubformMap subf) throws ParseException {
        String firstName = subf.getFieldValue(getFieldName(FormEntityConstants.Person.first_name, subf));
        String gender = subf.getFieldValue(getFieldName(FormEntityConstants.Person.gender, subf));
        String bb = subf.getFieldValue(getFieldName(FormEntityConstants.Person.birthdate, subf));

        Map<String, String> idents = extractIdentifiers(subf);
        //these bunch of lines are making it impossible to create a child model since a child doesnt have a firstname but only gender
//        if (StringUtils.isEmpty(firstName)
//                && StringUtils.isEmpty(bb)
//                && idents.size() < 1 && StringUtils.isEmpty(gender)) {//we need to ignore uuid of entity
//            // if empty repeat group leave this entry and move to next
//            return null;
//        }

        String middleName = subf.getFieldValue(getFieldName(FormEntityConstants.Person.middle_name, subf));
        String lastName = subf.getFieldValue(getFieldName(FormEntityConstants.Person.last_name, subf));
        DateTime birthdate = new DateTime(bb).withTimeAtStartOfDay();
        String dd = subf.getFieldValue(getFieldName(FormEntityConstants.Person.deathdate, subf));
        DateTime deathdate = dd == null ? null : new DateTime(dd).withTimeAtStartOfDay();
        String aproxbd = subf.getFieldValue(getFieldName(FormEntityConstants.Person.birthdate_estimated, subf));
        Boolean birthdateApprox = false;
        if (!StringUtils.isEmpty(aproxbd) && NumberUtils.isNumber(aproxbd)) {
            int bde = 0;
            try {
                bde = Integer.parseInt(aproxbd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            birthdateApprox = bde > 0 ? true : false;
        }
        String aproxdd = subf.getFieldValue(getFieldName(FormEntityConstants.Person.deathdate_estimated, subf));
        Boolean deathdateApprox = false;
        if (!StringUtils.isEmpty(aproxdd) && NumberUtils.isNumber(aproxdd)) {
            int dde = 0;
            try {
                dde = Integer.parseInt(aproxdd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            deathdateApprox = dde > 0 ? true : false;
        }

        List<Address> addresses = new ArrayList<>(extractAddressesForSubform(subf).values());

        Client c = (Client) new Client(subf.getFieldValue("id"))
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withBirthdate(new DateTime(birthdate).toDate(), birthdateApprox)
                .withDeathdate(new DateTime(deathdate).toDate(), deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withAddresses(addresses)
                .withAttributes(extractAttributes(subf))
                .withIdentifiers(idents);


        addRelationship(subf, c);

        return c;
    }

    Map<String, String> extractIdentifiers(SubformMap subf) {
        Map<String, String> pids = new HashMap<>();
        fillIdentifiers(pids, subf.fields());
        return pids;
    }

    Map<String, Object> extractAttributes(SubformMap subf) {
        Map<String, Object> pattributes = new HashMap<>();
        fillAttributes(pattributes, subf.fields());
        return pattributes;
    }


    private void addRelationship(SubformMap subformMap, Client client) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, mContext);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field")) {
                    //is this a new child registration, add person relationships -mother
                    if (subformMap.getField(rObject.getString("field")) != null) {

                        client.addRelationship(rObject.getString("client_relationship"), subformMap.getField(rObject.getString("field")).value());

                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    } */
}
