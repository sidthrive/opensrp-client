package org.ei.opensrp.demographic.demographic;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.demographic.R;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.dialog.FilterOption;

public class DemogSearchOption implements FilterOption {
    private final String criteria;

    public DemogSearchOption(String criteria) {
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
            if(currentclient.getDetails().get("nama_kk") != null) {
                if (currentclient.getDetails().get("nama_kk").toLowerCase().contains(criteria.toLowerCase())) {
                    result = true;
                }
            }
        }
        if(!result) {
            if(currentclient.getDetails().get("nama_kk") != null) {
                if (currentclient.getDetails().get("nama_kk").contains(criteria)) {
                    result = true;
                }
            }
        }

        return result;
    }
}
