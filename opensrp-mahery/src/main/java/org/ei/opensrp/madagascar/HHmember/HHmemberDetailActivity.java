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
        TextView Sexs = (TextView) findViewById(R.id.Sex);
        TextView Educations = (TextView) findViewById(R.id.Educations);
        TextView Professions = (TextView) findViewById(R.id.Profession);
       // TextView Other_Profession = (TextView) findViewById(R.id.Other_Profession);
        TextView Marital_Statuss = (TextView) findViewById(R.id.Marital_Status);
        TextView Prior_HealthCares = (TextView) findViewById(R.id.Prior_HealthCare);
        TextView Prior_Diagnosiss = (TextView) findViewById(R.id.Prior_Diagnosis);
        TextView Visual_Health_Deformitiess = (TextView) findViewById(R.id.Visual_Health_Deformities);
        TextView Prior_Surgeriess = (TextView) findViewById(R.id.Prior_Surgeries);
        TextView Vaccine_Cards = (TextView) findViewById(R.id.Vaccine_Card);
        TextView Vaccination_Historys = (TextView) findViewById(R.id.Vaccination_History);
        TextView Pregnants = (TextView) findViewById(R.id.Pregnant);
        TextView Prior_Pregnanciess = (TextView) findViewById(R.id.Prior_Pregnancies);
        TextView Number_Deliveriess = (TextView) findViewById(R.id.Number_Deliveries);
        TextView Number_Live_Birthss = (TextView) findViewById(R.id.Number_Live_Births);
        TextView Prior_Birthweights = (TextView) findViewById(R.id.Prior_Birthweight);
        TextView Birthweight_grams = (TextView) findViewById(R.id.Birthweight_gram);
        TextView Is_Person_Alives = (TextView) findViewById(R.id.Is_Person_Alive);
        TextView Cause_of_Deaths = (TextView) findViewById(R.id.Cause_of_Death);
        TextView death_dates = (TextView) findViewById(R.id.death_date);


        /***
         *
         * Child Health
         * */

        TextView childWeights = (TextView) findViewById(R.id.childWeight);
        TextView childHeights = (TextView) findViewById(R.id.childHeight);
        TextView anthropmetryUpperArms = (TextView) findViewById(R.id.anthropmetryUpperArm);
        TextView anthropmetryHeads = (TextView) findViewById(R.id.anthropmetryHead);

        childWeights.setText(humanize(memberclient.getDetails().get("childWeight")));
        childHeights.setText(humanize(memberclient.getDetails().get("childHeight")));
        anthropmetryUpperArms.setText(humanize(memberclient.getDetails().get("anthropmetryUpperArm")));
        anthropmetryHeads.setText(humanize(memberclient.getDetails().get("anthropmetryHead")));

        /*===========================================================*/
        TextView Censuss = (TextView) findViewById(R.id.Census);
        TextView follow_ups = (TextView) findViewById(R.id.follow_up);
        TextView child_healths = (TextView) findViewById(R.id.child_health);

     /*   final TextView id1 = (TextView) findViewById(R.id.id1);
        TextView id2 = (TextView) findViewById(R.id.id2);*/

        Censuss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id1).setVisibility(View.GONE);
                findViewById(R.id.id3).setVisibility(View.GONE);
                findViewById(R.id.id2).setVisibility(View.VISIBLE);
            }
        });

        follow_ups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.id3).setVisibility(View.GONE);
                findViewById(R.id.id1).setVisibility(View.VISIBLE);
                findViewById(R.id.id2).setVisibility(View.GONE);
            }
        });

        child_healths.setOnClickListener(new View.OnClickListener() {
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

        Sexs.setText(humanize(memberclient.getColumnmaps().get("Sex").equalsIgnoreCase("Male")?getResources().getString(R.string.Male):
                memberclient.getColumnmaps().get("Sex").equalsIgnoreCase("Female")?getResources().getString(R.string.Female):""));
        
        String educate = value_compare(memberclient.getColumnmaps().get("Education"),"None","EPP","CEG","Lycee","University","-","-","-",
                getResources().getString(R.string.None),getResources().getString(R.string.EPP),
                getResources().getString(R.string.CEG),getResources().getString(R.string.Lycee),getResources().getString(R.string.University),null,null,null);
        String profession = value_compare(memberclient.getColumnmaps().get("Profession"),"Farmer","Shop_owner","Fisher","Teacher","Government","Profession_Other","student","Profession_NA",
                getResources().getString(R.string.Farmer),getResources().getString(R.string.Shop_owner),
                getResources().getString(R.string.Fisher),getResources().getString(R.string.Teacher),getResources().getString(R.string.Government)
                ,getResources().getString(R.string.Profession_Other),getResources().getString(R.string.student),getResources().getString(R.string.Profession_NA));
        String maried = value_compare(memberclient.getDetails().get("Marital_Status"),"Single","Married","Fisher","Divorced","Widowed","-","-","-",
                getResources().getString(R.string.Single),getResources().getString(R.string.Married),
                getResources().getString(R.string.Fisher),getResources().getString(R.string.Divorced),getResources().getString(R.string.Widowed)
                ,null, null, null);

        Educations.setText(humanize(educate));
        Professions.setText(humanize(profession));
        Marital_Statuss.setText(humanize(maried));

        Prior_HealthCares.setText(humanize(memberclient.getDetails().get("Prior_HealthCare")));
        Prior_Diagnosiss.setText(humanize(memberclient.getDetails().get("Prior_Diagnosis")));
       // Other_Prior_Diagnosis.setText(humanize(memberclient.getDetails().get("Other_Prior_Diagnosis")));

        Visual_Health_Deformitiess.setText(humanize(memberclient.getDetails().get("Visual_Health_Deformities")));

        //Other_Visual_Health_Deformities.setText(humanize(memberclient.getDetails().get("Other_Visual_Health_Deformities")));
        Prior_Surgeriess.setText(humanize(memberclient.getDetails().get("Prior_Surgeries")));
        Vaccine_Cards.setText(humanize(memberclient.getDetails().get("Vaccine_Card")));
        Vaccination_Historys.setText(humanize(memberclient.getDetails().get("Vaccination_History")));
        Pregnants.setText(humanize(memberclient.getDetails().get("Pregnant_now")));
        Prior_Pregnanciess.setText(humanize(memberclient.getDetails().get("Prior_Pregnancies")));
        Number_Deliveriess.setText(humanize(memberclient.getDetails().get("Number_Deliveries")));
        Number_Live_Birthss.setText(humanize(memberclient.getDetails().get("Number_Live_Births")));
        Prior_Birthweights.setText(humanize(memberclient.getDetails().get("Prior_Birthweight")));
        Birthweight_grams.setText(humanize(memberclient.getDetails().get("Birthweight_gram")));
        Is_Person_Alives.setText(humanize(memberclient.getDetails().get("Is_Person_Alive")));
        Cause_of_Deaths.setText(humanize(memberclient.getDetails().get("Cause_of_Death")));

        String ddate = memberclient.getDetails().get("death_date") != null? memberclient.getDetails().get("death_date"):"n/a";
        String dmonth = memberclient.getDetails().get("deathmonth") != null? memberclient.getDetails().get("deathmonth"):"n/a";
        String dyear = memberclient.getDetails().get("deathyear") != null? memberclient.getDetails().get("deathyear"):"n/a";

        death_dates.setText(ddate + "-" +dmonth +"-"+dyear);

        ImageButton back = (ImageButton) findViewById(R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(HHmemberDetailActivity.this, HHmemberSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        nama.setText(getResources().getString(R.string.name)+ ": "+(memberclient.getColumnmaps().get("Name_family_member") != null ? memberclient.getColumnmaps().get("Name_family_member") : "-"));
        nik.setText(getResources().getString(R.string.ethnic)+": "+ (memberclient.getColumnmaps().get("Ethnic_Group") != null ? memberclient.getColumnmaps().get("Ethnic_Group") : "-"));
        husband_name.setText(getResources().getString(R.string.education)+": "+ (memberclient.getColumnmaps().get("namaSuami") != null ? memberclient.getColumnmaps().get("namaSuami") : "-"));

        String date = memberclient.getDetails().get("dob_date") != null? memberclient.getDetails().get("dob_date"):"n/a";
        String month = memberclient.getDetails().get("dob_month") != null? memberclient.getDetails().get("dob_month"):"n/a";
        String year = memberclient.getDetails().get("dob_year") != null? memberclient.getDetails().get("dob_year"):"n/a";

        String dob = date+" - "+month+" - "+year ;
        txt_dob.setText(getResources().getString(R.string.date_of_birth)+": "+ dob);

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

        pic1.setVisibility(View.GONE);
        pic2.setVisibility(View.GONE);
        pic3.setVisibility(View.GONE);
        pic4.setVisibility(View.GONE);

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


        /*****
         * Comment it at the moment because it replace other photo
         *
         * */
       /* pic1.setOnClickListener(new View.OnClickListener() {
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
        });*/

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
    private String value_compare(String value_education,String value1,String value2,String value3,String value4,String value5,String value6,String value7,String value8,
                                 String a, String b, String c, String d, String e, String f, String g, String h) {

        String as =value_education.equalsIgnoreCase(value1) ? a:
                value_education.equalsIgnoreCase(value2)?b:
                        value_education.equalsIgnoreCase(value3)?c:
                                value_education.equalsIgnoreCase(value4)?d:
                                        value_education.equalsIgnoreCase(value5)?e:
                                                value_education.equalsIgnoreCase(value6)?f:
                                                        value_education.equalsIgnoreCase(value7)?g:
                                                                value_education.equalsIgnoreCase(value8)?h:
                                                                        "";
        return as;
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
        d.Recent_Deathss.setText(idontremember(getDetails("Recent_Births")));
        d.Recent_Birthss.setText(idontremember(getDetails("Recent_Births")));
        d.Birth_locations.setText(idontremember(getDetails("Birth_location")));
//        d.Other_Birth_locations.setText(idontremember(getDetails(""));
        d.Visited_HealthCares.setText(idontremember(getDetails("Visited_HealthCare")));
        d.Treatment_Locations.setText(idontremember(getDetails("Treatment_Location")));
//        d.Treatment_Location_texts.setText(idontremember(getDetails(""));
        d.New_Diagnosiss.setText(idontremember(getDetails("New_Diagnosis")));
//        d.New_Diagnosis_texts.setText(idontremember(getDetails(""));
        d.Pregnant_Last_Months.setText(idontremember(getDetails("Pregnant_Last_Month")));
        d.Pregnants.setText(idontremember(getDetails("Pregnant")));
        d.Last_Periods.setText(idontremember(getDetails("Last_Period")));


        String date = memberclient.getDetails().get("LMP_date") != null? memberclient.getDetails().get("LMP_date"):"n/a";
        String month = memberclient.getDetails().get("LMP_month") != null? memberclient.getDetails().get("LMP_month"):"n/a";
        String year = memberclient.getDetails().get("LMP_year") != null? memberclient.getDetails().get("LMP_year"):"n/a";

        d.LMP_dates.setText(date+" - "+month+" - "+ year);

        d.Prenatal_Visitss.setText(idontremember(getDetails("Prenatal_Visits")));
        d.Prenatal_Medications.setText(idontremember(getDetails("Prenatal_Medication")));
//        d.Prenatal_Medication_Types.setText(idontremember(getDetails("Prenatal_Medication_Type"));
        d.Pelvic_Crampss.setText(idontremember(getDetails("Pelvic_Cramps")));
        d.Amniotic_Fluids.setText(idontremember(getDetails("Amniotic_Fluid")));
        d.Dizzinesss.setText(idontremember(getDetails("Dizziness")));
        d.Feet_Swellings.setText(idontremember(getDetails("Feet_Swelling")));
        d.Vaginal_discharges.setText(idontremember(getDetails("Vaginal_discharge")));
        d.Miscarriage_Stillbirths.setText(idontremember(getDetails("Miscarriage_Stillbirth")));
        d.Contraceptions.setText(idontremember(getDetails("Contraception")));
        d.Contraception_Types.setText(idontremember(getDetails("Contraception_Type")));
        d.Breastfeedings.setText(idontremember(getDetails("Breastfeeding")));
        d.Exlusive_breastfeedings.setText(idontremember(getDetails("Exlusive_breastfeeding")));
        d.Menopauses.setText(idontremember(getDetails("Menopause")));
        d.Diarrheas.setText(idontremember(getDetails("Diarrhea")));
        d.Diarrhea_Dayss.setText(idontremember(getDetails("Diarrhea_Days")));
        d.Diarrhea_Blacks.setText(idontremember(getDetails("Diarrhea_Black")));
        d.Diarrhea_Bloodys.setText(idontremember(getDetails("Diarrhea_Bloody")));
        d.Eyes_Sunkens.setText(idontremember(getDetails("Eyes_Sunken")));
        d.Tearss.setText(idontremember(getDetails("Tears")));
        d.Mouth_drys.setText(idontremember(getDetails("Mouth_dry")));
        d.Diarrhea_Treatments.setText(idontremember(getDetails("Diarrhea_Treatment")));
        d.Diarrhea_Treatment_Locations.setText(idontremember(getDetails("Diarrhea_Treatment_Location")));
//        d.Other_Diarrhea_Treatments.setText(idontremember(getDetails(""));
        d.Diarrhea_Treatment_Outcomes.setText(idontremember(getDetails("Diarrhea_Treatment_Outcome")));
        d.Fever_TFs.setText(idontremember(getDetails("Fever_TF")));
        d.Fever_Durations.setText(idontremember(getDetails("Fever_Duration")));
        d.Fever_Treatments.setText(idontremember(getDetails("Fever_Treatment")));
        d.Fever_Treatment_Locations.setText(idontremember(getDetails("Fever_Treatment_Location")));
//        d.Other_Fever_Treatments.setText(idontremember(getDetails(""));
        d.TDRs.setText(idontremember(getDetails("TDR")));
        d.TDR_results.setText(idontremember(getDetails("TDR_result")));
        d.Temperatures.setText(idontremember(getDetails("Temperature")));
        d.Fever_Treatment_Outcomes.setText(idontremember(getDetails("Fever_Treatment_Outcome")));
        d.Vomitings.setText(idontremember(getDetails("Vomiting")));
        d.Headaches.setText(idontremember(getDetails("Headache")));
        d.Animal_Contacts.setText(idontremember(getDetails("Animal_Contact")));
        d.Animal_Incidents.setText(idontremember(getDetails("Animal_Incident")));
        d.Rat_contacts.setText(idontremember(getDetails("Rat_contact")));
        d.Jaundices.setText(idontremember(getDetails("Jaundice")));
        d.Measless.setText(idontremember(getDetails("Measles")));
        d.Weight_losss.setText(idontremember(getDetails("Weight_loss")));
        d.Persistent_Coughs.setText(idontremember(getDetails("Persistent_Cough")));
        d.Coughing_Bloods.setText(idontremember(getDetails("Coughing_Blood")));
        d.Sputums.setText(idontremember(getDetails("Sputum")));
        d.Breathings.setText(idontremember(getDetails("Breathing")));
        d.Stomach_Pains.setText(idontremember(getDetails("Stomach_Pain")));
        d.Stomach_Pain_Durations.setText(idontremember(getDetails("Stomach_Pain_Duration")));
        d.Stomach_Pain_foods.setText(idontremember(getDetails("Stomach_Pain_food")));
        d.Open_Woundss.setText(idontremember(getDetails("Open_Wounds")));
        d.Wound_Conditions.setText(idontremember(getDetails("Wound_Condition")));
        d.Cigarettess.setText(idontremember(getDetails("Cigarettes")));
        d.Drinkings.setText(idontremember(getDetails("Drinking")));
        d.Urine_Colors.setText(idontremember(getDetails("Urine_Color")));
        d.Puss.setText(idontremember(getDetails("Pus")));
        d.Pain_Urinations.setText(idontremember(getDetails("Pain_Urination")));
        d.Urination_Bloods.setText(idontremember(getDetails("Urination_Blood")));
        d.Back_Pains.setText(idontremember(getDetails("Back_Pain")));
        d.Medicationss.setText(idontremember(getDetails("Medications")));
        d.Medications_Names.setText(idontremember(getDetails("Medications_Name")));
//        d.Medications_Name_opens.setText(idontremember(getDetails(""));
        d.Medications_Purposes.setText(idontremember(getDetails("Medications_Purpose")));
        d.STIs.setText(idontremember(getDetails("STI")));
        d.STI_Treatments.setText(idontremember(getDetails("STI_Treatment")));
        d.STI_Treatment_Outcomes.setText(idontremember(getDetails("STI_Treatment_Outcome")));
        d.New_vaccines.setText(idontremember(getDetails("New_vaccine")));
        d.New_vaccine_typess.setText(idontremember(getDetails("New_vaccine_types")));
//        d.submissionDate.setText(getDetails(""));
    }

    private void initializeMemberDetail(MemberDetail2 d){
        if(d==null)
            return;
        d.initialized=true;
        d.Recent_Deathss = (TextView) findViewById(R.id.Recent_Deaths);
        d.Recent_Birthss  = (TextView) findViewById(R.id.Recent_Births);
        d.Birth_locations  = (TextView) findViewById(R.id.Birth_location);
//        d.Other_Birth_location  = (TextView) findViewById(R.id.);
        d.Visited_HealthCares  = (TextView) findViewById(R.id.Visited_HealthCare);
        d.Treatment_Locations  = (TextView) findViewById(R.id.Treatment_Location);
//        d.Treatment_Location_text  = (TextView) findViewById(R.id.);
        d.New_Diagnosiss  = (TextView) findViewById(R.id.New_Diagnosis);
//        d.New_Diagnosis_text  = (TextView) findViewById(R.id.);
        d.Pregnant_Last_Months  = (TextView) findViewById(R.id.Pregnant_Last_Month);
        d.Pregnants  = (TextView) findViewById(R.id.Pregnant);
        d.Last_Periods  = (TextView) findViewById(R.id.Last_Period);
        d.LMP_dates  = (TextView) findViewById(R.id.LMP_date);
      //  d.LMP_month  = (TextView) findViewById(R.id.LMP_month);
     //   d.LMP_year  = (TextView) findViewById(R.id.LMP_year);
        d.Prenatal_Visitss  = (TextView) findViewById(R.id.Prenatal_Visits);
        d.Prenatal_Medications  = (TextView) findViewById(R.id.Prenatal_Medication);
        d.Prenatal_Medication_Types  = (TextView) findViewById(R.id.Prenatal_Medication_Type);
        d.Pelvic_Crampss  = (TextView) findViewById(R.id.Pelvic_Cramps);
        d.Amniotic_Fluids  = (TextView) findViewById(R.id.Amniotic_Fluid);
        d.Dizzinesss  = (TextView) findViewById(R.id.Dizziness);
        d.Feet_Swellings  = (TextView) findViewById(R.id.Feet_Swelling);
        d.Vaginal_discharges  = (TextView) findViewById(R.id.Vaginal_discharge);
        d.Miscarriage_Stillbirths  = (TextView) findViewById(R.id.Miscarriage_Stillbirth);
        d.Contraceptions  = (TextView) findViewById(R.id.Contraception);
        d.Contraception_Types  = (TextView) findViewById(R.id.Contraception_Type);
        d.Breastfeedings  = (TextView) findViewById(R.id.Breastfeeding);
        d.Exlusive_breastfeedings  = (TextView) findViewById(R.id.Exlusive_breastfeeding);
        d.Menopauses  = (TextView) findViewById(R.id.Menopause);
        d.Diarrheas  = (TextView) findViewById(R.id.Diarrhea);
        d.Diarrhea_Dayss  = (TextView) findViewById(R.id.Diarrhea_Days);
        d.Diarrhea_Blacks  = (TextView) findViewById(R.id.Diarrhea_Black);
        d.Diarrhea_Bloodys  = (TextView) findViewById(R.id.Diarrhea_Bloody);
        d.Eyes_Sunkens  = (TextView) findViewById(R.id.Eyes_Sunken);
        d.Tearss  = (TextView) findViewById(R.id.Tears);
        d.Mouth_drys  = (TextView) findViewById(R.id.Mouth_dry);
        d.Diarrhea_Treatments  = (TextView) findViewById(R.id.Diarrhea_Treatment);
        d.Diarrhea_Treatment_Locations  = (TextView) findViewById(R.id.Diarrhea_Treatment_Location);
//        d.Other_Diarrhea_Treatment  = (TextView) findViewById(R.id.);
        d.Diarrhea_Treatment_Outcomes  = (TextView) findViewById(R.id.Diarrhea_Treatment_Outcome);
        d.Fever_TFs  = (TextView) findViewById(R.id.Fever_TF);
        d.Fever_Durations  = (TextView) findViewById(R.id.Fever_Duration);
        d.Fever_Treatments  = (TextView) findViewById(R.id.Fever_Treatment);
        d.Fever_Treatment_Locations  = (TextView) findViewById(R.id.Fever_Treatment_Location);
//        d.Other_Fever_Treatment  = (TextView) findViewById(R.id.);
        d.TDRs  = (TextView) findViewById(R.id.TDR);
        d.TDR_results  = (TextView) findViewById(R.id.TDR_result);
        d.Temperatures  = (TextView) findViewById(R.id.Temperature);
        d.Fever_Treatment_Outcomes  = (TextView) findViewById(R.id.Fever_Treatment_Outcome);
        d.Vomitings  = (TextView) findViewById(R.id.Vomiting);
        d.Headaches  = (TextView) findViewById(R.id.Headache);
        d.Animal_Contacts  = (TextView) findViewById(R.id.Animal_Contact);
        d.Animal_Incidents  = (TextView) findViewById(R.id.Animal_Incident);
        d.Rat_contacts  = (TextView) findViewById(R.id.Rat_contact);
        d.Jaundices  = (TextView) findViewById(R.id.Jaundice);
        d.Measless  = (TextView) findViewById(R.id.Measles);
        d.Weight_losss  = (TextView) findViewById(R.id.Weight_loss);
        d.Persistent_Coughs  = (TextView) findViewById(R.id.Persistent_Cough);
        d.Coughing_Bloods  = (TextView) findViewById(R.id.Coughing_Blood);
        d.Sputums  = (TextView) findViewById(R.id.Sputum);
        d.Breathings  = (TextView) findViewById(R.id.Breathing);
        d.Stomach_Pains  = (TextView) findViewById(R.id.Stomach_Pain);
        d.Stomach_Pain_Durations  = (TextView) findViewById(R.id.Stomach_Pain_Duration);
        d.Stomach_Pain_foods  = (TextView) findViewById(R.id.Stomach_Pain_food);
        d.Open_Woundss  = (TextView) findViewById(R.id.Open_Wounds);
        d.Wound_Conditions  = (TextView) findViewById(R.id.Wound_Condition);
        d.Cigarettess  = (TextView) findViewById(R.id.Cigarettes);
        d.Drinkings  = (TextView) findViewById(R.id.Drinking);
        d.Urine_Colors  = (TextView) findViewById(R.id.Urine_Color);
        d.Puss  = (TextView) findViewById(R.id.Pus);
        d.Pain_Urinations  = (TextView) findViewById(R.id.Pain_Urination);
        d.Urination_Bloods  = (TextView) findViewById(R.id.Urination_Blood);
        d.Back_Pains  = (TextView) findViewById(R.id.Back_Pain);
        d.Medicationss  = (TextView) findViewById(R.id.Medications);
        d.Medications_Names  = (TextView) findViewById(R.id.Medications_Name);
//        d.Medications_Name_open  = (TextView) findViewById(R.id.);
        d.Medications_Purposes  = (TextView) findViewById(R.id.Medications_Purpose);
        d.STIs  = (TextView) findViewById(R.id.STI);
        d.STI_Treatments  = (TextView) findViewById(R.id.STI_Treatment);
        d.STI_Treatment_Outcomes  = (TextView) findViewById(R.id.STI_Treatment_Outcome);
        d.New_vaccines  = (TextView) findViewById(R.id.New_vaccine);
        d.New_vaccine_typess  = (TextView) findViewById(R.id.New_vaccine_types);
//        d.submissionDate  = (TextView) findViewById(R.id.);

    }

    private class MemberDetail2 {
        boolean initialized = false;
        TextView Recent_Deathss;
        TextView Recent_Birthss;
        TextView Birth_locations;
        TextView Other_Birth_location;
        TextView Visited_HealthCares;
        TextView Treatment_Locations;
        TextView Treatment_Location_text;
        TextView New_Diagnosiss;
        TextView New_Diagnosis_text;
        TextView Pregnant_Last_Months;
        TextView Pregnants;
        TextView Last_Periods;
        TextView LMP_dates;
        TextView LMP_month;
        TextView LMP_year;
        TextView Prenatal_Visitss;
        TextView Prenatal_Medications;
        TextView Prenatal_Medication_Types;
        TextView Pelvic_Crampss;
        TextView Amniotic_Fluids;
        TextView Dizzinesss;
        TextView Feet_Swellings;
        TextView Vaginal_discharges;
        TextView Miscarriage_Stillbirths;
        TextView Contraceptions;
        TextView Contraception_Types;
        TextView Breastfeedings;
        TextView Exlusive_breastfeedings;
        TextView Menopauses;
        TextView Diarrheas;
        TextView Diarrhea_Dayss;
        TextView Diarrhea_Blacks;
        TextView Diarrhea_Bloodys;
        TextView Eyes_Sunkens;
        TextView Tearss;
        TextView Mouth_drys;
        TextView Diarrhea_Treatments;
        TextView Diarrhea_Treatment_Locations;
        TextView Other_Diarrhea_Treatment;
        TextView Diarrhea_Treatment_Outcomes;
        TextView Fever_TFs;
        TextView Fever_Durations;
        TextView Fever_Treatments;
        TextView Fever_Treatment_Locations;
        TextView Other_Fever_Treatment;
        TextView TDRs;
        TextView TDR_results;
        TextView Temperatures;
        TextView Fever_Treatment_Outcomes;
        TextView Vomitings;
        TextView Headaches;
        TextView Animal_Contacts;
        TextView Animal_Incidents;
        TextView Rat_contacts;
        TextView Jaundices;
        TextView Measless;
        TextView Weight_losss;
        TextView Persistent_Coughs;
        TextView Coughing_Bloods;
        TextView Sputums;
        TextView Breathings;
        TextView Stomach_Pains;
        TextView Stomach_Pain_Durations;
        TextView Stomach_Pain_foods;
        TextView Open_Woundss;
        TextView Wound_Conditions;
        TextView Cigarettess;
        TextView Drinkings;
        TextView Urine_Colors;
        TextView Puss;
        TextView Pain_Urinations;
        TextView Urination_Bloods;
        TextView Back_Pains;
        TextView Medicationss;
        TextView Medications_Names;
        TextView Medications_Name_opens;
        TextView Medications_Purposes;
        TextView STIs;
        TextView STI_Treatments;
        TextView STI_Treatment_Outcomes;
        TextView New_vaccines;
        TextView New_vaccine_typess;
        TextView submissionDate;
    }


private String idontremember (String value){
    String ret = value.equalsIgnoreCase("yes")?getResources().getString(R.string.Yes):
            value.equalsIgnoreCase("No")?getResources().getString(R.string.No):
                    value.equalsIgnoreCase("idr")?getResources().getString(R.string.idr):"";
      return ret;
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
