package org.ei.opensrp.madagascar.HHmember;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
public class HHmemberDetailActivity extends Activity {

    //image retrieving
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    //  private static KmsCalc  kmsCalc;
    private static int mImageThumbSize;
    private static int mImageThumbSpacing;
    private static String showbgm;
    private static ImageFetcher mImageFetcher;

    //image retrieving
    public static CommonPersonObjectClient memberclient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = Context.getInstance();
        setContentView(R.layout.member_detail_activity);

        final ImageView kiview = (ImageView)findViewById(R.id.motherdetailprofileview);
        //header
        TextView today = (TextView) findViewById(R.id.detail_today);

        //profile
        TextView nama = (TextView) findViewById(R.id.txt_wife_name);
        TextView nik = (TextView) findViewById(R.id.txt_nik);
        TextView husband_name = (TextView) findViewById(R.id.txt_husband_name);
        TextView dob = (TextView) findViewById(R.id.txt_dob);
        TextView phone = (TextView) findViewById(R.id.txt_contact_phone_number);

        //detail data
        TextView txt_Recent_Deaths = (TextView) findViewById(R.id.txt_Recent_Deaths);
        TextView txt_Recent_Births = (TextView) findViewById(R.id.txt_Recent_Births);
        TextView txt_Birth_location = (TextView) findViewById(R.id.txt_Birth_location);
        TextView txt_Pregnant_Last_Month = (TextView) findViewById(R.id.txt_Pregnant_Last_Month);
        TextView txt_Pregnant = (TextView) findViewById(R.id.txt_Pregnant);
        TextView txt_Last_Period = (TextView) findViewById(R.id.txt_Last_Period);
        TextView txt_LMP_exact_date = (TextView) findViewById(R.id.txt_LMP_exact_date);
        TextView txt_LMP_month_year = (TextView) findViewById(R.id.txt_LMP_month_year);
        TextView txt_Prenatal_Visits = (TextView) findViewById(R.id.txt_Prenatal_Visits);
        TextView txt_Prenatal_Medication = (TextView) findViewById(R.id.txt_Prenatal_Medication);
        TextView txt_Prenatal_Medication_Type = (TextView) findViewById(R.id.txt_Prenatal_Medication_Type);
        TextView txt_Vaginal_Bleeding = (TextView) findViewById(R.id.txt_Vaginal_Bleeding);
        TextView txt_Vaginal_Bleeding_Severity = (TextView) findViewById(R.id.txt_Vaginal_Bleeding_Severity);
        TextView txt_Vaginal_Bleeding_Treatmentu = (TextView) findViewById(R.id.txt_Vaginal_Bleeding_Treatmentu);

       /* TextView djj = (TextView) findViewById(R.id.txt_djj);
        TextView kepalaJaninTerhadapPAP = (TextView) findViewById(R.id.txt_kepalaJaninTerhadapPAP);
        TextView taksiranBeratJanin = (TextView) findViewById(R.id.txt_taksiranBeratJanin);
        TextView persentasiJanin = (TextView) findViewById(R.id.txt_persentasiJanin);
        TextView jumlahJanin = (TextView) findViewById(R.id.txt_jumlahJanin);


        TextView statusImunisasitt = (TextView) findViewById(R.id.txt_statusImunisasitt);
        TextView pelayananfe = (TextView) findViewById(R.id.txt_pelayananfe);
        TextView komplikasidalamKehamilan = (TextView) findViewById(R.id.txt_komplikasidalamKehamilan);

        TextView integrasiProgrampmtctvct = (TextView) findViewById(R.id.txt_integrasiProgrampmtctvct);
        TextView integrasiProgrampmtctPeriksaDarah = (TextView) findViewById(R.id.txt_integrasiProgrampmtctPeriksaDarah);
        TextView integrasiProgrampmtctSerologi = (TextView) findViewById(R.id.txt_integrasiProgrampmtctSerologi);
        TextView integrasiProgrampmtctarvProfilaksis = (TextView) findViewById(R.id.txt_integrasiProgrampmtctarvProfilaksis);
        TextView integrasiProgramMalariaPeriksaDarah = (TextView) findViewById(R.id.txt_integrasiProgramMalariaPeriksaDarah);
        TextView integrasiProgramMalariaObat = (TextView) findViewById(R.id.txt_integrasiProgramMalariaObat);
        TextView integrasiProgramMalariaKelambuBerinsektisida = (TextView) findViewById(R.id.txt_integrasiProgramMalariaKelambuBerinsektisida);
        TextView integrasiProgramtbDahak = (TextView) findViewById(R.id.txt_integrasiProgramtbDahak);
        TextView integrasiProgramtbObat = (TextView) findViewById(R.id.txt_integrasiProgramtbObat);

        TextView laboratoriumPeriksaHbHasil = (TextView) findViewById(R.id.txt_laboratoriumPeriksaHbHasil);
        TextView laboratoriumPeriksaHbAnemia = (TextView) findViewById(R.id.txt_laboratoriumPeriksaHbAnemia);
        TextView laboratoriumProteinUria = (TextView) findViewById(R.id.txt_laboratoriumProteinUria);
        TextView laboratoriumGulaDarah = (TextView) findViewById(R.id.txt_laboratoriumGulaDarah);
        TextView laboratoriumThalasemia = (TextView) findViewById(R.id.txt_laboratoriumThalasemia);
        TextView laboratoriumSifilis = (TextView) findViewById(R.id.txt_laboratoriumSifilis);
        TextView laboratoriumHbsAg = (TextView) findViewById(R.id.txt_laboratoriumHbsAg);

*/


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
     //   dob.setText(getResources().getString(R.string.dob)+ (memberclient.getDetails().get("Education") != null ? kiclients.getDetails().get("Education") : "-"));
      //  phone.setText("No HP: "+ (memberclient.getDetails().get("NomorTelponHp") != null ? memberclient.getDetails().get("NomorTelponHp") : "-"));


