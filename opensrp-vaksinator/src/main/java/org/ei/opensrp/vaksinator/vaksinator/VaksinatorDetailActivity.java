package org.ei.opensrp.vaksinator.vaksinator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.util.OpenSRPImageLoader;
import org.ei.opensrp.vaksinator.R;
import org.ei.opensrp.vaksinator.face.camera.SmartShutterActivity;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import util.ImageCache;
import util.ImageFetcher;

/**
 * Created by Iq on 09/06/16.
 */
public class VaksinatorDetailActivity extends Activity {
    SimpleDateFormat timer = new SimpleDateFormat("hh:mm:ss");
    //image retrieving
    private static final String TAG = VaksinatorDetailActivity.class.getSimpleName();
    private static final String IMAGE_CACHE_DIR = "thumbs";
    //  private static KmsCalc  kmsCalc;

    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;

    //image retrieving
    public static CommonPersonObjectClient controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.smart_register_jurim_detail_client);

        DetailsRepository detailsRepository = org.ei.opensrp.Context.getInstance().detailsRepository();
        detailsRepository.updateDetails(controller);

        AllCommonsRepository childRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ec_anak");
        CommonPersonObject controllers = childRepository.findByCaseID(controller.entityId());

        AllCommonsRepository kirep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ec_kartu_ibu");
        final CommonPersonObject kiparent = kirep.findByCaseID(controllers.getColumnmaps().get("relational_id"));

        String DetailStart = timer.format(new Date());
        Map<String, String> Detail = new HashMap<String, String>();
        Detail.put("start", DetailStart);
        FlurryAgent.logEvent("vaksinator_detail_view",Detail, true );
        //label
        TextView label001 = (TextView) findViewById(R.id.label001);
        TextView label002 = (TextView) findViewById(R.id.label002);
        TextView nameLabel = (TextView) findViewById(R.id.nameLabel);
        TextView fatherLabel = (TextView) findViewById(R.id.fatherNameLabel);
        TextView motherLabel = (TextView) findViewById(R.id.motherNameLabel);
        TextView villageLabel = (TextView) findViewById(R.id.villageLabel);
        TextView subVillageLabel = (TextView) findViewById(R.id.subVillageLabel);
        TextView dateOfBirthLabel = (TextView) findViewById(R.id.dateOfBirthLabel);
        TextView birthWeightLabel = (TextView) findViewById(R.id.birthWeightLabel);
        TextView antipiretikLabel = (TextView) findViewById(R.id.antipyreticLabel);
        TextView hbLabel = (TextView) findViewById(R.id.hbLabel);
        TextView campakLabel = (TextView) findViewById(R.id.campakLabel);
        TextView completeLabel = (TextView) findViewById(R.id.completeLabel);
        TextView additionalDPTLabel = (TextView) findViewById(R.id.additionalDPTLabel);
        TextView additionalMeaslesLabel = (TextView) findViewById(R.id.additionalMeaslesLabel);

        //profile
        TextView uid = (TextView) findViewById(R.id.uidValue);
        TextView nama = (TextView) findViewById(R.id.childName);
        TextView motherName = (TextView) findViewById(R.id.motherName);
        TextView fatherName = (TextView) findViewById(R.id.fatherName);
        TextView village = (TextView) findViewById(R.id.village);
        TextView subVillage = (TextView) findViewById(R.id.subvillage);
        TextView dateOfBirth = (TextView) findViewById(R.id.dateOfBirth);
        TextView birthWeight = (TextView) findViewById(R.id.birthWeight);
        TextView antipiretik = (TextView) findViewById(R.id.antypiretic);

        //vaccination date
        TextView hb1Under7 = (TextView) findViewById(R.id.hb1under7);
        TextView bcg = (TextView) findViewById(R.id.bcg);
        TextView pol1 = (TextView) findViewById(R.id.pol1);
        TextView dpt1 = (TextView) findViewById(R.id.dpt1);
        TextView pol2 = (TextView) findViewById(R.id.pol2);
        TextView dpt2 = (TextView) findViewById(R.id.dpt2);
        TextView pol3 = (TextView) findViewById(R.id.pol3);
        TextView dpt3 = (TextView) findViewById(R.id.dpt3);
        TextView pol4 = (TextView) findViewById(R.id.pol4);
        TextView ipv = (TextView) findViewById(R.id.ipv);
        TextView measles = (TextView) findViewById(R.id.measles);
        TextView complete = (TextView) findViewById(R.id.complete);
        TextView additionalDPT = (TextView) findViewById(R.id.additionalDPT);
        TextView additionalMeasles = (TextView) findViewById(R.id.additionalMeasles);
        final ImageView photo = (ImageView) findViewById(R.id.photo);

        ImageButton backButton = (ImageButton) findViewById(R.id.btn_back_to_home);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

                startActivity(new Intent(VaksinatorDetailActivity.this, VaksinatorSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
                //Start capture flurry log for FS
                String DetailEnd = timer.format(new Date());
                Map<String, String> Detail = new HashMap<String, String>();
                Detail.put("end", DetailEnd);
                FlurryAgent.logEvent("vaksinator_detail_view",Detail, true );
            }
        });

        TextView recapitulationLabel = (TextView)findViewById(R.id.recapitulation_label);
        recapitulationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                VaksinatorRecapitulationActivity.staticVillageName = (kiparent.getDetails().get("cityVillage") != null ? ": " + kiparent.getDetails().get("cityVillage") : "null");
                        startActivity(new Intent(VaksinatorDetailActivity.this, VaksinatorRecapitulationActivity.class));
                overridePendingTransition(0, 0);
                FlurryFacade.logEvent("click_recapitulation_button");
            }
        });

        //Label rename
        label001.setText(getString(R.string.title001));
        label002.setText(getString(R.string.title002));
        nameLabel.setText(getString(R.string.namaAnak));
        fatherLabel.setText(getString(R.string.namaAyah));
        motherLabel.setText(getString(R.string.namaIbu));
        villageLabel.setText(getString(R.string.desa));
        subVillageLabel.setText(getString(R.string.dusun));
        dateOfBirthLabel.setText(getString(R.string.tanggalLahir));
        birthWeightLabel.setText(getString(R.string.beratLahir));
        antipiretikLabel.setText(getString(R.string.dapatAntipiretik));
        hbLabel.setText("HB0 (0-7 "+getString(R.string.hari)+")");
        campakLabel.setText(getString(R.string.measles));
        completeLabel.setText(getString(R.string.imunisasiLengkap));
        additionalDPTLabel.setText(getString(R.string.dptTambahan));
        additionalMeaslesLabel.setText(getString(R.string.campakTambahan));


        uid.setText(": "+(controller.getDetails().get("UniqueId")!=null ? controller.getDetails().get("UniqueId") : "-"));
        nama.setText(": " + (controller.getColumnmaps().get("namaBayi") != null ? controller.getColumnmaps().get("namaBayi") : "-"));

        //System.out.println("details: "+controller.getDetails().toString());

        if(kiparent != null) {
            detailsRepository.updateDetails(kiparent);
            String namaayah = kiparent.getDetails().get("namaSuami") != null ? kiparent.getDetails().get("namaSuami") : "";
            String namaibu = kiparent.getColumnmaps().get("namalengkap") != null ? kiparent.getColumnmaps().get("namalengkap") : "";

            fatherName.setText(": " + namaayah);
            motherName.setText(": " +namaibu);
            village.setText(kiparent.getDetails().get("cityVillage") != null ? ": " + kiparent.getDetails().get("cityVillage") : "");
            subVillage.setText(kiparent.getDetails().get("address1") != null ? ": " + kiparent.getDetails().get("address1") : "");
            // viewHolder.no_ibu.setText(kiparent.getDetails().get("noBayi") != null ? kiparent.getDetails().get("noBayi") : "");
        }

        /*fatherName.setText(": " + (controller.getDetails().get("namaAyah") != null ? transformToddmmyyyy(controller.getDetails().get("namaAyah") : "-"));
        motherName.setText(": " + (controller.getDetails().get("namaIbu") != null
                ? controller.getDetails().get("namaIbu")
                : controller.getDetails().get("nama_orang_tua")!=null
                        ? controller.getDetails().get("nama_orang_tua")
                        : "-"
            )
        );*/
       /* village.setText(": " + (controller.getDetails().get("desa") != null
                ? controller.getDetails().get("desa")
                : "-"));*/

   /*     subVillage.setText(": " + (controller.getDetails().get("dusun") != null
                ? controller.getDetails().get("dusun")
                : controller.getDetails().get("village") != null
                    ? controller.getDetails().get("village")
                    : "-")
        );
*/

        dateOfBirth.setText(": " + (controller.getColumnmaps().get("tanggalLahirAnak") != null ? transformToddmmyyyy(controller.getColumnmaps().get("tanggalLahirAnak").substring(0,10)) : "-"));
        birthWeight.setText(": " + (controller.getDetails().get("beratLahir") != null
                                    ? Double.toString(Integer.parseInt(controller.getDetails()
                                                        .get("beratLahir"))/1000)
                                                        + " kg"
                                    : "-"));
        antipiretik.setText(": " + (controller.getDetails().get("antipiretik") != null ? yesNo(controller.getDetails().get("antipiretik").toLowerCase().contains("y")) : "-"));

        hb1Under7.setText(": " + transformToddmmyyyy(hasDate(controller,"hb0")
                ? controller.getDetails().get("hb0")
                : hasDate(controller,"hb1_kurang_7_hari")
                    ? controller.getDetails().get("hb1_kurang_7_hari")
                    :"-"));

        bcg.setText(": " + (controller.getDetails().get("bcg") != null ? transformToddmmyyyy(controller.getDetails().get("bcg")) : "-"));
        pol1.setText(": " + (controller.getDetails().get("polio1") != null ? transformToddmmyyyy(controller.getDetails().get("polio1")) : "-"));
        dpt1.setText(": " + (controller.getDetails().get("dptHb1") != null ? transformToddmmyyyy(controller.getDetails().get("dptHb1")) : "-"));
        pol2.setText(": " + (controller.getDetails().get("polio2") != null ? transformToddmmyyyy(controller.getDetails().get("polio2")) : "-"));
        dpt2.setText(": " + (controller.getDetails().get("dptHb2") != null ? transformToddmmyyyy(controller.getDetails().get("dptHb2")) : "-"));
        pol3.setText(": " + (controller.getDetails().get("polio3") != null ? transformToddmmyyyy(controller.getDetails().get("polio3")) : "-"));
        dpt3.setText(": " + (controller.getDetails().get("dptHb3") != null ? transformToddmmyyyy(controller.getDetails().get("dptHb3")) : "-"));
        pol4.setText(": " + (controller.getDetails().get("polio4") != null ? transformToddmmyyyy(controller.getDetails().get("polio4")) : "-"));
        ipv.setText(": " + (controller.getDetails().get("ipv") != null ? transformToddmmyyyy(controller.getDetails().get("ipv")) : "-"));
        measles.setText(": " + (controller.getDetails().get("campak") != null ? transformToddmmyyyy(controller.getDetails().get("campak")) : "-"));

        complete.setText(": " + yesNo(isComplete()));
        additionalDPT.setText(": " + (controller.getDetails().get("dpt_hb_campak_lanjutan") != null ? transformToddmmyyyy(controller.getDetails().get("dpt_hb_campak_lanjutan")) : "-"));
        additionalMeasles.setText(": " + (controller.getDetails().get("dpt_hb_campak_lanjutan") != null ? transformToddmmyyyy(controller.getDetails().get("dpt_hb_campak_lanjutan")) : "-"));


        String mgender = controller.getDetails().containsKey("gender") ? controller.getDetails().get("gender"):"laki";


        //start profile image

        int placeholderDrawable= mgender.equalsIgnoreCase("male") ? R.drawable.child_boy_infant:R.drawable.child_girl_infant;

        photo.setTag(R.id.entity_id, controller.getCaseId());//required when saving file to disk
        if(controller.getCaseId()!=null){//image already in local storage most likey ):
            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(controller.getCaseId(), OpenSRPImageLoader.getStaticImageListener(photo, placeholderDrawable, placeholderDrawable));

        }

