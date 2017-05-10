package org.ei.opensrp.indonesia.child;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.kartu_ibu.KIDetailActivity;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.joda.time.LocalDateTime.parse;

import static org.ei.opensrp.util.StringUtil.humanize;

public class AnakRegisterClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private Drawable iconPencilDrawable;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    AlertService alertService;
    public AnakRegisterClientsProvider(Context context,
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

            viewHolder.childs_name = (TextView) convertView.findViewById(R.id.child_name);
            viewHolder.mother_name = (TextView) convertView.findViewById(R.id.mother_name);
            viewHolder.village_name = (TextView) convertView.findViewById(R.id.txt_village_name);
            viewHolder.childs_age = (TextView) convertView.findViewById(R.id.child_age);
            // viewHolder.no_ibu = (TextView) convertView.findViewById(R.id.txt_ibu_ki_no);
            // viewHolder.unique_id = (TextView)convertView.findViewById(R.id.unique_id);

            viewHolder.hp_badge = (ImageView) convertView.findViewById(R.id.img_hr_badge);

            //delivery documentation
            viewHolder.anak_register_dob = (TextView) convertView.findViewById(R.id.anak_register_dob);/*
            viewHolder.tempat_lahir = (TextView) convertView.findViewById(R.id.tempat_lahir);
            viewHolder.berat_lahir = (TextView) convertView.findViewById(R.id.berat_lahir);
            viewHolder.tipe_lahir = (TextView) convertView.findViewById(R.id.tipe_lahir);*/
            viewHolder.status_gizi = (TextView) convertView.findViewById(R.id.txt_status_gizi);

            viewHolder.icon_salep_yes = (ImageView) convertView.findViewById(R.id.icon_salep_yes);
            viewHolder.icon_salep_no = (ImageView) convertView.findViewById(R.id.icon_salep_no);

            viewHolder.icon_imd_yes = (ImageView) convertView.findViewById(R.id.icon_imd_yes);
            viewHolder.icon_imd_no = (ImageView) convertView.findViewById(R.id.icon_imd_no);




            viewHolder.hb0_no = (ImageView) convertView.findViewById(R.id.icon_immuni_hb_no);
            viewHolder.hb0_yes = (ImageView) convertView.findViewById(R.id.icon_immuni_hb_yes);
            viewHolder.vitk_no = (ImageView) convertView.findViewById(R.id.icon_vit_k_no);
            viewHolder.vitk_yes = (ImageView) convertView.findViewById(R.id.icon_vit_k_yes);
            viewHolder.campak_no = (ImageView) convertView.findViewById(R.id.icon_campak_no);
            viewHolder.campak_yes = (ImageView) convertView.findViewById(R.id.icon_campak_yes);
            viewHolder.ivp_no = (ImageView) convertView.findViewById(R.id.icon_ivp_no);
            viewHolder.ivp_yes = (ImageView) convertView.findViewById(R.id.icon_ivp_yes);

            viewHolder.pol1_no = (ImageView) convertView.findViewById(R.id.icon_pol1_no);
            viewHolder.pol1_yes = (ImageView) convertView.findViewById(R.id.icon_pol1_yes);
            viewHolder.pol2_no = (ImageView) convertView.findViewById(R.id.icon_pol2_no);
            viewHolder.pol2_yes = (ImageView) convertView.findViewById(R.id.icon_pol2_yes);
            viewHolder.pol3_no = (ImageView) convertView.findViewById(R.id.icon_pol3_no);
            viewHolder.pol3_yes = (ImageView) convertView.findViewById(R.id.icon_pol3_yes);
            viewHolder.pol4_no = (ImageView) convertView.findViewById(R.id.icon_pol4_no);
            viewHolder.pol4_yes = (ImageView) convertView.findViewById(R.id.icon_pol4_yes);

            viewHolder.berat_badan = (TextView) convertView.findViewById(R.id.txt_current_weight);
            viewHolder.tanggal_kunjungan_anc = (TextView) convertView.findViewById(R.id.txt_visit_date);
            viewHolder.tinggi = (TextView) convertView.findViewById(R.id.txt_current_height);

            viewHolder.profilepic = (ImageView) convertView.findViewById(R.id.img_profile);
            viewHolder.follow_up = (ImageButton) convertView.findViewById(R.id.btn_edit);
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.child_boy));
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
        //set image
        //set image
        final ImageView childview = (ImageView)convertView.findViewById(R.id.img_profile);
        if (pc.getDetails().get("profilepic") != null) {
            AnakDetailActivity.setImagetoHolderFromUri((Activity) context, pc.getDetails().get("profilepic"), childview, R.mipmap.child_boy);
            childview.setTag(smartRegisterClient);
        }
        else {
            if(pc.getDetails().get("jenisKelamin").equals("laki")) {
                viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.drawable.child_boy_infant));
            }
            else
                viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.drawable.child_girl_infant));
        }
        
        viewHolder.childs_name.setText(pc.getColumnmaps().get("namaBayi")!=null?pc.getColumnmaps().get("namaBayi"):"Bayi");
