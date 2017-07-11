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
            viewHolder.adult_hh_member = (TextView)convertView.findViewById(R.id.adult_hh_member);
            viewHolder.child_hh_member_under_5 = (TextView)convertView.findViewById(R.id.child_hh_member_under_5);

         viewHolder.household_size = (TextView)convertView.findViewById(R.id.household_size);
          //  viewHolder.child_hh_member_6 = (TextView)convertView.findViewById(R.id.child_hh_member_6);
               viewHolder.anc_visit_num = (TextView)convertView.findViewById(R.id.anc_visit_num);
            viewHolder.attendance_at_posyandu = (TextView)convertView.findViewById(R.id.attendance_at_posyandu);
            viewHolder.attendance_at_puskesmas =(TextView)convertView.findViewById(R.id.attendance_at_puskesmas);

            viewHolder.have_mch_book =(TextView)convertView.findViewById(R.id.have_mch_book);
            viewHolder.bcg =(TextView)convertView.findViewById(R.id.bcg);
            viewHolder.pol =(TextView)convertView.findViewById(R.id.pol);
            viewHolder.dpt =(TextView)convertView.findViewById(R.id.dpt);

          viewHolder.toilet = (TextView)convertView.findViewById(R.id.toilet);

            viewHolder.icon_hb0_no = (ImageView)convertView.findViewById(R.id.icon_hb0_no);
            viewHolder.icon_hb0_yes = (ImageView)convertView.findViewById(R.id.icon_hb0_yes);
            viewHolder.icon_pol1_yes = (ImageView)convertView.findViewById(R.id.icon_pol1_yes);
            viewHolder.icon_pol1_no = (ImageView)convertView.findViewById(R.id.icon_pol1_no);

            viewHolder.id_detail_layout =  (LinearLayout)convertView.findViewById(R.id.id_detail_layout);
            viewHolder.pol1Logo = (ImageView) convertView.findViewById(R.id.pol1Logo);
            viewHolder.pol1Logo1 = (ImageView) convertView.findViewById(R.id.pol1Logo1);
            viewHolder.detail_layout_logo = (ImageView) convertView.findViewById(R.id.detail_layout_logo);
            viewHolder.detail_layout_logo1 = (ImageView) convertView.findViewById(R.id.detail_layout_logo1);
            viewHolder.attend = (ImageView) convertView.findViewById(R.id.attend);
            viewHolder.attend1 = (ImageView) convertView.findViewById(R.id.attend1);

           /* viewHolder.knowlegde = (TextView)convertView.findViewById(R.id.knowlegde);
            viewHolder.attitude = (TextView)convertView.findViewById(R.id.attitude);
            viewHolder.information_source = (TextView)convertView.findViewById(R.id.information_source);*/


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



//        viewHolder.immulogo.setVisibility(View.GONE);
        viewHolder.detail_layout_logo.setVisibility(View.GONE);

        viewHolder.detail_layout_logo1.setVisibility(View.GONE);
        viewHolder.pol1Logo.setVisibility(View.GONE);
        viewHolder.pol1Logo1.setVisibility(View.GONE);
         viewHolder.attend.setVisibility(View.GONE);
        viewHolder.attend1.setVisibility(View.GONE);
        viewHolder.household_size.setText("");
        viewHolder.adult_hh_member.setText("");
        viewHolder.child_hh_member_under_5.setText("");
//        viewHolder.child_hh_member_6.setText("");
        viewHolder.anc_visit_num.setText("");
        viewHolder.attendance_at_posyandu.setText("");
        viewHolder.attendance_at_puskesmas.setText("");
        viewHolder.bcg.setText("");
        viewHolder.pol.setText("");
        viewHolder.dpt.setText("");

//        viewHolder.knowlegde.setText("");
 //       viewHolder.attitude.setText("");
