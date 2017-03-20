package org.ei.opensrp.path.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.path.activity.LoginActivity;
import org.ei.opensrp.path.receiver.PathSyncBroadcastReceiver;
import org.ei.opensrp.sync.DrishtiSyncScheduler;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

import static org.ei.opensrp.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class VaccinatorApplication extends DrishtiApplication {
    private Locale locale = null;
    private Context context;
    private boolean lastModified;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        DrishtiSyncScheduler.setReceiverClass(PathSyncBroadcastReceiver.class);

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());
        applyUserLanguagePreference();
        cleanUpSyncState();
        setCrashlyticsUser(context);
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    private void cleanUpSyncState() {
        DrishtiSyncScheduler.stop(getApplicationContext());
        context.allSharedPreferences().saveIsSyncInProgress(false);
    }


    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Bidan Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        super.onTerminate();
    }

    private void applyUserLanguagePreference() {
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = context.allSharedPreferences().fetchLanguagePreference();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            updateConfiguration(config);
        }
    }

    private void updateConfiguration(Configuration config) {
        config.locale = locale;
        Locale.setDefault(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private String[] getFtsSearchFields(String tableName) {
        if (tableName.equals("ec_child")) {
            String[] ftsSearchFileds = {"zeir_id", "epi_card_number", "first_name", "last_name"};
            return ftsSearchFileds;
        } else if (tableName.equals("ec_mother")) {
            String[] ftsSearchFileds = {"zeir_id", "epi_card_number", "first_name", "last_name", "father_name", "husband_name", "contact_phone_number"};
            return ftsSearchFileds;
        }
        return null;
    }

    private String[] getFtsSortFields(String tableName) {
        if (tableName.equals("ec_child")) {
            String[] sortFields = {"first_name", "dob", "zeir_id", "last_interacted_with",
                    "alerts.BCG",
                    "alerts.OPV_0",

                    "alerts.OPV_1",
                    "alerts.Penta_1",
                    "alerts.PCV_1",
                    "alerts.ROTA_1",

                    "alerts.OPV_2",
                    "alerts.PENTA_2",
                    "alerts.PCV_2",
                    "alerts.ROTA_2",

                    "alerts.OPV_3",
                    "alerts.PENTA_3",
                    "alerts.PCV_3",

                    "alerts.MEASLES_1",
                    "alerts.MR_1",
                    "alerts.OPV_4",

                    "alerts.MEASLES_2",
                    "alerts.MR_2"
            };
            return sortFields;
        } else if (tableName.equals("ec_mother")) {
            String[] sortFields = {"first_name", "dob", "zeir_id", "last_interacted_with"};
            return sortFields;
        }
        return null;
    }

    private String[] getFtsTables() {
        String[] ftsTables = {"ec_child", "ec_mother"};
        return ftsTables;
    }

    private Map<String, Pair<String, Boolean>> getAlertScheduleMap() {
        Map<String, Pair<String, Boolean>> map = new HashMap<String, Pair<String, Boolean>>();
        map.put("BCG", Pair.create("ec_child", false));
        map.put("OPV 0", Pair.create("ec_child", false));

        map.put("OPV 1", Pair.create("ec_child", false));
        map.put("PENTA 1", Pair.create("ec_child", false));
        map.put("PCV 1", Pair.create("ec_child", false));
        map.put("ROTA 1", Pair.create("ec_child", false));

        map.put("OPV 2", Pair.create("ec_child", false));
        map.put("PENTA 2", Pair.create("ec_child", false));
        map.put("PCV 2", Pair.create("ec_child", false));
        map.put("ROTA 2", Pair.create("ec_child", false));

        map.put("OPV 3", Pair.create("ec_child", false));
        map.put("PENTA 3", Pair.create("ec_child", false));
        map.put("PCV 3", Pair.create("ec_child", false));

        map.put("MEASLES 1", Pair.create("ec_child", false));
        map.put("MR 1", Pair.create("ec_child", false));
        map.put("OPV 4", Pair.create("ec_child", false));

        map.put("MEASLES 2", Pair.create("ec_child", false));
        map.put("MR 2", Pair.create("ec_child", false));

        return map;
    }

    private CommonFtsObject createCommonFtsObject() {
        CommonFtsObject commonFtsObject = new CommonFtsObject(getFtsTables());
        for (String ftsTable : commonFtsObject.getTables()) {
            commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
            commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap());
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }

    private void grantPhotoDirectoryAccess() {
        Uri uri = FileProvider.getUriForFile(this,
                "com.vijay.jsonwizard.fileprovider",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        grantUriPermission("com.vijay.jsonwizard", uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }
}
