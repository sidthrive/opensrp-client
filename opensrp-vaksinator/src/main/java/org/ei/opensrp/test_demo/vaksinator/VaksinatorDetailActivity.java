package org.ei.opensrp.test_demo.vaksinator;

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

import org.apache.commons.io.FilenameUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.test_demo.R;
import org.ei.opensrp.test_demo.face.camera.SmartShutterActivity;
import org.ei.opensrp.test_demo.face.camera.util.Tools;

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
    public static CommonPersonObjectClient kiclient;
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


    private static HashMap<String, String> hash;
    private boolean updateMode = false;
    private String mode;

    private String photo_path;
    private File tb_photo;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.smart_register_jurim_detail_client);

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
        TextView posyanduLabel = (TextView) findViewById(R.id.healthPostLabel);
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
        TextView nama = (TextView) findViewById(R.id.childName);
        TextView motherName = (TextView) findViewById(R.id.motherName);
        TextView fatherName = (TextView) findViewById(R.id.fatherName);
        TextView posyandu = (TextView) findViewById(R.id.posyandu);
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
        posyanduLabel.setText(getString(R.string.posyandu));
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

        nama.setText(": " + (controller.getColumnmaps().get("nama_bayi") != null ? controller.getColumnmaps().get("nama_bayi") : "-"));

        fatherName.setText(": " + (controller.getDetails().get("namaAyah") != null ? controller.getDetails().get("namaAyah") : "-"));
        motherName.setText(": " + (controller.getDetails().get("namaIbu") != null
                ? controller.getDetails().get("namaIbu")
                : controller.getDetails().get("nama_orang_tua")!=null
                        ? controller.getDetails().get("nama_orang_tua")
                        : "-"
            )
        );
        village.setText(": " + (controller.getDetails().get("desa") != null
                ? controller.getDetails().get("desa")
                : "-"));

        subVillage.setText(": " + (controller.getDetails().get("dusun") != null
                ? controller.getDetails().get("dusun")
                : controller.getDetails().get("village") != null
                    ? controller.getDetails().get("village")
                    : "-")
        );

        posyandu.setText(": " + (controller.getDetails().get("nama_lokasi") != null
                ? controller.getDetails().get("nama_lokasi")
                : controller.getDetails().get("posyandu")!=null
                    ? controller.getDetails().get("posyandu")
                    : "-"));
        dateOfBirth.setText(": " + (controller.getColumnmaps().get("tanggalLahirAnak") != null ? controller.getColumnmaps().get("tanggalLahirAnak") : "-"));
        birthWeight.setText(": " + (controller.getDetails().get("berat_badan_saat_lahir") != null
                                    ? Double.toString(Integer.parseInt(controller.getDetails()
                                                        .get("berat_badan_saat_lahir"))/1000)
                                                        + " kg"
                                    : "-"));
        antipiretik.setText(": " + (controller.getDetails().get("getAntypiretic") != null ? controller.getDetails().get("getAntypiretic") : "-"));

        hb1Under7.setText(": " + (hasDate(controller,"hb0")
                ? controller.getDetails().get("hb0")
                : hasDate(controller,"hb1_kurang_7_hari")
                    ? controller.getDetails().get("hb1_kurang_7_hari")
                    :"-"));

        bcg.setText(": " + (controller.getDetails().get("bcg") != null ? controller.getDetails().get("bcg") : "-"));
        pol1.setText(": " + (controller.getDetails().get("polio1") != null ? controller.getDetails().get("polio1") : "-"));
        dpt1.setText(": " + (controller.getDetails().get("dpt_hb1") != null ? controller.getDetails().get("dpt_hb1") : "-"));
        pol2.setText(": " + (controller.getDetails().get("polio2") != null ? controller.getDetails().get("polio2") : "-"));
        dpt2.setText(": " + (controller.getDetails().get("dpt_hb2") != null ? controller.getDetails().get("dpt_hb2") : "-"));
        pol3.setText(": " + (controller.getDetails().get("polio3") != null ? controller.getDetails().get("polio3") : "-"));
        dpt3.setText(": " + (controller.getDetails().get("dpt_hb3") != null ? controller.getDetails().get("dpt_hb3") : "-"));
        pol4.setText(": " + (controller.getDetails().get("polio4") != null ? controller.getDetails().get("polio4") : "-"));
        ipv.setText(": " + (controller.getDetails().get("ipv") != null ? controller.getDetails().get("ipv") : "-"));
        measles.setText(": " + (controller.getDetails().get("imunisasi_campak") != null ? controller.getDetails().get("imunisasi_campak") : "-"));

        complete.setText(": " + (controller.getDetails().get("imunisasi_lengkap") != null ? controller.getDetails().get("imunisasi_lengkap") : "-"));
        additionalDPT.setText(": " + (controller.getDetails().get("dpt_hb_campak_lanjutan") != null ? controller.getDetails().get("dpt_hb_campak_lanjutan") : "-"));
        additionalMeasles.setText(": " + (controller.getDetails().get("dpt_hb_campak_lanjutan") != null ? controller.getDetails().get("dpt_hb_campak_lanjutan") : "-"));

