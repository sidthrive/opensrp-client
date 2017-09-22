package org.ei.opensrp.ddtk.parana;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.ei.opensrp.ddtk.R;
import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.util.Log;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.joda.time.LocalDateTime.parse;

/**
 * Created by Dimas Ciputra on 3/4/15.
 */
public class KIParanaClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private Drawable iconPencilDrawable;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    AlertService alertService;
    public KIParanaClientsProvider(Context context,
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
            viewHolder.profilelayout = (LinearLayout) convertView.findViewById(R.id.profile_info_layout);

            viewHolder.wife_name = (TextView) convertView.findViewById(R.id.wife_name);
            viewHolder.husband_name = (TextView) convertView.findViewById(R.id.txt_husband_name);
            viewHolder.village_name = (TextView) convertView.findViewById(R.id.txt_village_name);
            viewHolder.wife_age = (TextView) convertView.findViewById(R.id.wife_age);

            viewHolder.mmn_date = (TextView) convertView.findViewById(R.id.lbl_tgl);
            viewHolder.txt_total = (TextView) convertView.findViewById(R.id.txt_total);


            viewHolder.hr_badge = (ImageView) convertView.findViewById(R.id.img_hr_badge);
         //   viewHolder.img_hrl_badge = (ImageView) convertView.findViewById(R.id.img_hrl_badge);
            viewHolder.bpl_badge = (ImageView) convertView.findViewById(R.id.img_bpl_badge);
          //  viewHolder.txt_total = (ImageView) convertView.findViewById(R.id.txt_total);
          //  viewHolder.hrpp_badge = (ImageView) convertView.findViewById(R.id.img_hrpp_badge);

            viewHolder.tgl1 = (TextView) convertView.findViewById(R.id.txt_tgl1);
            viewHolder.tgl2 = (TextView) convertView.findViewById(R.id.txt_tgl2);
            viewHolder.tgl3 = (TextView) convertView.findViewById(R.id.txt_tgl3);
            viewHolder.tgl4 = (TextView) convertView.findViewById(R.id.txt_tgl4);

            viewHolder.sesi1 = (ImageView) convertView.findViewById(R.id.image1);
            viewHolder.sesi2 = (ImageView) convertView.findViewById(R.id.image2);
            viewHolder.sesi3 = (ImageView) convertView.findViewById(R.id.image3);
            viewHolder.sesi4 = (ImageView) convertView.findViewById(R.id.image4);


            viewHolder.parana1 = (LinearLayout) convertView.findViewById(R.id.parana1);
            viewHolder.parana2 = (LinearLayout) convertView.findViewById(R.id.parana2);
            viewHolder.parana3 = (LinearLayout) convertView.findViewById(R.id.parana3);
            viewHolder.parana4 = (LinearLayout) convertView.findViewById(R.id.parana4);

            viewHolder.profilepic = (ImageView) convertView.findViewById(R.id.img_profile);
            viewHolder.follow_up = (ImageButton) convertView.findViewById(R.id.btn_edit);

            viewHolder.img_p_red_badge = (ImageView) convertView.findViewById(R.id.img_p_red_badge);
            viewHolder.img_p_yellow_badge = (ImageView) convertView.findViewById(R.id.img_p_yellow_badge);

            viewHolder.img_p_red_badge.setVisibility(View.INVISIBLE);
            viewHolder.img_p_yellow_badge.setVisibility(View.INVISIBLE);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.woman_placeholder));

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
        //set image
        DetailsRepository detailsRepository = org.ei.opensrp.Context.getInstance().detailsRepository();
        detailsRepository.updateDetails(pc);
        AllCommonsRepository kiRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ibu");

        CommonPersonObject kiobject = kiRepository.findByCaseID(pc.entityId());
        

        viewHolder.wife_name.setText(pc.getColumnmaps().get("namalengkap")!=null?pc.getColumnmaps().get("namalengkap"):"");
        viewHolder.husband_name.setText(pc.getColumnmaps().get("namaSuami")!=null?pc.getColumnmaps().get("namaSuami"):"");
        viewHolder.village_name.setText(pc.getDetails().get("address1")!=null?pc.getDetails().get("address1"):"");
        viewHolder.wife_age.setText(pc.getDetails().get("umur")!=null?pc.getDetails().get("umur"):"");

        viewHolder.mmn_date.setText(pc.getDetails().get("anc_date")!=null?pc.getDetails().get("anc_date"):"");
        viewHolder.txt_total.setText(pc.getDetails().get("jumlahMmn")!=null?pc.getDetails().get("jumlahMmn"):"");

        viewHolder.tgl1.setText("");
        viewHolder.tgl2.setText("");
        viewHolder.tgl3.setText("");
        viewHolder.tgl4.setText("");
        viewHolder.sesi1.setVisibility(View.INVISIBLE);
        viewHolder.sesi2.setVisibility(View.INVISIBLE);
        viewHolder.sesi3.setVisibility(View.INVISIBLE);
        viewHolder.sesi4.setVisibility(View.INVISIBLE);

        viewHolder.parana1.setBackgroundColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
        viewHolder.parana2.setBackgroundColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
        viewHolder.parana3.setBackgroundColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
        viewHolder.parana4.setBackgroundColor(context.getResources().getColor(R.color.status_bar_text_almost_white));
       String invitation = pc.getDetails().get("date_invitation")!=null?pc.getDetails().get("date_invitation"):"";

        if(StringUtils.isNotBlank(pc.getDetails().get("date_invitation"))) {
            String _invitation = invitation;
            String _due_invitation = "";
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            LocalDate date = parse(_invitation, formatter).toLocalDate();
            LocalDate dateNow = LocalDate.now();
            Log.logInfo("Today"+dateNow);
            Log.logInfo("date"+date);
            date = date.withDayOfMonth(1);
            dateNow = dateNow.withDayOfMonth(1);
            int days = Days.daysBetween(date,dateNow).getDays();
            Log.logInfo(""+days);
            //int months = Months.monthsBetween(dateNow, date).ge();
            if(days < 14) {
                if(pc.getDetails().get("paranaStatus1") == null || pc.getDetails().get("aktif_sesi1" )  == null){
                    viewHolder.parana1.setBackgroundColor(context.getResources().getColor(R.color.alert_urgent_red));
                }
                //setTextColor(context.getResources().getColor(R.color.alert_in_progress_blue));
            }
/*            if(days >=7 && days < 14) {
                if(pc.getDetails().get("paranaStatus1") == null){
                    viewHolder.parana1.setBackgroundColor(context.getResources().getColor(R.color.alert_in_progress_blue));
                }
                        //setTextColor(context.getResources().getColor(R.color.alert_in_progress_blue));
            }*/ else if(days >=14 && days < 21){
                if(pc.getDetails().get("paranaStatus1") == null || pc.getDetails().get("aktif_sesi1" )  == null){
                    viewHolder.parana1.setBackgroundColor(context.getResources().getColor(R.color.alert_urgent_red));
                }
                else if(pc.getDetails().get("paranaStatus2") == null || pc.getDetails().get("aktif_sesi2" )  == null){
                    viewHolder.parana2.setBackgroundColor(context.getResources().getColor(R.color.alert_in_progress_blue));
                }
            }
            else if(days >=21 && days < 28){
                if(pc.getDetails().get("paranaStatus2") == null || pc.getDetails().get("aktif_sesi2" )  == null) {
                    viewHolder.parana2.setBackgroundColor(context.getResources().getColor(R.color.alert_urgent_red));
                }
                else if(pc.getDetails().get("paranaStatus3") == null || pc.getDetails().get("aktif_sesi3" )  == null) {
                    viewHolder.parana3.setBackgroundColor(context.getResources().getColor(R.color.alert_in_progress_blue));
                }

            }
            else if(days >=28 && days < 35){
                if(pc.getDetails().get("paranaStatus3") == null || pc.getDetails().get("aktif_sesi3" )  == null) {
                    viewHolder.parana3.setBackgroundColor(context.getResources().getColor(R.color.alert_urgent_red));
                }
                else if(pc.getDetails().get("paranaStatus4") == null || pc.getDetails().get("aktif_sesi4" )  == null) {
                    viewHolder.parana4.setBackgroundColor(context.getResources().getColor(R.color.alert_in_progress_blue));
                }
            }
            else if(days >=35){
                if(pc.getDetails().get("paranaStatus4") == null || pc.getDetails().get("aktif_sesi14" )  == null) {
                    viewHolder.parana4.setBackgroundColor(context.getResources().getColor(R.color.alert_urgent_red));
                }
            }

        }
        else{
//            viewHolder.edd_due.setText("-");
        }


        Status_parana(pc.getDetails().get("aktif_sesi1") != null? pc.getDetails().get("aktif_sesi1")
                :pc.getDetails().get("paranaStatus1"),
                pc.getDetails().get("tanggal_sesi1"),
                viewHolder.tgl1,viewHolder.sesi1);
        Status_parana(pc.getDetails().get("aktif_sesi2")!= null? pc.getDetails().get("aktif_sesi2")
                :pc.getDetails().get("paranaStatus1"),pc.getDetails().get("tanggal_sesi2"),viewHolder.tgl2,viewHolder.sesi2);
        Status_parana(pc.getDetails().get("aktif_sesi3")!= null? pc.getDetails().get("aktif_sesi3")
                :pc.getDetails().get("paranaStatus3"), pc.getDetails().get("tanggal_sesi3"),viewHolder.tgl3,viewHolder.sesi3);
        Status_parana(pc.getDetails().get("aktif_sesi4")!= null? pc.getDetails().get("aktif_sesi4")
                :pc.getDetails().get("paranaStatus4"), pc.getDetails().get("tanggal_sesi4"),viewHolder.tgl4,viewHolder.sesi4);



        convertView.setLayoutParams(clientViewLayoutParams);
        int baselineIt = 0, endlineIT = 0;
        for (int i = 1 ; i <=45 ; i++){
            String home_endline = "home"+i+"_it";
            if(pc.getDetails().get(home_endline) !=null) {
                if (pc.getDetails().get(home_endline).equalsIgnoreCase("Yes"))
                    baselineIt++;
            }
            if(pc.getDetails().get(home_endline+"_end") !=null){
                if(pc.getDetails().get(home_endline+"_end") !=null)
                    endlineIT++;
            }


        }

        int baselineEC = 0,endlineEC=0;
        for (int i = 1 ; i <=45 ; i++){
            String home_endline = "home"+i+"_ec";
            if(pc.getDetails().get(home_endline) !=null) {
                if (pc.getDetails().get(home_endline).equalsIgnoreCase("Yes"))
                    baselineEC++;
            }
            if(pc.getDetails().get(home_endline+"_end")!=null){
                if(pc.getDetails().get(home_endline+"_end")!=null)
                    endlineEC++;
            }
        }

        int counter = 0;
        if(pc.getDetails().get("umur") != null ? isTooYoungMother(pc.getDetails().get("umur")) : false){
            counter++;
        }
        if(pc.getDetails().get("hidup") != null ? isTooManyChildren(pc.getDetails().get("hidup")) : false){
            counter++;
        }
        if(pc.getDetails().get("pendidikan") != null ? isLowEducated(pc.getDetails().get("pendidikan")) : false){
            counter++;
        }
        if(pc.getDetails().get("gravida") != null ? isPrimigravida(pc.getDetails().get("gravida")) : false){
            counter++;
        }

        if(counter>2)
            viewHolder.img_p_red_badge.setVisibility(View.VISIBLE);
        else if(counter>0)
            viewHolder.img_p_yellow_badge.setVisibility(View.VISIBLE);

        //   return convertView;
    }
    // CommonPersonObjectController householdelcocontroller;


    //    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    private void Status_parana(String status, String date,TextView dates, ImageView emoticon){
        if(status != null && status.equalsIgnoreCase("Yes")){

            emoticon.setImageResource(R.drawable.senyum);
            emoticon.setVisibility(View.VISIBLE);
            dates.setText("Tgl : "+date);
        }
        if(status != null && status.equalsIgnoreCase("No")){
            emoticon.setImageResource(R.drawable.sedih);
            emoticon.setVisibility(View.VISIBLE);
            dates.setText("Tgl : "+date);
        }
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
        View View = (ViewGroup) inflater().inflate(R.layout.smart_register_parana_client, null);
        return View;
    }

    // ---------------------------- RISK FLAG HANDLER -------------------------------------------

    // ------------------------ RISK FLAG CLASSIFICATION ----------------------------------------

    private boolean isPrimigravida(String gravida){
        return Integer.parseInt(gravida)==1;
    }

    private boolean isLowEducated(String education){
        return !education.toLowerCase().contains("tinggi");
    }

    private boolean isTooManyChildren(String children){
        return Integer.parseInt(children)>3;
    }

    private boolean isTooYoungMother(String birthDate){
        return Integer.parseInt(birthDate)<20;
    }

    private boolean isTooYoungMother(int age){
        return age<20;
    }

    private boolean isMalnourished(boolean bgm, boolean yellow){
        return bgm || yellow;
    }

    private boolean isLowHomeScore(CommonPersonObjectClient pc){
        int baselineCount_it = 0, baselineCount_ec = 0;
        for(int i=1;i<=45;i++){
            if(pc.getDetails().get("home"+i+"_it") != null){
                if(pc.getDetails().get("home"+i+"_it").toLowerCase().contains("yes"))
                    baselineCount_it++;
            }
            if(pc.getDetails().get("home"+i+"_ec") != null) {
                if (pc.getDetails().get("home" + i + "_ec").toLowerCase().contains("yes"))
                    baselineCount_ec++;
            }
        }

        return isLowHomeScore(baselineCount_it,baselineCount_ec);

    }

    private boolean isLowHomeScore(int baselineCount_it, int baselineCount_ec){
        return (baselineCount_it<22 || baselineCount_ec<34);
    }

    private int age(String date){
        if(date.toLowerCase().contains("t"))
            date = date.substring(0,10);

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return  (Integer.parseInt(today.substring(0,4)) - Integer.parseInt(date.substring(0,4))) -
                (Integer.parseInt(today.substring(5,7)) - Integer.parseInt(date.substring(5,7))<0 ? 1 : 0);
    }

    // ------------------------------- HIGH RISK FLAG MANAGER -------------------------------------

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
        TextView kb_method;
        TextView usia_klinis;
        TextView htpt;
        TextView ki_lila_bb;
        TextView beratbadan_tb;
        TextView anc_penyakit_kronis;
        TextView status_type;
        TextView status_date;
        TextView alert_status;
        RelativeLayout status_layout;
        TextView tanggal_kunjungan_anc;
        TextView anc_number;
        TextView kunjugan_ke;
        ImageView hr_badge  ;
        ImageView hp_badge;
        ImageView hrpp_badge;
        ImageView bpl_badge;
        ImageView hrp_badge;
        ImageView img_hrl_badge;
        ImageView img_p_yellow_badge;
        ImageView img_p_red_badge;

        TextView edd_due;
        TextView tgl1;
        TextView tgl2;
        TextView tgl3;
        TextView tgl4;

        ImageView sesi1;
        ImageView sesi2;
        ImageView sesi3;
        ImageView sesi4;

        public TextView mmn_date;
        public TextView txt_total;
        public LinearLayout parana1;

        public LinearLayout parana2;

        public LinearLayout parana3;

        public LinearLayout parana4;
    }


}