//        if(controller.getDetails().get("profilepic")!= null){
//            if((controller.getDetails().get("gender")!=null?controller.getDetails().get("gender"):"").equalsIgnoreCase("female")) {
//                setImagetoHolderFromUri(VaksinatorDetailActivity.this, controller.getDetails().get("profilepic"), photo, R.drawable.child_girl_infant);
//            } else if ((controller.getDetails().get("gender")!=null?controller.getDetails().get("gender"):"").equalsIgnoreCase("male")){
//                setImagetoHolderFromUri(VaksinatorDetailActivity.this, controller.getDetails().get("profilepic"), photo, R.drawable.child_boy_infant);
//
//            }
//        }
//        else {
//            if (controller.getDetails().get("gender").equalsIgnoreCase("male") ) {
//                photo.setImageDrawable(getResources().getDrawable(R.drawable.child_boy_infant));
//            } else {
//                photo.setImageDrawable(getResources().getDrawable(R.drawable.child_girl_infant));
//            }
//        }

        /*if(controller.getDetails().get("profilepic")!= null){
            setImagetoHolderFromUri(VaksinatorDetailActivity.this, controller.getDetails().get("profilepic"), photo, R.drawable.child_boy_infant);
        }
        else {
            photo.setImageResource(controller.getDetails().get("jenisKelamin").contains("em")
                    ? R.drawable.child_girl_infant
                    : R.drawable.child_boy_infant);

        }
*/
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryFacade.logEvent("taking_anak_pictures_on_child_detail_view");
                bindobject = "anak";
                entityid = controller.entityId();
                android.util.Log.e(TAG, "onClick: " + entityid);
