package org.ei.opensrp.path.application;

import android.content.Intent;
import android.content.res.Configuration;

import com.crashlytics.android.Crashlytics;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.path.activity.LoginActivity;
import org.ei.opensrp.path.receiver.ConfigSyncReceiver;
import org.ei.opensrp.path.receiver.PathSyncBroadcastReceiver;
import org.ei.opensrp.sync.DrishtiSyncScheduler;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static org.ei.opensrp.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class VaccinatorApplication extends DrishtiApplication{
    private Locale locale = null;
    private Context context;

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
        ConfigSyncReceiver.scheduleFirstSync(getApplicationContext());
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

    private String[] getFtsSearchFields(String tableName){
        if(tableName.equals("ec_child")){
            String[] ftsSearchFileds =  { "program_client_id", "epi_card_number", "first_name", "last_name" };
            return ftsSearchFileds;
        }else if(tableName.equals("ec_woman")){
            String[] ftsSearchFileds =  { "program_client_id", "epi_card_number", "first_name", "last_name", "father_name", "husband_name", "contact_phone_number" };
            return ftsSearchFileds;
        }
        return null;
    }

    private String[] getFtsSortFields(String tableName){
        if(tableName.equals("ec_child") || tableName.equals("ec_woman")) {
            String[] sortFields = {"first_name", "dob", "program_client_id"};
            return sortFields;
        }
        return null;
    }

    private String[] getFtsTables(){
        String[] ftsTables = { "ec_child", "ec_woman" };
        return ftsTables;
    }

    private CommonFtsObject createCommonFtsObject(){
        CommonFtsObject commonFtsObject = new CommonFtsObject(getFtsTables());
        for(String ftsTable: commonFtsObject.getTables()){
            commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
            commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
        }
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last
     *
     * @param context   The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if(context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }
}
