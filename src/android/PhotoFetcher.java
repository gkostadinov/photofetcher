package com.phonegap.plugins.photofetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;

public class PhotoFetcher extends CordovaPlugin {

    private static final String TAG = "PhotoFetcher";
    private static final int MAXTHUMBSIZE = 256;

    /*
     * (non-Javadoc)
     * 
     * @see com.phonegap.api.Plugin#execute(java.lang.String,
     * org.json.JSONArray, java.lang.String)
     */
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

                        String[] projection = { MediaStore.Images.Thumbnails._ID };

                        Cursor cursor_external = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                                projection,
                                null,
                                null,
                                null);
                        Cursor cursor_internal = getContentResolver().query(MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI,
                                projection,
                                null,
                                null,
                                null);
                        
                        Log.e(TAG, "External cursor size: " + cursor_external.getCount());
                        Log.e(TAG, "Internal cursor size: " + cursor_internal.getCount());

                        Cursor cursor = new MergeCursor(new Cursor[] {
                            cursor_external,
                            cursor_internal
                        });
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
                        int size = cursor.getCount();

                        if (size != 0) {
                            int imageID = 0;
                            Bitmap bitmap = null;
                            Bitmap newBitmap = null;
                            Uri uri = null;

                            for (int i = 0; i < size; i++) {
                                cursor.moveToPosition(i);
                                imageID = cursor.getInt(columnIndex);

                                try {
                                    uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI, "" + imageID);
                                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                                }
                                catch (Exception ex) {
                                    uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID);
                                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                                }

                                if (bitmap != null) {
                                    newBitmap = ThumbnailUtils.extractThumbnail(
                                        bitmap, MAXTHUMBSIZE, MAXTHUMBSIZE
                                    );
                                    
                                    bitmap.recycle();
                                    if (newBitmap != null) {
                                        photo.put("id", imageID);
                                        photo.put("uri", uri);
                                        photo.put("bitmap", newBitmap);

                                        photos.put(photo);

                                        photo = new JSONObject();
                                    }                        
                                }
                            }

                            result.put("photos", photos);
                        } else {
                            result.put("error", "No photos found!");
                        }
                        cursor.close();

                        callbackContext.success(result);
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