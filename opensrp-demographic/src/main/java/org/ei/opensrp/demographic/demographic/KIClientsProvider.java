package org.ei.opensrp.demographic.demographic;

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
import org.ei.opensrp.demographic.R;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by user on 2/12/15.
 */
public class KIClientsProvider implements SmartRegisterClientsProvider {

    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    static String bindobject = "anak";
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;
    private Drawable iconPencilDrawable;
    protected CommonPersonObjectController controller;

    AlertService alertService;

    public KIClientsProvider(Context context,
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
            convertView = (ViewGroup) inflater().inflate(R.layout.smart_register_demog_client, null);
            viewHolder = new ViewHolder();
            viewHolder.profilelayout =  (LinearLayout)convertView.findViewById(R.id.profile_info_layout);
            viewHolder.name = (TextView)convertView.findViewById(R.id.txt_kk_name);
            viewHolder.no_kk = (TextView)convertView.findViewById(R.id.no_kk);
            viewHolder.tgl_kk = (TextView)convertView.findViewById(R.id.tgl_kk);
            viewHolder.tanggal = (TextView)convertView.findViewById(R.id.tanggal);
            viewHolder.txt_tgl_relokasi = (TextView)convertView.findViewById(R.id.txt_tgl_relokasi);
            viewHolder.jenis_perpindahan = (TextView)convertView.findViewById(R.id.txt_jenis_perpindahan);
            viewHolder.lama_pindah = (TextView)convertView.findViewById(R.id.txt_lama_pindah);
            viewHolder.alasan_pindah = (TextView)convertView.findViewById(R.id.txt_alasan_pindah);

            viewHolder.txt_tgl_kelahiran = (TextView)convertView.findViewById(R.id.txt_tgl_kelahiran);
            viewHolder.nama_ibu = (TextView)convertView.findViewById(R.id.txt_tgl_kelahiran);
            viewHolder.birthplace = (TextView)convertView.findViewById(R.id.txt_birthplace);
            viewHolder.birth_assistant = (TextView)convertView.findViewById(R.id.txt_birth_assistant);

            viewHolder.txt_tgl_kematian = (TextView)convertView.findViewById(R.id.txt_tgl_kematian);
            viewHolder.txt_place_death = (TextView)convertView.findViewById(R.id.txt_place_death);
            viewHolder.txt_pregnancy_status = (TextView)convertView.findViewById(R.id.txt_pregnancy_status);
            viewHolder.txt_penyebab_kematian = (TextView)convertView.findViewById(R.id.txt_penyebab_kematian);


            viewHolder.profilepic =(ImageView)convertView.findViewById(R.id.profilepic);
            viewHolder.follow_up = (ImageButton)convertView.findViewById(R.id.btn_edit);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.child_boy_infant));
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
       // final ImageView childview = (ImageView)convertView.findViewById(R.id.profilepic);

        viewHolder.name.setText(pc.getColumnmaps().get("nama_kk")!=null?pc.getColumnmaps().get("nama_kk"):"");
        viewHolder.no_kk.setText(pc.getDetails().get("no_kk")!=null?"No KK :"+pc.getDetails().get("no_kk"):"");
        viewHolder.tgl_kk.setText(pc.getDetails().get("tgl_kk")!=null?"Tgl KK :"+pc.getDetails().get("tgl_kk"):"");
        viewHolder.tanggal.setText(pc.getDetails().get("tanggal")!=null?pc.getDetails().get("tanggal"):"");

        viewHolder.txt_tgl_relokasi.setText(pc.getDetails().get("Migration_Form_tanggal")!=null?"Tanggal : "+pc.getDetails().get("Migration_Form_tanggal"):"");
        viewHolder.jenis_perpindahan.setText(pc.getDetails().get("jenis_perpindahan")!=null?"Jenis : "+pc.getDetails().get("jenis_perpindahan"):"");
        viewHolder.lama_pindah.setText(pc.getDetails().get("lama_pindah")!=null?"Lama : "+pc.getDetails().get("lama_pindah"):"");
        viewHolder.alasan_pindah.setText(pc.getDetails().get("alasan_pindah")!=null?"Alasan : "+pc.getDetails().get("alasan_pindah"):"");

      //  AllCommonsRepository kiRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("kartu_keluarga");

       // CommonPersonObject kiobject = kiRepository.findByCaseID(pc.entityId());

    //    AllCommonsRepository iburep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("kartu_ibu");
    //    final CommonPersonObject ibuparent = iburep.findByCaseID(kiobject.getColumnmaps().get("id"));

        //ibu
        viewHolder.txt_tgl_kelahiran.setText(pc.getDetails().get("Birth_Form_tanggal")!=null?"Tanggal : "+pc.getDetails().get("Birth_Form_tanggal"):"");
        viewHolder.nama_ibu.setText(pc.getDetails().get("nama_ibu")!=null?"Nama Ibu : "+pc.getDetails().get("nama_ibu"):"");
        viewHolder.birthplace.setText(pc.getDetails().get("birthplace")!=null?"Tmepat Lahir : "+pc.getDetails().get("birthplace"):"");
        viewHolder.birth_assistant.setText(pc.getDetails().get("birth_assistant")!=null?"Pembantu : "+pc.getDetails().get("birth_assistant"):"");


        viewHolder.txt_tgl_kematian.setText(pc.getDetails().get("DeathForm_tanggal")!=null?"Tanggal : "+pc.getDetails().get("DeathForm_tanggal"):"");
        viewHolder.txt_place_death.setText(pc.getDetails().get("place_death")!=null?"Tempat : "+pc.getDetails().get("place_death"):"");
        viewHolder.txt_pregnancy_status.setText(pc.getDetails().get("pregnancy_status")!=null?"Status Kehamilan : "+pc.getDetails().get("pregnancy_status"):"");
        viewHolder.txt_penyebab_kematian.setText(pc.getDetails().get("penyebab_kematian")!=null?"Penyebab : "+pc.getDetails().get("penyebab_kematian"):"");

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

        TextView name ;
        TextView nama_ibu ;
        TextView birthplace;
        TextView alasan_pindah;
        LinearLayout profilelayout;
        ImageView profilepic;
        FrameLayout due_date_holder;
        Button warnbutton;
        ImageButton follow_up;
        TextView birth_assistant;
        TextView gender;
        TextView visitDate;
        TextView height;
        TextView weight;
        TextView underweight;
        TextView lama_pindah;
        TextView wasting_status;
        TextView txt_penyebab_kematian;
        ImageView weightLogo;
        ImageView heightLogo;
        ImageView vitALogo;
        TextView txt_pregnancy_status;
        ImageView antihelminticLogo;
        TextView txt_place_death;


        public TextView no_kk;
        public TextView tgl_kk;
        public TextView tanggal;
        public TextView txt_tgl_relokasi;
        public TextView txt_tgl_kelahiran;
        public TextView txt_tgl_kematian;
        public TextView jenis_perpindahan;
    }


}

