package org.ei.opensrp.indonesia.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.indonesia.AllConstantsINA;
import org.ei.opensrp.indonesia.application.BidanApplication;
import org.ei.opensrp.repository.EcRepository;

public class BidanRepository extends EcRepository {

    private static final String TAG = BidanRepository.class.getCanonicalName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;

    public BidanRepository(Context context) {
        super(context, AllConstantsINA.DATABASE_NAME, AllConstantsINA.DATABASE_VERSION, org.ei.opensrp.Context.getInstance().session(), BidanApplication.createCommonFtsObject());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        VectorImageRepository.createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BidanRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        upgradeToVersion2(db, oldVersion);
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
        } catch (Exception e) {
            Log.e(TAG, "Database Error. " + e.getMessage());
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


    /**
     * @param database
     * @param oldVersion
     */
    private void upgradeToVersion2(SQLiteDatabase database, int oldVersion) {
        if (oldVersion < 2) {

        }
    }
}
