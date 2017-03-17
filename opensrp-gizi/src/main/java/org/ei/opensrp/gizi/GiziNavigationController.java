package org.ei.opensrp.gizi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


//import org.ei.opensrp.gizi.test.TestSmartRegisterActivity;
import org.ei.opensrp.gizi.gizi.GiziSmartRegisterActivity;
import org.ei.opensrp.view.activity.ReportsActivity;
import org.ei.opensrp.view.controller.ANMController;


import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class GiziNavigationController extends org.ei.opensrp.view.controller.NavigationController {
    private Activity activity;
    private ANMController anmController;

    public GiziNavigationController(Activity activity, ANMController anmController) {
        super(activity,anmController);
        this.activity = activity;
        this.anmController = anmController;
    }
    @Override
    public void startECSmartRegistry() {
      //  activity.startActivity(new Intent(activity, TestSmartRegisterActivity.class));
      ///  activity.startActivity(new Intent(activity, HouseHoldSmartRegisterActivity.class));
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this.activity);

        if(sharedPreferences.getBoolean("firstlauch",true)) {
            sharedPreferences.edit().putBoolean("firstlauch",false).commit();
       //     activity.startActivity(new Intent(activity, tutorialCircleViewFlow.class));
        }

    }
    @Override
    public void startFPSmartRegistry() {
     //   activity.startActivity(new Intent(activity, ElcoSmartRegisterActivity.class));
    }
    @Override
    public void startANCSmartRegistry() {
       // activity.startActivity(new Intent(activity, TestSmartRegisterActivity.class));
    }

    @Override
    public void startChildSmartRegistry() {
         activity.startActivity(new Intent(activity, GiziSmartRegisterActivity.class));
    }

    @Override
    public void startReports() {
//        activity.startActivity(new Intent(activity, ReportsActivity.class));
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://dho-report.sid-indonesia.org")));
    }

}
