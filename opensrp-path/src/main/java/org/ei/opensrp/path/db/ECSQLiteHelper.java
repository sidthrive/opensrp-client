package org.ei.opensrp.path.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
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

import util.JsonFormUtils;
import util.Utils;

public class ECSQLiteHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "opensrp.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;

    public ECSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private SQLiteDatabase getDatabase() {
        if (database == null) {
            database = this.getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        this.database = database;
        createTable(Table.client, client_column.values());
        createTable(Table.address, address_column.values());
        createTable(Table.event, event_column.values());
        createTable(Table.obs, obs_column.values());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ECSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        //db.execSQL("DROP TABLE IF EXISTS " + SmsTarseelTables.unsubmitted_outbound);
    }

	/*public void open() throws SQLException {
        if(database == null){
			database = super.getWritableDatabase();
		}
	}*/

	/*public void close() {
        super.close();
	}*/


    private void insert(Class<?> cls, Table table, Column[] cols, Object o) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        insert(cls, table, cols, null, null, o);
    }

    private void insert(Class<?> cls, Table table, Column[] cols, String referenceColumn, String referenceValue, Object o) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
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
        getDatabase().execSQL(sql);
    }

    public void insert(Client client) {
        try {
            JSONObject jsonClient = getClient(client.getBaseEntityId());
            if (jsonClient != null) {
                return;
            }
            insert(Client.class, Table.client, client_column.values(), client);
            for (Address a : client.getAddresses()) {
                insert(Address.class, Table.address, address_column.values(), address_column.baseEntityId.name(), client.getBaseEntityId(), a);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
        }
    }

    public void insert(Event event) {
        try {
            if(StringUtils.isBlank(event.getFormSubmissionId())){
                event.setFormSubmissionId(generateRandomUUIDString());
            }
            insert(Event.class, Table.event, event_column.values(), event);
            for (Obs o : event.getObs()) {
                insert(Obs.class, Table.obs, obs_column.values(), obs_column.formSubmissionId.name(), event.getFormSubmissionId(), o);
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

        getDatabase().beginTransaction();

        for (int i = 0; i < array.length(); i++) {
            Object o = array.get(i);
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                Client c = convert(jo, Client.class);
                if (c != null) {
                    insert(c);
                    if (c.getServerVersion() > 01) {
                        lastServerVersion = c.getServerVersion();
                    }
                }
            }
        }

        getDatabase().setTransactionSuccessful();
        getDatabase().endTransaction();
        return lastServerVersion;
    }

    public long batchInsertEvents(JSONArray array, long serverVersion) throws Exception {
        if (array == null || array.length() == 0) {
            return 0l;
        }

        long lastServerVersion = serverVersion;

        getDatabase().beginTransaction();

        for (int i = 0; i < array.length(); i++) {
            Object o = array.get(i);
            if (o instanceof JSONObject) {
                JSONObject jo = (JSONObject) o;
                Event e = convert(jo, Event.class);
                if (e != null) {
                    insert(e);
                    if (e.getServerVersion() > 01) {
                        lastServerVersion = e.getServerVersion();
                    }
                }
            }
        }

        getDatabase().setTransactionSuccessful();
        getDatabase().endTransaction();
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
        try {
            Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + Table.event.name() +
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
                Cursor cursorObs = getDatabase().rawQuery("SELECT * FROM " + Table.obs.name() + " WHERE " + obs_column.formSubmissionId.name() + "='" + ev.getString(event_column.formSubmissionId.name()) + "'", null);
                while (cursorObs.moveToNext()) {
                    JSONObject o = new JSONObject();
                    for (Column oc : Table.obs.columns()) {
                        if (!oc.name().equalsIgnoreCase(event_column.formSubmissionId.name())) {//skip reference column
                            o.put(oc.name(), getValue(cursorObs, oc));
                        }
                    }
                    olist.put(o);
                }

                ev.put("obs", olist);

                if (ev.has(event_column.baseEntityId.name())) {
                    String baseEntityId = ev.getString(event_column.baseEntityId.name());
                    JSONObject cl = getClient(baseEntityId);
                    ev.put("client", cl);
                }
                Log.i(getClass().getName(), "Event Retrieved: " + ev.toString());
                list.add(ev);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        }
        return list;
    }

    public JSONObject getClient(String baseEntityId) {
        try {
            Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + Table.client.name() +
                    " WHERE " + client_column.baseEntityId.name() + "='" + baseEntityId + "' ", null);
            if (cursor.moveToNext()) {
                JSONObject cl = new JSONObject();
                for (Column cc : Table.client.columns()) {
                    cl.put(cc.name(), getValue(cursor, cc));
                }

                JSONArray alist = new JSONArray();
                Cursor ares = getDatabase().rawQuery("SELECT * FROM " + Table.address.name() + " WHERE " + address_column.baseEntityId.name() + "='" + cl.getString(client_column.baseEntityId.name()) + "'", null);
                while (ares.moveToNext()) {
                    JSONObject a = new JSONObject();
                    for (Column cc : Table.address.columns()) {
                        if (!cc.name().equalsIgnoreCase(client_column.baseEntityId.name())) {//skip reference column
                            a.put(cc.name(), getValue(ares, cc));
                        }
                    }
                    alist.put(a);
                }

                cl.put("addresses", alist);
                Log.i(getClass().getName(), "Client Retrieved: " + cl.toString());

                return cl;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
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

    private void createTable(Table table, Column[] columns) {
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

        getDatabase().execSQL(create_tb);
        getDatabase().execSQL(create_id);
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

    public ArrayList<HashMap<String, String>> rawQuery(String query) {
        Cursor cursor = getDatabase().rawQuery(query, null);
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
        getDatabase().close();
        // return contact list
        return maplist;
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
