package org.ei.opensrp.madagascar;

import android.app.Activity;
import android.content.Intent;


import org.ei.opensrp.madagascar.HH.HHSmartRegisterActivity;
import org.ei.opensrp.view.controller.ANMController;

public class NavigationController extends org.ei.opensrp.view.controller.NavigationController {
    private Activity activity;
    private ANMController anmController;

    public NavigationController(Activity activity, ANMController anmController) {
        super(activity,anmController);
        this.activity = activity;
        this.anmController = anmController;
    }
    @Override
    public void startECSmartRegistry() {
       //    activity.startActivity(new Intent(activity, HHSmartRegisterActivity.class));
        ///  activity.startActivity(new Intent(activity, HouseHoldSmartRegisterActivity.class));
        activity.startActivity(new Intent(activity, HHSmartRegisterActivity.class));

    }
    @Override
    public void startFPSmartRegistry() {
           activity.startActivity(new Intent(activity, HHSmartRegisterActivity.class));
    }
    @Override
    public void startANCSmartRegistry() {
        // activity.startActivity(new Intent(activity, NativeKIANCSmartRegisterActivity.class));
    }
    @Override
    public void startPNCSmartRegistry() {
      //  activity.startActivity(new Intent(activity, NativeKIPNCSmartRegisterActivity.class));
    }
    @Override
    public void startChildSmartRegistry() {
      //  activity.startActivity(new Intent(activity, NativeKIAnakSmartRegisterActivity.class));
    }

}
