package org.ei.opensrp.gizi_demo.gizi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.apache.commons.io.FilenameUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.gizi_demo.R;
import org.ei.opensrp.gizi_demo.face.camera.SmartShutterActivity;
import org.ei.opensrp.gizi_demo.face.camera.util.Tools;
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

/**
 * Created by Iq on 26/04/16.
 */
public class ChildDetailActivity extends Activity {
    public static CommonPersonObjectClient kiclient;
    SimpleDateFormat timer = new SimpleDateFormat("hh:mm:ss");
    //image retrieving
    private static final String TAG = ChildDetailActivity.class.getSimpleName();
    private static final String IMAGE_CACHE_DIR = "thumbs";
  //  private static KmsCalc  kmsCalc;
    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;

    //image retrieving

    public static CommonPersonObjectClient childclient;


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
        setContentView(R.layout.gizi_detail_activity);
        String DetailStart = timer.format(new Date());
                Map<String, String> Detail = new HashMap<String, String>();
                Detail.put("start", DetailStart);
                FlurryAgent.logEvent("gizi_detail_view",Detail, true );

        final ImageView childview = (ImageView)findViewById(R.id.detail_profilepic);
        //header
        TextView header_name = (TextView) findViewById(R.id.header_name);
        //sub header
        TextView subheader = (TextView) findViewById(R.id.txt_title_label);
        //profile
        TextView uniqueId = (TextView) findViewById(R.id.txt_profile_unique_id);
        TextView nama = (TextView) findViewById(R.id.txt_profile_child_name);
        TextView father_name = (TextView) findViewById(R.id.txt_profile_father_name);
        TextView mother_name = (TextView) findViewById(R.id.txt_profile_mother_name);
        TextView posyandu = (TextView) findViewById(R.id.txt_profile_posyandu);
        TextView village_name = (TextView) findViewById(R.id.txt_profile_village_name);
        TextView birth_date = (TextView) findViewById(R.id.txt_profile_birth_date);
        TextView gender = (TextView) findViewById(R.id.txt_profile_child_gender);
        TextView birthWeight = (TextView) findViewById(R.id.txt_profile_birth_weight);
        TextView weight = (TextView) findViewById(R.id.txt_profile_last_weight);
        TextView height = (TextView) findViewById(R.id.txt_profile_last_height);
        //child growth
        TextView nutrition_status = (TextView) findViewById(R.id.txt_profile_nutrition_status);
        TextView bgm = (TextView) findViewById(R.id.txt_profile_bgm);
        TextView dua_t = (TextView) findViewById(R.id.txt_profile_2t);
        TextView under_yellow_line = (TextView) findViewById(R.id.txt_profile_under_yellow_line);
        TextView breast_feeding = (TextView) findViewById(R.id.txt_profile_breastfeeding);
        TextView mpasi = (TextView) findViewById(R.id.txt_profile_mp_asi);
        TextView vitA = (TextView) findViewById(R.id.txt_vitA);
        TextView obatCacing = (TextView) findViewById(R.id.txt_anthelmintic);
        TextView lastVitA = (TextView) findViewById(R.id.txt_profile_last_vitA);
        TextView lastAnthelmintic = (TextView) findViewById(R.id.txt_profile_last_anthelmintic);

        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ChildDetailActivity.this, GiziSmartRegisterActivity.class));
                overridePendingTransition(0, 0);

                String DetailEnd = timer.format(new Date());
                Map<String, String> Detail = new HashMap<String, String>();
                Detail.put("end", DetailEnd);
                FlurryAgent.logEvent("gizi_detail_view",Detail, true );
            }
        });


        System.out.println("cl maps: "+childclient.getColumnmaps().toString());
        System.out.println("details: " +  childclient.getDetails().toString());


        //        Profile Picture
        photo_path = childclient.getDetails().get("profilepic");

        if (Tools.getPhotoPath() != null) {
            String absoluteFilePathNoExt = FilenameUtils.removeExtension(Tools.getPhotoPath());
            fileName = absoluteFilePathNoExt.substring(absoluteFilePathNoExt.lastIndexOf("/") + 1);
        }

        if (photo_path != null) {
            tb_photo = new File(photo_path);
            if (!tb_photo.exists()) {
                childview.setImageDrawable(getResources().getDrawable(R.drawable.fr_not_found_404));
            } else {
                setImagetoHolderFromUri(this, childclient.getDetails().get("profilepic"), childview, R.drawable.child_boy_infant);
            }
        } else if (Tools.getPhotoPath() != null && fileName.equals(childclient.getCaseId())) {
            setImagetoHolderFromUri(this, Tools.getPhotoPath(), childview, R.drawable.child_boy_infant);

        } else {
            childview.setImageResource(childclient.getDetails().get("jenisKelamin").contains("em")
                    ? R.drawable.child_girl_infant
                    : R.drawable.child_boy_infant);
        }


