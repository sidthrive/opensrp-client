package org.ei.opensrp.indonesia.face.camera.util;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import org.apache.commons.lang3.ArrayUtils;
import org.ei.opensrp.Context;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.indonesia.face.camera.ClientsList;
import org.ei.opensrp.indonesia.face.camera.SmartShutterActivity;
import org.ei.opensrp.indonesia.face.camera.util.FaceConstants;
import org.ei.opensrp.indonesia.child.AnakDetailActivity;
import org.ei.opensrp.indonesia.face.camera.ClientsList;
import org.ei.opensrp.indonesia.face.camera.ImageConfirmation;
import org.ei.opensrp.indonesia.face.camera.SmartShutterActivity;
//import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.indonesia.kartu_ibu.KIDetailActivity;
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

/**
 * Created by wildan on 1/4/17.
 */
public class Tools {

    private static final String TAG = Tools.class.getSimpleName();
    public static final int CONFIDENCE_VALUE = 58;
    private static String bindobject;
    private static ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
    public static Context appContext;
    private static HashMap<String, String> hash;
    private static ProfileImage profileImage = new ProfileImage();
    private static String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();

    private Canvas canvas = null;
    SmartShutterActivity ss = new SmartShutterActivity();
    ClientsList cl = new ClientsList();
    private static String photoPath;

    static String emptyAlbum = "[32, 0, 0, 0, 76, 65, -68, -20, 77, 116, 46, 83, 105, 110, 97, 105, 6, 0, 0, 0, -24, 3, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0]";
    private static String headerOne = emptyAlbum;
    static String singleHeader = "[76, 1, 0, 0, 76, 65, -68, -20, 77, 116, 46, 83, 105, 110, 97, 105, 6, 0, 0, 0, -24, 3, 0, 0, 10, 0, 0, 0, 1, 0, 0, 0]";


    public Tools(Context context) {
        appContext = context;
    }

    public Tools() {
    }

    public static boolean WritePictureToFile(Bitmap bitmap, String entityId, String[] faceVector, boolean updated, String className) {

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

            String photoPath = pictureFile.toString();
            Log.e(TAG, "Photo Path = " + photoPath);

//            Create Thumbs
            FileOutputStream tfos = new FileOutputStream(thumbs_photo);
            final int THUMBSIZE = FaceConstants.THUMBSIZE;

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoPath),
                    THUMBSIZE, THUMBSIZE);
            ThumbImage.compress(Bitmap.CompressFormat.PNG, 100, tfos);
            tfos.close();
            Log.e(TAG, "Wrote Thumbs image to " + thumbs_photo);

