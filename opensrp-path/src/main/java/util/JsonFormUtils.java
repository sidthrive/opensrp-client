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
import org.ei.opensrp.util.AssetHandler;
import org.ei.opensrp.util.FormUtils;
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
    public static final String ZEIR_ID = "ZEIR_ID";
    public static final String M_ZEIR_ID = "M_ZEIR_ID";


    public static final SimpleDateFormat FORM_DATE = new SimpleDateFormat("dd-MM-yyyy");

    public static void save(Context context, String jsonString, String providerId, String imageKey, String bindType, String subBindType) {
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

            Client s = null;

            if (StringUtils.isNotBlank(subBindType)) {
                s = JsonFormUtils.createSubformClient(context, fields, c, subBindType);
            }

            Event se = null;
            if (s != null && e != null) {
                JSONObject subBindTypeJson = getJSONObject(jsonForm, subBindType);
                if (subBindTypeJson != null) {
                    String subBindTypeEncounter = getString(subBindTypeJson, ENCOUNTER_TYPE);
                    if (StringUtils.isNotBlank(subBindTypeEncounter)) {
                        se = JsonFormUtils.createSubFormEvent(null, metadata, e, s.getBaseEntityId(), subBindTypeEncounter, providerId, subBindType);

                    }
                }
            }

            CloudantDataHandler cloudantDataHandler = CloudantDataHandler.getInstance(context.getApplicationContext());

            if (c != null) {
                org.ei.opensrp.cloudant.models.Client client = new org.ei.opensrp.cloudant.models.Client(c);
                cloudantDataHandler.createClientDocument(client);
            }

            if (e != null) {
                org.ei.opensrp.cloudant.models.Event event = new org.ei.opensrp.cloudant.models.Event(e);
                cloudantDataHandler.createEventDocument(event);
            }

            if (s != null) {
                org.ei.opensrp.cloudant.models.Client client = new org.ei.opensrp.cloudant.models.Client(s);
                cloudantDataHandler.createClientDocument(client);

            }

            if (se != null) {
                org.ei.opensrp.cloudant.models.Event event = new org.ei.opensrp.cloudant.models.Event(se);
                cloudantDataHandler.createEventDocument(event);
            }

            String zeirId = c.getIdentifier(ZEIR_ID);
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

        String firstName = getFieldValue(fields, FormEntityConstants.Person.first_name);
        String middleName = getFieldValue(fields, FormEntityConstants.Person.middle_name);
        String lastName = getFieldValue(fields, FormEntityConstants.Person.last_name);
        String bd = getFieldValue(fields, FormEntityConstants.Person.birthdate);
        DateTime birthdate = formatDate(bd, true);
        String dd = getFieldValue(fields, FormEntityConstants.Person.deathdate);
        DateTime deathdate = formatDate(dd, true);
        String aproxbd = getFieldValue(fields, FormEntityConstants.Person.birthdate_estimated);
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
        String aproxdd = getFieldValue(fields, FormEntityConstants.Person.deathdate_estimated);
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
        String gender = getFieldValue(fields, FormEntityConstants.Person.gender);

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

        String encounterDateField = getFieldValue(fields, FormEntityConstants.Encounter.encounter_date);
        String encounterLocation = getFieldValue(fields, FormEntityConstants.Encounter.location_id);

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

        if (StringUtils.isNotBlank(getString(jsonObject, ENTITY_ID))) {
            return;
        }

        String entity = PERSON_INDENTIFIER;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);

            if (entityIdVal.equals(ZEIR_ID) && StringUtils.isNotBlank(value) && !value.contains("-")) {
                StringBuilder stringBuilder = new StringBuilder(value);
                stringBuilder.insert(value.length() - 1, '-');
                value = stringBuilder.toString();
            }

            pids.put(entityIdVal, value);
        }


    }


    private static void fillAttributes(Map<String, Object> pattributes, JSONObject jsonObject) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }

        if (StringUtils.isNotBlank(getString(jsonObject, ENTITY_ID))) {
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

            if (StringUtils.isNotBlank(getString(jsonObject, ENTITY_ID))) {
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

    private static String getFieldValue(JSONArray jsonArray, FormEntityConstants.Person person) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        if (person == null) {
            return null;
        }

        return value(jsonArray, person.entity(), person.entityId());
    }

    private static String getFieldValue(JSONArray jsonArray, FormEntityConstants.Encounter encounter) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        if (encounter == null) {
            return null;
        }

        return value(jsonArray, encounter.entity(), encounter.entityId());
    }

    private static String value(JSONArray jsonArray, String entity, String entityId) {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            if (StringUtils.isNotBlank(getString(jsonObject, ENTITY_ID))) {
                continue;
            }
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

    public static Client createSubformClient(Context context, JSONArray fields, Client parent, String bindType) throws ParseException {

        if (StringUtils.isBlank(bindType)) {
            return null;
        }

        String entityId = generateRandomUUIDString();
        String firstName = getSubFormFieldValue(fields, FormEntityConstants.Person.first_name, bindType);
        String gender = getSubFormFieldValue(fields, FormEntityConstants.Person.gender, bindType);
        String bb = getSubFormFieldValue(fields, FormEntityConstants.Person.birthdate, bindType);

        Map<String, String> idents = extractIdentifiers(fields, bindType);
        String parentIdentifier = parent.getIdentifier(ZEIR_ID);
        if (StringUtils.isNotBlank(parentIdentifier)) {
            String identifier = parentIdentifier.concat("_").concat(bindType);
            idents.put(M_ZEIR_ID, identifier);
        }

        String middleName = getSubFormFieldValue(fields, FormEntityConstants.Person.middle_name, bindType);
        String lastName = getSubFormFieldValue(fields, FormEntityConstants.Person.last_name, bindType);
        DateTime birthdate = formatDate(bb, true);
        String dd = getSubFormFieldValue(fields, FormEntityConstants.Person.deathdate, bindType);
        DateTime deathdate = formatDate(dd, true);
        String aproxbd = getSubFormFieldValue(fields, FormEntityConstants.Person.birthdate_estimated, bindType);
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
        String aproxdd = getSubFormFieldValue(fields, FormEntityConstants.Person.deathdate_estimated, bindType);
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

        List<Address> addresses = new ArrayList<>(extractAddresses(fields, bindType).values());

        Client c = (Client) new Client(entityId)
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withBirthdate(new DateTime(birthdate).toDate(), birthdateApprox)
                .withDeathdate(new DateTime(deathdate).toDate(), deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withAddresses(addresses)
                .withAttributes(extractAttributes(fields, bindType))
                .withIdentifiers(idents);

        if(addresses.isEmpty()){
            c.withAddresses(parent.getAddresses());
        }

        addRelationship(context, c, parent);

        return c;
    }

    public static Event createSubFormEvent(JSONArray fields, JSONObject metadata, Event parent, String entityId, String encounterType, String providerId, String bindType) {


        Event e = (Event) new Event()
                .withBaseEntityId(entityId)//should be different for main and subform
                .withEventDate(parent.getEventDate())
                .withEventType(encounterType)
                .withLocationId(parent.getLocationId())
                .withProviderId(providerId)
                .withEntityType(bindType)
                .withFormSubmissionId(generateRandomUUIDString())
                .withDateCreated(new Date());

        if (fields != null && fields.length() != 0)
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


    private static Map<String, String> extractIdentifiers(JSONArray fields, String bindType) {
        Map<String, String> pids = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillSubFormIdentifiers(pids, jsonObject, bindType);
        }
        return pids;
    }

    private static Map<String, Object> extractAttributes(JSONArray fields, String bindType) {
        Map<String, Object> pattributes = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillSubFormAttributes(pattributes, jsonObject, bindType);
        }
        return pattributes;
    }

    private static Map<String, Address> extractAddresses(JSONArray fields, String bindType) {
        Map<String, Address> paddr = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillSubFormAddressFields(jsonObject, paddr, bindType);
        }
        return paddr;
    }


    private static void addRelationship(Context context, Client parent, Client child) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, context);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field") && getString(rObject, "field").equals(ENTITY_ID)) {
                    child.addRelationship(rObject.getString("client_relationship"), parent.getBaseEntityId());
                } else {
                    //TODO how to add other kind of relationships
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private static String getSubFormFieldValue(JSONArray jsonArray, FormEntityConstants.Person person, String bindType) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        if (person == null) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = getJSONObject(jsonArray, i);
            String bind = getString(jsonObject, ENTITY_ID);
            if (bind == null || !bind.equals(bindType)) {
                continue;
            }
            String entityVal = getString(jsonObject, OPENMRS_ENTITY);
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
            if (entityVal != null && entityVal.equals(person.entity()) && entityIdVal != null && entityIdVal.equals(person.name())) {
                return getString(jsonObject, VALUE);
            }

        }
        return null;
    }

    private static void fillSubFormIdentifiers(Map<String, String> pids, JSONObject jsonObject, String bindType) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }

        String bind = getString(jsonObject, ENTITY_ID);
        if (bind == null || !bind.equals(bindType)) {
            return;
        }

        String entity = PERSON_INDENTIFIER;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);

            if (entityIdVal.equals(ZEIR_ID) && StringUtils.isNotBlank(value) && !value.contains("-")) {
                StringBuilder stringBuilder = new StringBuilder(value);
                stringBuilder.insert(value.length() - 1, '-');
                value = stringBuilder.toString();
            }

            pids.put(entityIdVal, value);
        }
    }

    private static void fillSubFormAttributes(Map<String, Object> pattributes, JSONObject jsonObject, String bindType) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }

        String bind = getString(jsonObject, ENTITY_ID);
        if (bind == null || !bind.equals(bindType)) {
            return;
        }

        String entity = PERSON_ATTRIBUTE;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
            pattributes.put(entityIdVal, value);
        }
    }

    private static void fillSubFormAddressFields(JSONObject jsonObject, Map<String, Address> addresses, String bindType) {

        if (jsonObject == null) {
            return;
        }

        try {
            String value = getString(jsonObject, VALUE);
            if (StringUtils.isBlank(value)) {
                return;
            }

            String bind = getString(jsonObject, ENTITY_ID);
            if (bind == null || !bind.equals(bindType)) {
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

    public static JSONArray generateLocationHierarchyTree(org.ei.opensrp.Context context, boolean withOtherOption, ArrayList<String> allowedLevels) {
        JSONArray array = new JSONArray();
        try {
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has("locationsHierarchy")
                    && locationData.getJSONObject("locationsHierarchy").has("map")) {
                JSONObject map = locationData.getJSONObject("locationsHierarchy").getJSONObject("map");
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    getFormJsonData(array, map.getJSONObject(curKey), allowedLevels);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (withOtherOption) {
            try {
                JSONObject other = new JSONObject();
                other.put("name", "Other");
                other.put("level", "");
                array.put(other);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return array;
    }

    public static JSONArray generateDefaultLocationHierarchy(org.ei.opensrp.Context context, ArrayList<String> allowedLevels) {
        try {
            String defaultLocationUuid = context.allSharedPreferences()
                    .fetchDefaultLocalityId(context.allSharedPreferences().fetchRegisteredANM());
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has("locationsHierarchy")
                    && locationData.getJSONObject("locationsHierarchy").has("map")) {
                JSONObject map = locationData.getJSONObject("locationsHierarchy").getJSONObject("map");
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    JSONArray curResult = getDefaultLocationHierarchy(defaultLocationUuid, map.getJSONObject(curKey), new JSONArray(), allowedLevels);
                    if (curResult != null) {
                        return curResult;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    private static JSONArray getDefaultLocationHierarchy(String defaultLocationUuid, JSONObject openMrsLocationData, JSONArray parents, ArrayList<String> allowedLevels) throws JSONException {
        if(allowedLevels.contains(openMrsLocationData.getJSONObject("node").getJSONArray("tags").getString(0))) {
            parents.put(openMrsLocationData.getJSONObject("node").getString("name"));
        }

        if (openMrsLocationData.getJSONObject("node").getString("locationId").equals(defaultLocationUuid)) {
            return parents;
        }

        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                JSONArray curResult = getDefaultLocationHierarchy(defaultLocationUuid, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), new JSONArray(parents.toString()), allowedLevels);
                if (curResult != null) return curResult;
            }
        }

        return null;
    }

    private static void getFormJsonData(JSONArray allLocationData, JSONObject openMrsLocationData, ArrayList<String> allowedLevels) throws JSONException {
        JSONObject jsonFormObject = new JSONObject();
        jsonFormObject.put("name", openMrsLocationData.getJSONObject("node").getString("name"));
        String level = "";
        try {
            level = openMrsLocationData.getJSONObject("node").getJSONArray("tags").getString(0);
        } catch (JSONException e) {
        }
        jsonFormObject.put("level", "");
        JSONArray children = new JSONArray();
        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                getFormJsonData(children, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), allowedLevels);
            }
            if(allowedLevels.contains(level)) {
                jsonFormObject.put("nodes", children);
            } else {
                for(int i = 0; i < children.length(); i++) {
                    allLocationData.put(children.getJSONObject(i));
                }
            }
        }
        if(allowedLevels.contains(level)) {
            allLocationData.put(jsonFormObject);
        }
    }

    public static void addChildRegLocHierarchyQuestions(JSONObject form, org.ei.opensrp.Context context) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("Province");
            allLevels.add("District");
            allLevels.add("Health Facility");
            allLevels.add("Zone");

            ArrayList<String> healthFacilities = new ArrayList<>();
            healthFacilities.add("Country");
            healthFacilities.add("Province");
            healthFacilities.add("District");
            healthFacilities.add("Health Facility");

            JSONArray defaultLocation = generateDefaultLocationHierarchy(context, allLevels);
            JSONArray defaultFacility = generateDefaultLocationHierarchy(context, healthFacilities);
            JSONArray upToFacilities = generateLocationHierarchyTree(context, false, healthFacilities);
            JSONArray entireTree = generateLocationHierarchyTree(context, true, allLevels);

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")
                        || questions.getJSONObject(i).getString("key").equals("Birth_Facility_Name")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(upToFacilities.toString()));
                    if (defaultFacility != null) {
                        questions.getJSONObject(i).put("default", defaultFacility.toString());
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Residential_Area")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(entireTree.toString()));
                    if (defaultLocation != null) {
                        questions.getJSONObject(i).put("default", defaultLocation.toString());
                    }
                }
            }
        } catch (JSONException e) {
            //Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
