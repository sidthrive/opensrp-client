package org.ei.opensrp.path.repository;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.db.Address;
import org.ei.opensrp.path.db.Client;
import org.ei.opensrp.path.db.Column;
import org.ei.opensrp.path.db.ColumnAttribute;
import org.ei.opensrp.path.db.Event;
import org.ei.opensrp.path.db.Obs;
import org.ei.opensrp.repository.Repository;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import util.PathConstants;
import util.Utils;

public class PathRepository extends Repository {

    private static final String TAG = PathRepository.class.getCanonicalName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;

    public PathRepository(Context context) {
        super(context, PathConstants.DATABASE_NAME, PathConstants.DATABASE_VERSION, org.ei.opensrp.Context.getInstance().session(), VaccinatorApplication.createCommonFtsObject(), org.ei.opensrp.Context.getInstance().sharedRepositoriesArray());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        createTable(database, Table.client, client_column.values());
        createTable(database, Table.address, address_column.values());
        createTable(database, Table.event, event_column.values());
        createTable(database, Table.obs, obs_column.values());
        UniqueIdRepository.createTable(database);
        WeightRepository.createTable(database);
        VaccineRepository.createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PathRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        //db.execSQL("DROP TABLE IF EXISTS " + SmsTarseelTables.unsubmitted_outbound);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(VaccinatorApplication.getInstance().getPassword());
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(VaccinatorApplication.getInstance().getPassword());
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
       try {
           if (readableDatabase == null || !readableDatabase.isOpen()) {
               if (readableDatabase != null) {
                   readableDatabase.close();
               }
               readableDatabase = super.getReadableDatabase(password);
           }
           return readableDatabase;
       }catch(Exception e){
           Log.e(TAG,"Database Error. "+e.getMessage());
           return null;
       }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (writableDatabase == null || !writableDatabase.isOpen()) {
            if (writableDatabase != null) {
                writableDatabase.close();
            }
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }

    private void insert(SQLiteDatabase db, Class<?> cls, Table table, Column[] cols, Object o) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        insert(db, cls, table, cols, null, null, o);
    }

    private void insert(SQLiteDatabase db, Class<?> cls, Table table, Column[] cols, String referenceColumn, String referenceValue, Object o) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        Map<Column, Object> fm = new HashMap<Column, Object>();

        for (Column c : cols) {
            if (c.name().equalsIgnoreCase(referenceColumn)) {
                continue;//skip reference column as it is already appended
            }
            Field f = null;
            try {
                f = cls.getDeclaredField(c.name());// 1st level
            } catch (NoSuchFieldException e) {
                try {
                    f = cls.getSuperclass().getDeclaredField(c.name()); // 2nd level
                } catch (NoSuchFieldException e2) {
                    continue;
                }
            }

            f.setAccessible(true);
            Object v = f.get(o);
            fm.put(c, v);
        }

        String columns = referenceColumn == null ? "" : ("`" + referenceColumn + "`,");
        String values = referenceColumn == null ? "" : ("'" + referenceValue + "',");
        for (Column c : fm.keySet()) {
            columns += "`" + c.name() + "`,";
            values += formatValue(fm.get(c), c.column()) + ",";
        }

        columns = removeEndingComma(columns);
        values = removeEndingComma(values);