//        if(controller.getDetails().get("profilepic")!= null){
//            setImagetoHolderFromUri(VaksinatorDetailActivity.this, controller.getDetails().get("profilepic"), photo, R.drawable.child_boy_infant);
//        }
//        else {
//            photo.setImageResource(controller.getDetails().get("jenis_kelamin").contains("em")
//                    ? R.drawable.child_girl_infant
//                    : R.drawable.child_boy_infant);
//
//        }

//        Profile Picture
        photo_path = controller.getDetails().get("profilepic");

        if (Tools.getPhotoPath() != null) {
            String absoluteFilePathNoExt = FilenameUtils.removeExtension(Tools.getPhotoPath());
            fileName = absoluteFilePathNoExt.substring(absoluteFilePathNoExt.lastIndexOf("/") + 1);
        }

        if (photo_path != null) {
            tb_photo = new File(photo_path);
            if (!tb_photo.exists()) {
                photo.setImageDrawable(getResources().getDrawable(R.drawable.fr_not_found_404));
            } else {
                setImagetoHolderFromUri(this, controller.getDetails().get("profilepic"), photo, R.drawable.child_boy_infant);
            }
        } else if (Tools.getPhotoPath() != null && fileName.equals(controller.getCaseId())) {
            setImagetoHolderFromUri(this, Tools.getPhotoPath(), photo, R.drawable.child_boy_infant);

        } else {
            photo.setImageResource(controller.getDetails().get("jenis_kelamin").contains("em")
                    ? R.drawable.child_girl_infant
                    : R.drawable.child_boy_infant);
        }



        hash = Tools.retrieveHash(context.applicationContext());

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bindobject = "anak";
                entityid = controller.entityId();

                if (hash.containsValue(entityid)) {
                    Log.e(TAG, "onClick: " + entityid + " updated");
                    mode = "updated";
                    updateMode = true;

                }

                Intent intent = new Intent(VaksinatorDetailActivity.this, SmartShutterActivity.class);
                intent.putExtra("IdentifyPerson", false);
                intent.putExtra("org.sid.sidface.ImageConfirmation.id", entityid);
                intent.putExtra("org.sid.sidface.ImageConfirmation.origin", TAG); // send Class Name
                startActivity(intent);

            }
        });
    }

    String mCurrentPhotoPath;

    static String bindobject;
    static String entityid;

    public static void setImagetoHolderFromUri(Activity activity,String file, ImageView view, int placeholder){
        view.setImageDrawable(activity.getResources().getDrawable(placeholder));
        File externalFile = new File(file);
        Uri external = Uri.fromFile(externalFile);
        view.setImageURI(external);


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
}