package com.shiznatix.mediacomrade.android.tables;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AbstractTables extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "mediaRemote";
	
	private static final String CREATE_SERVER_TABLE = "CREATE TABLE "+ServersTable.TABLE_NAME+"("
		+ServersTable.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ServersTable.KEY_URL+" TEXT )";
	
	private static final String CREATE_RANDOM_SHOWS_TABLE = "CREATE TABLE "+RandomShowsTable.TABLE_NAME+"("
			+RandomShowsTable.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+RandomShowsTable.KEY_TITLE+" TEXT )";
	
	private static final String CREATE_DIRECTORIES_TABLE = "CREATE TABLE "+LastDirectoryTable.TABLE_NAME+"("
			+LastDirectoryTable.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+LastDirectoryTable.KEY_URL+" TEXT,"
			+LastDirectoryTable.KEY_DIRECTORY+" TEXT )";
	
	public AbstractTables(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SERVER_TABLE);
		db.execSQL(CREATE_RANDOM_SHOWS_TABLE);
		db.execSQL(CREATE_DIRECTORIES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