        String sql = "INSERT INTO " + table.name() + " (" + columns + ") VALUES (" + values + ")";
        Log.i("", sql);
        db.execSQL(sql);
    }

    public void insert(SQLiteDatabase db, Client client) {
        try {
            JSONObject jsonClient = getClient(db, client.getBaseEntityId());
            if (jsonClient != null) {
                return;
            }
            insert(db, Client.class, Table.client, client_column.values(), client);
            for (Address a : client.getAddresses()) {
                insert(db, Address.class, Table.address, address_column.values(), address_column.baseEntityId.name(), client.getBaseEntityId(), a);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
        }
    }

    public void insert(SQLiteDatabase db, Event event) {
        try {
            if (StringUtils.isBlank(event.getFormSubmissionId())) {
                event.setFormSubmissionId(generateRandomUUIDString());
            }
            insert(db, Event.class, Table.event, event_column.values(), event);
            for (Obs o : event.getObs()) {
                insert(db, Obs.class, Table.obs, obs_column.values(), obs_column.formSubmissionId.name(), event.getFormSubmissionId(), o);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
        }
    }

    public long batchInsertClients(JSONArray array) throws Exception {
        if (array == null || array.length() == 0) {
            return 0l;
        }

        long lastServerVersion = 0l;

        getWritableDatabase().beginTransaction();

        for (int i = 0; i < array.length(); i++) {
            Object o = array.get(i);
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                Client c = convert(jo, Client.class);
                if (c != null) {
                    insert(getWritableDatabase(), c);
                    if (c.getServerVersion() > 01) {
                        lastServerVersion = c.getServerVersion();
                    }
                }
            }
        }

        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
        return lastServerVersion;
    }

    public long batchInsertEvents(JSONArray array, long serverVersion) throws Exception {
        if (array == null || array.length() == 0) {
            return 0l;
        }

        long lastServerVersion = serverVersion;

        getWritableDatabase().beginTransaction();

        for (int i = 0; i < array.length(); i++) {
            Object o = array.get(i);
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                Event e = convert(jo, Event.class);
                if (e != null) {
                    insert(getWritableDatabase(), e);
                    if (e.getServerVersion() > 01) {
                        lastServerVersion = e.getServerVersion();
                    }
                }
            }
        }

        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
        return lastServerVersion;
    }

    private <T> T convert(JSONObject jo, Class<T> t) {
        if (jo == null) {
            return null;
        }
        try {
            return Utils.getLongDateAwareGson().fromJson(jo.toString(), t);
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            Log.e(getClass().getName(), "Unable to convert: " + jo.toString());
            return null;
        }
    }

    public List<JSONObject> getEvents(long startServerVersion, long lastServerVersion) throws JSONException, ParseException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery("SELECT * FROM " + Table.event.name() +
                    " WHERE " + event_column.serverVersion.name() + " > " + startServerVersion +
                    " AND " + event_column.serverVersion.name() + " <= " + lastServerVersion +
                    " ORDER BY " + event_column.serverVersion.name()
                    , null);
            while (cursor.moveToNext()) {
                JSONObject ev = new JSONObject();
                for (Column ec : Table.event.columns()) {
                    ev.put(ec.name(), getValue(cursor, ec));
                }

                JSONArray olist = new JSONArray();
                Cursor cursorObs = null;
                try {
                    cursorObs = getWritableDatabase().rawQuery("SELECT * FROM " + Table.obs.name() + " WHERE " + obs_column.formSubmissionId.name() + "='" + ev.getString(event_column.formSubmissionId.name()) + "'", null);
                    while (cursorObs.moveToNext()) {
                        JSONObject o = new JSONObject();
                        for (Column oc : Table.obs.columns()) {
                            if (!oc.name().equalsIgnoreCase(event_column.formSubmissionId.name())) {//skip reference column
                                o.put(oc.name(), getValue(cursorObs, oc));
                            }
                        }
                        olist.put(o);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    if (cursorObs != null)
                        cursorObs.close();
                }
                ev.put("obs", olist);

                if (ev.has(event_column.baseEntityId.name())) {
                    String baseEntityId = ev.getString(event_column.baseEntityId.name());
                    JSONObject cl = getClient(getWritableDatabase(), baseEntityId);
                    ev.put("client", cl);
                }
                list.add(ev);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return list;
    }

    public JSONObject getClient(SQLiteDatabase db, String baseEntityId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + Table.client.name() +
                    " WHERE " + client_column.baseEntityId.name() + "='" + baseEntityId + "' ", null);
            if (cursor.moveToNext()) {
                JSONObject cl = new JSONObject();
                for (Column cc : Table.client.columns()) {
                    cl.put(cc.name(), getValue(cursor, cc));
                }

                JSONArray alist = new JSONArray();
                Cursor ares = null;
                try {
                    ares = db.rawQuery("SELECT * FROM " + Table.address.name() + " WHERE " + address_column.baseEntityId.name() + "='" + cl.getString(client_column.baseEntityId.name()) + "'", null);
                    while (ares.moveToNext()) {
                        JSONObject a = new JSONObject();
                        for (Column cc : Table.address.columns()) {
                            if (!cc.name().equalsIgnoreCase(client_column.baseEntityId.name())) {//skip reference column
                                a.put(cc.name(), getValue(ares, cc));
                            }
                        }
                        alist.put(a);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    if (ares != null)
                        ares.close();
                }

                cl.put("addresses", alist);
                
                return cl;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    private String getCreateTableColumn(Column col) {
        ColumnAttribute c = col.column();
        return "`" + col.name() + "` " + getSqliteType(c.type()) + (c.pk() ? " PRIMARY KEY " : "");
    }

    private String removeEndingComma(String str) {
        if (str.trim().endsWith(",")) {
            return str.substring(0, str.lastIndexOf(","));
        }
        return str;
    }

    private void createTable(SQLiteDatabase db, Table table, Column[] columns) {
        String cl = "";
        String indl = "";
        for (Column cc : columns) {
            cl += getCreateTableColumn(cc) + ",";
            if (cc.column().index()) {
                indl += cc.name() + ",";
            }
        }
        cl = removeEndingComma(cl);
        indl = removeEndingComma(indl);
        String create_tb = "CREATE TABLE " + table.name() + " ( " + cl + " )";
        String create_id = "CREATE INDEX " + table.name() + "_index ON " + table.name() + " (" + indl + "); ";

        db.execSQL(create_tb);
        db.execSQL(create_id);
    }

    private Object getValue(Cursor cur, Column c) throws JSONException, ParseException {
        int ind = cur.getColumnIndex(c.name());
        if (cur.isNull(ind)) {
            return null;
        }

        ColumnAttribute.Type type = c.column().type();
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.text.name())) {
            return "" + cur.getString(ind) + "";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.bool.name())) {
            return cur.getInt(ind) == 0 ? false : true;
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.date.name())) {
            return new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cur.getString(ind)).getTime());
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.list.name())) {
            return new JSONArray(cur.getString(ind));
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.map.name())) {
            return new JSONObject(cur.getString(ind));
        }

        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.longnum.name())) {
            return cur.getLong(ind);
        }

        return null;
    }

    private String formatValue(Object v, ColumnAttribute c) {
        if (v == null || v.toString().trim().equalsIgnoreCase("")) {
            return null;
        }

        ColumnAttribute.Type type = c.type();
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.text.name())) {
            return "'" + v.toString() + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.bool.name())) {
            return (Boolean.valueOf(v.toString()) ? 1 : 0) + "";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.date.name())) {
            return "'" + getSQLDate((DateTime) v) + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.list.name())) {
            return "'" + new Gson().toJson(v) + "'";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.map.name())) {
            return "'" + new Gson().toJson(v) + "'";
        }

        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.longnum.name())) {
            return v.toString();
        }
        return null;
    }

    private String getSQLDate(DateTime date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date.toDate());
        } finally {

        }
    }

    public ArrayList<HashMap<String, String>> rawQuery(SQLiteDatabase db, String query) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        map.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    maplist.add(map);
                } while (cursor.moveToNext());
            }
            db.close();
            // return contact list
            return maplist;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    // Definitions
    private enum Table {
        client(client_column.values()), event(event_column.values()),
        address(address_column.values()), obs(obs_column.values());
        private Column[] columns;

        public Column[] columns() {
            return columns;
        }

        private Table(Column[] columns) {
            this.columns = columns;
        }
    }


    public enum client_column implements Column {
        creator(ColumnAttribute.Type.text, false, false),
        dateCreated(ColumnAttribute.Type.date, false, true),
        editor(ColumnAttribute.Type.text, false, false),
        dateEdited(ColumnAttribute.Type.date, false, true),
        voided(ColumnAttribute.Type.bool, false, false),
        dateVoided(ColumnAttribute.Type.date, false, false),
        voider(ColumnAttribute.Type.text, false, false),
        voidReason(ColumnAttribute.Type.text, false, false),

        baseEntityId(ColumnAttribute.Type.text, true, true),
        identifiers(ColumnAttribute.Type.map, false, true),
        attributes(ColumnAttribute.Type.map, false, true),
        firstName(ColumnAttribute.Type.text, false, false),
        middleName(ColumnAttribute.Type.text, false, false),
        lastName(ColumnAttribute.Type.text, false, false),
        birthdate(ColumnAttribute.Type.date, false, false),
        deathdate(ColumnAttribute.Type.date, false, false),
        birthdateApprox(ColumnAttribute.Type.bool, false, false),
        deathdateApprox(ColumnAttribute.Type.bool, false, false),
        gender(ColumnAttribute.Type.text, false, false),
        relationships(ColumnAttribute.Type.map, false, false),
        serverVersion(ColumnAttribute.Type.longnum, false, true);

        private client_column(ColumnAttribute.Type type, boolean pk, boolean index) {
            this.column = new ColumnAttribute(type, pk, index);
        }

        private ColumnAttribute column;

        public ColumnAttribute column() {
            return column;
        }
    }

    public enum address_column implements Column {
        baseEntityId(ColumnAttribute.Type.text, false, true),
        addressType(ColumnAttribute.Type.text, false, true),
        startDate(ColumnAttribute.Type.date, false, false),
        endDate(ColumnAttribute.Type.date, false, false),
        addressFields(ColumnAttribute.Type.map, false, false),
        latitude(ColumnAttribute.Type.text, false, false),
        longitude(ColumnAttribute.Type.text, false, false),
        geopoint(ColumnAttribute.Type.text, false, false),
        postalCode(ColumnAttribute.Type.text, false, false),
        subTown(ColumnAttribute.Type.text, false, false),
        town(ColumnAttribute.Type.text, false, false),
        subDistrict(ColumnAttribute.Type.text, false, false),
        countyDistrict(ColumnAttribute.Type.text, false, false),
        cityVillage(ColumnAttribute.Type.text, false, false),
        stateProvince(ColumnAttribute.Type.text, false, false),
        country(ColumnAttribute.Type.text, false, false);

        private address_column(ColumnAttribute.Type type, boolean pk, boolean index) {
            this.column = new ColumnAttribute(type, pk, index);
        }

        private ColumnAttribute column;

        public ColumnAttribute column() {
            return column;
        }
    }

    public enum event_column implements Column {
        creator(ColumnAttribute.Type.text, false, false),
        dateCreated(ColumnAttribute.Type.date, false, true),
        editor(ColumnAttribute.Type.text, false, false),
        dateEdited(ColumnAttribute.Type.date, false, false),
        voided(ColumnAttribute.Type.bool, false, false),
        dateVoided(ColumnAttribute.Type.date, false, false),
        voider(ColumnAttribute.Type.text, false, false),
        voidReason(ColumnAttribute.Type.text, false, false),

        eventId(ColumnAttribute.Type.text, true, false),
        baseEntityId(ColumnAttribute.Type.text, false, true),
        locationId(ColumnAttribute.Type.text, false, false),
        eventDate(ColumnAttribute.Type.date, false, true),
        eventType(ColumnAttribute.Type.text, false, true),
        formSubmissionId(ColumnAttribute.Type.text, false, false),
        providerId(ColumnAttribute.Type.text, false, false),
        entityType(ColumnAttribute.Type.text, false, false),
        details(ColumnAttribute.Type.map, false, false),
        version(ColumnAttribute.Type.text, false, false),
        serverVersion(ColumnAttribute.Type.longnum, false, true);

        private event_column(ColumnAttribute.Type type, boolean pk, boolean index) {
            this.column = new ColumnAttribute(type, pk, index);
        }

        private ColumnAttribute column;

        public ColumnAttribute column() {
            return column;
        }
    }

    public enum obs_column implements Column {
        formSubmissionId(ColumnAttribute.Type.text, false, true),
        fieldType(ColumnAttribute.Type.text, false, false),
        fieldDataType(ColumnAttribute.Type.text, false, false),
        fieldCode(ColumnAttribute.Type.text, false, false),
        parentCode(ColumnAttribute.Type.text, false, false),
        values(ColumnAttribute.Type.list, false, false),
        comments(ColumnAttribute.Type.text, false, false),
        formSubmissionField(ColumnAttribute.Type.text, false, true);

        private obs_column(ColumnAttribute.Type type, boolean pk, boolean index) {
            this.column = new ColumnAttribute(type, pk, index);
        }

        private ColumnAttribute column;

        public ColumnAttribute column() {
            return column;
        }
    }

    private String getSqliteType(ColumnAttribute.Type type) {
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.text.name())) {
            return "varchar";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.bool.name())) {
            return "boolean";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.date.name())) {
            return "datetime";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.list.name())) {
            return "varchar";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.map.name())) {
            return "varchar";
        }
        if (type.name().equalsIgnoreCase(ColumnAttribute.Type.longnum.name())) {
            return "integer";
        }
        return null;
    }

    private String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }
}
