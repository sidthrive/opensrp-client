package org.ei.opensrp.indonesia.face.camera.util;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.AllConstants;
import org.ei.opensrp.Context;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.indonesia.BidanHomeActivity;
import org.ei.opensrp.indonesia.application.BidanApplication;
import org.ei.opensrp.indonesia.face.camera.ClientsList;
import org.ei.opensrp.indonesia.face.camera.SmartShutterActivity;
import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.ei.opensrp.util.Log.logError;

/**
 * Created by wildan on 1/4/17.
 */
public class Tools {

    private static final String TAG = Tools.class.getSimpleName();
    public static final int CONFIDENCE_VALUE = 58;
    public static org.ei.opensrp.Context appContext;
    private Canvas canvas = null;
    SmartShutterActivity ss = new SmartShutterActivity();
    ClientsList cl = new ClientsList();
    private static HashMap<String, String> hash;

    public static boolean WritePictureToFile(android.content.Context context, Bitmap bitmap, String entityId, byte[] faceVector) {

        File pictureFile = getOutputMediaFile(0, entityId);
        File thumbs_photo = getOutputMediaFile(1, entityId);

        if (pictureFile == null || thumbs_photo == null) {
            Log.e(TAG, "Error creating media file, check storage permissions!");
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.e(TAG, "Wrote image to " + pictureFile);

            MediaScannerConnection.scanFile(context, new String[]{
                    pictureFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
            String photoPath = pictureFile.toString();
            Log.e(TAG, "Photo Path = " + photoPath);

//            Create Thumbs
            FileOutputStream tfos = new FileOutputStream(thumbs_photo);
            final int THUMBSIZE = FaceConstants.THUMBSIZE;

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoPath ),
                    THUMBSIZE, THUMBSIZE);
            ThumbImage.compress(Bitmap.CompressFormat.PNG, 100, tfos);
            tfos.close();
            Log.e(TAG, "Wrote Thumbs image to " + thumbs_photo);

//            HashMap<String,String> details = new HashMap<>();
            // SAVE DATA VECTOR ON DB
//            details.put("profilepic", photoPath);
//            details.put("profilepic", thumbs_photo.toString());

//           FIXME File & Database Stored
            saveStaticImageToDisk(entityId, ThumbImage, Arrays.toString(faceVector));

            return true;

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return false;
    }

    private static File getOutputMediaFile(Integer mode, String entityId) {
        // Mode 0 = Original
        // Mode 1 = Thumbs

        // Location use app_dir
        String imgFolder = (mode == 0) ? DrishtiApplication.getAppDir():
                DrishtiApplication.getAppDir()+File.separator+".thumbs";
//        String imgFolder = (mode == 0) ? "OPENSRP_SID":"OPENSRP_SID"+File.separator+".thumbs";
//        File mediaStorageDir = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), imgFolder);
        File mediaStorageDir = new File(imgFolder);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            Log.e(TAG, "failed to find directory " + mediaStorageDir.getAbsolutePath());
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Created new directory " + mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String filename = entity);
        return new File(String.format("%s%s%s.jpg", mediaStorageDir.getPath(), File.separator, entityId));
    }

    public static void drawInfo(Rect rect, Bitmap mutableBitmap, float pixelDensity, String personName) {
        Log.e(TAG, "drawInfo: rect "+rect );
        Log.e(TAG, "drawInfo: bitmap"+mutableBitmap );
//        Rect rect = faceDatas[i].rect;
        // Extra padding around the faeRects
        rect.set(rect.left -= 20, rect.top -= 20, rect.right += 20, rect.bottom += 20);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paintForRectFill = new Paint();

        // Draw rect fill
        paintForRectFill.setStyle(Paint.Style.FILL);
        paintForRectFill.setColor(Color.WHITE);
        paintForRectFill.setAlpha(80);
        // Draw rect strokes
        Paint paintForRectStroke = new Paint();
        paintForRectStroke.setStyle(Paint.Style.STROKE);
        paintForRectStroke.setColor(Color.GREEN);
        paintForRectStroke.setStrokeWidth(5);
        canvas.drawRect(rect, paintForRectFill);
        canvas.drawRect(rect, paintForRectStroke);

//        float pixelDensity = getResources().getDisplayMetrics().density;
        int textSize = (int) (rect.width() / 25 * pixelDensity);

        Paint paintForText = new Paint();
        Paint paintForTextBackground = new Paint();
        Typeface tp = Typeface.SERIF;
        Rect backgroundRect = new Rect(rect.left, rect.bottom, rect.right, (rect.bottom + textSize));

        paintForText.setColor(Color.WHITE);
        paintForText.setTextSize(textSize);
        paintForTextBackground.setStyle(Paint.Style.FILL);
        paintForTextBackground.setColor(Color.BLACK);
        paintForText.setTypeface(tp);
        paintForTextBackground.setAlpha(80);

        if (personName != null) {
            canvas.drawRect(backgroundRect, paintForTextBackground);
            canvas.drawText(personName, rect.left, rect.bottom + (textSize), paintForText);
        } else {
            canvas.drawRect(backgroundRect, paintForTextBackground);
            canvas.drawText("Not identified", rect.left, rect.bottom + (textSize), paintForText);
        }

//        confirmationView.setImageBitmap(mutableBitmap);

    }

    public static void drawRectFace(Rect rect, Bitmap mutableBitmap, float pixelDensity) {

        Log.e(TAG, "drawRectFace: rect "+ rect );
        Log.e(TAG, "drawRectFace: bitmap "+ mutableBitmap );
        Log.e(TAG, "drawRectFace: pixelDensity "+ pixelDensity );

        // Extra padding around the faceRects
        rect.set(rect.left -= 20, rect.top -= 20, rect.right += 20, rect.bottom += 20);
        Canvas canvas = new Canvas(mutableBitmap);

        // Draw rect fill
        Paint paintForRectFill = new Paint();
        paintForRectFill.setStyle(Paint.Style.FILL);
        paintForRectFill.setColor(Color.WHITE);
        paintForRectFill.setAlpha(80);

        // Draw rect strokes
        Paint paintForRectStroke = new Paint();
        paintForRectStroke.setStyle(Paint.Style.STROKE);
        paintForRectStroke.setColor(Color.GREEN);
        paintForRectStroke.setStrokeWidth(5);

        canvas.drawRect(rect, paintForRectFill);
        canvas.drawRect(rect, paintForRectStroke);

    }

    public void saveHash(HashMap<String, String> hashMap, android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        Log.e(TAG, "Hash Save Size = " + hashMap.size());
        for (String s : hashMap.keySet()) {
            editor.putString(s, hashMap.get(s));
        }
        editor.apply();
    }

    public HashMap<String, String> retrieveHash(android.content.Context context) {
        Log.e(TAG, "retrieveHash: "+"Fetch" );
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);
        HashMap<String, String> hash = new HashMap<>();
        hash.putAll((Map<? extends String, ? extends String>) settings.getAll());
        return hash;
    }