//        if(childclient.getDetails().get("profilepic")!= null){
//            if((childclient.getDetails().get("gender")!=null?childclient.getDetails().get("gender"):"").equalsIgnoreCase("female")) {
//                setImagetoHolderFromUri(ChildDetailActivity.this, childclient.getDetails().get("profilepic"), childview, R.mipmap.child_boy_infant);
//            } else if ((childclient.getDetails().get("gender")!=null?childclient.getDetails().get("gender"):"").equalsIgnoreCase("male")){
//                setImagetoHolderFromUri(ChildDetailActivity.this, childclient.getDetails().get("profilepic"), childview, R.mipmap.child_boy_infant);
//
//            }
//        }
//        else {
//            if (childclient.getDetails().get("gender").equalsIgnoreCase("male") || childclient.getDetails().get("gender").equalsIgnoreCase("laki-laki")) {
//                childview.setImageDrawable(getResources().getDrawable(R.mipmap.child_boy_infant));
//            } else {
//                childview.setImageDrawable(getResources().getDrawable(R.mipmap.child_girl_infant));
//            }
//        }

        header_name.setText(R.string.child_profile);
        subheader.setText(R.string.child_profile);
        uniqueId.setText(getString(R.string.unique_id) + " " + (childclient.getDetails().get("unique_id") != null ? childclient.getDetails().get("unique_id"):"-"));
        nama.setText(getString(R.string.child_name) +" "+ (childclient.getColumnmaps().get("namaBayi") != null ? childclient.getColumnmaps().get("namaBayi") : "-"));
        father_name.setText(getString(R.string.father_name)+" "+(childclient.getDetails().get("namaAyah")!=null ? childclient.getDetails().get("namaAyah") : "-"));
        mother_name.setText(getString(R.string.mother_name) +" : "+ (childclient.getDetails().get("namaIbu") != null ? childclient.getDetails().get("namaIbu")
                : childclient.getDetails().get("namaOrtu")!=null
                    ? childclient.getDetails().get("namaOrtu")
                    : "-"));
        posyandu.setText(getString(R.string.posyandu) +" "+ (childclient.getDetails().get("posyandu") != null ? childclient.getDetails().get("posyandu") : "-"));
        village_name.setText(getString(R.string.village) +" "+ (childclient.getDetails().get("desa") != null ? childclient.getDetails().get("desa") : "-"));
        birth_date.setText(getString(R.string.birth_date) +" "+ (childclient.getDetails().get("tanggalLahir") != null ? childclient.getDetails().get("tanggalLahir") : "-"));
        gender.setText(getString(R.string.gender) +" "+ (childclient.getDetails().get("gender") != null ? gender(childclient.getDetails().get("gender")) : "-"));
        birthWeight.setText(getString(R.string.birth_weight) + " " + (childclient.getDetails().get("beratLahir") != null ? childclient.getDetails().get("beratLahir") + " gr" : "-"));
        weight.setText(getString(R.string.weight) +" "+ (childclient.getDetails().get("beratBadan") != null ? childclient.getDetails().get("beratBadan")+"Kg" : "- Kg"));
        height.setText(getString(R.string.height) +" "+ (childclient.getDetails().get("tinggiBadan") != null ? childclient.getDetails().get("tinggiBadan")+"Cm" : "- Cm"));
        vitA.setText(getString(R.string.vitamin_a) +" : "+ (inTheSameRegion(childclient.getDetails().get("lastVitA")) ? getString(R.string.yes) : getString(R.string.no)));
        mpasi.setText(getString(R.string.mpasi) + " "+(childclient.getDetails().get("mp_asi")!=null ? yesNo(childclient.getDetails().get("mp_asi")) : "-"));
        obatCacing.setText(getString(R.string.obatcacing)+ " "+(inTheSameRegionAnth(childclient.getDetails().get("lastAnthelmintic")) ? getString(R.string.yes) : getString(R.string.no)));
        lastVitA.setText(getString(R.string.lastVitA)+" "+(childclient.getDetails().get("lastVitA")!=null ? childclient.getDetails().get("lastVitA") : "-"));
        lastAnthelmintic.setText(getString(R.string.lastAnthelmintic)+" "+(childclient.getDetails().get("lastAnthelmintic")!=null ? childclient.getDetails().get("lastAnthelmintic") : "-"));
        //set value
        String berats = childclient.getDetails().get("history_berat")!= null ? childclient.getDetails().get("history_berat") :"0";
        String[] history_berat = berats.split(",");
        String umurs = childclient.getDetails().get("history_umur")!= null ? childclient.getDetails().get("history_umur") :"0";
        String[] history_umur = umurs.split(",");


            dua_t.setText(getString(R.string.dua_t) +" "+ (childclient.getDetails().get("dua_t") != null ? yesNo(childclient.getDetails().get("dua_t")) : "-"));
            bgm.setText(getString(R.string.bgm) + " "+ (childclient.getDetails().get("bgm") != null ? yesNo(childclient.getDetails().get("bgm")) : "-"));
            under_yellow_line.setText(getString(R.string.under_yellow_line) + " "+ (childclient.getDetails().get("garis_kuning") != null ? yesNo(childclient.getDetails().get("garis_kuning")) : "-"));
            breast_feeding.setText(getString(R.string.asi) + " " + (childclient.getDetails().get("asi") != null ? yesNo(childclient.getDetails().get("asi")) : "-"));
            nutrition_status.setText(getString(R.string.nutrition_status) + " "+ (childclient.getDetails().get("nutrition_status") != null ? weightStatus(childclient.getDetails().get("nutrition_status")) : "-"));


        GraphView graph = (GraphView) findViewById(R.id.graph);
        //set data for graph
        DataPoint dataPoint[] = new DataPoint[history_berat.length];
        for(int i=0;i<history_berat.length;i++){
            dataPoint[i]= new DataPoint(Double.parseDouble(history_umur[i]),Double.parseDouble(history_berat[i]));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoint);
        //add series data into chart
        graph.addSeries(series);

        //series.setTitle("Random Curve 1");
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(6);
        series.setThickness(10);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        series.setCustomPaint(paint);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX )+ " Month";
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + " Kg";
                }
            }

        });

        hash = Tools.retrieveHash(context.applicationContext());

        childview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bindobject = "anak";
                entityid = childclient.entityId();
