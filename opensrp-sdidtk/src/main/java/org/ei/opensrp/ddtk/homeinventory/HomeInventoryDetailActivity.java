package org.ei.opensrp.ddtk.homeinventory;

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

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.ddtk.R;
import org.ei.opensrp.repository.DetailsRepository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import util.ImageCache;
import util.ImageFetcher;

/**
 * Created by muhammad.ahmed@ihsinformatics.com on 20-Oct-15.
 */
public class HomeInventoryDetailActivity extends Activity {

    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;




    //image retrieving

    public static CommonPersonObjectClient childclient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.home_invent_detail_activity);


        //header
        TextView anakName = (TextView) findViewById(R.id.Nama);
        TextView anakJenisKelamin = (TextView) findViewById(R.id.Kelamin);
        TextView anakNamaIbu = (TextView) findViewById(R.id.txt_ortu);
        TextView anakUmur = (TextView) findViewById(R.id.Lahir);
        TextView anakBerat = (TextView) findViewById(R.id.Berat);
        TextView anakTinggi = (TextView) findViewById(R.id.childdetail_height);
        TextView anakLingKepala = (TextView) findViewById(R.id.childdetail_headcir);

        TextView responsivitas = (TextView) findViewById(R.id.Responsivitas);
        TextView penerimaan = (TextView) findViewById(R.id.Penerimaan);
        TextView keteraturan = (TextView) findViewById(R.id.Keteraturan);
        TextView sumber_belajar = (TextView) findViewById(R.id.Sumber_belajar);
        TextView keterlibatan = (TextView) findViewById(R.id.Keterlibatan);
        TextView varisai = (TextView) findViewById(R.id.Varisai);

        TextView responsivitasEnd = (TextView) findViewById(R.id.ResponsivitasEnd);
        TextView penerimaanEnd = (TextView) findViewById(R.id.PenerimaanEnd);
        TextView keteraturanEnd = (TextView) findViewById(R.id.KeteraturanEnd);
        TextView sumber_belajarEnd = (TextView) findViewById(R.id.Sumber_belajarEnd);
        TextView keterlibatanEnd = (TextView) findViewById(R.id.KeterlibatanEnd);
        TextView varisaiEnd = (TextView) findViewById(R.id.VarisaiEnd);

        TextView materiPembelajaran = (TextView) findViewById(R.id.materi_pembelajaran3);
        TextView stimulasiBahasa = (TextView) findViewById(R.id.stimulasi_bahasa);
        TextView lingkunganFisik = (TextView) findViewById(R.id.lingkungan_fisik);
        TextView responsivitas3 = (TextView) findViewById(R.id.responsivitas3);
        TextView stimulasiAkademik = (TextView) findViewById(R.id.stimulasi_akademik);
        TextView keteladanan = (TextView) findViewById(R.id.keteladanan);
        TextView variasi = (TextView) findViewById(R.id.variasi);
        TextView penerimaan3 = (TextView) findViewById(R.id.penerimaan);

        TextView materiPembelajaranEnd = (TextView) findViewById(R.id.materi_pembelajaran3_end);
        TextView stimulasiBahasaEnd = (TextView) findViewById(R.id.stimulasi_bahasaEnd);
        TextView lingkunganFisikEnd = (TextView) findViewById(R.id.lingkungan_fisikEnd);
        TextView responsivitas3End = (TextView) findViewById(R.id.responsivitas3End);
        TextView stimulasiAkademikEnd = (TextView) findViewById(R.id.stimulasi_akademikEnd);
        TextView keteladananEnd = (TextView) findViewById(R.id.keteladananEnd);
        TextView variasiEnd = (TextView) findViewById(R.id.variasiEnd);
        TextView penerimaan3End = (TextView) findViewById(R.id.penerimaanEnd);

        DetailsRepository detailsRepository = org.ei.opensrp.Context.getInstance().detailsRepository();
        detailsRepository.updateDetails(childclient);

        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(HomeInventoryDetailActivity.this, HomeInventorySmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });
        final ImageView childview = (ImageView)findViewById(R.id.childdetailprofileview);
        if(childclient.getDetails().get("profilepic")!= null){
            if(getStringDetails("jenis_kelamin").equalsIgnoreCase("female")) {
                setImagetoHolderFromUri(HomeInventoryDetailActivity.this, childclient.getDetails().get("profilepic"), childview, R.mipmap.womanimageload);
            } else if (getStringDetails("jenis_kelamin").equalsIgnoreCase("male")){
                setImagetoHolderFromUri(HomeInventoryDetailActivity.this, childclient.getDetails().get("profilepic"), childview, R.mipmap.householdload);
            }
        }else{

            if(getStringDetails("jenis_kelamin").equalsIgnoreCase("female")){
                childview.setImageDrawable(getResources().getDrawable(R.drawable.child_girl_infant));
            }else if (getStringDetails("jenis_kelamin").equalsIgnoreCase("male")){
                childview.setImageDrawable(getResources().getDrawable(R.drawable.child_boy_infant));
            }
        }
        // }
       /* childview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bindobject = "anak";
                entityid = childclient.entityId();
                dispatchTakePictureIntent(childview);

            }
        });*/

        anakName.setText(getString(R.string.detailNamaAnak) +": "+ (getStringDetails("namaBayi")).replaceAll("_"," "));
        anakJenisKelamin.setText(getString(R.string.detailJenisKelamin) +": "+ getStringDetails("gender").replaceAll("_"," "));

        String ages = getStringColumnmaps("tanggalLahirAnak").substring(0, getStringColumnmaps("tanggalLahirAnak").indexOf("T"));
        anakUmur.setText(getString(R.string.detailUmur) +": "+ Integer.toString(monthRangeToToday(ages))+" Bulan" );

        AllCommonsRepository childRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ec_anak");
        CommonPersonObject childobject = childRepository.findByCaseID(childclient.entityId());
        AllCommonsRepository kirep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ec_kartu_ibu");
        final CommonPersonObject kiparent = kirep.findByCaseID(childobject.getColumnmaps().get("relational_id"));

        if(kiparent != null) {
            detailsRepository.updateDetails(kiparent);
            String namaayah = getStringColumnmaps(kiparent,"namaSuami");
            String namaibu = getStringColumnmaps(kiparent,"namalengkap");

            anakNamaIbu.setText(namaibu + "," + namaayah);
            //   viewHolder.village_name.setText(kiparent.getDetails().get("address1")!=null?kiparent.getDetails().get("address1") :"-");
        }

        anakBerat.setText(getString(R.string.detailBerat) +": "+ getStringDetails("berat").replaceAll("_"," "));
        anakTinggi.setText(getString(R.string.detailTinggi) +": "+ getStringDetails("tinggi").replaceAll("_"," "));
        anakLingKepala.setText(getString(R.string.detailLingkarKepala) +": "+ getStringDetails("lingkar_kepala").replaceAll("_"," "));

