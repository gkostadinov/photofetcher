package com.phonegap.plugins.photofetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;

public class PhotoFetcher extends CordovaPlugin {

	private static final String TAG = "PhotoFetcher";

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
						JSONObject photos = new JSONObject();

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
						photos.put("external_size", cursor_external.getCount());
						photos.put("internal_size", cursor_internal.getCount());

						callbackContext.success(photos);
					}
					catch (JSONException ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                    catch (Exception ex) {
                        Log.e(TAG, ex.getStackTrace().toString());
                        Log.e(TAG, ex.getMessage());
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