//                dispatchTakePictureIntent(childview);

                if (hash.containsValue(entityid)) {
                    Log.e(TAG, "onClick: " + entityid + " updated");
                    mode = "updated";
                    updateMode = true;

                }
//                dispatchTakePictureIntent(kiview, updateMode);

                Intent intent = new Intent(ChildDetailActivity.this, SmartShutterActivity.class);
                intent.putExtra("IdentifyPerson", false);
                intent.putExtra("org.sid.sidface.ImageConfirmation.id", entityid);
                intent.putExtra("org.sid.sidface.ImageConfirmation.origin", TAG); // send Class Name
                startActivity(intent);


            }
        });

    }

    private  String gender(String value){
        if (value.toLowerCase().contains("em"))
            return getString(R.string.child_female);
        else
            return getString(R.string.child_male);
    }

    private String yesNo(String value){
        if(value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("ya"))
            return getString(R.string.yes);
        else
            return getString(R.string.no);
    }

    private String weightStatus(String value){
        if(value.toLowerCase().contains("gain") || value.toLowerCase().contains("idak"))
            return getString(R.string.weight_not_increase);
        else if(value.toLowerCase().contains("ncrea"))
            return getString(R.string.weight_increase);
        else if(value.toLowerCase().contains("atten"))
            return getString(R.string.weight_not_attend);
        else
            return getString(R.string.weight_new);
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
            HashMap <String,String> details = new HashMap<String,String>();
            details.put("profilepic",currentfile.getAbsolutePath());
            saveimagereference(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
        }
    }

    private boolean inTheSameRegion(String date){
        if(date==null || date.length()<6)
            return false;
        int currentDate = Integer.parseInt(new SimpleDateFormat("MM").format(new java.util.Date()));
        int visitDate = Integer.parseInt(date.substring(5, 7));

        int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new java.util.Date()));
        int visitYear = Integer.parseInt(date.substring(0, 4));

        boolean date1 = currentDate < 2 || currentDate >=8;
        boolean date2 = visitDate < 2 || visitDate >=8;

        int indicator = currentDate == 1 ? 2:1;

        return (!((!date1 && date2) || (date1 && !date2)) && ((currentYear-visitYear)<indicator));
    }

    private boolean inTheSameRegionAnth(String date){
        if(date==null || date.length()<6)
            return false;
        int visitDate = Integer.parseInt(date.substring(5, 7));

        int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new java.util.Date()));
        int visitYear = Integer.parseInt(date.substring(0, 4));

        return (((currentYear-visitYear)*12) + (8-visitDate)) <=12;
    }

    public void saveimagereference(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(
                UUID.randomUUID().toString(),
                anmId,entityid,"Image",
                details.get("profilepic"),
                ImageRepository.TYPE_Unsynced,"dp", null, null
        );
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
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
        startActivity(new Intent(this, GiziSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }
}
