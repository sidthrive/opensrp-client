package org.ei.opensrp.indonesia.face.camera.util;

import android.content.ContentValues;

import org.ei.opensrp.indonesia.application.BidanApplication;
import org.ei.opensrp.indonesia.repository.BidanRepository;
import org.ei.opensrp.indonesia.repository.VectorImageRepository;

/**
 * Created by sid on 2/23/17.
 */
public class FaceRepository extends VectorImageRepository {

    public FaceRepository(BidanRepository pathRepository) {
        super(pathRepository);
    }

    public void updateByEntityId(String entityId, String faceVector) {
//        SQLiteDatabase database = masterRepository.getReadableDatabase();
//        Cursor cursor = database.query(Image_TABLE_NAME, Image_TABLE_COLUMNS, entityID_COLUMN + " = ?", new String[]{entityId}, null, null, null, null);
//        List<ProfileImage> allcursor = readAll(cursor);
//        return (!allcursor.isEmpty()) ? allcursor.get(0) : null;

        ContentValues values = new ContentValues();
        values.put(filevector_COLUMN, faceVector);
         BidanApplication.getInstance().getRepository().getWritableDatabase().update(Image_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{entityId});
    }

}
