package org.ei.opensrp.madagascar.HH;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.madagascar.R;/*
import org.ei.opensrp.mcare.elco.ElcoSmartRegisterActivity;*/
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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static java.text.MessageFormat.format;
import static org.ei.opensrp.util.StringUtil.humanize;

/**
 * Created by raihan on 5/11/15.
 */
public class HouseHoldDetailActivity extends Activity {

    //image retrieving



//    private static ImageFetcher mImageFetcher;




    //image retrieving

    public static CommonPersonObjectClient householdclient;
    public static CommonPersonObjectController householdcontroller;
    private SmartRegisterPaginatedAdapter clientsAdapter;
    private final PaginationViewHandler paginationViewHandler = new PaginationViewHandler();
    ListView Clientsview;
    Context context;
    public Button nidbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = Context.getInstance();
        setContentView(R.layout.household_detail_activity);

        TextView householdhead_name = (TextView)findViewById(R.id.name_household_head);
        TextView mauza = (TextView)findViewById(R.id.mauza);
       // TextView household_hhid = (TextView)findViewById(R.id.house_detail_hhid);
       // TextView household_hhid_jivita = (TextView)findViewById(R.id.hh_detail_jivita);

        ImageButton back = (ImageButton)findViewById(R.id.btn_back_to_home);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(HouseHoldDetailActivity.this, HHSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });


        householdhead_name.setText(humanize(householdclient.getColumnmaps().get("HH.name_household_head")));
        mauza.setText(humanize((householdclient.getColumnmaps().get("HH_GPS_Point") != null ? householdclient.getColumnmaps().get("HH_GPS_Point") : "")));
//        household_hhid.setText(getResources().getString(R.string.hhid_gob) + householdclient.getColumnmaps().get("FWGOBHHID"));
//        household_hhid_jivita.setText(getResources().getString(R.string.hhid_jivita) + householdclient.getColumnmaps().get("FWJIVHHID"));


        /*Clientsview = (ListView)findViewById(R.id.list);
        paginationViewHandler.addPagination(Clientsview);

        householdcontroller = new CommonPersonObjectController(Context.getInstance().allCommonsRepositoryobjects("HHMember"), Context.getInstance().allBeneficiaries(),context.listCache(),
                context.personObjectClientsCache(),"Name_family_member","HHMember","relationalid",householdclient.entityId(), CommonPersonObjectController.ByColumnAndByDetails.byrelationalid,"Name_family_member", CommonPersonObjectController.ByColumnAndByDetails.byColumn);
        clientsAdapter = adapter();
        clientsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                paginationViewHandler.refresh();
            }
        });
        Clientsview.setAdapter(clientsAdapter);
        Log.v("view size", "" + householdcontroller.getClients().size());
        if(householdcontroller.getClients().size()<1){

            Clientsview.setVisibility(INVISIBLE);
        }
        if(!(clientsAdapter.getCount()>1)){
            paginationViewHandler.footerView.setVisibility(INVISIBLE);
        }*/
//        clientsProgressView.setVisibility(View.GONE);