//        viewHolder.information_source.setText("");
        viewHolder.have_mch_book.setText("");

        if(pc.getColumnmaps().get("relation_to_child").equalsIgnoreCase("mother") || pc.getColumnmaps().get("relation_to_child").equalsIgnoreCase("female-care_giver") ){
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.woman_placeholder));
        }
        else{
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile));

        }

        //}

        viewHolder.wife_name.setText(pc.getColumnmaps().get("respondent_name")!=null?pc.getColumnmaps().get("respondent_name"):"");
        viewHolder.husband_name.setText(pc.getColumnmaps().get("relation_to_child")!=null?pc.getColumnmaps().get("relation_to_child").replace("_"," "):"");
        viewHolder.village_name.setText(pc.getDetails().get("Sub-village")!=null?pc.getDetails().get("Sub-village").replace("_"," "):"");
        viewHolder.wife_age.setText(pc.getColumnmaps().get("respondent_age")!=null?pc.getColumnmaps().get("respondent_age"):"");

        /**
         * HouseHold Characteristic
         */
        if(pc.getDetails().get("IsDraft2") != null){
            if(pc.getDetails().get("IsDraft2").equalsIgnoreCase("2")) {
                viewHolder.household_size.setText(pc.getDetails().get("household_size") != null ? "Household size : " + pc.getDetails().get("household_size") : "");
                viewHolder.adult_hh_member.setText(pc.getDetails().get("adult_hh_member") != null ? "Adult members : " + pc.getDetails().get("adult_hh_member") : "");
                viewHolder.child_hh_member_under_5.setText(pc.getDetails().get("child_hh_member_under_5") != null ? "child  members (<5): " + pc.getDetails().get("child_hh_member_under_5") : "");
                //    viewHolder.child_hh_member_6.setText(pc.getDetails().get("child_hh_member_6-14") != null ? "child  members (6-14): " + pc.getDetails().get("child_hh_member_6-14") : "");

            }
            else if(pc.getDetails().get("IsDraft2").equalsIgnoreCase("1")){
                viewHolder.detail_layout_logo1.setVisibility(View.VISIBLE);
                viewHolder.adult_hh_member.setText("IS DRAFT");
                viewHolder.household_size.setVisibility(View.GONE);

            }
        }
        else{
            viewHolder.detail_layout_logo.setVisibility(View.VISIBLE);
       //     viewHolder.detail_layout_logo.setBackgroundResource(R.mipmap.vacc_late);
          //  viewHolder.detail_layout_logo.set

        }

        /**
         * Health seeking behaviour
        */
        if(pc.getDetails().get("IsDraft3") != null) {
            if (pc.getDetails().get("IsDraft3").equalsIgnoreCase("2")) {
                    viewHolder.anc_visit_num.setText(pc.getDetails().get("anc_visit_num") != null ? "ANC Visit :" + pc.getDetails().get("anc_visit_num").replace("_", " ") : "");
                    viewHolder.attendance_at_posyandu.setText(pc.getDetails().get("attendance_at_posyandu") != null ? "Posyandu: " + pc.getDetails().get("attendance_at_posyandu").replace("_", " ") : "");
                    viewHolder.attendance_at_puskesmas.setText(pc.getDetails().get("attendance_at_puskesmas") != null ? "Puskesmas: " + pc.getDetails().get("attendance_at_puskesmas").replace("_", " ") : "");
                    //  viewHolder.pol1Logo.setVisibility(View.GONE);
            }
            else if(pc.getDetails().get("IsDraft3").equalsIgnoreCase("1")){
                viewHolder.pol1Logo1.setVisibility(View.VISIBLE);
                viewHolder.attendance_at_posyandu.setText("IS DRAFT");
                viewHolder.anc_visit_num.setVisibility(View.GONE);

            }
        }
        else{
            viewHolder.pol1Logo.setVisibility(View.VISIBLE);
        //    viewHolder.pol1Logo.setBackgroundResource(R.mipmap.vacc_late);
         //   viewHolder.connect.setWeightSum(40);

        }

        /**
         * Immunization coverage
         */
        if(pc.getDetails().get("IsDraft4") != null) {
            if (pc.getDetails().get("IsDraft4").equalsIgnoreCase("2")) {
                if (pc.getDetails().get("have_mch_book").equalsIgnoreCase("Yes_shown")) {
                    viewHolder.have_mch_book.setText(pc.getDetails().get("have_mch_book") != null ? "MCH Book :" + pc.getDetails().get("have_mch_book").replace("_", " ") : "");
                    viewHolder.bcg.setText(pc.getDetails().get("bcg") != null ? "BCG: " + pc.getDetails().get("bcg").replace("_", " ") : "");
                    if (pc.getDetails().get("polio_0") != null || pc.getDetails().get("polio_1") != null
                            || pc.getDetails().get("polio_2") != null || pc.getDetails().get("polio_3") != null) {
                        if (pc.getDetails().get("polio_0").equalsIgnoreCase("yes") || pc.getDetails().get("polio_1").equalsIgnoreCase("yes")
                                || pc.getDetails().get("polio_2").equalsIgnoreCase("yes") || pc.getDetails().get("polio_3").equalsIgnoreCase("yes")) {
                            viewHolder.pol.setText("Polio : Yes");
                        }
                    }
                    if (pc.getDetails().get("dpt_1") != null || pc.getDetails().get("dpt_2") != null
                            || pc.getDetails().get("dpt_3") != null) {
                        if (pc.getDetails().get("dpt_1").equalsIgnoreCase("yes") || pc.getDetails().get("dpt_2").equalsIgnoreCase("yes")
                                || pc.getDetails().get("dpt_3").equalsIgnoreCase("yes")) {
                            viewHolder.dpt.setText("DPT : Yes");
                        }
                    }
                    // viewHolder.dpt.setText(pc.getDetails().get("dpt_3") != null ? "DPT : " + pc.getDetails().get("dpt_3") : "");
                } else {
                    viewHolder.have_mch_book.setText(pc.getDetails().get("have_mch_book") != null ? "MCH Book :" + pc.getDetails().get("have_mch_book").replace("_", " ") : "");
                    viewHolder.bcg.setText(pc.getDetails().get("A_BCG_vaccination") != null ? "BCG: " + pc.getDetails().get("A_BCG_vaccination").replace("_", " ") : "");
                    viewHolder.pol.setText(pc.getDetails().get("Polio_vaccine") != null ? "Polio : " + pc.getDetails().get("Polio_vaccine").replace("_", " ") : "");
                    viewHolder.dpt.setText(pc.getDetails().get("A_DPT_vaccination") != null ? "DPT : " + pc.getDetails().get("A_DPT_vaccination") : "");
                }
            }
            else if(pc.getDetails().get("IsDraft4").equalsIgnoreCase("1")){
                viewHolder.attend1.setVisibility(View.VISIBLE);
                viewHolder.bcg.setText("IS DRAFT");
                viewHolder.have_mch_book.setVisibility(View.GONE);

            }
        }
        else{
           // attend
            viewHolder.attend.setVisibility(View.VISIBLE);
        //    viewHolder.attend.setBackgroundResource(R.mipmap.vacc_late);
        }

        /**
         * Knowledge and attitute
         */
        //immunization
        if(pc.getDetails().get("heard_about_immu")!=null
                || pc.getDetails().get("information_source")!=null
                || pc.getDetails().get("other_source")!=null){
            viewHolder.icon_hb0_no.setVisibility(View.INVISIBLE);
            viewHolder.icon_hb0_yes.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.icon_hb0_no.setVisibility(View.VISIBLE);
            viewHolder.icon_hb0_yes.setVisibility(View.INVISIBLE);
        }

        if(pc.getDetails().get("attitude_1")!=null || pc.getDetails().get("attitude_2")!=null
                || pc.getDetails().get("attitude_4")!=null  || pc.getDetails().get("attitude_3")!=null){
            viewHolder.icon_pol1_no.setVisibility(View.INVISIBLE);
            viewHolder.icon_pol1_yes.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.icon_pol1_no.setVisibility(View.VISIBLE);
            viewHolder.icon_pol1_yes.setVisibility(View.INVISIBLE);
        }


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
        TextView adult_hh_member;
        TextView child_hh_member_under_5;
        TextView anc_visit_num;
        TextView attendance_at_posyandu;
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
        public TextView household_size;
        public TextView label_child_hh_member_under_5;
        public TextView child_hh_member_6;
        public TextView attendance_at_puskesmas;
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
        public TextView have_mch_book;
        public TextView bcg;
        public TextView pol;
        public TextView dpt;
        public ImageView icon_hb0_no;
        public ImageView icon_hb0_yes;
        public ImageView icon_pol1_yes;
        public ImageView icon_pol1_no;
        public TextView knowlegde;
        public TextView attitude;
        public TextView information_source;
        public ImageView immu_know;
        public ImageView detail_layout_logo1;
        public ImageView pol1Logo1;
        public ImageView attend1;
    }


}