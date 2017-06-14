package org.ei.opensrp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by koros on 4/19/16.
 */
public class DetailsRepository extends DrishtiRepository {

    private static final String TAG = "DetailsRepository";
    private static final String SQL = "CREATE virtual table ec_details using fts4 (base_entity_id VARCHAR, key VARCHAR, value VARCHAR, event_date datetime)";
    private static final String TABLE_NAME = "ec_details";
    private static final String BASE_ENTITY_ID_COLUMN = "base_entity_id";
    private static final String KEY_COLUMN = "key";
    private static final String VALUE_COLUMN = "value";
    private static final String EVENT_DATE_COLUMN = "event_date";

    @Override
    protected void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL);
    }

    public void add(String baseEntityId, String key, String value, Long timestamp) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();
        Boolean exists = getIdForDetailsIfExists(baseEntityId, key, value);
        if(exists == null){ // Value has not changed, no need to update
            return;
        }

        ContentValues values = new ContentValues();
        values.put(BASE_ENTITY_ID_COLUMN, baseEntityId);
        values.put(KEY_COLUMN, key);
        values.put(VALUE_COLUMN, value);
        values.put(EVENT_DATE_COLUMN, timestamp);

        if (exists){
            int updated = database.update(TABLE_NAME, values, BASE_ENTITY_ID_COLUMN + " = ? AND " + KEY_COLUMN + " MATCH ? ", new String[]{baseEntityId, key});
            Log.i(getClass().getName(), "Detail Row Updated: " + String.valueOf(updated));
        } else {
            long rowId = database.insert(TABLE_NAME, null, values);
            Log.i(getClass().getName(), "Details Row Inserted : " + String.valueOf(rowId));
        }
    }

    private Boolean getIdForDetailsIfExists(String baseEntityId, String key, String value) {
        Cursor mCursor = null;
        try {
            SQLiteDatabase db = masterRepository.getWritableDatabase();
            String query = "SELECT " + VALUE_COLUMN + " FROM " + TABLE_NAME + " WHERE " + BASE_ENTITY_ID_COLUMN + " = '" + baseEntityId + "' AND " + KEY_COLUMN + " MATCH '" + key + "' ";
            mCursor = db.rawQuery(query, null);
            if (mCursor != null && mCursor.moveToFirst()){
                if(value != null){
                    String currentValue = mCursor.getString(mCursor.getColumnIndex(VALUE_COLUMN));
                    if(value.equals(currentValue)) { // Value has not changed, no need to update
                        return null;
                    }
                }
                return true;
            }
        }catch (Exception e){
            Log.e(TAG, e.toString(), e);
        }finally {
            if (mCursor != null) mCursor.close();
        }
        return false;
    }

    public Map<String, String> getAllDetailsForClient(String baseEntityId) {
        Cursor cursor = null;
        Map<String, String> clientDetails = new HashMap<String, String>();
        try {
            SQLiteDatabase db = masterRepository.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + BASE_ENTITY_ID_COLUMN + " MATCH '\""+baseEntityId+"\"'";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    String key = cursor.getString(cursor.getColumnIndex(KEY_COLUMN));
                    String value = cursor.getString(cursor.getColumnIndex(VALUE_COLUMN));
                    clientDetails.put(key, value);
                }while (cursor.moveToNext());
            }
            return clientDetails;
        }catch (Exception e){
            Log.e(TAG, e.toString(), e);
        }finally {
            if (cursor != null) cursor.close();
        }
        return clientDetails;
    }

    public Map<String, String> updateDetails(CommonPersonObjectClient commonPersonObjectClient){
        Map<String, String> details =  getAllDetailsForClient(commonPersonObjectClient.entityId());
        details.putAll(commonPersonObjectClient.getColumnmaps());

        if(commonPersonObjectClient.getDetails() != null) {
            commonPersonObjectClient.getDetails().putAll(details);
        }else{
            commonPersonObjectClient.setDetails(details);
        }
        return details;
    }

    public Map<String, String> updateDetails(CommonPersonObject commonPersonObject){
        Map<String, String> details =  getAllDetailsForClient(commonPersonObject.getCaseId());
        details.putAll(commonPersonObject.getColumnmaps());

        if(commonPersonObject.getDetails() != null) {
            commonPersonObject.getDetails().putAll(details);
        }else{
            commonPersonObject.setDetails(details);
        }
        return details;
    }

}
