package org.ei.opensrp.test.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.test.R;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.ei.opensrp.util.StringUtil.humanize;

/**
 * Created by user on 2/12/15.
 */
public class FormulirDdtkSmartClientsProvider implements SmartRegisterClientsProvider {

    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;

    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private Drawable iconPencilDrawable;
    protected CommonPersonObjectController controller;

    AlertService alertService;

    public FormulirDdtkSmartClientsProvider(Context context,
                                            View.OnClickListener onClickListener,
                                            CommonPersonObjectController controller, AlertService alertService) {
        this.onClickListener = onClickListener;
        this.controller = controller;
        this.context = context;
        this.alertService = alertService;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) context.getResources().getDimension(org.ei.opensrp.R.dimen.list_item_height));
        txtColorBlack = context.getResources().getColor(org.ei.opensrp.R.color.text_black);

    }

    @Override
    public View getView(SmartRegisterClient smartRegisterClient, View convertView, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if (convertView == null){
           convertView = (ViewGroup) inflater().inflate(R.layout.smart_register_test_client, null);
            viewHolder = new ViewHolder();
            viewHolder.profilelayout =  (LinearLayout)convertView.findViewById(R.id.profile_info_layout);
            viewHolder.antrolayout =  (LinearLayout)convertView.findViewById(R.id.profile_antro_layout);
            viewHolder.village = (TextView)convertView.findViewById(R.id.village);
            viewHolder.nama_anak = (TextView)convertView.findViewById(R.id.nama_anak);
            viewHolder.jenis_kelamin = (TextView)convertView.findViewById(R.id.jenis_kelamin);
            viewHolder.umur = (TextView)convertView.findViewById(R.id.umur);
            viewHolder.nama_ibu = (TextView)convertView.findViewById(R.id.nama_ibu);
            viewHolder.berat = (TextView)convertView.findViewById(R.id.berat);
            viewHolder.tinggi = (TextView)convertView.findViewById(R.id.tinggi);
            viewHolder.lingkar_kepala = (TextView)convertView.findViewById(R.id.lingkar_kepala);
            viewHolder.status_kembang = (TextView)convertView.findViewById(R.id.status_kembang);
            viewHolder.daya_dengar = (TextView)convertView.findViewById(R.id.daya_dengar);
            viewHolder.daya_lihat = (TextView)convertView.findViewById(R.id.daya_lihat);
            viewHolder.mental_emosional = (TextView)convertView.findViewById(R.id.mental_emosional);
            viewHolder.autis = (TextView)convertView.findViewById(R.id.autis);
            viewHolder.gpph = (TextView)convertView.findViewById(R.id.gpph);
            viewHolder.anthropometry_date = (TextView)convertView.findViewById(R.id.anthropometry_date);
            viewHolder.hear_test_date = (TextView)convertView.findViewById(R.id.hear_test_date);
            viewHolder.sight_test_date = (TextView)convertView.findViewById(R.id.sight_test_date);
            viewHolder.mental_test_date = (TextView)convertView.findViewById(R.id.mental_test_date);
            viewHolder.autis_test_date = (TextView)convertView.findViewById(R.id.autis_test_date);
            viewHolder.gpph_test_date = (TextView)convertView.findViewById(R.id.gpph_test_date);
            viewHolder.kpsp_test_date = (TextView)convertView.findViewById(R.id.kpsp_test_date);
            viewHolder.profilepic =(ImageView)convertView.findViewById(R.id.profilepic);
            viewHolder.follow_up = (ImageButton)convertView.findViewById(R.id.btn_edit);
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile_thumb));
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile_thumb));
        }

        viewHolder.follow_up.setOnClickListener(onClickListener);
        viewHolder.follow_up.setTag(smartRegisterClient);
           viewHolder.profilelayout.setOnClickListener(onClickListener);
        viewHolder.profilelayout.setTag(smartRegisterClient);
        viewHolder.antrolayout.setOnClickListener(onClickListener);
        viewHolder.antrolayout.setTag(smartRegisterClient);
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (iconPencilDrawable == null) {
            iconPencilDrawable = context.getResources().getDrawable(R.drawable.ic_pencil);
        }
        viewHolder.follow_up.setImageDrawable(iconPencilDrawable);
        viewHolder.follow_up.setOnClickListener(onClickListener);
       // viewHolder.follow_up.setTag(client);

        /*
      //  List<Alert> alertlist_for_client = alertService.findByEntityIdAndAlertNames(pc.entityId(), "FW CENSUS");


      // set image picture
        if(pc.getDetails().get("profilepic")!=null){


            if((pc.getDetails().get("gender")!=null?pc.getDetails().get("gender"):"").equalsIgnoreCase("2")) {
              //  HouseHoldDetailActivity.setImagetoHolder((Activity) context, pc.getDetails().get("profilepic"), viewHolder.profilepic, R.mipmap.womanimageload);
            }else if ((pc.getDetails().get("gender")!=null?pc.getDetails().get("gender"):"").equalsIgnoreCase("1")){
              //  HouseHoldDetailActivity.setImagetoHolder((Activity) context, pc.getDetails().get("profilepic"), viewHolder.profilepic, R.mipmap.householdload);
            }

        }else{
            if((pc.getDetails().get("gender")!=null?pc.getDetails().get("gender"):"").equalsIgnoreCase("2")){
                viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.drawable.woman_placeholder));
            }else if ((pc.getDetails().get("gender")!=null?pc.getDetails().get("gender"):"").equalsIgnoreCase("1")){
                viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile_thumb));
            }
        }
        */

        //set default image for mother
        viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.drawable.child_boy_infant));
        //viewHolder.village.setText(pc.getDetails().get("village") != null ? pc.getDetails().get("village") : "");
        viewHolder.nama_anak.setText(pc.getDetails().get("nama_anak") != null ? pc.getDetails().get("nama_anak") : "");
        viewHolder.jenis_kelamin.setText(pc.getDetails().get("jenis_kelamin")!=null?pc.getDetails().get("jenis_kelamin"):"");
        viewHolder.umur.setText(pc.getDetails().get("umur")!=null?pc.getDetails().get("umur"):"");
        viewHolder.nama_ibu.setText(pc.getDetails().get("nama_ibu") != null ? pc.getDetails().get("nama_ibu") : "");
        viewHolder.berat.setText(pc.getDetails().get("berat")!=null?pc.getDetails().get("berat"):"");
        viewHolder.tinggi.setText(pc.getDetails().get("tinggi") != null ? pc.getDetails().get("tinggi") : "");
        viewHolder.lingkar_kepala.setText(pc.getDetails().get("lingkar_kepala")!=null?pc.getDetails().get("lingkar_kepala"):"");
        viewHolder.status_kembang.setText(pc.getDetails().get("status_kembang")!=null?pc.getDetails().get("status_kembang"):"");
        viewHolder.daya_dengar.setText(pc.getDetails().get("daya_dengar") != null ? pc.getDetails().get("daya_dengar") : "");
        viewHolder.daya_lihat.setText(pc.getDetails().get("daya_lihat")!=null?pc.getDetails().get("daya_lihat"):"");
        viewHolder.mental_emosional.setText(pc.getDetails().get("mental_emosional")!=null?pc.getDetails().get("mental_emosional"):"");
        viewHolder.autis.setText(pc.getDetails().get("autis")!=null?pc.getDetails().get("autis"):"");
        viewHolder.gpph.setText(pc.getDetails().get("gpph")!=null?pc.getDetails().get("gpph"):"");
        viewHolder.anthropometry_date.setText(pc.getDetails().get("anthropometry_date")!=null?pc.getDetails().get("anthropometry_date"):"");
        viewHolder.sight_test_date.setText(pc.getDetails().get("sight_test_date")!=null?pc.getDetails().get("sight_test_date"):"");
        viewHolder.mental_test_date.setText(pc.getDetails().get("mental_test_date")!=null?pc.getDetails().get("mental_test_date"):"");
        viewHolder.autis_test_date.setText(pc.getDetails().get("autis_test_date")!=null?pc.getDetails().get("autis_test_date"):"");
        viewHolder.gpph_test_date.setText(pc.getDetails().get("gpph_test_date")!=null?pc.getDetails().get("gpph_test_date"):"");
        viewHolder.kpsp_test_date.setText(pc.getDetails().get("kpsp_test_date")!=null?pc.getDetails().get("kpsp_test_date"):"");
        viewHolder.hear_test_date.setText(pc.getDetails().get("hear_test_date")!=null?pc.getDetails().get("hear_test_date"):"");
      //  viewHolder.headofhouseholdname.setText(pc.getDetails().get("FWHOHFNAME")!=null?pc.getDetails().get("FWHOHFNAME"):"");
      //  viewHolder.no_of_mwra.setText(pc.getDetails().get("ELCO")!=null?pc.getDetails().get("ELCO"):"");
       // Date lastdate = null;



        convertView.setLayoutParams(clientViewLayoutParams);
        return convertView;
    }
    CommonPersonObjectController householdelcocontroller;





    @Override
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

     class ViewHolder {

         TextView today ;
         TextView umur;
         TextView village;
         TextView husbandname;
         LinearLayout profilelayout;
         LinearLayout antrolayout;
         ImageView profilepic;
         FrameLayout due_date_holder;
         Button warnbutton;
         ImageButton follow_up;
         TextView nama_anak;
         TextView jenis_kelamin;
         TextView nama_ibu;
         TextView berat;
         TextView tinggi;
         TextView lingkar_kepala;
         TextView status_kembang;
         TextView daya_dengar;
         TextView daya_lihat;
         TextView mental_emosional;
         TextView autis;
         TextView gpph;
         TextView anthropometry_date;
         TextView hear_test_date;
         TextView sight_test_date;
         TextView mental_test_date;
         TextView autis_test_date;
         TextView kpsp_test_date;
         TextView gpph_test_date;
     }


}

