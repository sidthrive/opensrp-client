package org.ei.opensrp.vaksinator.imunisasiTT;

import android.view.View;

import org.ei.opensrp.Context;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.vaksinator.R;
import org.ei.opensrp.view.contract.ANCSmartRegisterClient;
import org.ei.opensrp.view.contract.ChildSmartRegisterClient;
import org.ei.opensrp.view.contract.FPSmartRegisterClient;
import org.ei.opensrp.view.contract.pnc.PNCSmartRegisterClient;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.viewHolder.NativeANCSmartRegisterViewHolder;
import org.ei.opensrp.view.viewHolder.NativeChildSmartRegisterViewHolder;
import org.ei.opensrp.view.viewHolder.NativeFPSmartRegisterViewHolder;
import org.ei.opensrp.view.viewHolder.NativePNCSmartRegisterViewHolder;

import static org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity.ClientsHeaderProvider;

public class TTServiceModeOption extends ServiceModeOption {

    public TTServiceModeOption(SmartRegisterClientsProvider provider) {
        super(provider);
    }

    @Override
    public String name() {
       return Context.getInstance().getStringResource(R.string.registerTT);
    }

    @Override
    public ClientsHeaderProvider getHeaderProvider() {
        return new ClientsHeaderProvider() {
            @Override
            public int count() {
                return 7;
            }

            @Override
            public int weightSum() {
                return 1000;
            }

            @Override
            public int[] weights() {
                return new int[]{244, 90, 140, 120, 170, 135, 95};
            }

            @Override
            public int[] headerTextResourceIds() {
                return new int[]{
                        R.string.header_name, R.string.header_id, R.string.header_klinis,
                        R.string.header_pemeriksaan, R.string.header_history_anc,
                        R.string.tt_status, R.string.header_edit
                        };
            }
        };
    }

    @Override
    public void setupListView(ChildSmartRegisterClient client,
                              NativeChildSmartRegisterViewHolder viewHolder,
                              View.OnClickListener clientSectionClickListener) {

    }

    @Override
    public void setupListView(ANCSmartRegisterClient client, NativeANCSmartRegisterViewHolder viewHolder, View.OnClickListener clientSectionClickListener) {

    }

    @Override
    public void setupListView(FPSmartRegisterClient client, NativeFPSmartRegisterViewHolder viewHolder, View.OnClickListener clientSectionClickListener) {

    }

    @Override
    public void setupListView(PNCSmartRegisterClient client, NativePNCSmartRegisterViewHolder viewHolder, View.OnClickListener clientSectionClickListener) {

    }


}
