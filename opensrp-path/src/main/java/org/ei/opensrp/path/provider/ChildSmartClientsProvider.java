package org.ei.opensrp.path.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.domain.Weight;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.repository.VaccineRepository;
import org.ei.opensrp.path.repository.WeightRepository;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.util.OpenSRPImageLoader;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import util.DateUtils;
import util.ImageUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static util.Utils.fillValue;
import static util.Utils.getName;
import static util.Utils.getValue;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class ChildSmartClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    AlertService alertService;
    VaccineRepository vaccineRepository;
    WeightRepository weightRepository;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    public ChildSmartClientsProvider(Context context, View.OnClickListener onClickListener,
                                     AlertService alertService, VaccineRepository vaccineRepository, WeightRepository weightRepository) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.alertService = alertService;
        this.vaccineRepository = vaccineRepository;
        this.weightRepository = weightRepository;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT, (int) context.getResources().getDimension(org.ei.opensrp.R.dimen.list_item_height));
    }

    @Override
    public void getView(SmartRegisterClient client, View convertView) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

        fillValue((TextView) convertView.findViewById(R.id.child_zeir_id), getValue(pc.getColumnmaps(), "zeir_id", false));

        String firstName = getValue(pc.getColumnmaps(), "first_name", true);
        String lastName = getValue(pc.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName);

        String motherFirstName = getValue(pc.getColumnmaps(), "mother_first_name", true);
        if (StringUtils.isBlank(childName) && StringUtils.isNotBlank(motherFirstName)) {
            childName = "B/o " + motherFirstName.trim();
        }
        fillValue((TextView) convertView.findViewById(R.id.child_name), childName);

        String motherName = getValue(pc.getColumnmaps(), "mother_first_name", true) + " " + getValue(pc, "mother_last_name", true);
        if (!StringUtils.isNotBlank(motherName)) {
            motherName = "M/G: " + motherName.trim();
        }
        fillValue((TextView) convertView.findViewById(R.id.child_mothername), motherName);

        String gender = getValue(pc.getColumnmaps(), "gender", true);
        ((ImageView) convertView.findViewById(R.id.child_profilepic)).setImageResource(ImageUtils.profileImageResourceByGender(gender));

        String dobString = getValue(pc.getColumnmaps(), "dob", false);
        String duration = "";
        if (StringUtils.isNotBlank(dobString)) {
            DateTime dateTime = new DateTime(getValue(pc.getColumnmaps(), "dob", false));
            duration = DateUtils.getDuration(dateTime);
            if (duration != null) {
                fillValue((TextView) convertView.findViewById(R.id.child_age), duration);
            }
        }

        fillValue((TextView) convertView.findViewById(R.id.child_card_number), pc.getColumnmaps(), "epi_card_number", false);

        if (client.entityId() != null) {//image already in local storage most likey ):
            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
            convertView.findViewById(R.id.child_profilepic).setTag(org.ei.opensrp.R.id.entity_id, pc.getCaseId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.getCaseId(), OpenSRPImageLoader.getStaticImageListener((ImageView) convertView.findViewById(R.id.child_profilepic), ImageUtils.profileImageResourceByGender(gender), ImageUtils.profileImageResourceByGender(gender)));
        }

        convertView.findViewById(R.id.child_profile_info_layout).setTag(client);
        convertView.findViewById(R.id.child_profile_info_layout).setOnClickListener(onClickListener);

        View recordWeight = convertView.findViewById(R.id.record_weight);
        recordWeight.setTag(client);
        recordWeight.setOnClickListener(onClickListener);

        Weight weight = weightRepository.findUnSyncedByEntityId(pc.entityId());
        if (weight != null) {
            TextView recordWeightText = (TextView) convertView.findViewById(R.id.record_weight_text);
            recordWeightText.setText(weight.getKg().toString() + " kg");

            ImageView recordWeightCheck = (ImageView) convertView.findViewById(R.id.record_weight_check);
            recordWeightCheck.setVisibility(View.VISIBLE);
        }

        Button recordVaccination = (Button) convertView.findViewById(R.id.record_vaccination);
        recordVaccination.setTag(client);
        recordVaccination.setOnClickListener(onClickListener);

        convertView.setLayoutParams(clientViewLayoutParams);

        // Alerts
        List<Vaccine> vaccines = vaccineRepository.findByEntityId(pc.entityId());
        Map<String, Triple<Long, Long, String>> map = vaccinesMap(vaccines, dobString);

        State state = State.FULLY_IMMUNIZED;
        String stateKey = null;
        for (Triple<Long, Long, String> triple : map.values()) {
            Date dateDue = new Date(triple.getLeft());

            Date dateDone = null;
            if (triple.getMiddle() > 0l) {
                dateDone = new Date(triple.getMiddle());
            }

            if (dateDone == null) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (dateDue.getTime() < (today.getTimeInMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))) {
                    state = State.OVERDUE;
                    stateKey = triple.getRight();
                    break;
                } else if (dateDue.getTime() >= (today.getTimeInMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) && dateDue.getTime() < (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))) {
                    state = State.DUE;
                    stateKey = triple.getRight();
                    break;
                } else if (dateDue.getTime() >= (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) && dateDue.getTime() < (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))) {
                    state = State.UPCOMING_NEXT_7_DAYS;
                    stateKey = triple.getRight();
                    break;
                } else if (dateDue.getTime() >= (today.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))) {
                    state = State.UPCOMING;
                    stateKey = triple.getRight();
                    break;
                }
            }
        }

        if (state.equals(State.FULLY_IMMUNIZED)) {
            recordVaccination.setText("Fully\nimmunized");
            recordVaccination.setTextColor(context.getResources().getColor(R.color.client_list_grey));
            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_check, 0, 0, 0);
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING)) {
            recordVaccination.setText("Due\n" + stateKey);
            recordVaccination.setTextColor(context.getResources().getColor(R.color.client_list_grey));
            recordVaccination.setBackgroundColor(context.getResources().getColor(R.color.white));
            recordVaccination.setEnabled(false);
        } else if (state.equals(State.UPCOMING_NEXT_7_DAYS)) {
            recordVaccination.setText("Record\n" + stateKey);
            recordVaccination.setTextColor(context.getResources().getColor(R.color.client_list_grey));
            recordVaccination.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.due_vaccine_light_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.DUE)) {
            recordVaccination.setText("Record\n" + stateKey);
            recordVaccination.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
            recordVaccination.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.due_vaccine_blue_bg));
            recordVaccination.setEnabled(true);
        } else if (state.equals(State.OVERDUE)) {
            recordVaccination.setText("Record\n" + stateKey);
            recordVaccination.setTextColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
            recordVaccination.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.due_vaccine_red_bg));
            recordVaccination.setEnabled(true);
        }

    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption
            serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {

    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String
            metaData) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        ViewGroup view = (ViewGroup) inflater().inflate(R.layout.smart_register_child_client, null);
        return view;
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    private Map<String, Triple<Long, Long, String>> vaccinesMap(List<Vaccine> vaccines, String
            dobString) {
        Map<String, Triple<Long, Long, String>> map = new LinkedHashMap<>();

        int daysAfter = 0;
        String text = "at birth";

        map.put("OPV 0", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("BCG", Triple.of(dueDate(dobString, daysAfter), 0l, text));

        daysAfter = 42;
        text = "6 weeks";

        map.put("OPV 1", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("Penta 1", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("PCV 1", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("Rota 1", Triple.of(dueDate(dobString, daysAfter), 0l, text));

        daysAfter = 70;
        text = "10 weeks";

        map.put("OPV 2", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("Penta 2", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("PCV 2", Triple.of(dueDate(dobString, daysAfter), 0l, text));
        map.put("Rota 2", Triple.of(dueDate(dobString, daysAfter), 0l, text));


        if (vaccines != null) {
            for (Vaccine vaccine : vaccines) {
                if (map.containsKey(vaccine.getName()) && vaccine.getDate() != null) {
                    Triple<Long, Long, String> triple = map.get(vaccine.getName());
                    map.put(vaccine.getName(), Triple.of(triple.getLeft(), vaccine.getDate().getTime(), triple.getRight()));
                }
            }
        }
        return map;
    }

    private Long dueDate(String dobString, int daysAfter) {
        if (StringUtils.isNotBlank(dobString)) {
            Calendar dobCalender = Calendar.getInstance();
            DateTime dateTime = new DateTime(dobString);
            dobCalender.setTime(dateTime.toDate());
            dobCalender.add(Calendar.DATE, daysAfter);
            return dobCalender.getTimeInMillis();
        }
        return 0l;
    }

    public enum State {
        DUE,
        OVERDUE,
        UPCOMING_NEXT_7_DAYS,
        UPCOMING,
        INACTIVE,
        FULLY_IMMUNIZED
    }
}