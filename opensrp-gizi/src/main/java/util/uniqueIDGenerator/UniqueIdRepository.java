package util.uniqueIDGenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import org.ei.opensrp.repository.DrishtiRepository;

import java.util.ArrayList;
import java.util.List;

/**
 + * Created by Dimas on 9/7/2015, modify by Marwan on 17/11/2016
 + */
public class UniqueIdRepository extends SQLiteOpenHelper{

    private static final String UNIQUE_ID_SQL = "CREATE TABLE IF NOT EXISTS unique_id(id INTEGER PRIMARY KEY AUTOINCREMENT, uniqueId Varchar)";
    private static final String DB_NAME = "uniqueiddb";

    private static final String UNIQUE_ID_TABLE_NAME = "unique_id";
    private static final String UNIQUE_ID_COLUMN = "uniqueId";

    public UniqueIdRepository(Context context){
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(UNIQUE_ID_SQL);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db,int oldVersion, int newVersion){

    }

    public void saveUniqueId(String uniqueId) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UNIQUE_ID_COLUMN, uniqueId);
        database.insert(UNIQUE_ID_TABLE_NAME, null, values);
    }

    public String getUniqueIdFromLastUsedId(String lastUsedId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT " + UNIQUE_ID_COLUMN +
                    " FROM " + UNIQUE_ID_TABLE_NAME +
                    " WHERE " + UNIQUE_ID_COLUMN + " > ?" +
                    " ORDER BY " + UNIQUE_ID_COLUMN + " ASC  " +
                    " LIMIT 1", new String[]{lastUsedId});
        String uniqueId = null;
        if(cursor!=null) {
            if(cursor.moveToFirst()) {
                    uniqueId = cursor.getString(0);
                }
            cursor.close();
        }
        return uniqueId;
    }

    public List<Long> getAllUniqueId() {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT id, " + UNIQUE_ID_COLUMN +
                    " FROM " + UNIQUE_ID_TABLE_NAME , new String[]{});
        cursor.moveToFirst();
        List<Long> uids = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            uids.add(cursor.getLong(1));
            cursor.moveToNext();
        }
        cursor.close();
        return uids;
    }


}