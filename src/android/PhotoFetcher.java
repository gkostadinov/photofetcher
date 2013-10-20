package com.phonegap.plugins.photofetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Log;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class PhotoFetcher extends CordovaPlugin {

    private static final String TAG = "PhotoFetcher";
    private static final int MAXTHUMBSIZE = 128;

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) {
        Log.d(TAG, "Plugin Called");

        if (action.equals("fetch")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONObject result = new JSONObject();
                        JSONArray photos = new JSONArray();
                        JSONObject photo = new JSONObject();
                        result.put("photos", photos);

                        Cursor cursor_external = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                        Cursor cursor_internal = getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                        
                        Log.e(TAG, "External cursor size: " + cursor_external.getCount());
                        Log.e(TAG, "Internal cursor size: " + cursor_internal.getCount());

                        Cursor cursor = new MergeCursor(new Cursor[] {
                            cursor_external,
                            cursor_internal
                        });

                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH);
                        int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT);

                        if (cursor.moveToFirst()) {
                            do {
                                int imageId = cursor.getInt(columnIndex);
                                String path = cursor.getString(dataColumn);
                                int width = cursor.getInt(widthColumn);
                                int height = cursor.getInt(heightColumn);

                                if (path != null && width > 200 && height > 200) {
                                    Log.e(TAG, "Data with image ID " + imageId + ": " + path + ", width: " + width + ", height: " + height);
                                    double ratio = width/(double)height;
                                    int newWidth = MAXTHUMBSIZE;
                                    int newHeight = (int)(newWidth/ratio);

                                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(
                                                        BitmapFactory.decodeFile(path),
                                                        newWidth,
                                                        newHeight);

                                    if (bitmap != null) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] byteArrayImage = baos.toByteArray();
                                        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

                                        photo.put("id", imageId);
                                        photo.put("path", path);
                                        photo.put("data", encodedImage);

                                        photos.put(photo);

                                        result.remove("photos");
                                        result.put("photos", photos);

                                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                                        pluginResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(pluginResult);

                                        photo = new JSONObject();
                                    }
                                }
                            }
                            while (cursor.moveToNext());

                            result.remove("photos");
                            result.put("done", true);
                            result.put("photos", photos);
                        } else {
                            result.put("error", "No photos found!");
                        }
                        cursor.close();

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                        pluginResult.setKeepCallback(false);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                    catch (JSONException ex) {
                        Log.e(TAG, ex.getMessage());
                        callbackContext.error(ex.getMessage());
                    }
                    catch (Exception ex) {
                        Log.e(TAG, ex.getStackTrace().toString());
                        Log.e(TAG, ex.getMessage());
                        callbackContext.error(ex.getMessage());
                    }
                }
            });

            return true;
        }

        return false;
    }

    private ContentResolver getContentResolver(){
        return cordova.getActivity().getContentResolver();
    }
}