    public void saveAlbum() {
//        byte[] albumBuffer = SmartShutterActivity.faceProc.serializeRecogntionAlbum();
//		saveCloud(albumBuffer);
//        Log.e(TAG, "Size of byte Array =" + albumBuffer.length);
//        SharedPreferences settings = getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("albumArray", Arrays.toString(albumBuffer));
//        editor.apply();
    }

    public void loadAlbum() {
//        Toast.makeText(this, "Load FacialActivity Album", Toast.LENGTH_SHORT).show();
//        Log.e(TAG, "loadAlbum: ");
//        SharedPreferences settings = getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
//        String arrayOfString = settings.getString("albumArray", null);

//        byte[] albumArray;
//        if (arrayOfString != null) {
//            String[] splitStringArray = arrayOfString.substring(1,
//                    arrayOfString.length() - 1).split(", ");
//
//            albumArray = new byte[splitStringArray.length];
//            for (int i = 0; i < splitStringArray.length; i++) {
//                albumArray[i] = Byte.parseByte(splitStringArray[i]);
//            }
//            SmartShutterActivity.faceProc.deserializeRecognitionAlbum(albumArray);
//            Log.e(TAG, "De-Serialized Album Success!");
//        }
    }

    public static void alertDialog(android.content.Context context, int opt){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        Tools tools = new Tools();
//        alertDialog.setMessage(message);
        String message = "";
        switch (opt){
            case 0 :
                message = "Are you sure to empty The Album?";
//                doEmpty;
                break;
            case 1 :
                message = "Are you sure to delete item";
                break;
            default:
                break;
        }
        alertDialog.setMessage(message);
//        alertDialog.setButton("OK", do);
        alertDialog.setPositiveButton("ERASE", tools.doEmpty);
        alertDialog.show();
    }

    private DialogInterface.OnClickListener doEmpty = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            boolean result = SmartShutterActivity.faceProc.resetAlbum();
            if (result) {
//                HashMap<String, String> hashMap = SmartShutterActivity.retrieveHash(getApplicationContext());
//                HashMap<String, String> hashMap = retrieveHash(getApplicationContext());
//                HashMap<String, String> hashMap = retrieveHash();
//                hashMap.clear();
//                SmartShutterActivity ss = new SmartShutterActivity();
//                saveHash(hashMap, getApplicationContext());
//                saveAlbum();
//                Toast.makeText(getApplicationContext(),
//                        "Album Reset Successful.",
//                        Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(
//                        getApplicationContext(),
//                        "Internal Error. Reset album failed",
//                        Toast.LENGTH_LONG).show();
            }
        }
    };

