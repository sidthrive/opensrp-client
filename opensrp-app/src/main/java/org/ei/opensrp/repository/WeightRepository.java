package org.ei.opensrp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.domain.UniqueId;
import org.ei.opensrp.domain.Weight;

import java.net.IDN;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.addAll;

public class WeightRepository extends DrishtiRepository {
    private static final String TAG = WeightRepository.class.getCanonicalName();
    private static final String WEIGHT_SQL = "CREATE TABLE weights (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,kg REAL NOT NULL,date DATETIME NOT NULL,anmid VARCHAR NULL,location_id VARCHAR NULL,sync_status VARCHAR,updated_at INTEGER NULL)";
    public static final String WEIGHT_TABLE_NAME = "weights";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String KG = "kg";
    private static final String DATE = "date";
    private static final String ANMID = "anmid";
    private static final String LOCATIONID = "location_id";
    private static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String[] WEIGHT_TABLE_COLUMNS = {ID_COLUMN, BASE_ENTITY_ID, KG, DATE, ANMID, LOCATIONID, SYNC_STATUS, UPDATED_AT_COLUMN};

    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + WEIGHT_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + WEIGHT_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String SYNC_STATUS_INDEX = "CREATE INDEX " + WEIGHT_TABLE_NAME + "_" + SYNC_STATUS + "_index ON " + WEIGHT_TABLE_NAME + "(" + SYNC_STATUS + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + WEIGHT_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + WEIGHT_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";

    public static String TYPE_Unsynced = "Unsynced";
    public static String TYPE_Synced = "Synced";

    @Override
    protected void onCreate(SQLiteDatabase database) {
        database.execSQL(WEIGHT_SQL);
        database.execSQL(BASE_ENTITY_ID_INDEX);
        database.execSQL(SYNC_STATUS_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }

    public void add(Weight weight) {
        if (weight == null) {
            return;
        }
        if (StringUtils.isBlank(weight.getSyncStatus())) {
            weight.setSyncStatus(TYPE_Unsynced);
        }


        if (weight.getUpdatedAt() == null) {
            weight.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
        }

        SQLiteDatabase database = masterRepository.getWritableDatabase();
        if (weight.getId() == null) {
            weight.setId(database.insert(WEIGHT_TABLE_NAME, null, createValuesFor(weight)));
        } else {
            String idSelection = ID_COLUMN + " = ?";
            database.update(WEIGHT_TABLE_NAME, createValuesFor(weight), idSelection, new String[]{weight.getId().toString()});
        }
        database.close();
    }

    public List<Weight> findUnSyncedBeforeTime(int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -hours);

        Long time = calendar.getTimeInMillis();

        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(WEIGHT_TABLE_NAME, WEIGHT_TABLE_COLUMNS, UPDATED_AT_COLUMN + " < ? AND " + SYNC_STATUS + " = ?", new String[]{time.toString(), TYPE_Unsynced}, null, null, null, null);
        return readAllWeights(cursor);
    }

    public Weight findUnSyncedByEntityId(String entityId) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(WEIGHT_TABLE_NAME, WEIGHT_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? AND " + SYNC_STATUS + " = ?", new String[]{entityId, TYPE_Unsynced}, null, null, null, null);
        List<Weight> weights = readAllWeights(cursor);
        if (!weights.isEmpty()) {
            return weights.get(0);
        }

        return null;
    }

    public Weight find(Long caseId) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(WEIGHT_TABLE_NAME, WEIGHT_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()}, null, null, null, null);
        List<Weight> weights = readAllWeights(cursor);
        if (!weights.isEmpty()) {
            return weights.get(0);
        }

        return null;
    }
    public List<Weight> findLast5(String entityid) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(WEIGHT_TABLE_NAME, WEIGHT_TABLE_COLUMNS, BASE_ENTITY_ID + " = ?", new String[]{entityid}, null, null, UPDATED_AT_COLUMN, null);
        List<Weight> weights = readAllWeights(cursor);
//        if (!weights.isEmpty()) {
            return weights;
//        }

//        return null;
    }
    public void delete(String entityID) {
        masterRepository.getWritableDatabase().delete(WEIGHT_TABLE_NAME,  BASE_ENTITY_ID + " = ?", new String[]{entityID});
    }

    public void close(Long caseId) {
        ContentValues values = new ContentValues();
        values.put(SYNC_STATUS, TYPE_Synced);
        masterRepository.getWritableDatabase().update(WEIGHT_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
    }

    private List<Weight> readAllWeights(Cursor cursor) {
        cursor.moveToFirst();
        List<Weight> weights = new ArrayList<Weight>();
        while (!cursor.isAfterLast()) {
            weights.add(
                    new Weight(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            cursor.getFloat(cursor.getColumnIndex(KG)),
                            new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                            cursor.getString(cursor.getColumnIndex(ANMID)),
                            cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                            cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN))
                    ));

            cursor.moveToNext();
        }
        cursor.close();
        return weights;
    }


    private ContentValues createValuesFor(Weight weight) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, weight.getId());
        values.put(BASE_ENTITY_ID, weight.getBaseEntityId());
        values.put(KG, weight.getKg());
        values.put(DATE, weight.getDate() != null ? weight.getDate().getTime() : null);
        values.put(ANMID, weight.getAnmId());
        values.put(LOCATIONID, weight.getLocationId());
        values.put(SYNC_STATUS, weight.getSyncStatus());
        values.put(UPDATED_AT_COLUMN, weight.getUpdatedAt() != null ? weight.getUpdatedAt() : null);
        return values;
    }
}
