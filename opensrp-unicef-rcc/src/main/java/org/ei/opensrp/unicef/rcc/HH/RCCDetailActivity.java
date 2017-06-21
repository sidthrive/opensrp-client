package org.ei.opensrp.unicef.rcc.HH;

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
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.unicef.rcc.R;
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

/**
 * Created by Iq on 07/09/16.
 */
public class RCCDetailActivity extends Activity {

    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    //  private static KmsCalc  kmsCalc;
    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;

    //image retrieving

    public static CommonPersonObjectClient kiclient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.ki_detail_activity);

        final ImageView kiview = (ImageView)findViewById(R.id.motherdetailprofileview);
        //header
      //  TextView risk = (TextView) findViewById(R.id.detail_risk);

        //profile
        TextView nama = (TextView) findViewById(R.id.txt_wife_name);
        TextView nik = (TextView) findViewById(R.id.txt_nik);
        TextView husband_name = (TextView) findViewById(R.id.txt_husband_name);
        TextView dob = (TextView) findViewById(R.id.txt_dob);
        TextView phone = (TextView) findViewById(R.id.txt_contact_phone_number);
        TextView risk1 = (TextView) findViewById(R.id.txt_risk1);
        TextView risk2 = (TextView) findViewById(R.id.txt_risk2);
        TextView risk3 = (TextView) findViewById(R.id.txt_risk3);
        TextView risk4 = (TextView) findViewById(R.id.txt_risk4);

        final TextView show_risk = (TextView) findViewById(R.id.health);
        final TextView show_detail = (TextView) findViewById(R.id.hh_char);


        //detail data
        TextView a = (TextView) findViewById(R.id.a);
        TextView b = (TextView) findViewById(R.id.b);
        TextView c = (TextView) findViewById(R.id.c);
        TextView d = (TextView) findViewById(R.id.d);
        TextView e = (TextView) findViewById(R.id.e);
        TextView f = (TextView) findViewById(R.id.f);
        TextView g = (TextView) findViewById(R.id.g);
        TextView h = (TextView) findViewById(R.id.h);
        TextView i = (TextView) findViewById(R.id.i);
        TextView j = (TextView) findViewById(R.id.j);

        TextView k = (TextView) findViewById(R.id.k);
        TextView l = (TextView) findViewById(R.id.l);
        TextView m = (TextView) findViewById(R.id.m);
        TextView n = (TextView) findViewById(R.id.n);
        TextView o = (TextView) findViewById(R.id.o);
        TextView p = (TextView) findViewById(R.id.p);
        TextView q = (TextView) findViewById(R.id.q);
        TextView s = (TextView) findViewById(R.id.s);
        TextView r = (TextView) findViewById(R.id.r);
        TextView t = (TextView) findViewById(R.id.t);


        TextView aa = (TextView) findViewById(R.id.aa);
        TextView bb = (TextView) findViewById(R.id.bb);
        TextView cc = (TextView) findViewById(R.id.cc);
        TextView dd = (TextView) findViewById(R.id.dd);
        TextView ee = (TextView) findViewById(R.id.ee);
        TextView ff = (TextView) findViewById(R.id.ff);
        TextView gg = (TextView) findViewById(R.id.gg);
        TextView hh = (TextView) findViewById(R.id.hh);

        TextView kk = (TextView) findViewById(R.id.kk);
        TextView ll = (TextView) findViewById(R.id.ll);
        TextView mm = (TextView) findViewById(R.id.mm);
        TextView nn = (TextView) findViewById(R.id.nn);
        TextView oo = (TextView) findViewById(R.id.oo);
        TextView pp = (TextView) findViewById(R.id.pp);
        TextView qq = (TextView) findViewById(R.id.qq);
        TextView tt = (TextView) findViewById(R.id.tt);


      //  final TextView show_risk = (TextView) findViewById(R.id.show_more);
      //  final TextView show_detail = (TextView) findViewById(R.id.show_more_detail);


        ImageButton back = (ImageButton) findViewById(org.ei.opensrp.R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(RCCDetailActivity.this, HHSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });


        if(kiclient.getDetails().get("profilepic")!= null){
                setImagetoHolderFromUri(RCCDetailActivity.this, kiclient.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);
        }
        else {
                kiview.setImageDrawable(getResources().getDrawable(R.mipmap.household_profile));
        }

        nama.setText(getResources().getString(R.string.name)+ (kiclient.getColumnmaps().get("respondent_name") != null ? kiclient.getColumnmaps().get("respondent_name") : "-"));
        nik.setText("Relationship : "+ (kiclient.getDetails().get("relation_to_child") != null ? kiclient.getDetails().get("relation_to_child") : "-"));
        husband_name.setText("Age : "+ (kiclient.getDetails().get("respondent_age") != null ? kiclient.getDetails().get("respondent_age") : "-"));
        dob.setText("Education : "+ (kiclient.getDetails().get("respondent_education") != null ? kiclient.getDetails().get("respondent_education") : "-"));

        a.setText( (kiclient.getDetails().get("household_size") != null ? kiclient.getDetails().get("household_size") : "-"));
        b.setText( (kiclient.getDetails().get("adult_hh_member") != null ? kiclient.getDetails().get("adult_hh_member") : "-"));
        d.setText( (kiclient.getDetails().get("child_hh_member_6-14") != null ? kiclient.getDetails().get("child_hh_member_6-14") : "-"));
        e.setText( (kiclient.getDetails().get("elderly_hh_member") != null ? kiclient.getDetails().get("elderly_hh_member") : "-"));
        f.setText( (kiclient.getDetails().get("mobile_connectivity") != null ? kiclient.getDetails().get("mobile_connectivity").replace("_"," ") : "-"));
        g.setText( (kiclient.getDetails().get("phone_purpose") != null ? kiclient.getDetails().get("phone_purpose").replace("_"," ") : "-"));
        h.setText( (kiclient.getDetails().get("cell_provider") != null ? kiclient.getDetails().get("cell_provider") : "-"));
        i.setText( (kiclient.getDetails().get("expenditure_mobile_phone") != null ? kiclient.getDetails().get("expenditure_mobile_phone").replace("<_","Below than ") : "-"));
        j.setText( (kiclient.getDetails().get("expenditure_electricity") != null ? kiclient.getDetails().get("expenditure_electricity").replace("<_","Below than ") : "-"));
        k.setText( (kiclient.getDetails().get("expenditure_food") != null ? kiclient.getDetails().get("expenditure_food").replace("<_","Below than ") : "-"));
        l.setText( (kiclient.getDetails().get("expenditure_consumer_goods") != null ? kiclient.getDetails().get("expenditure_consumer_goods").replace("<_","Below than ") : "-"));
        m.setText( (kiclient.getDetails().get("have_radio") != null ? kiclient.getDetails().get("have_radio") : "-"));
        n.setText( (kiclient.getDetails().get("have_tv") != null ? kiclient.getDetails().get("have_tv") : "-"));
        o.setText( (kiclient.getDetails().get("have_refrigerator") != null ? kiclient.getDetails().get("have_refrigerator") : "-"));
        p.setText( (kiclient.getDetails().get("have_toilet") != null ? kiclient.getDetails().get("have_toilet") : "-"));
        q.setText( (kiclient.getDetails().get("have_separate_kitchen") != null ? kiclient.getDetails().get("have_separate_kitchen") : "-"));
        r.setText( (kiclient.getDetails().get("number_of_rooms") != null ? kiclient.getDetails().get("number_of_rooms") : "-"));
        s.setText( (kiclient.getDetails().get("number_of_motorcycles") != null ? kiclient.getDetails().get("number_of_motorcycles") : "-"));
        c.setText( (kiclient.getDetails().get("child_hh_member_under_5") != null ? kiclient.getDetails().get("child_hh_member_under_5") : "-"));
     //   dob.setText( (kiclient.getDetails().get("respondent_education") != null ? kiclient.getDetails().get("respondent_education") : "-"));


        aa.setText( (kiclient.getDetails().get("anc_visit_num") != null ? kiclient.getDetails().get("anc_visit_num").replace("_"," ") : "-"));
        bb.setText( (kiclient.getDetails().get("place_of_birth") != null ? kiclient.getDetails().get("place_of_birth").replace("_"," ") : "-"));
        cc.setText( (kiclient.getDetails().get("delivery_companion") != null ? kiclient.getDetails().get("delivery_companion").replace("_"," ") : "-"));
        dd.setText( (kiclient.getDetails().get("use_of_contraceptives") != null ? kiclient.getDetails().get("use_of_contraceptives").replace("_"," ") : "-"));
        ee.setText( (kiclient.getDetails().get("attendance_at_posyandu") != null ? kiclient.getDetails().get("attendance_at_posyandu").replace("_"," ") : "-"));
        ff.setText( (kiclient.getDetails().get("reason1") != null ? kiclient.getDetails().get("reason1").replace("_"," ") : "-"));
        gg.setText( (kiclient.getDetails().get("last_time_to_posyandu") != null ? kiclient.getDetails().get("last_time_to_posyandu").replace("_"," ") : "-"));
        hh.setText( (kiclient.getDetails().get("posyandu_service") != null ? kiclient.getDetails().get("posyandu_service").replace("_"," ") : "-"));

      //  gg.setText( (kiclient.getDetails().get("attendance_at_puskesmas") != null ? kiclient.getDetails().get("phone_purpose").replace("_"," ") : "-"));
      //  hh.setText( (kiclient.getDetails().get("reason2") != null ? kiclient.getDetails().get("cell_provider") : "-"));

        kk.setText( (kiclient.getDetails().get("attendance_at_puskesmas") != null ? kiclient.getDetails().get("attendance_at_puskesmas").replace("_"," ") : "-"));
        ll.setText( (kiclient.getDetails().get("reason2") != null ? kiclient.getDetails().get("reason2").replace("_"," ") : "-"));
        mm.setText( (kiclient.getDetails().get("last_time_to_puskesmas") != null ? kiclient.getDetails().get("last_time_to_puskesmas").replace("_"," ") : "-"));
        nn.setText( (kiclient.getDetails().get("puskesmas_service") != null ? kiclient.getDetails().get("puskesmas_service").replace("_"," ") : "-"));
        oo.setText( (kiclient.getDetails().get("nearest_puskesmas") != null ? kiclient.getDetails().get("nearest_puskesmas").replace("_"," ") : "-"));
        pp.setText( (kiclient.getDetails().get("has_been_sick") != null ? kiclient.getDetails().get("has_been_sick").replace("_"," ") : "-"));
        qq.setText( (kiclient.getDetails().get("action_taken") != null ? kiclient.getDetails().get("action_taken").replace("_"," ") : "-"));
      //  tt.setText( (kiclient.getDetails().get("cell_provider") != null ? kiclient.getDetails().get("cell_provider") : "-"));



        show_risk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  FlurryFacade.logEvent("click_risk_detail");
                findViewById(R.id.id1).setVisibility(View.GONE);
                findViewById(R.id.id2).setVisibility(View.VISIBLE);
               // findViewById(R.id.hh_char).setVisibility(View.VISIBLE);
               // findViewById(R.id.health).setVisibility(View.GONE);
            }
        });

        show_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id1).setVisibility(View.VISIBLE);
                findViewById(R.id.id2).setVisibility(View.GONE);
               // findViewById(R.id.health).setVisibility(View.VISIBLE);
               // findViewById(R.id.hh_char).setVisibility(View.GONE);
            }
        });

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
//                kiclient.entityId();
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
        startActivity(new Intent(this, HHSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }
}
