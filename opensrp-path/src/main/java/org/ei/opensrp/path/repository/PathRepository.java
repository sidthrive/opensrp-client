package org.ei.opensrp.path.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.repository.EcRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import util.PathConstants;

public class PathRepository extends EcRepository {

    private static final String TAG = PathRepository.class.getCanonicalName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;

    public PathRepository(Context context) {
        super(context, PathConstants.DATABASE_NAME, PathConstants.DATABASE_VERSION, org.ei.opensrp.Context.getInstance().session(), VaccinatorApplication.createCommonFtsObject());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        UniqueIdRepository.createTable(database);
        WeightRepository.createTable(database);
        VaccineRepository.createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(PathRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        upgradeToVersion2(db, oldVersion);
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
     * Version 2 added some columns to the ec_child table
     *
     * @param database
     * @param oldVersion
     */
    private void upgradeToVersion2(SQLiteDatabase database, int oldVersion) {
        if (oldVersion < 2) {
            // Create the new ec_child table
            String newTableNameSuffix = "_v2";
            String originalTableName = "ec_child";

            Set<String> searchColumns = new LinkedHashSet<String>();
            searchColumns.add(CommonFtsObject.idColumn);
            searchColumns.add(CommonFtsObject.relationalIdColumn);
            searchColumns.add(CommonFtsObject.phraseColumn);
            searchColumns.add(CommonFtsObject.isClosedColumn);

            String[] mainConditions = this.commonFtsObject.getMainConditions(originalTableName);
            if (mainConditions != null)
                for (String mainCondition : mainConditions) {
                    if (!mainCondition.equals(CommonFtsObject.isClosedColumnName))
                        searchColumns.add(mainCondition);
                }

            String[] sortFields = this.commonFtsObject.getSortFields(originalTableName);
            if (sortFields != null) {
                for (String sortValue : sortFields) {
                    if (sortValue.startsWith("alerts.")) {
                        sortValue = sortValue.split("\\.")[1];
                    }
                    searchColumns.add(sortValue);
                }
            }

            String joinedSearchColumns = StringUtils.join(searchColumns, ",");

            String searchSql = "create virtual table "
                    + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                    + " using fts4 (" + joinedSearchColumns + ");";
            Log.d(TAG, "Create query is\n---------------------------\n" + searchSql);

            database.execSQL(searchSql);

            // Run insert query
            ArrayList<String> newlyAddedFields = new ArrayList<>();
            newlyAddedFields.add("BCG_2");
            newlyAddedFields.add("inactive");
            newlyAddedFields.add("lost_to_follow_up");
            ArrayList<String> oldFields = new ArrayList<>();

            for (String curColumn : searchColumns) {
                curColumn = curColumn.trim();
                if (curColumn.contains(" ")) {
                    String[] curColumnParts = curColumn.split(" ");
                    curColumn = curColumnParts[0];
                }

                if (!newlyAddedFields.contains(curColumn)) {
                    oldFields.add(curColumn);
                } else {
                    Log.d(TAG, "Skipping field " + curColumn + " from the select query");
                }
            }

            String insertQuery = "insert into "
                    + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                    + " (" + StringUtils.join(oldFields, ", ") + ")"
                    + " select " + StringUtils.join(oldFields, ", ") + " from "
                    + CommonFtsObject.searchTableName(originalTableName);

            Log.d(TAG, "Insert query is\n---------------------------\n" + insertQuery);
            database.execSQL(insertQuery);

            // Run the drop query
            String dropQuery = "drop table " + CommonFtsObject.searchTableName(originalTableName);
            Log.d(TAG, "Drop query is\n---------------------------\n" + dropQuery);
            database.execSQL(dropQuery);

            // Run rename query
            String renameQuery = "alter table "
                    + CommonFtsObject.searchTableName(originalTableName) + newTableNameSuffix
                    + " rename to " + CommonFtsObject.searchTableName(originalTableName);
            Log.d(TAG, "Rename query is\n---------------------------\n" + renameQuery);
            database.execSQL(renameQuery);
        }
    }
}
