package org.ei.opensrp.unicef.rcc.HH;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.ei.opensrp.unicef.rcc.R;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.joda.time.LocalDateTime.parse;

/**
 * Created by Dimas Ciputra on 2/16/15.
 */
public class HHClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private Drawable iconPencilDrawable;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    AlertService alertService;
    public HHClientsProvider(Context context,
                             View.OnClickListener onClickListener,
                             AlertService alertService) {
        this.onClickListener = onClickListener;
//        this.controller = controller;
        this.context = context;
        this.alertService = alertService;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) context.getResources().getDimension(org.ei.opensrp.R.dimen.list_item_height));
        txtColorBlack = context.getResources().getColor(org.ei.opensrp.R.color.text_black);

    }

    @Override
    public void getView(SmartRegisterClient smartRegisterClient, View convertView) {
        ViewHolder viewHolder;
        if(convertView.getTag() == null || !(convertView.getTag() instanceof  ViewHolder)) {
            viewHolder = new ViewHolder();
            viewHolder.profilelayout =  (LinearLayout)convertView.findViewById(R.id.profile_info_layout);
            viewHolder.wife_name = (TextView)convertView.findViewById(R.id.wife_name);
            viewHolder.husband_name = (TextView)convertView.findViewById(R.id.txt_husband_name);
            viewHolder.village_name = (TextView)convertView.findViewById(R.id.txt_village_name);
            viewHolder.wife_age = (TextView)convertView.findViewById(R.id.wife_age);
            viewHolder.no_ibu = (TextView)convertView.findViewById(R.id.no_ibu);
            viewHolder.unique_id = (TextView)convertView.findViewById(R.id.unique_id);

         viewHolder.label_no_ibu = (TextView)convertView.findViewById(R.id.label_no_ibu);
            viewHolder.label_unique_id = (TextView)convertView.findViewById(R.id.label_unique_id);
               viewHolder.edd = (TextView)convertView.findViewById(R.id.txt_edd);
            viewHolder.edd_due = (TextView)convertView.findViewById(R.id.txt_edd_due);
            viewHolder.txt_expen =(TextView)convertView.findViewById(R.id.txt_expen);
            viewHolder.anc_tot =(TextView)convertView.findViewById(R.id.asdd);
            viewHolder.last_time =(TextView)convertView.findViewById(R.id.last_time);
            viewHolder.services =(TextView)convertView.findViewById(R.id.services);
            viewHolder.distance =(TextView)convertView.findViewById(R.id.distance);
          viewHolder.toilet = (TextView)convertView.findViewById(R.id.toilet);

            viewHolder.imm1 = (TextView)convertView.findViewById(R.id.imm1);
            viewHolder.imm2 = (TextView)convertView.findViewById(R.id.imm2);
            viewHolder.imm3 = (TextView)convertView.findViewById(R.id.imm3);
            viewHolder.imm4 = (TextView)convertView.findViewById(R.id.imm4);

            viewHolder.id_detail_layout =  (LinearLayout)convertView.findViewById(R.id.id_detail_layout);
            viewHolder.pol1Logo = (ImageView) convertView.findViewById(R.id.pol1Logo);
            viewHolder.detail_layout_logo = (ImageView) convertView.findViewById(R.id.detail_layout_logo);
            viewHolder.attend = (ImageView) convertView.findViewById(R.id.attend);
            viewHolder.immulogo = (ImageView) convertView.findViewById(R.id.immulogo);

            viewHolder.connect =  (LinearLayout)convertView.findViewById(R.id.connect);
            viewHolder.profilepic =(ImageView)convertView.findViewById(R.id.img_profile);
            viewHolder.follow_up = (ImageButton)convertView.findViewById(R.id.btn_edit);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.follow_up.setOnClickListener(onClickListener);
        viewHolder.follow_up.setTag(smartRegisterClient);
        viewHolder.profilelayout.setOnClickListener(onClickListener);
        viewHolder.profilelayout.setTag(smartRegisterClient);
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (iconPencilDrawable == null) {
            iconPencilDrawable = context.getResources().getDrawable(R.drawable.ic_pencil);
        }
        viewHolder.follow_up.setImageDrawable(iconPencilDrawable);
        viewHolder.follow_up.setOnClickListener(onClickListener);



        viewHolder.immulogo.setVisibility(View.GONE);
        viewHolder.detail_layout_logo.setVisibility(View.GONE);
        viewHolder.pol1Logo.setVisibility(View.GONE);
         viewHolder.attend.setVisibility(View.GONE);
        viewHolder.label_no_ibu.setText("");
        viewHolder.no_ibu.setText("");
        viewHolder.unique_id.setText("");
        viewHolder.edd.setText("");
        viewHolder.edd_due.setText("");
        viewHolder.txt_expen.setText("");
        viewHolder.anc_tot.setText("");
        viewHolder.last_time.setText("");
        viewHolder.services.setText("");
        viewHolder.distance.setText("");
        viewHolder.imm1.setText("");
        viewHolder.imm2.setText("");
        viewHolder.imm3.setText("");
        viewHolder.imm4.setText("");

        if(pc.getDetails().get("relation_to_child").equalsIgnoreCase("mother") || pc.getDetails().get("relation_to_child").equalsIgnoreCase("female-care_giver") ){
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.woman_placeholder));
        }
        else{
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile));

        }

        //}

        viewHolder.wife_name.setText(pc.getColumnmaps().get("respondent_name")!=null?pc.getColumnmaps().get("respondent_name"):"");
        viewHolder.husband_name.setText(pc.getDetails().get("relation_to_child")!=null?pc.getDetails().get("relation_to_child").replace("_"," "):"");
        viewHolder.village_name.setText(pc.getDetails().get("Sub-village")!=null?pc.getDetails().get("Sub-village").replace("_"," "):"");
        viewHolder.wife_age.setText(pc.getDetails().get("respondent_age")!=null?pc.getDetails().get("respondent_age"):"");

        if(pc.getDetails().get("household_size") != null || pc.getDetails().get("adult_hh_member") != null) {
            viewHolder.label_no_ibu.setText(pc.getDetails().get("household_size") != null ? "Household size : " + pc.getDetails().get("household_size") : "");
            viewHolder.no_ibu.setText(pc.getDetails().get("adult_hh_member") != null ? "Adult members : " + pc.getDetails().get("adult_hh_member") : "");
            viewHolder.unique_id.setText(pc.getDetails().get("child_hh_member_under_5") != null ? "child  members (<5): " + pc.getDetails().get("child_hh_member_under_5") : "");
       //     viewHolder.detail_layout_logo.setVisibility(View.GONE);
            viewHolder.label_unique_id.setText(pc.getDetails().get("child_hh_member_6-14") != null ? "child  members (6-14): " + pc.getDetails().get("child_hh_member_6-14") : "");
          //  viewHolder.label_no_ibu.setText("adult HH members : ");

        }
        else{
            viewHolder.detail_layout_logo.setVisibility(View.VISIBLE);
       //     viewHolder.detail_layout_logo.setBackgroundResource(R.mipmap.vacc_late);
          //  viewHolder.detail_layout_logo.set

        }

        if(pc.getDetails().get("mobile_connectivity") != null || pc.getDetails().get("cell_provider") != null) {
            viewHolder.edd.setText(pc.getDetails().get("mobile_connectivity") != null ? "Connectivity :" + pc.getDetails().get("mobile_connectivity").replace("_", " ") : "");
            viewHolder.edd_due.setText(pc.getDetails().get("cell_provider") != null ? "Cell provider: " + pc.getDetails().get("cell_provider") : "");
            viewHolder.txt_expen.setText(pc.getDetails().get("expenditure_mobile_phone") != null ? "Expenditure : " + pc.getDetails().get("expenditure_mobile_phone").replace("_", " ") : "");
          //  viewHolder.pol1Logo.setVisibility(View.GONE);
        }
        else{
            viewHolder.pol1Logo.setVisibility(View.VISIBLE);
        //    viewHolder.pol1Logo.setBackgroundResource(R.mipmap.vacc_late);
         //   viewHolder.connect.setWeightSum(40);

        }

        if(pc.getDetails().get("attendance_at_posyandu")!=null || pc.getDetails().get("last_time_to_posyandu")!=null) {
            viewHolder.anc_tot.setText(pc.getDetails().get("attendance_at_posyandu") != null ? "Attendance :" + pc.getDetails().get("attendance_at_posyandu").replace("_", " ") : "");
            viewHolder.last_time.setText(pc.getDetails().get("last_time_to_posyandu") != null ? "Lastattended: " + pc.getDetails().get("last_time_to_posyandu").replace("_", " ") : "");
            viewHolder.services.setText(pc.getDetails().get("posyandu_service") != null ? "Services : " + pc.getDetails().get("posyandu_service").replace("_", " ") : "");
            viewHolder.distance.setText(pc.getDetails().get("nearest_puskesmas") != null ? "Distace : " + pc.getDetails().get("nearest_puskesmas") : "");
        }
        else{
           // attend
            viewHolder.attend.setVisibility(View.VISIBLE);
        //    viewHolder.attend.setBackgroundResource(R.mipmap.vacc_late);
        }
        if(pc.getDetails().get("immunization_knowlegde")!=null) {
          //  viewHolder.immulogo.setVisibility(View.GONE);
            viewHolder.imm1.setText(pc.getDetails().get("immunization_knowlegde") != null ? "Immunzation Knowledge : " + pc.getDetails().get("immunization_knowlegde").replace("_", " ") : "");
            viewHolder.imm2.setText(pc.getDetails().get("source_of_information") != null ? "Source : " + pc.getDetails().get("source_of_information").replace("_", " ") : "");
            viewHolder.imm3.setText(pc.getDetails().get("have_mch_book") != null ? "MCH Book : " + pc.getDetails().get("have_mch_book").replace("_", " ") : "");
            viewHolder.imm4.setText(pc.getDetails().get("caregiver_understand") != null ? "Understand of Growht Chart : " + pc.getDetails().get("caregiver_understand") : "");
        }
        else{
            // attend
            viewHolder.immulogo.setVisibility(View.VISIBLE);
         //   viewHolder.immulogo.setBackgroundResource(R.mipmap.vacc_late);
        }
        /*//immunization
        if(pc.getDetails().get("tanggalpemberianimunisasiHb07")!=null){
            viewHolder.hb0_no.setVisibility(View.INVISIBLE);
            viewHolder.hb0_yes.setVisibility(View.VISIBLE);
        } else {
            viewHolder.hb0_no.setVisibility(View.VISIBLE);
            viewHolder.hb0_yes.setVisibility(View.INVISIBLE);
        }

        if(pc.getDetails().get("first_polio_vaccine_given")!=null){
            viewHolder.pol1_no.setVisibility(View.INVISIBLE);
            viewHolder.pol1_yes.setVisibility(View.VISIBLE);
        } else {
            viewHolder.pol1_no.setVisibility(View.VISIBLE);
            viewHolder.pol1_yes.setVisibility(View.INVISIBLE);
        }

        if(pc.getDetails().get("DPT_vaccine_given")!=null){
            viewHolder.pol2_no.setVisibility(View.INVISIBLE);
            viewHolder.pol2_yes.setVisibility(View.VISIBLE);
        } else {
            viewHolder.pol2_no.setVisibility(View.VISIBLE);
            viewHolder.pol2_yes.setVisibility(View.INVISIBLE);
        }

        if(pc.getDetails().get("tanggalpemberianimunisasiDPTHB2Polio3")!=null){
            viewHolder.pol3_no.setVisibility(View.INVISIBLE);
            viewHolder.pol3_yes.setVisibility(View.VISIBLE);
        } else {
            viewHolder.pol3_no.setVisibility(View.VISIBLE);
            viewHolder.pol3_yes.setVisibility(View.INVISIBLE);
        }*/


        //distance to nearest
        convertView.setLayoutParams(clientViewLayoutParams);
      //  return convertView;
    }
    CommonPersonObjectController householdelcocontroller;
    

    //    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {
        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
        // do nothing.
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        View View = inflater().inflate(R.layout.smart_register_ki_client, null);
        return View;
    }

    class ViewHolder {

        TextView wife_name ;
        TextView husband_name ;
        TextView village_name;
        TextView wife_age;
        LinearLayout profilelayout;
        ImageView profilepic;
        TextView gravida;
        Button warnbutton;
        ImageButton follow_up;
        TextView parity;
        TextView number_of_abortus;
        TextView number_of_alive;
        TextView no_ibu;
        TextView unique_id;
        TextView edd;
        TextView edd_due;
        TextView children_age_left;
        TextView anc_status_layout;
         TextView visit_status;
         TextView date_status;
         TextView children_age_right;
        ImageView hr_badge;
        ImageView hrpp_badge;
        ImageView bpl_badge;
        ImageView hrp_badge;
        ImageView img_hrl_badge;
        public TextView label_no_ibu;
        public TextView label_unique_id;
        public TextView txt_expen;
        public TextView anc_tot;
        public TextView last_time;
        public TextView services;
        public TextView distance;
        public TextView toilet;
        ImageView hb0_no;
        ImageView hb0_yes;
        ImageView pol1_no;
        ImageView pol1_yes;
        ImageView pol2_no;
        ImageView pol2_yes;
        ImageView pol3_no;
        ImageView immulogo;

        TextView imm1;
        TextView imm2;
        TextView imm3;
        TextView imm4;
        public LinearLayout id_detail_layout;
        public LinearLayout connect;
        public ImageView pol1Logo;
        public ImageView detail_layout_logo;
        public ImageView attend;
    }


}