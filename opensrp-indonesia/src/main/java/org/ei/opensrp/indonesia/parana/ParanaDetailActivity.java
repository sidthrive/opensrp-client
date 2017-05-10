package org.ei.opensrp.indonesia.parana;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import org.ei.opensrp.indonesia.R;
import org.ei.opensrp.indonesia.kartu_ibu.NativeKISmartRegisterActivity;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
import org.ei.opensrp.repository.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import util.ImageCache;
import util.ImageFetcher;

import static org.ei.opensrp.util.StringUtil.humanize;
import static org.ei.opensrp.util.StringUtil.humanizeAndDoUPPERCASE;

/**
 * Created by Iq on 07/09/16.
 */
public class ParanaDetailActivity extends Activity {
    SimpleDateFormat timer = new SimpleDateFormat("hh:mm:ss");
    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    //  private static KmsCalc  kmsCalc;
    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;

    //image retrieving

    public static CommonPersonObjectClient ancclient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.parana_detail_activity);

        String DetailStart = timer.format(new Date());
        Map<String, String> ANCDetail = new HashMap<String, String>();
        ANCDetail.put("start", DetailStart);
        FlurryAgent.logEvent("anc_detail_view",ANCDetail, true);

        final ImageView kiview = (ImageView)findViewById(R.id.motherdetailprofileview);
        //header
        TextView today = (TextView) findViewById(R.id.detail_today);

        //profile
        TextView nama = (TextView) findViewById(R.id.txt_wife_name);
        TextView nik = (TextView) findViewById(R.id.txt_nik);
        TextView husband_name = (TextView) findViewById(R.id.txt_husband_name);
        TextView dob = (TextView) findViewById(R.id.txt_dob);
        TextView phone = (TextView) findViewById(R.id.txt_contact_phone_number);

        TextView risk1 = (TextView) findViewById(R.id.txt_risk1);
        TextView risk2 = (TextView) findViewById(R.id.txt_risk2);
        TextView risk3 = (TextView) findViewById(R.id.txt_risk3);

        TextView risk5 = (TextView) findViewById(R.id.txt_risk5);
        TextView risk6 = (TextView) findViewById(R.id.txt_risk6);

        TextView risk7 = (TextView) findViewById(R.id.txt_risk7);
        TextView risk8 = (TextView) findViewById(R.id.txt_risk8);

        TextView sesi1 = (TextView) findViewById(R.id.txt_sesi1);
        TextView sesi2 = (TextView) findViewById(R.id.txt_sesi2);
        TextView sesi3 = (TextView) findViewById(R.id.txt_sesi3);
        TextView sesi4 = (TextView) findViewById(R.id.txt_sesi4);
        TextView txt_head = (TextView) findViewById(R.id.txt_head);

        TextView txt_nama  = (TextView) findViewById(R.id.txt_nama);
        TextView        txt_tgl_lahir = (TextView) findViewById(R.id.txt_tgl_lahir);
        TextView txt_BBL = (TextView) findViewById(R.id.txt_BBL);
        TextView       txt_BBT = (TextView) findViewById(R.id.txt_BBT);
        TextView txt_mmn = (TextView) findViewById(R.id.txt_mmn);
        TextView        txt_kpsp = (TextView) findViewById(R.id.txt_kpsp);
        TextView txt_status1s = (TextView) findViewById(R.id.txt_status);
        
        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ParanaDetailActivity.this, NativeKIParanaSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
                String DetailEnd = timer.format(new Date());
                Map<String, String> KBDetail = new HashMap<String, String>();
                KBDetail.put("end", DetailEnd);
                FlurryAgent.logEvent("anc_detail_view",KBDetail, true);
            }
        });

        nama.setText(getResources().getString(R.string.name)+ (ancclient.getColumnmaps().get("namalengkap") != null ? ancclient.getColumnmaps().get("namalengkap") : "-"));
        nik.setText(getResources().getString(R.string.nik)+ (ancclient.getDetails().get("nik") != null ? ancclient.getDetails().get("nik") : "-"));
        husband_name.setText(getResources().getString(R.string.husband_name)+ (ancclient.getColumnmaps().get("namaSuami") != null ? ancclient.getColumnmaps().get("namaSuami") : "-"));
        dob.setText(getResources().getString(R.string.dob)+ (ancclient.getDetails().get("tanggalLahir") != null ? ancclient.getDetails().get("tanggalLahir") : "-"));
        phone.setText("No HP: "+ (ancclient.getDetails().get("NomorTelponHp") != null ? ancclient.getDetails().get("NomorTelponHp") : "-"));

        sesi1.setText(ancclient.getDetails().get("aktif_sesi1"));
        sesi2.setText(ancclient.getDetails().get("aktif_sesi2"));
        sesi3.setText(ancclient.getDetails().get("aktif_sesi3"));
        sesi4.setText(ancclient.getDetails().get("aktif_sesi4"));

        AllCommonsRepository anak = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("anak");
        if(ancclient.getDetails().get("childId") != null) {
            final CommonPersonObject anakid = anak.findByCaseID(ancclient.getDetails().get("childId"));
            txt_nama.setText(anakid.getColumnmaps().get("namaBayi") != null ? anakid.getColumnmaps().get("namaBayi") : "");
            txt_tgl_lahir.setText(anakid.getColumnmaps().get("tanggalLahirAnak") != null ? anakid.getColumnmaps().get("tanggalLahirAnak") : "");
            txt_BBL.setText(anakid.getDetails().get("beratLahir")!=null?anakid.getDetails().get("beratLahir"):"");

            String berat = anakid.getDetails().get("beratBayi")!=null?" "+anakid.getDetails().get("beratBayi"):"";
            String status_gizi = anakid.getDetails().get("statusGizi")!=null?anakid.getDetails().get("statusGizi"):"";

            txt_kpsp.setText(": "+ humanize(anakid.getDetails().get("hasilDilakukannyaKPSP") != null ? anakid.getDetails().get("hasilDilakukannyaKPSP") : "-"));

            txt_BBT.setText(berat);
            txt_status1s.setText(status_gizi);
        }

        AllCommonsRepository kiRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ibu");

        if(ancclient.getColumnmaps().get("ibu.id") != null) {
            CommonPersonObject kiobject = kiRepository.findByCaseID(ancclient.getColumnmaps().get("ibu.id"));


        String visit_date = kiobject.getColumnmaps().get("ancDate")!= null?kiobject.getColumnmaps().get("ancDate"):"";
        String mmn = kiobject.getDetails().get("jumlahMmn")!= null?kiobject.getDetails().get("jumlahMmn"):"";
        String tt = kiobject.getDetails().get("statusImunisasitt")!= null?kiobject.getDetails().get("statusImunisasitt").replace("tt_ke_",""):"";

        risk1.setText(getResources().getString(R.string.date_visit_title)+" "+visit_date);

        risk2.setText(getResources().getString(R.string.MMN)+" "+mmn);

        risk3.setText(getResources().getString(R.string.tt_ke)+" "+tt);
        }

        int yes = 0;
        String ya1 ="";
        String ya2 ="";
        String ya3 ="";
        String ya4 ="";
        txt_head.setText("RESIKO :");
        if(ancclient.getColumnmaps().get("umur") != null ? isTooYoungMother(ancclient.getColumnmaps().get("umur")) : false){
           ya1 = "Ya";
        }
        if(ancclient.getDetails().get("hidup") != null ? isTooManyChildren(ancclient.getDetails().get("hidup")) : false){
            ya2 = "Ya";
        }
        if(ancclient.getDetails().get("pendidikan") != null ? isLowEducated(ancclient.getDetails().get("pendidikan")) : false){
            ya3 = "Ya";
        }
        if(ancclient.getDetails().get("gravida") != null ? isPrimigravida(ancclient.getDetails().get("gravida")) : false){
            ya4 = "Ya";
        }
        risk5.setText("Ibu Terlalu Muda : "+ya1);
        risk6.setText("Anak Lebih dari 3 : "+ya2);
        risk7.setText("Pendidikan Rendah : "+ya3);
        risk8.setText("Primigravida : "+ya4);


    }

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

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;
    static ImageView mImageView;
    static File currentfile;
    static String bindobject;
    static String entityid;
    private void dispatchTakePictureIntent(ImageView imageView) {
        mImageView = imageView;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentfile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            String imageBitmap = (String) extras.get(MediaStore.EXTRA_OUTPUT);
//            Toast.makeText(this,imageBitmap,Toast.LENGTH_LONG).show();
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("profilepic",currentfile.getAbsolutePath());
            saveimagereference(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
        }
    }
    public void saveimagereference(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("profilepic"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
//                ancclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }
    public static void setImagetoHolder(Activity activity, String file, ImageView view, int placeholder){
        mImageThumbSize = 300;
        mImageThumbSpacing = Context.getInstance().applicationContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);


        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.50f); // Set memory cache to 25% of app memory
        mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
        mImageFetcher.setLoadingImage(placeholder);
        mImageFetcher.addImageCache(activity.getFragmentManager(), cacheParams);
//        Toast.makeText(activity,file,Toast.LENGTH_LONG).show();
        mImageFetcher.loadImage("file:///"+file,view);

//        Uri.parse(new File("/sdcard/cats.jpg")



//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(file, options);
//        view.setImageBitmap(bitmap);
    }
    public static void setImagetoHolderFromUri(Activity activity,String file, ImageView view, int placeholder){
        view.setImageDrawable(activity.getResources().getDrawable(placeholder));
        File externalFile = new File(file);
        Uri external = Uri.fromFile(externalFile);
        view.setImageURI(external);


    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, NativeKIParanaSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }
}