//        paginationViewHandler.refresh();
    }
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(new HouseholdDetailsSmartClientsProvider(this,paginationViewHandler ,householdcontroller));
    }
    private class PaginationViewHandler implements View.OnClickListener {
        private Button nextPageView;
        private Button previousPageView;
        private TextView pageInfoView;
        public ViewGroup footerView;

        private void addPagination(ListView clientsView) {
            footerView = getPaginationView();
            nextPageView = (Button) footerView.findViewById(R.id.btn_next_page);
            previousPageView = (Button) footerView.findViewById(R.id.btn_previous_page);
            pageInfoView = (TextView) footerView.findViewById(R.id.txt_page_info);

            nextPageView.setOnClickListener(this);
            previousPageView.setOnClickListener(this);

            footerView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(org.ei.opensrp.R.dimen.pagination_bar_height)));

        }

        public ViewGroup getPaginationView() {
            return (ViewGroup) getLayoutInflater().inflate(org.ei.opensrp.R.layout.smart_register_pagination, null);
        }

        private int getCurrentPageCount() {
            return clientsAdapter.currentPage() + 1 > clientsAdapter.pageCount() ? clientsAdapter.pageCount() : clientsAdapter.currentPage() + 1;
        }

        public void refresh() {
            pageInfoView.setText(
                    format(getResources().getString(org.ei.opensrp.R.string.str_page_info),
                            (getCurrentPageCount()),
                            (clientsAdapter.pageCount())));
            nextPageView.setVisibility(clientsAdapter.hasNextPage() ? VISIBLE : INVISIBLE);
            previousPageView.setVisibility(clientsAdapter.hasPreviousPage() ? VISIBLE : INVISIBLE);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_next_page:
                    gotoNextPage();
                    break;
                case R.id.btn_previous_page:
                    goBackToPreviousPage();
                    break;
                case R.id.registerlink:
                    startActivity(new Intent(HouseHoldDetailActivity.this, HHSmartRegisterActivity.class));
                    break;
                /*case R.id.profilepic:
                    entityid = ((CommonPersonObjectClient)view.getTag()).entityId();
                    bindobject = "elco";
                    mImageView = (ImageView)view;
//                    mImageView.setTag("womanpic");
                    dispatchTakePictureIntent((ImageView) view);
                    break;
                case R.id.registerlink:
                    startActivity(new Intent(HouseHoldDetailActivity.this, ElcoSmartRegisterActivity.class));
                    break;
                case R.id.nidpic_capture:
                    entityid = ((CommonPersonObjectClient)view.getTag()).entityId();
                    bindobject = "elco";
//                    mImageView = (ImageView)view;
                    nidbutton = (Button)view;
                    dispatchTakePictureIntentforNId(view);
                    break;
*/
            }
        }

        private void gotoNextPage() {
            clientsAdapter.nextPage();
            clientsAdapter.notifyDataSetChanged();
        }

        private void goBackToPreviousPage() {
            clientsAdapter.previousPage();
            clientsAdapter.notifyDataSetChanged();
        }
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
    static final int REQUEST_TAKE_PHOTO_NID = 9000;
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


    private void dispatchTakePictureIntentforNId(View imageView) {
//        mImageView = imageView;
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
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_NID);
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
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
//            mImageView.setImageBitmap(bitmap);
//            setImagetoHolder(this,currentfile.getAbsolutePath(),mImageView,R.drawable.householdload);
//            Log.v("see imageview",""+(String)mImageView.getTag());
            Log.v("see imageview", "" + currentfile.getAbsolutePath());
            setImagetoHolderFromUri(this, currentfile.getAbsolutePath(), mImageView, R.mipmap.householdload);
            recalladapterinitialization();
        }else  if (requestCode == REQUEST_TAKE_PHOTO_NID && resultCode == RESULT_OK) {

//            Bundle extras = data.getExtras();
//            String imageBitmap = (String) extras.get(MediaStore.EXTRA_OUTPUT);
//            Toast.makeText(this,imageBitmap,Toast.LENGTH_LONG).show();
            HashMap <String,String> details = new HashMap<String,String>();
            details.put("nidImage",currentfile.getAbsolutePath());
            saveimagereferenceforNID(bindobject,entityid,details);
            recalladapterinitialization();

//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            Bitmap bitmap = BitmapFactory.decodeFile(currentfile.getPath(), options);
//            mImageView.setImageBitmap(bitmap);
//            setImagetoHolder(this,currentfile.getAbsolutePath(),mImageView,R.drawable.householdload);
        }
    }
    public void saveimagereference(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("profilepic"), ImageRepository.TYPE_Unsynced,"dp");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
//                householdclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }
    public void saveimagereferenceforNID(String bindobject,String entityid,Map<String,String> details){
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("nidImage"), ImageRepository.TYPE_Unsynced,"nidImage");
        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
        try {
            nidbutton.setText("");
            nidbutton.setBackground(getDrawableFromPath(details.get("nidImage")));
        }catch (Exception e){

        }
//                householdclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
    }
    public Drawable getDrawableFromPath(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        //Here you can make logic for decode bitmap for ignore oom error.
        return new BitmapDrawable(bitmap);
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

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, HHSmartRegisterActivity.class));
        overridePendingTransition(0, 0);


    }
    public void recalladapterinitialization(){
        Clientsview = null;
        Clientsview = (ListView)findViewById(R.id.list);
        paginationViewHandler.addPagination(Clientsview);

        householdcontroller = new CommonPersonObjectController(Context.getInstance().allCommonsRepositoryobjects("HHMember"), Context.getInstance().allBeneficiaries(),context.listCache(),
                context.personObjectClientsCache(),"Name_family_member","HHMember","relationalid",householdclient.entityId(), CommonPersonObjectController.ByColumnAndByDetails.byrelationalid,"Name_family_member", CommonPersonObjectController.ByColumnAndByDetails.byDetails);
        clientsAdapter = adapter();
            clientsAdapter = adapter();
        clientsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                paginationViewHandler.refresh();
            }
        });
        Clientsview.setAdapter(clientsAdapter);
        Log.v("view size", "" + householdcontroller.getClients().size());
        if(householdcontroller.getClients().size()<1){

            Clientsview.setVisibility(INVISIBLE);
        }
        if(!(clientsAdapter.getCount()>1)){
            paginationViewHandler.footerView.setVisibility(INVISIBLE);
        }
    }
}
