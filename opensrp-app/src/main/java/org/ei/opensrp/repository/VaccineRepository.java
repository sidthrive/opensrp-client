package org.ei.opensrp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.domain.Vaccine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VaccineRepository extends DrishtiRepository {
    private static final String TAG = VaccineRepository.class.getCanonicalName();
    private static final String VACCINE_SQL = "CREATE TABLE vaccines (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,program_client_id VARCHAR NULL,name VARCHAR NOT NULL,calculation INTEGER,date DATETIME NOT NULL,anmid VARCHAR NULL,location_id VARCHAR NULL,sync_status VARCHAR,updated_at INTEGER NULL, UNIQUE(base_entity_id, program_client_id, name) ON CONFLICT IGNORE)";
    public static final String VACCINE_TABLE_NAME = "vaccines";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String PROGRAM_CLIENT_ID = "program_client_id";
    public static final String NAME = "name";
    public static final String CALCULATION = "calculation";
    private static final String DATE = "date";
    private static final String ANMID = "anmid";
    private static final String LOCATIONID = "location_id";
    private static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String[] VACCINE_TABLE_COLUMNS = {ID_COLUMN, BASE_ENTITY_ID, PROGRAM_CLIENT_ID, NAME, CALCULATION, DATE, ANMID, LOCATIONID, SYNC_STATUS, UPDATED_AT_COLUMN};

    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + VACCINE_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + VACCINE_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + VACCINE_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + VACCINE_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";

    public static String TYPE_Unsynced = "Unsynced";
    public static String TYPE_Synced = "Synced";

    @Override
    protected void onCreate(SQLiteDatabase database) {
        database.execSQL(VACCINE_SQL);
        database.execSQL(BASE_ENTITY_ID_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }

    public void add(Vaccine vaccine) {
        if (vaccine == null) {
            return;
        }
        if (StringUtils.isBlank(vaccine.getSyncStatus())) {
            vaccine.setSyncStatus(TYPE_Unsynced);
        }

        if (vaccine.getUpdatedAt() == null) {
            vaccine.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
        }

        SQLiteDatabase database = masterRepository.getWritableDatabase();
        if (vaccine.getId() == null) {
            vaccine.setId(database.insert(VACCINE_TABLE_NAME, null, createValuesFor(vaccine)));
        } else {
            String idSelection = ID_COLUMN + " = ?";
            database.update(VACCINE_TABLE_NAME, createValuesFor(vaccine), idSelection, new String[]{vaccine.getId().toString()});
        }
        database.close();
    }

    public List<Vaccine> findUnSyncedBeforeTime(int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -hours);

        Long time = calendar.getTimeInMillis();

        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(VACCINE_TABLE_NAME, VACCINE_TABLE_COLUMNS, UPDATED_AT_COLUMN + " < ? AND " + SYNC_STATUS + " = ?", new String[]{time.toString(), TYPE_Unsynced}, null, null, null, null);
        return readAllVaccines(cursor);
    }

    public List<Vaccine> findByEntityId(String entityId) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(VACCINE_TABLE_NAME, VACCINE_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? ORDER BY " + UPDATED_AT_COLUMN, new String[]{entityId}, null, null, null, null);
        return readAllVaccines(cursor);
    }

    public Vaccine find(Long caseId) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(VACCINE_TABLE_NAME, VACCINE_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()}, null, null, null, null);
        List<Vaccine> vaccines = readAllVaccines(cursor);
        if (!vaccines.isEmpty()) {
            return vaccines.get(0);
        }

        return null;
    }

    public void deleteVaccine(Long caseId) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();
        database.delete(VACCINE_TABLE_NAME, ID_COLUMN + "= ?", new String[]{caseId.toString()});
    }

    public void close(Long caseId) {
        ContentValues values = new ContentValues();
        values.put(SYNC_STATUS, TYPE_Synced);
        masterRepository.getWritableDatabase().update(VACCINE_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
    }

    private List<Vaccine> readAllVaccines(Cursor cursor) {
        cursor.moveToFirst();
        List<Vaccine> vaccines = new ArrayList<Vaccine>();
        while (!cursor.isAfterLast()) {
            vaccines.add(
                    new Vaccine(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                            cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                            cursor.getString(cursor.getColumnIndex(PROGRAM_CLIENT_ID)),
                            cursor.getString(cursor.getColumnIndex(NAME)),
                            cursor.getInt(cursor.getColumnIndex(CALCULATION)),
                            new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                            cursor.getString(cursor.getColumnIndex(ANMID)),
                            cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                            cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                            cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN))
                    ));

            cursor.moveToNext();
        }
        cursor.close();
        return vaccines;
    }


    private ContentValues createValuesFor(Vaccine vaccine) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, vaccine.getId());
        values.put(BASE_ENTITY_ID, vaccine.getBaseEntityId());
        values.put(PROGRAM_CLIENT_ID, vaccine.getProgramClientId());
        values.put(NAME, vaccine.getName());
        values.put(CALCULATION, vaccine.getCalculation());
        values.put(DATE, vaccine.getDate() != null ? vaccine.getDate().getTime() : null);
        values.put(ANMID, vaccine.getAnmId());
        values.put(LOCATIONID, vaccine.getLocationId());
        values.put(SYNC_STATUS, vaccine.getSyncStatus());
        values.put(UPDATED_AT_COLUMN, vaccine.getUpdatedAt() != null ? vaccine.getUpdatedAt() : null);
        return values;
    }
}
