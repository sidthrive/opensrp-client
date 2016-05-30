package org.ei.opensrp.test.test;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.test.R;
import org.ei.opensrp.test.test.FormulirDdtkSmartRegisterActivity;

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
public class ChildDetailActivity extends Activity {

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
        setContentView(R.layout.child_detail_activity);


        //header
        TextView anakName = (TextView) findViewById(R.id.detail_nama_anak);
        TextView anakJenisKelamin = (TextView) findViewById(R.id.detail_jenis_kelamin);
        TextView anakNamaIbu = (TextView) findViewById(R.id.detail_nama_ibu);
        TextView anakUmur = (TextView) findViewById(R.id.detail_umur);
        TextView anakBerat = (TextView) findViewById(R.id.childdetail_weight);
        TextView anakTinggi = (TextView) findViewById(R.id.childdetail_height);
        TextView anakLingKepala = (TextView) findViewById(R.id.childdetail_headcir);
        TextView anakKpspDate1 = (TextView) findViewById(R.id.childdetail_kpsptestdate1);
        TextView anakKpspResult1 = (TextView) findViewById(R.id.childdetail_kpsptestresult1);
        TextView anakKpspDate2 = (TextView) findViewById(R.id.childdetail_kpsptestdate2);
        TextView anakKpspResult2 = (TextView) findViewById(R.id.childdetail_kpsptestresult2);
        TextView anakKpspDate3 = (TextView) findViewById(R.id.childdetail_kpsptestdate3);
        TextView anakKpspResult3 = (TextView) findViewById(R.id.childdetail_kpsptestresult3);
        TextView anakKpspDate4 = (TextView) findViewById(R.id.childdetail_kpsptestdate4);
        TextView anakKpspResult4 = (TextView) findViewById(R.id.childdetail_kpsptestresult4);
        TextView anakKpspDate5 = (TextView) findViewById(R.id.childdetail_kpsptestdate5);
        TextView anakKpspResult5 = (TextView) findViewById(R.id.childdetail_kpsptestresult5);
        TextView anakKpspDate6 = (TextView) findViewById(R.id.childdetail_kpsptestdate6);
        TextView anakKpspResult6 = (TextView) findViewById(R.id.childdetail_kpsptestresult6);
        TextView anakHearingDate = (TextView) findViewById(R.id.childdetail_hearingtestdate);
        TextView anakHearingResult = (TextView) findViewById(R.id.childdetail_hearingtestresult);
        TextView anakVisualDate = (TextView) findViewById(R.id.childdetail_visualtestdate);
        TextView anakVisualResult = (TextView) findViewById(R.id.childdetail_visualtestresult);
        TextView anakMentalDate = (TextView) findViewById(R.id.childdetail_mentaltestdate);
        TextView anakMentalResult = (TextView) findViewById(R.id.childdetail_mentaltestresult);
        TextView anakAutistDate = (TextView) findViewById(R.id.childdetail_autisttestdate);
        TextView anakAutistResult = (TextView) findViewById(R.id.childdetail_autisttestresult);
        TextView anakGgphDate = (TextView) findViewById(R.id.childdetail_ggphtestdate);
        TextView anakGgphResult = (TextView) findViewById(R.id.childdetail_ggphtestresult);

        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ChildDetailActivity.this, FormulirDdtkSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        anakName.setText(childclient.getDetails().get("nama_anak") != null ? childclient.getDetails().get("nama_anak") : "-");
        anakJenisKelamin.setText(childclient.getDetails().get("jenis_kelamin") != null ? childclient.getDetails().get("jenis_kelamin") : "-");
        anakNamaIbu.setText(childclient.getDetails().get("nama_ibu") != null ? childclient.getDetails().get("nama_ibu") : "-");
        anakUmur.setText(childclient.getDetails().get("umur") != null ? childclient.getDetails().get("umur") : "-");
        anakBerat.setText(childclient.getDetails().get("berat") != null ? childclient.getDetails().get("berat") : "-");
        anakTinggi.setText(childclient.getDetails().get("tinggi") != null ? childclient.getDetails().get("tinggi") : "-");
        anakLingKepala.setText(childclient.getDetails().get("lingkar_kepala") != null ? childclient.getDetails().get("lingkar_kepala") : "-");
        anakKpspDate1.setText(getString(R.string.kpspdate1) +" : "+ (childclient.getDetails().get("kpsp_test_date1") != null ? childclient.getDetails().get("kpsp_test_date1") : "-"));
        anakKpspResult1.setText(getString(R.string.kpspresult1) +" : "+ (childclient.getDetails().get("status_kembang1") != null ? childclient.getDetails().get("status_kembang1") : "-"));
        anakKpspDate2.setText(getString(R.string.kpspdate2) + " : " + (childclient.getDetails().get("kpsp_test_date2") != null ? childclient.getDetails().get("kpsp_test_date2") : "-"));
        anakKpspResult2.setText(getString(R.string.kpspresult2) +" : "+ (childclient.getDetails().get("status_kembang2") != null ? childclient.getDetails().get("status_kembang2") : "-"));
        anakKpspDate3.setText(getString(R.string.kpspdate3) +" : "+ (childclient.getDetails().get("kpsp_test_date3") != null ? childclient.getDetails().get("kpsp_test_date3") : "-"));
        anakKpspResult3.setText(getString(R.string.kpspresult3) +" : "+ (childclient.getDetails().get("status_kembang3") != null ? childclient.getDetails().get("status_kembang3") : "-"));
        anakKpspDate4.setText(getString(R.string.kpspdate4) +" : "+ (childclient.getDetails().get("kpsp_test_date4") != null ? childclient.getDetails().get("kpsp_test_date4") : "-"));
        anakKpspResult4.setText(getString(R.string.kpspresult4) +" : "+ (childclient.getDetails().get("status_kembang4") != null ? childclient.getDetails().get("status_kembang4") : "-"));
        anakKpspDate5.setText(getString(R.string.kpspdate5) +" : "+ (childclient.getDetails().get("kpsp_test_date5") != null ? childclient.getDetails().get("kpsp_test_date5") : "-"));
        anakKpspResult5.setText(getString(R.string.kpspresult5) +" : "+ (childclient.getDetails().get("status_kembang5") != null ? childclient.getDetails().get("status_kembang5") : "-"));
        anakKpspDate6.setText(getString(R.string.kpspdate6) +" : "+ (childclient.getDetails().get("kpsp_test_date6") != null ? childclient.getDetails().get("kpsp_test_date6") : "-"));
        anakKpspResult6.setText(getString(R.string.kpspresult6) +" : "+ (childclient.getDetails().get("status_kembang6") != null ? childclient.getDetails().get("status_kembang6") : "-"));
        anakHearingDate.setText(getString(R.string.hearingdate) +" : "+ (childclient.getDetails().get("hear_test_date") != null ? childclient.getDetails().get("hear_test_date") : "-"));
        anakHearingResult.setText(getString(R.string.hearingresult) +" : "+ (childclient.getDetails().get("daya_dengar") != null ? childclient.getDetails().get("daya_dengar") : "-"));
        anakVisualDate.setText(getString(R.string.visualdate) +" : "+ (childclient.getDetails().get("sight_test_date") != null ? childclient.getDetails().get("sight_test_date") : "-"));
        anakVisualResult.setText(getString(R.string.visualresult) +" : "+ (childclient.getDetails().get("daya_lihat") != null ? childclient.getDetails().get("daya_lihat") : "-"));
        anakMentalDate.setText(getString(R.string.mentaldate) +" : "+ (childclient.getDetails().get("mental_test_date") != null ? childclient.getDetails().get("mental_test_date") : "-"));
        anakMentalResult.setText(getString(R.string.mentalresult) +" : "+ (childclient.getDetails().get("mental_emosional") != null ? childclient.getDetails().get("mental_emosional") : "-"));
        anakAutistDate.setText(getString(R.string.autisdate) +" : "+ (childclient.getDetails().get("autis_test_date") != null ? childclient.getDetails().get("autis_test_date") : "-"));
        anakAutistResult.setText(getString(R.string.autisresult) +" : "+ (childclient.getDetails().get("autis") != null ? childclient.getDetails().get("autis") : "-"));
        anakGgphDate.setText(getString(R.string.ggphdate) +" : "+ (childclient.getDetails().get("gpph_test_date") != null ? childclient.getDetails().get("gpph_test_date") : "-"));
        anakGgphResult.setText(getString(R.string.ggphresult) +" : "+ (childclient.getDetails().get("gpph") != null ? childclient.getDetails().get("gpph") : "-"));

        //KMS calculation

        //Graph




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
}