//    private HashMap<String, String> retrieveHash() {
//        SharedPreferences appPrefs = getSharedPreferences(FaceConstants.HASH_NAME, MODE_PRIVATE);
//        HashMap<String, String> hash = new HashMap<String, String>();
//        hash.putAll((Map<? extends String, ? extends String>) appPrefs.getAll());
//        return hash;
//    }

//    public static void saveimagereference(String bindobject, String entityid, Map<String, String> details){
//        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
//        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
//        ProfileImage profileImage = new ProfileImage(UUID.randomUUID().toString(),anmId,entityid,"jpeg",details.get("profilepic"), ImageRepository.TYPE_Unsynced,"dp");
//        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
//                kiclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
//    }

    public void resetAlbum() {

        Log.e(TAG, "resetAlbum: "+ "start" );
        boolean result = SmartShutterActivity.faceProc.resetAlbum();

        if (result){
            // Clear data
            // TODO: Null getApplication COntext
            HashMap<String, String> hashMap = SmartShutterActivity.retrieveHash(new ClientsList().getApplicationContext());
            hashMap.clear();
            saveHash(hashMap, cl.getApplicationContext());
            saveAlbum();

            Toast.makeText(cl.getApplicationContext(), "Reset Succesfully done!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(cl.getApplicationContext(), "Reset Failed!", Toast.LENGTH_LONG).show();

        }
        Log.e(TAG, "resetAlbum: "+ "finish" );
    }

    public static void saveStaticImageToDisk(String entityId, Bitmap image, String faceVector) {
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();

        if (image != null) {
            OutputStream os = null;
            try {

                if (entityId != null && !entityId.isEmpty()) {
                    final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId+".JPEG";

                    File outputFile = new File(absoluteFileName);
                    os = new FileOutputStream(outputFile);
                    Bitmap.CompressFormat compressFormat =  Bitmap.CompressFormat.JPEG;
                    if (compressFormat != null) {
                        image.compress(compressFormat, 100, os);
                    } else {
                        throw new IllegalArgumentException("Failed to save static image, could not retrieve image compression format from name "
                                + absoluteFileName);
                    }
                    // insert into the db local
                    ProfileImage profileImage= new ProfileImage();
                    profileImage.setImageid(UUID.randomUUID().toString());
                    profileImage.setAnmId(anmId);
                    profileImage.setEntityID(entityId);
                    profileImage.setContenttype("jpeg");
                    profileImage.setFilepath(absoluteFileName);
                    profileImage.setFilecategory("profilepic");
                    profileImage.setFilevector(faceVector);
                    profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                    ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
                    imageRepo.add(profileImage);
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to save static image to disk");
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                    }
                }
            }
        }
    }

    private ImageRepository imageRepo = null;

    public Tools(){
        Log.e(TAG, "Tools: 1" );
        imageRepo=(ImageRepository) org.ei.opensrp.Context.imageRepository();
    }

    public Tools(org.ei.opensrp.Context appContext){
        imageRepo=(ImageRepository)org.ei.opensrp.Context.imageRepository();
        Tools.appContext = appContext;
    }


    public void vector_findAllUnsaved() {
        hash = retrieveHash( appContext.applicationContext());

        try {
            List<ProfileImage> vectorImages = imageRepo.allVectorImages();
            for(int i = 0; i< vectorImages.size();i++){

                // save Hash
                hash.put(Integer.toString(i), vectorImages.get(i).getEntityID());
                saveHash(hash, appContext.applicationContext());

                parseSaveVector(vectorImages.get(i).getFilevector());

//                Toast.makeText(BidanApplication.getInstance(), i+1 +" to "+vectorImages.size()+" done", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            logError(TAG,e.getMessage());
        }

    }

    private void parseSaveVector(String filevector) {
        if (filevector != null ) {
            String[] res = filevector.substring(1, filevector.length() - 1).split(",");

            String[] newArr = Arrays.copyOfRange(res, 33, 332);

            Log.e(TAG, "parseSaveVector:  " + Arrays.toString(newArr));

        }


    }


//    String  DRISTHI_BASE_URL = appContext.applicationContext().configuration().dristhiBaseURL();
//    String url = DRISTHI_BASE_URL+"multimedia-file?anm-id=user28";
//
    public void setVectorfromAPI() {
//        Log.e(TAG, "setVectorfromAPI: "+ url );
    }
}
