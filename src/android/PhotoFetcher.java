package com.phonegap.plugins.photofetcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.provider.MediaStore;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class PhotoFetcher extends Plugin {

	private static final String TAG = "PhotoFetcher";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.phonegap.api.Plugin#execute(java.lang.String,
	 * org.json.JSONArray, java.lang.String)
	 */
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		Log.d(TAG, "Plugin Called");
		PluginResult result = null;
		JSONObject photos = new JSONObject();

        String[] projection = {MediaStore.Images.Thumbnails._ID};

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
		photos.put("external_size", cursor_external.getCount())
		photos.put("internal_size", cursor_internal.getCount())

		result = new PluginResult(Status.OK, photos)
		return result;
	}
}