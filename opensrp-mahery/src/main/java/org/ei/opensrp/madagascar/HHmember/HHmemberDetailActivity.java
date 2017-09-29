package org.ei.opensrp.madagascar.HHmember;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.madagascar.R;
import org.ei.opensrp.repository.ImageRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
public class HHmemberDetailActivity extends Activity {
    public static org.ei.opensrp.madagascar.util.Tools tools;
    private static ProfileImage profileImage = new ProfileImage();
    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    //  private static KmsCalc  kmsCalc;
    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;
    private static String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
    private static ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
    //image retrieving
    public static CommonPersonObjectClient memberclient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.member_detail_activity);

        final ImageView kiview = (ImageView)findViewById(R.id.motherdetailprofileview);
        //header
       // TextView today = (TextView) findViewById(R.id.detail_today);

        //profile
        TextView nama = (TextView) findViewById(R.id.txt_wife_name);
        TextView nik = (TextView) findViewById(R.id.txt_nik);
        TextView husband_name = (TextView) findViewById(R.id.txt_husband_name);
        TextView txt_dob = (TextView) findViewById(R.id.txt_dob);
        /**
         * Open cencus
         * */
    //    TextView Ethnic_Group = (TextView) findViewById(R.id.Ethnic_Group);
     //   TextView Other_Ethnic_Group = (TextView) findViewById(R.id.Other_Ethnic_Group);
        TextView Sex = (TextView) findViewById(R.id.Sex);
        TextView Education = (TextView) findViewById(R.id.Education);
        TextView Profession = (TextView) findViewById(R.id.Profession);
       // TextView Other_Profession = (TextView) findViewById(R.id.Other_Profession);
        TextView Marital_Status = (TextView) findViewById(R.id.Marital_Status);
        TextView Prior_HealthCare = (TextView) findViewById(R.id.Prior_HealthCare);
        TextView Prior_Diagnosis = (TextView) findViewById(R.id.Prior_Diagnosis);
        TextView Visual_Health_Deformities = (TextView) findViewById(R.id.Visual_Health_Deformities);
        TextView Prior_Surgeries = (TextView) findViewById(R.id.Prior_Surgeries);
        TextView Vaccine_Card = (TextView) findViewById(R.id.Vaccine_Card);
        TextView Vaccination_History = (TextView) findViewById(R.id.Vaccination_History);
        TextView Pregnant = (TextView) findViewById(R.id.Pregnant);
        TextView Prior_Pregnancies = (TextView) findViewById(R.id.Prior_Pregnancies);
        TextView Number_Deliveries = (TextView) findViewById(R.id.Number_Deliveries);
        TextView Number_Live_Births = (TextView) findViewById(R.id.Number_Live_Births);
        TextView Prior_Birthweight = (TextView) findViewById(R.id.Prior_Birthweight);
        TextView Birthweight_gram = (TextView) findViewById(R.id.Birthweight_gram);
        TextView Is_Person_Alive = (TextView) findViewById(R.id.Is_Person_Alive);
        TextView Cause_of_Death = (TextView) findViewById(R.id.Cause_of_Death);
        TextView death_date = (TextView) findViewById(R.id.death_date);


        /***
         *
         * Child Health
         * */

        TextView childWeight = (TextView) findViewById(R.id.childWeight);
        TextView childHeight = (TextView) findViewById(R.id.childHeight);
        TextView anthropmetryUpperArm = (TextView) findViewById(R.id.anthropmetryUpperArm);
        TextView anthropmetryHead = (TextView) findViewById(R.id.anthropmetryHead);

        childWeight.setText(humanize(memberclient.getDetails().get("childWeight")));
        childHeight.setText(humanize(memberclient.getDetails().get("childHeight")));
        anthropmetryUpperArm.setText(humanize(memberclient.getDetails().get("anthropmetryUpperArm")));
        anthropmetryHead.setText(humanize(memberclient.getDetails().get("anthropmetryHead")));

        /*===========================================================*/
        TextView Census = (TextView) findViewById(R.id.Census);
        TextView follow_up = (TextView) findViewById(R.id.follow_up);
        TextView child_health = (TextView) findViewById(R.id.child_health);

     /*   final TextView id1 = (TextView) findViewById(R.id.id1);
        TextView id2 = (TextView) findViewById(R.id.id2);*/

        Census.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id1).setVisibility(View.GONE);
                findViewById(R.id.id3).setVisibility(View.GONE);
                findViewById(R.id.id2).setVisibility(View.VISIBLE);
            }
        });

        follow_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id3).setVisibility(View.GONE);
                findViewById(R.id.id1).setVisibility(View.VISIBLE);
                findViewById(R.id.id2).setVisibility(View.GONE);
            }
        });

        child_health.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id3).setVisibility(View.VISIBLE);
                findViewById(R.id.id1).setVisibility(View.GONE);
                findViewById(R.id.id2).setVisibility(View.GONE);
            }
        });

        MemberDetail2 d = new MemberDetail2();

        refreshMemberDetails(d);

        /**
         * Open cencus
         * */
        //Ethnic_Group.setText(humanize(memberclient.getColumnmaps().get("Ethnic_Group")));
       // Ethnic_Group.setVisibility(View.GONE);
      //  Other_Ethnic_Group.setText(humanize(memberclient.getDetails().get("Other_Ethnic_Group")));
        Sex.setText(humanize(memberclient.getColumnmaps().get("Sex")));
        Education.setText(humanize(memberclient.getColumnmaps().get("Education")));
        Profession.setText(humanize(memberclient.getColumnmaps().get("Profession")));
       // Other_Profession.setText(humanize(memberclient.getDetails().get("Other_Profession")));
        Marital_Status.setText(humanize(memberclient.getDetails().get("Marital_Status")));
        Prior_HealthCare.setText(humanize(memberclient.getDetails().get("Prior_HealthCare")));
        Prior_Diagnosis.setText(humanize(memberclient.getDetails().get("Prior_Diagnosis")));
       // Other_Prior_Diagnosis.setText(humanize(memberclient.getDetails().get("Other_Prior_Diagnosis")));
        Visual_Health_Deformities.setText(humanize(memberclient.getDetails().get("Visual_Health_Deformities")));
        //Other_Visual_Health_Deformities.setText(humanize(memberclient.getDetails().get("Other_Visual_Health_Deformities")));
        Prior_Surgeries.setText(humanize(memberclient.getDetails().get("Prior_Surgeries")));
        Vaccine_Card.setText(humanize(memberclient.getDetails().get("Vaccine_Card")));
        Vaccination_History.setText(humanize(memberclient.getDetails().get("Vaccination_History")));
        Pregnant.setText(humanize(memberclient.getDetails().get("Pregnant")));
        Prior_Pregnancies.setText(humanize(memberclient.getDetails().get("Prior_Pregnancies")));
        Number_Deliveries.setText(humanize(memberclient.getDetails().get("Number_Deliveries")));
        Number_Live_Births.setText(humanize(memberclient.getDetails().get("Number_Live_Births")));
        Prior_Birthweight.setText(humanize(memberclient.getDetails().get("Prior_Birthweight")));
        Birthweight_gram.setText(humanize(memberclient.getDetails().get("Birthweight_gram")));
        Is_Person_Alive.setText(humanize(memberclient.getDetails().get("Is_Person_Alive")));
        Cause_of_Death.setText(humanize(memberclient.getDetails().get("Cause_of_Death")));
        death_date.setText(humanize(memberclient.getDetails().get("death_date")));

        ImageButton back = (ImageButton) findViewById(R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(HHmemberDetailActivity.this, HHmemberSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        nama.setText(getResources().getString(R.string.name)+ (memberclient.getColumnmaps().get("Name_family_member") != null ? memberclient.getColumnmaps().get("Name_family_member") : "-"));
        nik.setText("Ethnic : "+ (memberclient.getColumnmaps().get("Ethnic_Group") != null ? memberclient.getColumnmaps().get("Ethnic_Group") : "-"));
        husband_name.setText("Education : "+ (memberclient.getColumnmaps().get("namaSuami") != null ? memberclient.getColumnmaps().get("namaSuami") : "-"));

        String date = memberclient.getDetails().get("dob_date") != null? memberclient.getDetails().get("dob_date"):"n/a";
        String month = memberclient.getDetails().get("dob_month") != null? memberclient.getDetails().get("dob_month"):"n/a";
        String year = memberclient.getDetails().get("dob_year") != null? memberclient.getDetails().get("dob_year"):"n/a";

        String dob = date+" - "+month+" - "+year ;
        txt_dob.setText("Date of Birth : "+ dob);

        final ImageView pic1 = (ImageView)findViewById(R.id.photo1);
        final ImageView pic2 = (ImageView)findViewById(R.id.photo2);
        final ImageView pic3 = (ImageView)findViewById(R.id.photo3);
        final ImageView pic4 = (ImageView)findViewById(R.id.photo4);

        if(memberclient.getDetails().get("profilepic") !=null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);

        }
        else {
            if(memberclient.getColumnmaps().get("Sex")!= null) {
                if (memberclient.getColumnmaps().get("Sex").equalsIgnoreCase("Female")) {
                    kiview.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));

                }
            }
            kiview.setImageDrawable(getResources().getDrawable(R.mipmap.household_profile));
        }

        if (memberclient.getColumnmaps().get("Sex") != null) {
            if (memberclient.getColumnmaps().get("Sex").equalsIgnoreCase("Female")) {
                kiview.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));
                if(memberclient.getDetails().get("profilepic") !=null) {
                    setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);

                }
            }
            else if(memberclient.getColumnmaps().get("Sex").equalsIgnoreCase("Male")) {
                kiview.setImageDrawable(getResources().getDrawable(R.mipmap.household_profile));
                if(memberclient.getDetails().get("profilepic") !=null) {
                    setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);


                }
            }
        }
        else{
            kiview.setImageDrawable(getResources().getDrawable(R.mipmap.household_profile));

        }

        pic1.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));
        pic2.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));
        pic3.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));



        if(memberclient.getDetails().get("photo")!= null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("photo"), pic1, R.mipmap.warning);
        }
        else {
            pic1.setImageDrawable(getResources().getDrawable(R.mipmap.warning));
        }
        if(memberclient.getDetails().get("photo2")!= null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("photo2"), pic2, R.mipmap.warning);
        }
        else {
            pic2.setImageDrawable(getResources().getDrawable(R.mipmap.warning));
        }

        if(memberclient.getDetails().get("photo3")!= null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("photo3"), pic3, R.mipmap.warning);
        }
        else {
            pic3.setImageDrawable(getResources().getDrawable(R.mipmap.warning));
        }

        if(memberclient.getDetails().get("photo4")!= null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("photo4"), pic4, R.mipmap.warning);
        }
        else {
            pic4.setImageDrawable(getResources().getDrawable(R.mipmap.warning));
        }


        kiview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   FlurryFacade.logEvent("taking_mother_pictures_on_kohort_ibu_detail_view");
                bindobject = "HHMember";
                entityid = memberclient.entityId();
                takepictures(kiview);


            }
        });

        pic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   FlurryFacade.logEvent("taking_mother_pictures_on_kohort_ibu_detail_view");
                bindobject = "HHMember";
                entityid = memberclient.entityId();
                takepicture1(kiview);


            }
        });
        pic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   FlurryFacade.logEvent("taking_mother_pictures_on_kohort_ibu_detail_view");
                bindobject = "HHMember";
                entityid = memberclient.entityId();
                takepicture2(kiview);

            }
        });
        pic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   FlurryFacade.logEvent("taking_mother_pictures_on_kohort_ibu_detail_view");
                bindobject = "HHMember";
                entityid = memberclient.entityId();
                takepicture3(kiview);

            }
        });
        pic4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   FlurryFacade.logEvent("taking_mother_pictures_on_kohort_ibu_detail_view");
                bindobject = "HHMember";
                entityid = memberclient.entityId();
                takepicture4(kiview);

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
    static final int REQUEST_TAKE_PHOTO2 = 2;
    static final int REQUEST_TAKE_PHOTO3 = 3;
    static final int REQUEST_TAKE_PHOTO4 = 4;
    static final int REQUEST_TAKE_PHOTOS = 88;
    static ImageView mImageView;
    static File currentfile;
    static String bindobject;
    static String entityid;


    private void takepictures(ImageView imageView) {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTOS);

            }
        }
    }

    private void takepicture1(ImageView imageView) {
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

    private void takepicture2(ImageView imageView) {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO2);

            }
        }
    }

    private void takepicture3(ImageView imageView) {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO3);

            }
        }
    }

    private void takepicture4(ImageView imageView) {
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO4);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            HashMap<String,String> details = new HashMap<String,String>();
          //  details.put("photo",currentfile.getAbsolutePath());
          //  saveimagereference(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            tools.WritePictureToFile(bitmap,entityid,null,false,"image1");
           // onCreate(null);
            Intent refresh = new Intent(this, HHmemberSmartRegisterActivity.class);
            startActivity(refresh);//Start the same Activity
            finish(); //finish Activity.
        }
        else if (requestCode == REQUEST_TAKE_PHOTO2 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
           // details.put("photo2",currentfile.getAbsolutePath());
          //  saveimagereference2(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            tools.WritePictureToFile(bitmap,entityid,null,false,"image2");
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO3 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
          //  details.put("photo3",currentfile.getAbsolutePath());
         //   saveimagereference3(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            tools.WritePictureToFile(bitmap,entityid,null,false,"image3");
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO4 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
           // details.put("photo4",currentfile.getAbsolutePath());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            tools.WritePictureToFile(bitmap,entityid,null,false,"image4");
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTOS && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            tools.WritePictureToFile(bitmap,entityid,null,false,"profilepic");
           // Tools = new org.ei.opensrp.madagascar.util.Tools();
         //   saveToDb(entityid,currentfile.getAbsolutePath(),null,false,HHmemberDetailActivity.class.getSimpleName());
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }

    }
    public void reload() {
        finish();
        startActivity(new Intent(this, HHmemberDetailActivity.class));
        overridePendingTransition(0, 0);

    }



    public static void setImagetoHolder(Activity activity,String file, ImageView view, int placeholder){
        String TAG = "ImageGridFragment";
        String IMAGE_CACHE_DIR = "thumbs";

        int mImageThumbSize;
        int mImageThumbSpacing;

        mImageThumbSize = 300;
        mImageThumbSpacing = Context.getInstance().applicationContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);


        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(activity, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.80f); // Set memory cache to 25% of app memory
        ImageFetcher mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
        mImageFetcher.setLoadingImage(placeholder);
        mImageFetcher.addImageCache(activity.getFragmentManager(), cacheParams);
