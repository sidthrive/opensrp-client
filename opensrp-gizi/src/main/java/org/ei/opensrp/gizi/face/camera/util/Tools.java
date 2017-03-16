package org.ei.opensrp.gizi.face.camera.util;

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

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.gizi.face.camera.SmartShutterActivity;
import org.ei.opensrp.repository.DetailsRepository;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wildan on 1/4/17.
 */
public class Tools {

    private static final String TAG = Tools.class.getSimpleName();
    public static final int CONFIDENCE_VALUE = 58;
    private static String bindobject;
    private Canvas canvas = null;

    public static boolean SavePictureToFile(android.content.Context context, Bitmap bitmap, String entityId) {
        for (int i = 0; i < 2; i++) {
            File pictureFile = getOutputMediaFile(i, entityId);

            if (pictureFile == null) {
                Log.e(TAG, "Error creating media file, check path permissions!");
                return false;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Log.e(TAG, "Wrote image to " + pictureFile);

                MediaScannerConnection.scanFile(context,
                        new String[]{pictureFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
                String photoPath = pictureFile.toString();
                Log.e(TAG, "Photo Path = " + photoPath);

//            Database
                DetailsRepository detailsRepository = Context.getInstance().detailsRepository();

                Long tsLong = System.currentTimeMillis()/1000;
                detailsRepository.add(entityId, "profilepic"+i, photoPath, tsLong);
//            kiclient.getDetails().get("profilepic");

                return true;

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            if(i == 1) {
                final int THUMBSIZE = FaceConstants.THUMBSIZE;
                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(""),
                        THUMBSIZE, THUMBSIZE);
            }


        }
        return false;
    }

    public static boolean WritePictureToFile(android.content.Context context, Bitmap bitmap, String entityId) {

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
            Log.e(TAG, "Wrote image to " + thumbs_photo);

//            TODO
//            saveimagereference(bindobject, entityId, details);
//            HashMap<String,String> details = new HashMap<String,String>();
//            details.put("profilepic",currentfile.getAbsolutePath());


            bindobject = "kartu_ibu";

            HashMap<String,String> details = new HashMap<>();

//            saveimagereference(bindobject, entityId, details);
//            details.put("profilepic", photoPath);
            details.put("profilepic", thumbs_photo.toString());


//            KIDetailActivity.details = new HashMap<>();
//            HashMap<String,String> details = new HashMap<>();
//            KIDetailActivity.details.put("profilepic",photoPath);

//            Database Stored
            DetailsRepository detailsRepository = Context.getInstance().detailsRepository();
            Long tsLong = System.currentTimeMillis()/1000;
//            detailsRepository.add(entityId, "profilepic", photoPath, tsLong);
            detailsRepository.add(entityId, "profilepic", thumbs_photo.toString(), tsLong);

            String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
//            ProfileImage profileImage = new ProfileImage(
//                    UUID.randomUUID().toString(),
//                    anmId,
//                    entityId,
//                    "Image",
//                    details.get("profilepic"),
//                    ImageRepository.TYPE_Unsynced,
//                    "dp",
//                    "[byte]"
//                    );
//            ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
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


        String imgFolder = (mode == 0) ? DrishtiApplication.getAppDir():
                DrishtiApplication.getAppDir()+File.separator+".thumbs";
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
        return new File(String.format("%s%sOSRP_%s.jpg", mediaStorageDir.getPath(), File.separator, entityId));
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
        }

        assert ca != null;
        ca.close();
        return null;

    }

    public static void drawInfo(Rect rect, Bitmap mutableBitmap, float pixelDensity, String personName) {
        Log.e(TAG, "drawInfo: " );
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
        Log.e(TAG, "drawInfo: " );
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
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);
        HashMap<String, String> hash = new HashMap<String, String>();
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

//    public static void saveimagereference(String bindobject,String entityid,Map<String,String> details){
//        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(entityid,details);
//        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
//        ProfileImage profileImage = new ProfileImage(
//                UUID.randomUUID().toString(),anmId,entityid,"Image",details.get("profilepic"), ImageRepository.TYPE_Unsynced,
//                "dp"
//        );
//        ((ImageRepository) Context.getInstance().imageRepository()).add(profileImage);
//                kiclient.entityId();
//        Toast.makeText(this,entityid,Toast.LENGTH_LONG).show();
//    }


}