//        viewHolder.no_ibu.setText(pc.getDetails().get("noBayi")!=null?pc.getDetails().get("noBayi"):"");




        //delivery documentation
      //  viewHolder.tempat_lahir.setText(humanize(pc.getDetails().get("tempatBersalin")!=null?pc.getDetails().get("tempatBersalin"):""));
//        viewHolder.berat_lahir.setText(pc.getDetails().get("beratLahir")!=null?pc.getDetails().get("beratLahir"):"");
       // viewHolder.tipe_lahir.setText(pc.getDetails().get("tanggalLahirAnak")!=null?pc.getDetails().get("tanggalLahirAnak"):"");


        //immunization
        checkVisibility(pc.getDetails().get("hb0"),null, viewHolder.hb0_no, viewHolder.hb0_yes);
        checkVisibility(pc.getDetails().get("polio1"),pc.getDetails().get("bcg"), viewHolder.pol1_no, viewHolder.pol1_yes);
        checkVisibility(pc.getDetails().get("dptHb1"),pc.getDetails().get("polio2"), viewHolder.pol2_no, viewHolder.pol2_yes);
        checkVisibility(pc.getDetails().get("dptHb2"),pc.getDetails().get("polio3"), viewHolder.pol3_no, viewHolder.pol3_yes);
        checkVisibility(pc.getDetails().get("dptHb3"),pc.getDetails().get("polio4"), viewHolder.pol4_no, viewHolder.pol4_yes);
      //  checkVisibility(pc.getDetails().get("jenisPelayanan"),null, viewHolder.vitk_no, viewHolder.vitk_yes);
        checkVisibility(pc.getDetails().get("campak"),null, viewHolder.campak_no, viewHolder.campak_yes);
        checkVisibility(pc.getDetails().get("ipv"),null, viewHolder.ivp_no, viewHolder.ivp_yes);



       /* String berat = pc.getDetails().get("beratBadanBayiSetiapKunjunganBayiPerbulan")!=null?" "+pc.getDetails().get("beratBadanBayiSetiapKunjunganBayiPerbulan"):"";
        String tanggal = pc.getDetails().get("tanggalKunjunganBayiPerbulan")!=null?" "+pc.getDetails().get("tanggalKunjunganBayiPerbulan"):"";
        String tinggi = pc.getDetails().get("hasilPengukuranTinggiBayi")!=null?" "+pc.getDetails().get("hasilPengukuranTinggiBayi"):"";
        String status_gizi = pc.getDetails().get("statusGizi")!=null?pc.getDetails().get("statusGizi"):"";
      //  String gizi = status_gizi.equals("GB")?"Gizi Buruk":status_gizi.equals("GK")?"Gizi Kurang":status_gizi.equals("GR")?"Gizi Rendah":"";
        viewHolder.berat_badan.setText(context.getString(R.string.str_weight)+": "+berat);
        viewHolder.tanggal_kunjungan_anc.setText(context.getString(R.string.date_visit_title)+" "+tanggal);
        viewHolder.tinggi.setText(context.getString(R.string.height)+" "+tinggi);
        viewHolder.status_gizi.setText(context.getString(R.string.Nutrition_status)+" "+ status_gizi);*/

        //child visit status
        String berat = pc.getDetails().get("beratBayi")!=null?" "+pc.getDetails().get("beratBayi"):pc.getDetails().get("beratBadan")!=null?pc.getDetails().get("beratBadan"):"";
        String tanggal = pc.getDetails().get("tanggalKunjunganBayiPerbulan")!=null?" "+pc.getDetails().get("tanggalKunjunganBayiPerbulan"):pc.getDetails().get("tanggalPenimbangan")!=null?pc.getDetails().get("tanggalPenimbangan"):"";
        String tinggi = pc.getDetails().get("panjangBayi")!=null?" "+pc.getDetails().get("panjangBayi"):pc.getDetails().get("tinggiBadan")!=null?pc.getDetails().get("tinggiBadan"):"";
        String status_gizi = pc.getDetails().get("statusGizi")!=null?pc.getDetails().get("statusGizi"):"";
        String gizi = status_gizi.equals("GB")?"Gizi Baik":status_gizi.equals("GK")?"Gizi Kurang":status_gizi.equals("GR")?"Gizi Rendah":"";
        viewHolder.berat_badan.setText(context.getString(R.string.str_weight)+": "+berat);
        viewHolder.tanggal_kunjungan_anc.setText(context.getString(R.string.date_visit_title)+" "+tanggal);
        viewHolder.tinggi.setText(context.getString(R.string.height)+" "+tinggi);
        viewHolder.status_gizi.setText(context.getString(R.string.Nutrition_status)+" "+ gizi);


       /* //child visit status
        String berat = pc.getDetails().get("beratBayi")!=null?" "+pc.getDetails().get("beratBayi"):pc.getDetails().get("beratBadan")!=null?pc.getDetails().get("beratBadan"):"";
        String tanggal = pc.getDetails().get("tanggalKunjunganBayiPerbulan")!=null?" "+pc.getDetails().get("tanggalKunjunganBayiPerbulan"):pc.getDetails().get("tanggalPenimbangan")!=null?pc.getDetails().get("tanggalPenimbangan"):"";
        String tinggi = pc.getDetails().get("panjangBayi")!=null?" "+pc.getDetails().get("panjangBayi"):pc.getDetails().get("tinggiBadan")!=null?pc.getDetails().get("tinggiBadan"):"";
        String status_gizi = pc.getDetails().get("statusGizi")!=null?pc.getDetails().get("statusGizi"):"";
        String gizi = status_gizi.equals("GB")?"Gizi Buruk":status_gizi.equals("GK")?"Gizi Kurang":status_gizi.equals("GR")?"Gizi Rendah":"";
        viewHolder.berat_badan.setText(context.getString(R.string.str_weight)+": "+berat);
        viewHolder.tanggal_kunjungan_anc.setText(context.getString(R.string.date_visit_title)+" "+tanggal);
        viewHolder.tinggi.setText(context.getString(R.string.height)+" "+tinggi);
        viewHolder.status_gizi.setText(context.getString(R.string.Nutrition_status)+" "+ gizi);*/



        AllCommonsRepository childRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("anak");
        CommonPersonObject childobject = childRepository.findByCaseID(pc.entityId());

        AllCommonsRepository iburep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ibu");
        final CommonPersonObject ibuparent = iburep.findByCaseID(childobject.getColumnmaps().get("ibuCaseId"));
        String tempat = ibuparent.getDetails().get("tempatBersalin")!=null?ibuparent.getDetails().get("tempatBersalin"):"";

