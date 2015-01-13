package com.shiznatix.mediacomrade.android.tables;

import com.shiznatix.mediacomrade.android.entities.LastDirectory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LastDirectoryTable extends AbstractTables {
	static private final String LOG_TAG = "mc_LastDirectoryTable";
	
	protected static final String TABLE_NAME = "lastDirectory";
	
	protected static final String KEY_ID = "lastDirectoryid";
	protected static final String KEY_URL = "url";
	protected static final String KEY_DIRECTORY = "directory";
	
	public LastDirectoryTable(Context context) {
		super(context);
	}
	
	public void saveLastDirectory(LastDirectory lastDirectory) {
		ContentValues values = new ContentValues();
		values.put(KEY_URL, lastDirectory.url);
		values.put(KEY_DIRECTORY, lastDirectory.directory);
		
		LastDirectory checkLastDirectory = getLastDirectoryByServer(lastDirectory.url);
		
		SQLiteDatabase db = getWritableDatabase();
		
		if (null == checkLastDirectory) {
			Log.i(LOG_TAG, "inserting new directory: "+lastDirectory.url);
			db.insert(TABLE_NAME, null, values);
		}
		else {
			Log.i(LOG_TAG, "updating directory: "+lastDirectory.url);
			db.update(TABLE_NAME, values, null, null);
		}
		
		db.close();
	}
	
	public LastDirectory getLastDirectoryByServer(String url) {
		SQLiteDatabase db = getReadableDatabase();
	 
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL, KEY_DIRECTORY }, KEY_URL+" = ?", new String[] { url }, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			LastDirectory lastDirectory = new LastDirectory(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
			
			db.close();
			cursor.close();
			
			return lastDirectory;
		}
		
		db.close();
		cursor.close();
		
	    return null;
	}
}