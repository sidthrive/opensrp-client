package org.ei.opensrp.indonesia.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.repository.ImageRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class extends imagerepository class for normal images and adds a column for vector images data
 */
public class VectorImageRepository extends ImageRepository {
    private static final String TAG = VectorImageRepository.class.getCanonicalName();
    private static final String Alter_Image_Table_SQL = " ALTER TABLE ImageList ADD COLUMN filevector TEXT;";


    public static final String filevector_COLUMN = "filevector";
    public static final String[] Image_TABLE_COLUMNS = {ID_COLUMN, anm_ID_COLUMN, entityID_COLUMN, contenttype_COLUMN, filepath_COLUMN, syncStatus_COLUMN, filecategory_COLUMN, filevector_COLUMN};
    public static final String Vector_TABLE_NAME = "VectorList";
    public static final String VID_COLUMN = "vectorID";
    private static final String Vector_SQL = "CREATE TABLE VectorList(" + VID_COLUMN + " VARCHAR PRIMARY KEY, " + entityID_COLUMN + " VARCHAR, syncStatus VARCHAR )";
    public static final String[] VectorImage_TABLE_COLUMNS = {
            entityID_COLUMN,
            filevector_COLUMN
    };
    public static final String[] Vector_TABLE_COLUMNS = {
            VID_COLUMN,
            entityID_COLUMN,
            syncStatus_COLUMN
    };

    public static final String TYPE_ANC = "ANC";
    public static final String TYPE_PNC = "PNC";
    private static final String NOT_CLOSED = "false";
    public static String TYPE_Unsynced = "Unsynced";
    public static String TYPE_Synced = "Synced";

    private static final String ENTITY_ID_INDEX = "CREATE INDEX " + Image_TABLE_NAME + "_" + entityID_COLUMN + "_index ON " + Image_TABLE_NAME + "(" + entityID_COLUMN + " COLLATE NOCASE);";
    BidanRepository pathRepository;
    public VectorImageRepository(BidanRepository pathRepository) {
        this.pathRepository=pathRepository;
    }


    protected static void createTable(SQLiteDatabase database) {
        database.execSQL(Alter_Image_Table_SQL);
        database.execSQL(Vector_SQL);
    }

    protected ContentValues createValuesFor(ProfileImage image, String type) {
        ContentValues values = super.createValuesFor(image,type);
        values.put(filevector_COLUMN, image.getFilevector());
        return values;
    }
    protected List<ProfileImage> readAll(Cursor cursor) {
        List<ProfileImage> profileImages = new ArrayList<>();

        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (cursor.getCount() > 0 && !cursor.isAfterLast()) {

                    profileImages.add(new ProfileImage(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)));

                    cursor.moveToNext();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return profileImages;
    }


//    public void update(Mother mother) {
//        SQLiteDatabase database = masterRepository.getWritableDatabase();
//        database.update(MOTHER_TABLE_NAME, createValuesFor(mother, TYPE_ANC), ID_COLUMN + " = ?", new String[]{mother.caseId()});
//    }


    public List<ProfileImage> allVectorImages() {
        Cursor cursor = pathRepository.getReadableDatabase().query(Image_TABLE_NAME, Image_TABLE_COLUMNS, null, null, null, null, null, null);
        return readAll(cursor);
    }


    public ProfileImage findVectorByEntityId(String entityId) {
        Cursor cursor = pathRepository.getReadableDatabase().query(Image_TABLE_NAME, Vector_TABLE_COLUMNS, entityID_COLUMN + " = ?", new String[]{entityId}, null, null, null, null);
        List<ProfileImage> allcursor = readAll(cursor);
        return (!allcursor.isEmpty()) ? allcursor.get(0) : null;
    }

    public List<ProfileImage> findVectorAllUnSynced() {
        Cursor cursor = pathRepository.getReadableDatabase().query(Image_TABLE_NAME, Image_TABLE_COLUMNS, filevector_COLUMN + " = ?", null, null, null, null, null);
        return readAll(cursor);
    }

    public void vector_close(String caseId) {
        ContentValues values = new ContentValues();
        values.put(syncStatus_COLUMN, TYPE_Synced);
        masterRepository.getWritableDatabase().update(Vector_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId});
    }


    public void updateByEntityId(String entityId, String faceVector) {
//        SQLiteDatabase database = masterRepository.getReadableDatabase();
//        Cursor cursor = database.query(Image_TABLE_NAME, Image_TABLE_COLUMNS, entityID_COLUMN + " = ?", new String[]{entityId}, null, null, null, null);
//        List<ProfileImage> allcursor = readAll(cursor);
//        return (!allcursor.isEmpty()) ? allcursor.get(0) : null;

        ContentValues values = new ContentValues();
        values.put(filevector_COLUMN, faceVector);
        Log.e(TAG, "updateByEntityId: " + values);
        pathRepository.getReadableDatabase().update(Image_TABLE_NAME, values, "entityID" + " = ?", new String[]{entityId});
    }
}