//        viewHolder.tempat_lahir.setText(tempat.equals("podok_bersalin_desa")?"POLINDES":tempat.equals("pusat_kesehatan_masyarakat_pembantu")?"Puskesmas pembantu":tempat.equals("pusat_kesehatan_masyarakat")?"Puskesmas":humanize(tempat));

        if(StringUtils.isNotBlank(ibuparent.getColumnmaps().get("kartuIbuId"))) {
            AllCommonsRepository kirep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("kartu_ibu");
            final CommonPersonObject kiparent = kirep.findByCaseID(ibuparent.getColumnmaps().get("kartuIbuId"));
            String namaayah = kiparent.getColumnmaps().get("namaSuami")!=null?kiparent.getColumnmaps().get("namaSuami"):"";
            String namaibu = kiparent.getColumnmaps().get("namalengkap")!=null?kiparent.getColumnmaps().get("namalengkap"):"";
            viewHolder.mother_name.setText(namaibu +", "+ namaayah);
            viewHolder.village_name.setText(kiparent.getDetails().get("desa")!=null?kiparent.getDetails().get("desa"):"");

        }
        else {

            viewHolder.mother_name.setText("Inaq "+pc.getColumnmaps().get("namaBayi"));
            viewHolder.village_name.setText("");
            viewHolder.no_ibu.setText("");
        }
        viewHolder.anak_register_dob.setText(pc.getDetails().get("beratLahir") != null ? pc.getDetails().get("beratLahir") : "");

        String childAge = childobject.getColumnmaps().get("tanggalLahirAnak")!=null?childobject.getColumnmaps().get("tanggalLahirAnak"):"-";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        if(StringUtils.isNotBlank(childobject.getColumnmaps().get("tanggalLahirAnak"))) {
        //if(childobject.getColumnmaps().get("tanggalLahirAnak")!=null) {
            String age = childAge;
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            LocalDate dates = parse(age, formatter).toLocalDate();
            LocalDate dateNow = LocalDate.now();

            dates = dates.withDayOfMonth(1);
            dateNow = dateNow.withDayOfMonth(1);

            int months = Months.monthsBetween(dates, dateNow).getMonths();
            viewHolder.childs_age.setText(months+ " "+context.getString(R.string.month));
         //   viewHolder.anak_register_dob.setText(""+dates);

        }
        else{
            viewHolder.childs_age.setText("-");
        }

        if(pc.getDetails().get("jenisPelayanan") != null) {
            checkContain1(pc.getDetails().get("jenisPelayanan"), viewHolder.icon_salep_no, viewHolder.icon_salep_yes);
            checkContain2(pc.getDetails().get("jenisPelayanan"), viewHolder.icon_imd_no, viewHolder.icon_imd_yes);
            checkContain3(pc.getDetails().get("jenisPelayanan"), viewHolder.vitk_no, viewHolder.vitk_yes);
        }
        convertView.setLayoutParams(clientViewLayoutParams);
     //   return convertView;
    }

    private void checkContain1(String data,  ImageView icon_imd_no, ImageView icon_imd_yes) {
        if(data.contains("First")){
            icon_imd_no.setVisibility(View.INVISIBLE);
            icon_imd_yes.setVisibility(View.VISIBLE);
        }
    }
    private void checkContain2(String data,  ImageView icon_imd_no, ImageView icon_imd_yes) {
        if(data.contains("salep") ){
            icon_imd_no.setVisibility(View.INVISIBLE);
            icon_imd_yes.setVisibility(View.VISIBLE);
        }

    }
    private void checkContain3(String data,  ImageView icon_imd_no, ImageView icon_imd_yes) {
        if(data.contains("vit") ){
            icon_imd_no.setVisibility(View.INVISIBLE);
            icon_imd_yes.setVisibility(View.VISIBLE);
        }

    }
    // CommonPersonObjectController householdelcocontroller;


    //    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {
        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }
    void checkVisibility(String immunization1,String immunization2, ImageView no, ImageView yes){
        if(immunization1 != null || immunization2 != null){
            no.setVisibility(View.INVISIBLE);
            yes.setVisibility(View.VISIBLE);
        }
        else{
            no.setVisibility(View.VISIBLE);
            yes.setVisibility(View.INVISIBLE);
        }

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
        View View = (ViewGroup) inflater().inflate(R.layout.smart_register_anak_client, null);
        return View;
    }

    class ViewHolder {

        TextView village_name;
        LinearLayout profilelayout;
        ImageView profilepic;
        ImageButton follow_up;
        TextView no_ibu;
        TextView tanggal_kunjungan_anc;
        ImageView hp_badge;
        TextView childs_age;
        TextView mother_name;
        TextView childs_name;
        TextView anak_register_dob;
        TextView tempat_lahir;
        TextView berat_lahir;
        TextView tipe_lahir;
        ImageView hb0_no;
        ImageView hb0_yes;
        ImageView pol1_no;
        ImageView pol1_yes;
        ImageView pol2_no;
        ImageView pol2_yes;
        ImageView pol3_no;
        ImageView pol3_yes;
        ImageView pol4_no;
        ImageView pol4_yes;
        TextView berat_badan;
        TextView tinggi;
        TextView status_gizi;
        ImageView vitk_no;
        ImageView vitk_yes;
        ImageView campak_no;
        ImageView campak_yes;
        ImageView ivp_no;
        ImageView ivp_yes;
        public ImageView icon_imd_no;
        public ImageView icon_imd_yes;
        public ImageView icon_salep_yes ;
        public ImageView icon_salep_no;
    }


}