//           FIXME File & Database Stored
//            saveStaticImageToDisk(entityId, ThumbImage, Arrays.toString(faceVector), updated);

            saveToDb(entityId, thumbs_photo.toString(), Arrays.toString(faceVector), updated, className);

            return true;

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return false;
    }

    public static void saveStaticImageToDisk(String entityId, Bitmap image, String contentVector, boolean updated) {
//        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();

        String[] res = contentVector.substring(1, contentVector.length() - 1).split(",");

        String[] faceVector = Arrays.copyOfRange(res, 32, 332);

        if (image != null) {
            OutputStream os = null;
            try {

                if (entityId != null && !entityId.isEmpty()) {

                    String folder_main = "SIDCR";

                    File f = new File(Environment.getExternalStorageDirectory(), folder_main);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

//                    final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";
                    final String absoluteFileName = f.toString() + File.separator + entityId + ".JPEG";

                    File outputFile = new File(absoluteFileName);
                    os = new FileOutputStream(outputFile);
                    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                    if (compressFormat != null) {
                        image.compress(compressFormat, 100, os);
                    } else {
                        throw new IllegalArgumentException(
                                "Failed to save static image, could not retrieve image compression format from name "
                                        + absoluteFileName);
                    }

                    // insert into the db local
                    ProfileImage profileImage = new ProfileImage();

                    profileImage.setImageid(entityId);
                    profileImage.setAnmId(anmId);
                    profileImage.setEntityID(entityId);
                    profileImage.setContenttype("jpeg");
                    profileImage.setFilepath(absoluteFileName);
                    profileImage.setFilecategory("profilepic");
                    profileImage.setFilevector(Arrays.toString(faceVector));
                    profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);

                    ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
                    imageRepo.add(profileImage, entityId);
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

    private static File getOutputMediaFile(Integer mode, String entityId) {
        // Mode 0 = Original
        // Mode 1 = Thumbs

        // Location use app_dir
        String imgFolder = (mode == 0) ? DrishtiApplication.getAppDir() :
                DrishtiApplication.getAppDir() + File.separator + ".thumbs";
//        String imgFolder = (mode == 0) ? "OPENSRP_SID":"OPENSRP_SID"+File.separator+".thumbs";
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), imgFolder);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            Log.e(TAG, "failed to find directory " + mediaStorageDir.getAbsolutePath());
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory " + mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String filename = entity);
        return new File(String.format("%s%s%s.jpg", mediaStorageDir.getPath(), File.separator, entityId));
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        assert ca != null;
        ca.close();
        return null;

    }

    public static void drawInfo(Rect rect, Bitmap mutableBitmap, float pixelDensity, String personName) {
        Log.e(TAG, "drawInfo: ");
//        Rect rect = faceDatas[i].rect;
        // Extra padding around the faeRects
        rect.set(rect.left -= 20, rect.top -= 20, rect.right += 20, rect.bottom += 20);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paintForRectFill = new Paint(); // Draw rect
        // fill
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
        Log.e(TAG, "drawInfo: ");
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

    }

    public static void setPhotoPath(String photoPath) {
        Tools.photoPath = photoPath;
    }

    public static String getPhotoPath() {
        return photoPath;
    }

    /**
     * Stored list detected Base entity ID to Shared Preference for buffered
     *
     * @param hashMap HashMap
     * @param context context
     */
    public static void saveHash(HashMap<String, String> hashMap, android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
//        Log.e(TAG, "Hash Save Size = " + hashMap.size());
        for (String s : hashMap.keySet()) {
//            Log.e(TAG, "saveHash: " + s);
            editor.putString(s, hashMap.get(s));
        }
        editor.apply();
    }


    public static HashMap<String, String> retrieveHash(android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.putAll((Map<? extends String, ? extends String>) settings.getAll());
        return hash;
    }

    /**
     * Save Vector array to xml
     */
    public static void saveAlbum(String albumBuffer, android.content.Context context) {
        Log.e(TAG, "saveAlbum: " );
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(FaceConstants.ALBUM_ARRAY, albumBuffer);
        editor.apply();
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

    public static void alertDialog(android.content.Context context, int opt) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        Tools tools = new Tools();
//        alertDialog.setMessage(message);
        String message = "";
        switch (opt) {
            case 0:
                message = "Are you sure to empty The Album?";
//                doEmpty;
                break;
            case 1:
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
    private static void saveToDb(String entityId, String absoluteFileName, String faceVector, boolean updated, String className) {

        Log.e(TAG, "saveToDb: " + "start");
        // insert into the db local
        if (!updated) {
//            profileImage.setImageid(anmId);
//            profileImage.setAnmId(anmId);
//            profileImage.setEntityID(entityId);
//            profileImage.setContenttype("jpeg");
//            profileImage.setFilepath(absoluteFileName);
//            profileImage.setFilecategory("profilepic");
//            profileImage.setFilevector(faceVector);
//            profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
//
//            imageRepo.add(profileImage, entityId);

            // insert into the db local
            ProfileImage profileImage = new ProfileImage();

            profileImage.setImageid(entityId);
            profileImage.setAnmId(anmId);
            profileImage.setEntityID(entityId);
            profileImage.setContenttype("jpeg");
            profileImage.setFilepath(absoluteFileName);
            profileImage.setFilecategory("profilepic");
            profileImage.setFilevector(faceVector);
            profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);

            ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
            imageRepo.add(profileImage, entityId);

            // insert into details
            Map<String, String> details = new HashMap<>();
            details.put("profilepic", absoluteFileName);
            if (className.equals(KIDetailActivity.class.getSimpleName())) {
                bindobject = "kartu_ibu";
            } else {
                bindobject = "anak";
            }
            Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityId, details);


        } else {
            imageRepo.updateByEntityId(entityId, faceVector);
        }
        Log.e(TAG, "saveToDb: " + "done");

    }

    public static void saveimagereference(String bindobject, String entityid, Map<String, String> details) {
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid, details);
    }

