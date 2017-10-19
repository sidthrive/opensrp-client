package org.ei.opensrp.unicef.rcc.HH;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.unicef.rcc.R;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.dialog.FilterOption;

public class KISearchOption implements FilterOption {
    private final String criteria;

    public KISearchOption(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public String name() {
        return Context.getInstance().applicationContext().getResources().getString(R.string.hh_search_hint);
    }

    @Override
    public boolean filter(SmartRegisterClient client) {
        boolean result = false;
        CommonPersonObjectClient currentclient = (CommonPersonObjectClient) client;
//        AllCommonsRepository allElcoRepository = new AllCommonsRepository("elco");
        if(!result) {
            if(currentclient.getDetails().get("namaLengkap") != null) {
                if (currentclient.getDetails().get("namaLengkap").toLowerCase().contains(criteria.toLowerCase())) {
                    result = true;
                }
            }
        }

        return result;
    }
}
