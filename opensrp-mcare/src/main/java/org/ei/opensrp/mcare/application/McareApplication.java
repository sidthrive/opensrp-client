package org.ei.opensrp.mcare.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.mcare.LoginActivity;
import org.ei.opensrp.sync.DrishtiSyncScheduler;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.ei.opensrp.view.receiver.SyncBroadcastReceiver;
import static org.ei.opensrp.util.Log.logInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by koros on 1/22/16.
 */
@ReportsCrashes(
        formKey = "",
        formUri = "https://drishtiapp.cloudant.com/acra-drishtiapp/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.POST,
        formUriBasicAuthLogin = "sompleakereepeavoldiftle",
        formUriBasicAuthPassword = "ecUMrMeTKf1X1ODxHqo3b43W",
        mode = ReportingInteractionMode.SILENT
)
public class McareApplication extends DrishtiApplication {

    @Override
    public void onCreate() {
        DrishtiSyncScheduler.setReceiverClass(SyncBroadcastReceiver.class);
        super.onCreate();
        Fabric.with(this, new Crashlytics());
//        ACRA.init(this);

        DrishtiSyncScheduler.setReceiverClass(SyncBroadcastReceiver.class);

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());
        applyUserLanguagePreference();
        cleanUpSyncState();
    }

    @Override
    public void logoutCurrentUser(){
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
        super.onTerminate();
        logInfo("Application is terminating. Stopping Dristhi Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
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
    public static String convertToEnglishDigits(String value)
    {
//        ০১২৩৪৫৬৭৮৯
        String newValue = value.replace("১", "1").replace("২", "2").replace("৩", "3").replace("৪", "4").replace("৫", "5")
                .replace("৬", "6").replace("৭", "7").replace("৮", "8").replace("৯", "9").replace("০", "0");

        return newValue;
    }
    public static String convertToBengaliDigits(String value)
    {
//        ০১২৩৪৫৬৭৮৯
        String newValue = value.replace("1","১").replace( "2","২").replace( "3","৩").replace( "4","৪").replace( "5","৫")
                .replace( "6","৬").replace( "7","৭").replace( "8","৮").replace( "9","৯").replace( "0","০");

        return newValue;
    }

    private String[] getFtsSearchFields(String tableName){
        if(tableName.equals("household")){
            String[] ftsSearchFields =  { "FWHOHFNAME", "FWGOBHHID", "FWJIVHHID" };
            return ftsSearchFields;
        } else if(tableName.equals("elco")){
            String[] ftsSearchFields =  { "FWWOMFNAME", "GOBHHID", "JiVitAHHID" };
            return ftsSearchFields;
        } else if (tableName.equals("mcaremother")){
            String[] ftsSearchFields =  { "FWWOMFNAME", "GOBHHID", "JiVitAHHID", "Is_PNC", "alerts.visitCode" };
            return ftsSearchFields;
        } else if (tableName.equals("mcarechild")){
            String[] ftsSearchFields =  { "FWWOMFNAME", "GOBHHID", "JiVitAHHID", "alerts.visitCode" };
            return ftsSearchFields;
        }
        return null;
    }

    private String[] getFtsSortFields(String tableName){
        if(tableName.equals("household")) {
            String[] sortFields = {"FWHOHFNAME", "FWGOBHHID", "FWJIVHHID", "alerts.FW_CENSUS"};
            return sortFields;
        } else if(tableName.equals("elco")){
            String[] sortFields = {"FWWOMFNAME", "GOBHHID", "JiVitAHHID", "alerts.ELCO_PSRF"};
            return sortFields;
        } else if(tableName.equals("mcaremother")){
            String[] sortFields = {"FWWOMFNAME", "GOBHHID", "JiVitAHHID", "FWPSRLMP", "FWBNFDTOO", "FWSORTVALUE", "FWBNFSTS", "alerts.Ante_Natal_Care_Reminder_Visit", "alerts.BirthNotificationPregnancyStatusFollowUp", "alerts.Post_Natal_Care_Reminder_Visit"};
            return sortFields;
        } else if(tableName.equals("mcarechild")){
            String[] sortFields = {"FWWOMFNAME", "GOBHHID", "JiVitAHHID", "FWSORTVALUE", "alerts.Essential_Newborn_Care_Checklist"};
            return sortFields;
        }
        return null;
    }

    private String[] getFtsMainConditions(String tableName){
        if(tableName.equals("household")) {
            String[] mainConditions = {"FWHOHFNAME", "details"};
            return mainConditions;
        } else if(tableName.equals("elco")){
            String[] mainConditions = {"FWWOMFNAME", "details"};
            return mainConditions;
        } else if(tableName.equals("mcaremother")){
            String[] mainConditions = {"FWWOMFNAME", "Is_PNC", "details"};
            return mainConditions;
        } else if(tableName.equals("mcarechild")){
            String[] mainConditions = {"FWBNFGEN",  "details"};
            return mainConditions;
        }
        return null;
    }

    private String[] getFtsTables(){
        String[] ftsTables = { "household", "elco", "mcaremother", "mcarechild" };
        return ftsTables;
    }

    /**
     * Map value Pair<TableName, updateVisitCode>
     * @return
     */
    private Map<String, Pair<String, Boolean>> getAlertScheduleMap(){
        Map<String, Pair<String, Boolean>> map = new HashMap<String, Pair<String, Boolean>>();
        map.put("FW CENSUS", Pair.create("household", false));
        map.put("ELCO PSRF", Pair.create("elco", false));
        map.put("Ante Natal Care Reminder Visit", Pair.create("mcaremother", true));
        map.put("BirthNotificationPregnancyStatusFollowUp",  Pair.create("mcaremother", false));
        map.put("Post Natal Care Reminder Visit", Pair.create("mcaremother", true));
        map.put("Essential Newborn Care Checklist", Pair.create("mcarechild", true));

        return map;
    }

    private String[] getAlertFilterVisitCodes(){
        String[] ftsTables = { "ancrv_1", "ancrv_2", "ancrv_3", "ancrv_4", "pncrv_1", "pncrv_2", "pncrv_3", "enccrv_1", "enccrv_2", "enccrv_3" };
        return ftsTables;
    }

    private CommonFtsObject createCommonFtsObject(){
        CommonFtsObject commonFtsObject = new CommonFtsObject(getFtsTables());
        for(String ftsTable: commonFtsObject.getTables()){
            commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
            commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            commonFtsObject.updateMainConditions(ftsTable, getFtsMainConditions(ftsTable));
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap());
        commonFtsObject.updateAlertFilterVisitCodes(getAlertFilterVisitCodes());
        return commonFtsObject;
    }
    public static void setCrashlyticsUser(Context context) {
                if(context != null && context.userService() != null
                                && context.allSharedPreferences() != null) {
                       Crashlytics.setUserName(context.allSharedPreferences().fetchRegisteredANM());
                   }
           }

}
