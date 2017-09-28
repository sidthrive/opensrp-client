package org.ei.opensrp.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.domain.ProfileImage;

import java.util.ArrayList;
import java.util.List;

public class ImageRepository extends DrishtiRepository {
    private static final String Image_SQL = "CREATE TABLE ImageList(imageid VARCHAR PRIMARY KEY, anmId VARCHAR, entityID VARCHAR, contenttype VARCHAR, filepath VARCHAR, syncStatus VARCHAR, filecategory VARCHAR)";
     public static final String Image_TABLE_NAME = "ImageList";
    public static final String ID_COLUMN = "imageid";
    public static final String anm_ID_COLUMN = "anmId";
    public static final String entityID_COLUMN = "entityID";
    private static final String contenttype_COLUMN = "contenttype";
    public static final String filepath_COLUMN = "filepath";
    public static final String syncStatus_COLUMN = "syncStatus";
    public static final String filecategory_COLUMN = "filecategory";
    public static final String[] Image_TABLE_COLUMNS = {ID_COLUMN, anm_ID_COLUMN, entityID_COLUMN, contenttype_COLUMN, filepath_COLUMN, syncStatus_COLUMN,filecategory_COLUMN};

    public static final String TYPE_ANC = "ANC";
    public static final String TYPE_PNC = "PNC";
    private static final String NOT_CLOSED = "false";
    public static String TYPE_Unsynced = "Unsynced";
    public static String TYPE_Synced = "Synced";

    @Override
    protected void onCreate(SQLiteDatabase database) {
        database.execSQL(Image_SQL);
    }

    public void add(ProfileImage Image) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();
        database.insert(Image_TABLE_NAME, null, createValuesFor(Image, TYPE_ANC));
        database.close();
    }

    public void add(ProfileImage profileImage, String entityId) {
        SQLiteDatabase database = masterRepository.getWritableDatabase();
        long id = database.update(Image_TABLE_NAME, createValuesFor(profileImage, TYPE_ANC), ID_COLUMN + "=?", new String[]{String.valueOf(entityId)});
        if (id == 0) {
            Log.e("Image", "add: INSERT " );
            id = database.insertWithOnConflict(Image_TABLE_NAME, null, createValuesFor(profileImage, TYPE_ANC), SQLiteDatabase.CONFLICT_IGNORE);
        }

        database.close();
    }

    public List<ProfileImage> allProfileImages() {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(Image_TABLE_NAME, Image_TABLE_COLUMNS, syncStatus_COLUMN + " = ?", new String[]{TYPE_Unsynced}, null, null, null, null);
        return readAll(cursor);
    }

    public ProfileImage findByEntityId(String entityId) {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(Image_TABLE_NAME, Image_TABLE_COLUMNS, entityID_COLUMN + " = ?", new String[]{entityId}, null, null, null, null);
        return readAll(cursor).isEmpty()?null:readAll(cursor).get(0);
    }

    public List<ProfileImage> findAllUnSynced() {
        SQLiteDatabase database = masterRepository.getReadableDatabase();
        Cursor cursor = database.query(Image_TABLE_NAME, Image_TABLE_COLUMNS, syncStatus_COLUMN + " = ?", new String[]{TYPE_Unsynced}, null, null, null, null);
        return readAll(cursor);
    }

    public void close(String caseId) {
        ContentValues values = new ContentValues();
        values.put(syncStatus_COLUMN,TYPE_Synced);
        masterRepository.getWritableDatabase().update(Image_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId});
    }

    private ContentValues createValuesFor(ProfileImage image, String type) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, image.getImageid());
        values.put(anm_ID_COLUMN, image.getAnmId());
        values.put(contenttype_COLUMN, image.getContenttype());
        values.put(entityID_COLUMN, image.getEntityID());
        values.put(filepath_COLUMN, image.getFilepath());
        values.put(syncStatus_COLUMN, image.getSyncStatus());
        values.put(filecategory_COLUMN, image.getFilecategory());
        return values;
    }

    private List<ProfileImage> readAll(Cursor cursor) {
        List<ProfileImage> ProfileImages = new ArrayList<ProfileImage>();

        try {
            cursor.moveToFirst();
            while (cursor.getCount() > 0 && !cursor.isAfterLast()) {

                ProfileImages.add(new ProfileImage(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)));

                cursor.moveToNext();
            }
            cursor.close();
        }catch(Exception e){

        }
        return ProfileImages;
    }






//    public void update(Mother mother) {
//        SQLiteDatabase database = masterRepository.getWritableDatabase();
//        database.update(MOTHER_TABLE_NAME, createValuesFor(mother, TYPE_ANC), ID_COLUMN + " = ?", new String[]{mother.caseId()});
//    }
}
