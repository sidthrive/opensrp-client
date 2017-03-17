package org.ei.opensrp.gizi.application;

import android.content.Intent;
import android.content.res.Configuration;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.gizi.LoginActivity;
import org.ei.opensrp.gizi.gizi.ErrorReportingFacade;
import org.ei.opensrp.gizi.gizi.FlurryFacade;
import org.ei.opensrp.sync.DrishtiSyncScheduler;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.ei.opensrp.view.receiver.SyncBroadcastReceiver;
import static org.ei.opensrp.util.Log.logInfo;
import org.ei.opensrp.gizi.application.SyncGiziBroadcastReceiver;

import java.util.Locale;

/**
 * Created by koros on 1/22/16.
 */

public class GiziApplication extends DrishtiApplication {

    @Override
    public void onCreate() {
        DrishtiSyncScheduler.setReceiverClass(SyncBroadcastReceiver.class);
        super.onCreate();
        //  ACRA.init(this);

        DrishtiSyncScheduler.setReceiverClass(SyncBroadcastReceiver.class);
        ErrorReportingFacade.initErrorHandler(getApplicationContext());
        FlurryFacade.init(this);
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

    private String[] getFtsSearchFields(String tableName){
        if(tableName.equals("anak")){
            String[] ftsSearchFields =  { "namaBayi" };
            return ftsSearchFields;
        } else if (tableName.equals("ibu")){
            String[] ftsSearchFields =  { "namalengkap", "namaSuami" };
            return ftsSearchFields;
        }
        return null;
    }

    private String[] getFtsSortFields(String tableName){
        if(tableName.equals("anak")){
            String[] sortFields = { "namaBayi", "tanggalLahirAnak" };
            return sortFields;
        } else if(tableName.equals("ibu")){
            String[] sortFields = { "namalengkap", "umur", "noIbu", "htp" };
            return sortFields;
        }
        return null;
    }

    private String[] getFtsMainConditions(String tableName){
        if(tableName.equals("anak")){
            String[] mainConditions = { "isClosed", "ibuCaseId" };
            return mainConditions;
        } else if(tableName.equals("ibu")){
            String[] mainConditions = { "isClosed", "type", "kartuIbuId" };
            return mainConditions;
        }
        return null;
    }
    private String getFtsCustomRelationalId(String tableName){
        if(tableName.equals("anak")){
            String customRelationalId = "ibuCaseId";
            return customRelationalId;
        } else if(tableName.equals("ibu")){
            String customRelationalId =  "kartuIbuId" ;
            return customRelationalId;
        }
        return null;
    }

    private String[] getFtsTables(){
        String[] ftsTables = { "anak", "ibu" };
        return ftsTables;
    }

    private CommonFtsObject createCommonFtsObject(){
        CommonFtsObject commonFtsObject = new CommonFtsObject(getFtsTables());
        for(String ftsTable: commonFtsObject.getTables()){
            commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
            commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            commonFtsObject.updateMainConditions(ftsTable, getFtsMainConditions(ftsTable));
            commonFtsObject.updateCustomRelationalId(ftsTable, getFtsCustomRelationalId(ftsTable));
        }
        return commonFtsObject;
    }
}