/* TEMP 001 Location */
        HomeConstant constant = new HomeConstant();

        responsivitas.setText(countLine(constant.responsivitas,base02));
        penerimaan.setText(countLine(constant.penerimaan,base02));
        keteraturan.setText(countLine(constant.keteraturan,base02));
        sumber_belajar.setText(countLine(constant.materiPembelajaran,base02));
        keterlibatan.setText(countLine(constant.keterlibatan,base02));
        varisai.setText(countLine(constant.variasi,base02));

        responsivitasEnd.setText(countLine(constant.responsivitas,end02));
        penerimaanEnd.setText(countLine(constant.penerimaan,end02));
        keteraturanEnd.setText(countLine(constant.keteraturan,end02));
        sumber_belajarEnd.setText(countLine(constant.materiPembelajaran,end02));
        keterlibatanEnd.setText(countLine(constant.keterlibatan,end02));
        varisaiEnd.setText(countLine(constant.variasi,end02));

        materiPembelajaran.setText(countLine(constant.materiPembelajaran3,base36));
        stimulasiBahasa.setText(countLine(constant.stimulasiBahasa,base36));
        lingkunganFisik.setText(countLine(constant.lingkunganFisik,base36));
        responsivitas3.setText(countLine(constant.responsivitas3,base36));
        stimulasiAkademik.setText(countLine(constant.stimulasiAkademik,base36));
        keteladanan.setText(countLine(constant.keteladanan,base36));
        variasi.setText(countLine(constant.variasi3,base36));
        penerimaan3.setText(countLine(constant.penerimaan3,base36));

        materiPembelajaranEnd.setText(countLine(constant.materiPembelajaran3,end36));
        stimulasiBahasaEnd.setText(countLine(constant.stimulasiBahasa,end36));
        lingkunganFisikEnd.setText(countLine(constant.lingkunganFisik,end36));
        responsivitas3End.setText(countLine(constant.responsivitas3,end36));
        stimulasiAkademikEnd.setText(countLine(constant.stimulasiAkademik,end36));
        keteladananEnd.setText(countLine(constant.keteladanan,end36));
        variasiEnd.setText(countLine(constant.variasi3,end36));
        penerimaan3End.setText(countLine(constant.penerimaan3,end36));
    }

    public void checkHome02 (int fisrt, int last , TextView total1 ) {
        int _endlinecount = 0;
        for (int i = fisrt ; i <=last ; i++){
            String home_endline = "home"+i+"_it";
            if(childclient.getDetails().get(home_endline) !=null) {
                if (childclient.getDetails().get(home_endline).equalsIgnoreCase("Yes")) {
                    _endlinecount = _endlinecount + 1;
                }
            }
        }
        total1.setText(""+_endlinecount);
    }

    public void checkHome36 (int fisrt, int last , TextView total ) {
        int _endlinecount = 0;
        for (int i = fisrt ; i <=last ; i++){
            String home_endline = "home"+i+"_ec";
            if(childclient.getDetails().get(home_endline) !=null) {
                if (childclient.getDetails().get(home_endline).equalsIgnoreCase("Yes")) {
                    _endlinecount = _endlinecount + 1;
                } else {

                }
            }
        }
        total.setText(""+_endlinecount);
    }

    // NOT USING PICTURE AT THE MOMENT
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
            HashMap <String,String> details = new HashMap<String,String>();
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
//                childclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }
    public static void setImagetoHolder(Activity activity,String file, ImageView view, int placeholder){
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
        startActivity(new Intent(this, HomeInventorySmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }

    public boolean notNull(String data){
        return data != null;
    }

    /**
     * Util method
     */

    private int monthRangeToToday(String lastVisitDate){
        String currentDate[] = new SimpleDateFormat("yyyy-MM").format(new java.util.Date()).substring(0,7).split("-");
        return ((Integer.parseInt(currentDate[0]) - Integer.parseInt(lastVisitDate.substring(0,4)))*12 +
                (Integer.parseInt(currentDate[1]) - Integer.parseInt(lastVisitDate.substring(5,7))));
    }

    public String getStringDetails(String key){
        return notNull(childclient.getDetails().get(key)) ? childclient.getDetails().get(key) : "-";
    }

    public String getStringColumnmaps(String key){
        return notNull(childclient.getColumnmaps().get(key)) ? childclient.getColumnmaps().get(key) : "-";
    }

    public String getStringDetails(CommonPersonObject person, String key){
        return notNull(person.getDetails().get(key)) ? person.getDetails().get(key) : "-";
    }

    public String getStringColumnmaps(CommonPersonObject person, String key){
        return notNull(person.getColumnmaps().get(key)) ? person.getColumnmaps().get(key) : "-";
    }

    private class HomeConstant{
        public final int [] responsivitas = {1,27,28,29,30,31,32,33,34,35,36};
        public final int [] penerimaan ={2,17,26,37,38,39,40,41};
        public final int [] keteraturan = {3,4,5,6,8,44};
        public final int [] materiPembelajaran = {9,10,11,12,13,14,15,20,42};
        public final int [] keterlibatan = {7,18,19,21,43};
        public final int [] variasi = {7,18,19,21,43};

        public final int [] materiPembelajaran3 ={12,13,14,15,16,17,18,19,20,21,31,32};
        public final int [] stimulasiBahasa ={2,7,23,26,29,43,50};
        public final int [] lingkunganFisik ={1,39,40,41,42};
        public final int [] responsivitas3 ={3,45,46,47,48,49,51,55};
        public final int [] stimulasiAkademik ={24,25,27,28,38};
        public final int [] keteladanan ={4,5,9,11,};
        public final int [] variasi3 ={8,10,22,30,33,34,44,35,36,37};
        public final int [] penerimaan3 ={6,52,53,54,};
    }

    private final String base02 = "_it";
    private final String end02 = "_it_end";
    private final String base36 = "_ec";
    private final String end36 = "_ec_end";

    private String countLine(int[]list, String type){
        int a=0;
        for(int i:list){
            a += notNull(childclient.getDetails().get("home"+i+type))? 1 : 0;
        }
        return a+"/"+list.length;
    }
}


/* Temp01
        checkHome02(1,11,Responsivitas);
        checkHome02(12,19,Penerimaan);
        checkHome02(20,25,Keteraturan);
        checkHome02(26,34,Sumber_belajar);
        checkHome02(35,40,Keterlibatan);
        checkHome02(41,45,Varisai);

        checkHome36(1,5,Lingkungan_3);
        checkHome36(6,9,berbicara_3);
        checkHome36(10,15,Interaksi_3);
        checkHome36(16,21,Sikap_3);
        checkHome36(22,30,umum_3);
        checkHome36(31,37,Mainan_3);
        checkHome36(38,45,pembelajaran_3);
        checkHome36(48,55,Kebiasaan_3);
 */