//                dispatchTakePictureIntent(photo);
                Intent intent = new Intent(VaksinatorDetailActivity.this, SmartShutterActivity.class);
                intent.putExtra("IdentifyPerson", false);
                intent.putExtra("org.sid.sidface.ImageConfirmation.id", entityid);
                intent.putExtra("org.sid.sidface.ImageConfirmation.origin", TAG); // send Class Name
                startActivity(intent);


            }
        });
    }

    String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;
    static ImageView mImageView;
    static File currentfile;
    static String bindobject;
    static String entityid;

    private boolean isComplete(){
        return (controller.getDetails().get("hb0") != null &&
                controller.getDetails().get("bcg") != null &&
                controller.getDetails().get("polio1") != null &&
                controller.getDetails().get("dptHb1") != null &&
                controller.getDetails().get("polio2") != null &&
                controller.getDetails().get("dptHb2") != null &&
                controller.getDetails().get("polio3") != null &&
                controller.getDetails().get("dptHb3") != null &&
                controller.getDetails().get("polio4") != null &&
                controller.getDetails().get("campak") != null
        );
    }

    public String yesNo(boolean cond){
        return getString(cond? R.string.mcareyes_button_label : R.string.mcareno_button_label);
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, VaksinatorSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }

    /*
    * Used to check if the variable contains a date (10 character which representing yyyy-MM-dd) or not
    * params:
    * CommonPersonObjectClient pc
    * String variable
    *
    * return:
    * true - if the variable contains date
    * false - if the variable null or less than 10 character length
    * */
    private boolean hasDate(CommonPersonObjectClient pc, String variable){
        return pc.getDetails().get(variable)!=null && pc.getDetails().get(variable).length()==10;
    }

    private int age(String date1, String date2){
        return (Integer.parseInt(date2.substring(0,3)) - Integer.parseInt(date1.substring(0,3)))*360
                + (Integer.parseInt(date2.substring(5,7)) - Integer.parseInt(date1.substring(5,7)))*30
                + (Integer.parseInt(date2.substring(8)) - Integer.parseInt(date1.substring(8)));
    }

    public String transformToddmmyyyy(String date){
        if(date.length()>3) {
            if (date.charAt(4) == '-')
                date = String.format("%s/%s/%s", new String[]{date.substring(8, 10), date.substring(5, 7), date.substring(0, 4)});
        }
        return date;
    }
}