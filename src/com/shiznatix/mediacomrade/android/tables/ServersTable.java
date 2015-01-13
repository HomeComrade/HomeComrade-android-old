package com.shiznatix.mediacomrade.android.tables;

import java.util.ArrayList;
import java.util.List;

import com.shiznatix.mediacomrade.android.entities.Server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ServersTable extends AbstractTables {
	static private final String LOG_TAG = "mc_ServersTable";
	
	protected static final String TABLE_NAME = "servers";
	
	protected static final String KEY_ID = "serverid";
	protected static final String KEY_URL = "url";
	
	public ServersTable(Context context) {
		super(context);
	}
	
	public void addServer(String url) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_URL, url);
		
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	public Server getServer(int id) {
		SQLiteDatabase db = getReadableDatabase();
	 
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL }, KEY_ID+" = ?", new String[] { String.valueOf(id) }, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			Server server = new Server(cursor.getInt(0), cursor.getString(1));
			
			db.close();
			cursor.close();
			
			return server;
		}
		
		db.close();
		cursor.close();
		
	    return null;
	}
	
	public Server getServerByUrl(String url) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL }, KEY_URL+" = ?", new String[] { url }, null, null, null, null);
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			Server server = new Server(cursor.getInt(0), cursor.getString(1));
			
			db.close();
			cursor.close();
			
			return server;
		}
		
		db.close();
		cursor.close();
		
	    return null;
	}
	
	public List<Server> getAllServers() {
		List<Server> serverList = new ArrayList<Server>();
		
		String selectQuery = "SELECT * FROM "+TABLE_NAME;
		
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if (cursor.moveToFirst()) {
			do {
				Server contact = new Server(cursor.getInt(0), cursor.getString(1));
				
				serverList.add(contact);
			}
			while (cursor.moveToNext());
		}
		
		db.close();
		cursor.close();
		
		return serverList;
	}
	
	public void deleteServer(Server server) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID+" = ?", new String[] { String.valueOf(server.serverid) });
		db.close();
		
		Log.i(LOG_TAG, "server deleted");
	}
}