         txt_Recent_Deaths.setText(memberclient.getDetails().get("Recent_Deaths") != null ? memberclient.getDetails().get("Recent_Deaths") : "-");
         txt_Recent_Births.setText(memberclient.getDetails().get("Recent_Births") != null ? memberclient.getDetails().get("Recent_Births") : "-");
         txt_Birth_location.setText(memberclient.getDetails().get("Birth_location") != null ? memberclient.getDetails().get("Birth_location") : "-");
         txt_Pregnant_Last_Month.setText(memberclient.getDetails().get("Pregnant_Last_Month") != null ? memberclient.getDetails().get("Pregnant_Last_Month") : "-");
         txt_Pregnant.setText(memberclient.getDetails().get("Pregnant") != null ? memberclient.getDetails().get("Pregnant") : "-");
         txt_Last_Period.setText(memberclient.getDetails().get("Last_Period") != null ? memberclient.getDetails().get("Last_Period") : "-");
         txt_LMP_exact_date.setText(memberclient.getDetails().get("LMP_exact_date") != null ? memberclient.getDetails().get("LMP_exact_date") : "-");
         txt_LMP_month_year.setText(memberclient.getDetails().get("LMP_month_year") != null ? memberclient.getDetails().get("LMP_month_year") : "-");
         txt_Prenatal_Visits.setText(memberclient.getDetails().get("Prenatal_Visits") != null ? memberclient.getDetails().get("Prenatal_Visits") : "-");
         txt_Prenatal_Medication.setText(memberclient.getDetails().get("Prenatal_Medication") != null ? memberclient.getDetails().get("Prenatal_Medication") : "-");
         txt_Prenatal_Medication_Type.setText(memberclient.getDetails().get("Prenatal_Medication_Type") != null ? memberclient.getDetails().get("Prenatal_Medication_Type") : "-");
         txt_Vaginal_Bleeding.setText(memberclient.getDetails().get("Vaginal_Bleeding") != null ? memberclient.getDetails().get("Vaginal_Bleeding") : "-");
         txt_Vaginal_Bleeding_Severity.setText(memberclient.getDetails().get("Vaginal_Bleeding_Severity") != null ? memberclient.getDetails().get("Vaginal_Bleeding_Severity") : "-");
         txt_Vaginal_Bleeding_Treatmentu.setText(memberclient.getDetails().get("Vaginal_Bleeding_Treatment") != null ? memberclient.getDetails().get("Vaginal_Bleeding_Treatment") : "-");
        final ImageView pic1 = (ImageView)findViewById(R.id.photo1);
        final ImageView pic2 = (ImageView)findViewById(R.id.photo2);
        final ImageView pic3 = (ImageView)findViewById(R.id.photo3);
        final ImageView pic4 = (ImageView)findViewById(R.id.photo4);

        if(memberclient.getDetails().get("profilepic") !=null){
            setImagetoHolderFromUri(HHmemberDetailActivity.this, memberclient.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);

        }
        else {
            if(memberclient.getDetails().get("Sex")!= null) {
                if (memberclient.getDetails().get("Sex").equalsIgnoreCase("Female")) {
                    kiview.setImageDrawable(getResources().getDrawable(R.mipmap.woman_placeholder));

                }
            }
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
//            Bundle extras = data.getExtras();
//            String imageBitmap = (String) extras.get(MediaStore.EXTRA_OUTPUT);
//            Toast.makeText(this,imageBitmap,Toast.LENGTH_LONG).show();
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("photo",currentfile.getAbsolutePath());
            saveimagereference(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
           // onCreate(null);
            Intent refresh = new Intent(this, HHmemberSmartRegisterActivity.class);
            startActivity(refresh);//Start the same Activity
            finish(); //finish Activity.
        }
        else if (requestCode == REQUEST_TAKE_PHOTO2 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("photo2",currentfile.getAbsolutePath());
            saveimagereference2(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO3 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("photo3",currentfile.getAbsolutePath());
            saveimagereference3(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO4 && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("photo4",currentfile.getAbsolutePath());
            saveimagereference4(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
            finish();
            startActivity(new Intent(this, HHmemberSmartRegisterActivity.class));
            overridePendingTransition(0, 0);
        }
        else if (requestCode == REQUEST_TAKE_PHOTOS && resultCode == RESULT_OK) {
            HashMap<String,String> details = new HashMap<String,String>();
            details.put("profilepic",currentfile.getAbsolutePath());
            saveimagereferences(bindobject,entityid,details);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
            mImageView.setImageBitmap(bitmap);
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

    public void saveimagereferences(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("profilepic"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
//                kiclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }

    public void saveimagereference(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("photo"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);

    }

    public void saveimagereference2(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("photo2"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);

    }
    public void saveimagereference3(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("photo3"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
      //  recreate();
    }
    public void saveimagereference4(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("photo4"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
        //  recreate();
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
}