//    public void resetAlbum() {
//
//        Log.e(TAG, "resetAlbum: " + "start");
//        boolean result = SmartShutterActivity.faceProc.resetAlbum();
//
//        if (result) {
//            // Clear data
//            // TODO: Null getApplication COntext
//            HashMap<String, String> hashMap = SmartShutterActivity.retrieveHash(new ClientsList().getApplicationContext());
//            hashMap.clear();
//            saveHash(hashMap, cl.getApplicationContext());
//            saveAlbum();
//
//            Toast.makeText(cl.getApplicationContext(), "Reset Succesfully done!", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(cl.getApplicationContext(), "Reset Failed!", Toast.LENGTH_LONG).show();
//
//        }
//        Log.e(TAG, "resetAlbum: " + "finish");
//    }

    public static void setVectorsBuffered() {

        List<ProfileImage> vectorList = imageRepo.getAllVectorImages();

        if (vectorList.size() != 0) {

            hash = retrieveHash(appContext.applicationContext().getApplicationContext());

            String[] albumBuffered = new String[]{};

            int i = 0;
            for (ProfileImage profileImage : vectorList) {
                String[] vectorFace;
                if (profileImage.getFilevector() != null) {

                    vectorFace = profileImage.getFilevector().substring(1, profileImage.getFilevector().length() - 1).split(", ");
                    vectorFace[0] = String.valueOf(i);

                    albumBuffered = ArrayUtils.addAll(albumBuffered, vectorFace);
                    hash.put(profileImage.getEntityID(), String.valueOf(i));

                } else {
                    Log.e(TAG, "setVectorsBuffered: Profile Image Null");
                }
                i++;

            }

            albumBuffered = ArrayUtils.addAll(getHeaderBaseUserCount(vectorList.size()), albumBuffered);

            saveAlbum(Arrays.toString(albumBuffered), appContext.applicationContext());
            saveHash(hash, appContext.applicationContext());

        } else {
            Log.e(TAG, "setVectorsBuffered: " + "Multimedia Table Not ready");
        }

    }

    private static String[] getHeaderBaseUserCount(int n) {
//        String headerNew = imageRepo.findByUserCount(n);
// start formula
        int n0 = 76;
//        int seriesLength = 63; // 64 item
        int max = 128;
        int min = -128;

        int range = max - min;

        int idx0, idx1, idx2, idx3, idx4;


        idx0 = (((n0 + max) + (n * 44)) % range) + min;

        idx1 = (1 + n) + (((n0) + (n * 44)) / range);
        idx2 = (idx1 + 128) % 256 - 128;

        idx3 = n / 218;
        idx4 = (1 + n + 128) % 256 - 128;

        String[] newHeader = singleHeader.substring(1, singleHeader.length() - 1).split(", ");

        newHeader[0] = String.valueOf(idx0);
        newHeader[1] = String.valueOf(idx2);
        newHeader[2] = String.valueOf(idx3);
        newHeader[28] = String.valueOf(idx4);

        return newHeader;
// end formula
//        return headerNew.substring(1, headerNew.length() -1).split(", ");
    }

    /**
     * Save to Buffer from Local DB
     *
     * @param context
     */

    public static void saveAndClose(

            android.content.Context context,
            String entityId,
            boolean updated,
            FacialProcessing objFace,
            int arrayPossition,
            Bitmap storedBitmap,
            String className) {

        byte[] faceVector;

        if (!updated) {

            int result = objFace.addPerson(arrayPossition);
            faceVector = objFace.serializeRecogntionAlbum();

            saveAlbum(Arrays.toString(faceVector), context);

            String albumBufferArr = Arrays.toString(faceVector);

            String[] faceVectorContent = albumBufferArr.substring(1, albumBufferArr.length() - 1).split(", ");


            // Get Face Vector Contnt Only by removing Header
            faceVectorContent = Arrays.copyOfRange(faceVectorContent, faceVector.length - 300, faceVector.length);

            boolean savedFile = WritePictureToFile(storedBitmap, entityId, faceVectorContent, updated, className);

            if (savedFile){
                hash = retrieveHash(context);

                hash.put(entityId, Integer.toString(result));

                // Save Hash
                saveHash(hash, context);
            }

            // Reset Album to get Single Face Vector

        } else {

            int update_result = objFace.updatePerson(Integer.parseInt(hash.get(entityId)), 0);

            if (update_result == 0) {

                Log.e(TAG, "saveAndClose: " + "success");

            } else {

                Log.e(TAG, "saveAndClose: " + "Maximum Reached Limit for Face");

            }

            faceVector = objFace.serializeRecogntionAlbum();

            // TODO : update only face vector
            saveAlbum(Arrays.toString(faceVector), context);
        }

        new ImageConfirmation().finish();

        Class<?> origin_class = null;

        if (className.equals(KIDetailActivity.class.getSimpleName())) {
            origin_class = KIDetailActivity.class;
        }
//        else if(className.equals(KBDetailActivity.class.getSimpleName())){
//            origin_class = KBDetailActivity.class;
//        } else if(className.equals(ANCDetailActivity.class.getSimpleName())){
//            origin_class = ANCDetailActivity.class;
//        } else if(className.equals(PNCDetailActivity.class.getSimpleName())){
//            origin_class = PNCDetailActivity.class;
//        }
        else if (className.equals(AnakDetailActivity.class.getSimpleName())) {
            origin_class = AnakDetailActivity.class;
        }

        // TODO Crash saved after long time no use
        if (appContext == null) {
        }
        Intent resultIntent = new Intent(appContext.applicationContext(), origin_class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.applicationContext().startActivity(resultIntent);

        Log.e(TAG, "saveAndClose: " + "end");
    }

}