//        Toast.makeText(activity,file,Toast.LENGTH_LONG).show();
        mImageFetcher.loadImage("file:///"+file,view);

    }
    public static void setImagetoHolderFromUri(Activity activity,String file, ImageView view, int placeholder){
        view.setImageDrawable(activity.getResources().getDrawable(placeholder));
        File externalFile = new File(file);
        Uri external = Uri.fromFile(externalFile);
        view.setImageURI(external);


    }
    public void reload1(){
        finish();
        startActivity(new Intent(this, HHmemberDetailActivity.class));
        overridePendingTransition(0, 0);
    }
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }

    private String getDetails(String key){
        if(memberclient==null)
            return "-";
        String temp = memberclient.getDetails().get(key);
        if(temp==null)
            return "-";
        else if(temp.equals(""))
            return "-";
        return temp;
    }

    private void refreshMemberDetails(MemberDetail2 d){
        if(d==null)
            return;
        initializeMemberDetail(d);
        if(!d.initialized)
            return;
        d.Recent_Deaths.setText(getDetails("Recent_Deaths").replace("idr","I don't Remember"));
        d.Recent_Births.setText(getDetails("Recent_Births").replace("idr","I don't Remember"));
        d.Birth_location.setText(getDetails("Birth_location").replace("idr","I don't Remember"));
//        d.Other_Birth_location.setText(getDetails(""));
        d.Visited_HealthCare.setText(getDetails("Visited_HealthCare").replace("idr","I don't Remember"));
        d.Treatment_Location.setText(getDetails("Treatment_Location").replace("idr","I don't Remember"));
//        d.Treatment_Location_text.setText(getDetails(""));
        d.New_Diagnosis.setText(getDetails("New_Diagnosis").replace("idr","I don't Remember"));
//        d.New_Diagnosis_text.setText(getDetails(""));
        d.Pregnant_Last_Month.setText(getDetails("Pregnant_Last_Month").replace("idr","I don't Remember"));
        d.Pregnant.setText(getDetails("Pregnant").replace("idr","I don't Remember"));
        d.Last_Period.setText(getDetails("Last_Period").replace("idr","I don't Remember"));
        d.LMP_date.setText(getDetails("LMP_date").replace("idr","I don't Remember"));
//        d.LMP_month.setText(getDetails("LMP_month").replace("idr","I don't Remember"));
     //   d.LMP_year.setText(getDetails("LMP_year").replace("idr","I don't Remember"));
        d.Prenatal_Visits.setText(getDetails("Prenatal_Visits").replace("idr","I don't Remember"));
        d.Prenatal_Medication.setText(getDetails("Prenatal_Medication").replace("idr","I don't Remember"));
//        d.Prenatal_Medication_Type.setText(getDetails("Prenatal_Medication_Type"));
        d.Pelvic_Cramps.setText(getDetails("Pelvic_Cramps").replace("idr","I don't Remember"));
        d.Amniotic_Fluid.setText(getDetails("Amniotic_Fluid").replace("idr","I don't Remember"));
        d.Dizziness.setText(getDetails("Dizziness").replace("idr","I don't Remember"));
        d.Feet_Swelling.setText(getDetails("Feet_Swelling").replace("idr","I don't Remember"));
        d.Vaginal_discharge.setText(getDetails("Vaginal_discharge").replace("idr","I don't Remember"));
        d.Miscarriage_Stillbirth.setText(getDetails("Miscarriage_Stillbirth").replace("idr","I don't Remember"));
        d.Contraception.setText(getDetails("Contraception").replace("idr","I don't Remember"));
        d.Contraception_Type.setText(getDetails("Contraception_Type").replace("idr","I don't Remember"));
        d.Breastfeeding.setText(getDetails("Breastfeeding").replace("idr","I don't Remember"));
        d.Exlusive_breastfeeding.setText(getDetails("Exlusive_breastfeeding").replace("idr","I don't Remember"));
        d.Menopause.setText(getDetails("Menopause").replace("idr","I don't Remember"));
        d.Diarrhea.setText(getDetails("Diarrhea").replace("idr","I don't Remember"));
        d.Diarrhea_Days.setText(getDetails("Diarrhea_Days").replace("idr","I don't Remember"));
        d.Diarrhea_Black.setText(getDetails("Diarrhea_Black").replace("idr","I don't Remember"));
        d.Diarrhea_Bloody.setText(getDetails("Diarrhea_Bloody").replace("idr","I don't Remember"));
        d.Eyes_Sunken.setText(getDetails("Eyes_Sunken").replace("idr","I don't Remember"));
        d.Tears.setText(getDetails("Tears").replace("idr","I don't Remember"));
        d.Mouth_dry.setText(getDetails("Mouth_dry").replace("idr","I don't Remember"));
        d.Diarrhea_Treatment.setText(getDetails("Diarrhea_Treatment").replace("idr","I don't Remember"));
        d.Diarrhea_Treatment_Location.setText(getDetails("Diarrhea_Treatment_Location").replace("idr","I don't Remember"));
//        d.Other_Diarrhea_Treatment.setText(getDetails(""));
        d.Diarrhea_Treatment_Outcome.setText(getDetails("Diarrhea_Treatment_Outcome").replace("idr","I don't Remember"));
        d.Fever_TF.setText(getDetails("Fever_TF").replace("idr","I don't Remember"));
        d.Fever_Duration.setText(getDetails("Fever_Duration").replace("idr","I don't Remember"));
        d.Fever_Treatment.setText(getDetails("Fever_Treatment").replace("idr","I don't Remember"));
        d.Fever_Treatment_Location.setText(getDetails("Fever_Treatment_Location").replace("idr","I don't Remember"));
//        d.Other_Fever_Treatment.setText(getDetails(""));
        d.TDR.setText(getDetails("TDR").replace("idr","I don't Remember"));
        d.TDR_result.setText(getDetails("TDR_result").replace("idr","I don't Remember"));
        d.Temperature.setText(getDetails("Temperature").replace("idr","I don't Remember"));
        d.Fever_Treatment_Outcome.setText(getDetails("Fever_Treatment_Outcome").replace("idr","I don't Remember"));
        d.Vomiting.setText(getDetails("Vomiting").replace("idr","I don't Remember"));
        d.Headache.setText(getDetails("Headache").replace("idr","I don't Remember"));
        d.Animal_Contact.setText(getDetails("Animal_Contact").replace("idr","I don't Remember"));
        d.Animal_Incident.setText(getDetails("Animal_Incident").replace("idr","I don't Remember"));
        d.Rat_contact.setText(getDetails("Rat_contact").replace("idr","I don't Remember"));
        d.Jaundice.setText(getDetails("Jaundice").replace("idr","I don't Remember"));
        d.Measles.setText(getDetails("Measles").replace("idr","I don't Remember"));
        d.Weight_loss.setText(getDetails("Weight_loss").replace("idr","I don't Remember"));
        d.Persistent_Cough.setText(getDetails("Persistent_Cough").replace("idr","I don't Remember"));
        d.Coughing_Blood.setText(getDetails("Coughing_Blood").replace("idr","I don't Remember"));
        d.Sputum.setText(getDetails("Sputum").replace("idr","I don't Remember"));
        d.Breathing.setText(getDetails("Breathing").replace("idr","I don't Remember"));
        d.Stomach_Pain.setText(getDetails("Stomach_Pain").replace("idr","I don't Remember"));
        d.Stomach_Pain_Duration.setText(getDetails("Stomach_Pain_Duration").replace("idr","I don't Remember"));
        d.Stomach_Pain_food.setText(getDetails("Stomach_Pain_food").replace("idr","I don't Remember"));
        d.Open_Wounds.setText(getDetails("Open_Wounds").replace("idr","I don't Remember"));
        d.Wound_Condition.setText(getDetails("Wound_Condition").replace("idr","I don't Remember"));
        d.Cigarettes.setText(getDetails("Cigarettes").replace("idr","I don't Remember"));
        d.Drinking.setText(getDetails("Drinking").replace("idr","I don't Remember"));
        d.Urine_Color.setText(getDetails("Urine_Color").replace("idr","I don't Remember"));
        d.Pus.setText(getDetails("Pus").replace("idr","I don't Remember"));
        d.Pain_Urination.setText(getDetails("Pain_Urination").replace("idr","I don't Remember"));
        d.Urination_Blood.setText(getDetails("Urination_Blood").replace("idr","I don't Remember"));
        d.Back_Pain.setText(getDetails("Back_Pain").replace("idr","I don't Remember"));
        d.Medications.setText(getDetails("Medications").replace("idr","I don't Remember"));
        d.Medications_Name.setText(getDetails("Medications_Name").replace("idr","I don't Remember"));
//        d.Medications_Name_open.setText(getDetails(""));
        d.Medications_Purpose.setText(getDetails("Medications_Purpose").replace("idr","I don't Remember"));
        d.STI.setText(getDetails("STI").replace("idr","I don't Remember"));
        d.STI_Treatment.setText(getDetails("STI_Treatment").replace("idr","I don't Remember"));
        d.STI_Treatment_Outcome.setText(getDetails("STI_Treatment_Outcome").replace("idr","I don't Remember"));
        d.New_vaccine.setText(getDetails("New_vaccine").replace("idr","I don't Remember"));
        d.New_vaccine_types.setText(getDetails("New_vaccine_types").replace("idr","I don't Remember"));
//        d.submissionDate.setText(getDetails(""));
    }

    private void initializeMemberDetail(MemberDetail2 d){
        if(d==null)
            return;
        d.initialized=true;
        d.Recent_Deaths = (TextView) findViewById(R.id.Recent_Deaths);
        d.Recent_Births  = (TextView) findViewById(R.id.Recent_Births);
        d.Birth_location  = (TextView) findViewById(R.id.Birth_location);
//        d.Other_Birth_location  = (TextView) findViewById(R.id.);
        d.Visited_HealthCare  = (TextView) findViewById(R.id.Visited_HealthCare);
        d.Treatment_Location  = (TextView) findViewById(R.id.Treatment_Location);
//        d.Treatment_Location_text  = (TextView) findViewById(R.id.);
        d.New_Diagnosis  = (TextView) findViewById(R.id.New_Diagnosis);
//        d.New_Diagnosis_text  = (TextView) findViewById(R.id.);
        d.Pregnant_Last_Month  = (TextView) findViewById(R.id.Pregnant_Last_Month);
        d.Pregnant  = (TextView) findViewById(R.id.Pregnant);
        d.Last_Period  = (TextView) findViewById(R.id.Last_Period);
        d.LMP_date  = (TextView) findViewById(R.id.LMP_date);
      //  d.LMP_month  = (TextView) findViewById(R.id.LMP_month);
     //   d.LMP_year  = (TextView) findViewById(R.id.LMP_year);
        d.Prenatal_Visits  = (TextView) findViewById(R.id.Prenatal_Visits);
        d.Prenatal_Medication  = (TextView) findViewById(R.id.Prenatal_Medication);
        d.Prenatal_Medication_Type  = (TextView) findViewById(R.id.Prenatal_Medication_Type);
        d.Pelvic_Cramps  = (TextView) findViewById(R.id.Pelvic_Cramps);
        d.Amniotic_Fluid  = (TextView) findViewById(R.id.Amniotic_Fluid);
        d.Dizziness  = (TextView) findViewById(R.id.Dizziness);
        d.Feet_Swelling  = (TextView) findViewById(R.id.Feet_Swelling);
        d.Vaginal_discharge  = (TextView) findViewById(R.id.Vaginal_discharge);
        d.Miscarriage_Stillbirth  = (TextView) findViewById(R.id.Miscarriage_Stillbirth);
        d.Contraception  = (TextView) findViewById(R.id.Contraception);
        d.Contraception_Type  = (TextView) findViewById(R.id.Contraception_Type);
        d.Breastfeeding  = (TextView) findViewById(R.id.Breastfeeding);
        d.Exlusive_breastfeeding  = (TextView) findViewById(R.id.Exlusive_breastfeeding);
        d.Menopause  = (TextView) findViewById(R.id.Menopause);
        d.Diarrhea  = (TextView) findViewById(R.id.Diarrhea);
        d.Diarrhea_Days  = (TextView) findViewById(R.id.Diarrhea_Days);
        d.Diarrhea_Black  = (TextView) findViewById(R.id.Diarrhea_Black);
        d.Diarrhea_Bloody  = (TextView) findViewById(R.id.Diarrhea_Bloody);
        d.Eyes_Sunken  = (TextView) findViewById(R.id.Eyes_Sunken);
        d.Tears  = (TextView) findViewById(R.id.Tears);
        d.Mouth_dry  = (TextView) findViewById(R.id.Mouth_dry);
        d.Diarrhea_Treatment  = (TextView) findViewById(R.id.Diarrhea_Treatment);
        d.Diarrhea_Treatment_Location  = (TextView) findViewById(R.id.Diarrhea_Treatment_Location);
//        d.Other_Diarrhea_Treatment  = (TextView) findViewById(R.id.);
        d.Diarrhea_Treatment_Outcome  = (TextView) findViewById(R.id.Diarrhea_Treatment_Outcome);
        d.Fever_TF  = (TextView) findViewById(R.id.Fever_TF);
        d.Fever_Duration  = (TextView) findViewById(R.id.Fever_Duration);
        d.Fever_Treatment  = (TextView) findViewById(R.id.Fever_Treatment);
        d.Fever_Treatment_Location  = (TextView) findViewById(R.id.Fever_Treatment_Location);
//        d.Other_Fever_Treatment  = (TextView) findViewById(R.id.);
        d.TDR  = (TextView) findViewById(R.id.TDR);
        d.TDR_result  = (TextView) findViewById(R.id.TDR_result);
        d.Temperature  = (TextView) findViewById(R.id.Temperature);
        d.Fever_Treatment_Outcome  = (TextView) findViewById(R.id.Fever_Treatment_Outcome);
        d.Vomiting  = (TextView) findViewById(R.id.Vomiting);
        d.Headache  = (TextView) findViewById(R.id.Headache);
        d.Animal_Contact  = (TextView) findViewById(R.id.Animal_Contact);
        d.Animal_Incident  = (TextView) findViewById(R.id.Animal_Incident);
        d.Rat_contact  = (TextView) findViewById(R.id.Rat_contact);
        d.Jaundice  = (TextView) findViewById(R.id.Jaundice);
        d.Measles  = (TextView) findViewById(R.id.Measles);
        d.Weight_loss  = (TextView) findViewById(R.id.Weight_loss);
        d.Persistent_Cough  = (TextView) findViewById(R.id.Persistent_Cough);
        d.Coughing_Blood  = (TextView) findViewById(R.id.Coughing_Blood);
        d.Sputum  = (TextView) findViewById(R.id.Sputum);
        d.Breathing  = (TextView) findViewById(R.id.Breathing);
        d.Stomach_Pain  = (TextView) findViewById(R.id.Stomach_Pain);
        d.Stomach_Pain_Duration  = (TextView) findViewById(R.id.Stomach_Pain_Duration);
        d.Stomach_Pain_food  = (TextView) findViewById(R.id.Stomach_Pain_food);
        d.Open_Wounds  = (TextView) findViewById(R.id.Open_Wounds);
        d.Wound_Condition  = (TextView) findViewById(R.id.Wound_Condition);
        d.Cigarettes  = (TextView) findViewById(R.id.Cigarettes);
        d.Drinking  = (TextView) findViewById(R.id.Drinking);
        d.Urine_Color  = (TextView) findViewById(R.id.Urine_Color);
        d.Pus  = (TextView) findViewById(R.id.Pus);
        d.Pain_Urination  = (TextView) findViewById(R.id.Pain_Urination);
        d.Urination_Blood  = (TextView) findViewById(R.id.Urination_Blood);
        d.Back_Pain  = (TextView) findViewById(R.id.Back_Pain);
        d.Medications  = (TextView) findViewById(R.id.Medications);
        d.Medications_Name  = (TextView) findViewById(R.id.Medications_Name);
//        d.Medications_Name_open  = (TextView) findViewById(R.id.);
        d.Medications_Purpose  = (TextView) findViewById(R.id.Medications_Purpose);
        d.STI  = (TextView) findViewById(R.id.STI);
        d.STI_Treatment  = (TextView) findViewById(R.id.STI_Treatment);
        d.STI_Treatment_Outcome  = (TextView) findViewById(R.id.STI_Treatment_Outcome);
        d.New_vaccine  = (TextView) findViewById(R.id.New_vaccine);
        d.New_vaccine_types  = (TextView) findViewById(R.id.New_vaccine_types);
//        d.submissionDate  = (TextView) findViewById(R.id.);

    }

    private class MemberDetail2 {
        boolean initialized = false;
        TextView Recent_Deaths;
        TextView Recent_Births;
        TextView Birth_location;
        TextView Other_Birth_location;
        TextView Visited_HealthCare;
        TextView Treatment_Location;
        TextView Treatment_Location_text;
        TextView New_Diagnosis;
        TextView New_Diagnosis_text;
        TextView Pregnant_Last_Month;
        TextView Pregnant;
        TextView Last_Period;
        TextView LMP_date;
        TextView LMP_month;
        TextView LMP_year;
        TextView Prenatal_Visits;
        TextView Prenatal_Medication;
        TextView Prenatal_Medication_Type;
        TextView Pelvic_Cramps;
        TextView Amniotic_Fluid;
        TextView Dizziness;
        TextView Feet_Swelling;
        TextView Vaginal_discharge;
        TextView Miscarriage_Stillbirth;
        TextView Contraception;
        TextView Contraception_Type;
        TextView Breastfeeding;
        TextView Exlusive_breastfeeding;
        TextView Menopause;
        TextView Diarrhea;
        TextView Diarrhea_Days;
        TextView Diarrhea_Black;
        TextView Diarrhea_Bloody;
        TextView Eyes_Sunken;
        TextView Tears;
        TextView Mouth_dry;
        TextView Diarrhea_Treatment;
        TextView Diarrhea_Treatment_Location;
        TextView Other_Diarrhea_Treatment;
        TextView Diarrhea_Treatment_Outcome;
        TextView Fever_TF;
        TextView Fever_Duration;
        TextView Fever_Treatment;
        TextView Fever_Treatment_Location;
        TextView Other_Fever_Treatment;
        TextView TDR;
        TextView TDR_result;
        TextView Temperature;
        TextView Fever_Treatment_Outcome;
        TextView Vomiting;
        TextView Headache;
        TextView Animal_Contact;
        TextView Animal_Incident;
        TextView Rat_contact;
        TextView Jaundice;
        TextView Measles;
        TextView Weight_loss;
        TextView Persistent_Cough;
        TextView Coughing_Blood;
        TextView Sputum;
        TextView Breathing;
        TextView Stomach_Pain;
        TextView Stomach_Pain_Duration;
        TextView Stomach_Pain_food;
        TextView Open_Wounds;
        TextView Wound_Condition;
        TextView Cigarettes;
        TextView Drinking;
        TextView Urine_Color;
        TextView Pus;
        TextView Pain_Urination;
        TextView Urination_Blood;
        TextView Back_Pain;
        TextView Medications;
        TextView Medications_Name;
        TextView Medications_Name_open;
        TextView Medications_Purpose;
        TextView STI;
        TextView STI_Treatment;
        TextView STI_Treatment_Outcome;
        TextView New_vaccine;
        TextView New_vaccine_types;
        TextView submissionDate;
    }



    /*private static void saveToDb(String entityId, String absoluteFileName, String faceVector, boolean updated, String className) {

        Log.e(TAG, "saveToDb: " + "start");
        // insert into the db local
        if (!updated) {
            // insert into the db local
            ProfileImage profileImage = new ProfileImage();

            profileImage.setImageid(entityId);
            profileImage.setAnmId(anmId);
            profileImage.setEntityID(entityId);
            profileImage.setContenttype("jpeg");
            profileImage.setFilepath(absoluteFileName);
            profileImage.setFilecategory("profilepic");
          //  profileImage.setFilevector(faceVector);
            profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);

            ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
            imageRepo.add(profileImage,entityId);

            // insert into details
            Map<String, String> details = new HashMap<>();
            details.put("profilepic", absoluteFileName);
           *//* if (className.equals(HHmemberDetailActivity.class.getSimpleName())) {
                bindobject = "HHmember";
            } else {
                bindobject = "HH";
            }*//*
            bindobject = "HHMember";
            Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityId, details);


        } else {
            // do nothings
           // imageRepo.updateByEntityId(entityId, faceVector);
        }
        Log.e(TAG, "saveToDb: " + "done");

    }*/

}
