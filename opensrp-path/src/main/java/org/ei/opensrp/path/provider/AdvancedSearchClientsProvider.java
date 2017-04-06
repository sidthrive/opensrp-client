package org.ei.opensrp.path.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.fragment.AdvancedSearchFragment;
import org.ei.opensrp.path.repository.VaccineRepository;
import org.ei.opensrp.path.repository.WeightRepository;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.view.contract.SmartRegisterClient;

/**
 * Created by Keyman on 06-Apr-17.
 */
public class AdvancedSearchClientsProvider extends ChildSmartClientsProvider {
    private final Context context;
    private final View.OnClickListener onClickListener;

    public AdvancedSearchClientsProvider(Context context, View.OnClickListener onClickListener,
                                         AlertService alertService, VaccineRepository vaccineRepository, WeightRepository weightRepository) {
        super(context, onClickListener, alertService, vaccineRepository, weightRepository);
        this.onClickListener = onClickListener;
        this.context = context;

    }

    public void getView(Cursor cursor, SmartRegisterClient client, View convertView) {
        super.getView(client, convertView);

        //TODO check if record exists ...
        if (cursor instanceof AdvancedSearchFragment.AdvancedMatrixCursor) {
            TextView recordWeightText = (TextView) convertView.findViewById(R.id.record_weight_text);
            recordWeightText.setText("Record\nservice");

            View recordWeight = convertView.findViewById(R.id.record_weight);
            recordWeight.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
            recordWeight.setTag(client);
            recordWeight.setClickable(true);
            recordWeight.setEnabled(true);
            recordWeight.setOnClickListener(onClickListener);

            Button recordVaccination = (Button) convertView.findViewById(R.id.record_vaccination);
            recordVaccination.setText("Move to my\ncatchment");
            recordVaccination.setClickable(true);
            recordVaccination.setEnabled(true);
            recordVaccination.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            recordVaccination.setTextColor(context.getResources().getColor(R.color.text_black));
            recordVaccination.setBackground(context.getResources().getDrawable(R.drawable.record_weight_bg));
            recordVaccination.setOnClickListener(onClickListener);
        }

    }

    @Deprecated
    @Override
    public void getView(SmartRegisterClient client, View convertView) {
        super.getView(client, convertView);
    }
}