package com.shiznatix.mediacomrade.android.tables;

import java.util.ArrayList;
import java.util.List;

import com.shiznatix.mediacomrade.android.entities.RandomShow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RandomShowsTable extends AbstractTables {
	static private final String LOG_TAG = "mc_RandomSettingsTable";
	
	protected static final String TABLE_NAME = "randomShows";
	
	protected static final String KEY_ID = "randomShowid";
	protected static final String KEY_TITLE = "title";
 
	public RandomShowsTable(Context context) {
		super(context);
	}
	
	public void addShow(String title) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, title);
		
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	public RandomShow getShow(int id) {
		SQLiteDatabase db = getReadableDatabase();
	 
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_TITLE }, KEY_ID+" = ?", new String[] { String.valueOf(id) }, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			RandomShow randomShow = new RandomShow(cursor.getInt(0), cursor.getString(1));
			
			db.close();
			cursor.close();
			
			return randomShow;
		}
		
		db.close();
		cursor.close();
		
	    return null;
	}
	
	public RandomShow getShowByTitle(String title) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_TITLE }, KEY_TITLE+" = ?", new String[] { title }, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			RandomShow randomShow = new RandomShow(cursor.getInt(0), cursor.getString(1));
			
			db.close();
			cursor.close();
			
			return randomShow;
		}
		
		db.close();
		cursor.close();
		
	    return null;
	}
	
	public List<RandomShow> getAllShows() {
		List<RandomShow> randomShowList = new ArrayList<RandomShow>();
		
		String selectQuery = "SELECT * FROM "+TABLE_NAME;
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				RandomShow randomShow = new RandomShow(cursor.getInt(0), cursor.getString(1));
				
				randomShowList.add(randomShow);
			}
			while (cursor.moveToNext());
		}
		
		db.close();
		cursor.close();
		
		return randomShowList;
	}
	
	public List<String> getAllShowTitles() {
		List<String> randomShowTitles = new ArrayList<String>();
		
		String selectQuery = "SELECT "+KEY_TITLE+" FROM "+TABLE_NAME;
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				randomShowTitles.add(cursor.getString(0));
			}
			while (cursor.moveToNext());
		}
		
		db.close();
		cursor.close();
		
		return randomShowTitles;
	}
	
	public void deleteShow(RandomShow randomShow) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID+" = ?", new String[] { String.valueOf(randomShow.randomShowid) });
		db.close();
		
		Log.i(LOG_TAG, "show deleted");
	}
}