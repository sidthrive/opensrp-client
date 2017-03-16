package org.ei.opensrp.view.activity;

import org.ei.opensrp.view.controller.ANCSmartRegisterController;

public class ANCSmartRegisterActivity extends SmartRegisterActivity {
    @Override
    protected void onSmartRegisterInitialization() {
        webView.addJavascriptInterface(new ANCSmartRegisterController(
                context().serviceProvidedService(), context().alertService(),
                context().allBeneficiaries(), context().listCache(), context().ancClientsCache()),
                "context");
        webView.loadUrl("file:///android_asset/www/smart_registry/anc_register.html");
    }
}
