package com.napontaratan.vibernate.database;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import com.napontaratan.vibernate.model.TimerSession;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Visual representation of the database
 * ---------------------------------------
 * | ID (INTEGER) |	 VibrateTimer (BLOB) |
 * |--------------|----------------------|
 * |	...		  |	     	...			 |
 * |--------------|----------------------|
 * 
 * Database tutorial:
 * http://hmkcode.com/android-simple-sqlite-database-tutorial/
 */

public class VibernateDB extends SQLiteOpenHelper {
	// TODO support timers switching on/off

	// Database Version
	private static final int DATABASE_VERSION = 2;
	// Database Name
	private static final String DATABASE_NAME = "VibrateTimerDB";
	
	private static VibernateDB vibrateTimerDBInstance = null;
	
	public static VibernateDB getInstance(Context context){
		// source: http://stackoverflow.com/questions/18147354/sqlite-connection-leaked-although-everything-closed?answertab=oldest#tab-top
		// ensures only one database helper will exist across the entire application's lifecycle
		if(vibrateTimerDBInstance == null)
			vibrateTimerDBInstance = new VibernateDB(context.getApplicationContext());
		return vibrateTimerDBInstance;
	}
	
	private VibernateDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);  
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_ALARM_TABLE = "CREATE TABLE alarms ( " +
				"id INTEGER PRIMARY KEY, " + 
				"alarm BLOB )";

		db.execSQL(CREATE_ALARM_TABLE);
		System.out.println("on create");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("on upgrade");
		db.execSQL("DROP TABLE IF EXISTS alarms");
		this.onCreate(db);
	}

	/* ============================================================================== */

	private static final String TABLE_NAME = "alarms";
	private static final String KEY_ID = "id";
	private static final String KEY_ALARM = "alarm";

	/**
	 * Add a VibrateTimer to the database based on the ID
	 * @author Napon, Paul, Amelia
	 */
	public void addToDB(TimerSession vt) {
		System.out.println("add to db");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			values.put(KEY_ID, vt.getId());
			values.put(KEY_ALARM, TimerSession.serialize(vt));
		} catch (Exception e) {
			System.out.println("IOException caught in addToDB()");
		}

		db.insert(TABLE_NAME, null, values);
		db.close(); 
	}

	/**
	 * Retrieve all VibrateTimer objects in the database
	 * @return List<VibrateTimer>
	 * @author Napon, Paul, Amelia
	 */
	public List<TimerSession> getAllVibrateTimers() {

		List<TimerSession> result = new LinkedList<TimerSession>();
		String query = "SELECT  * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		TimerSession vt = null;

		if (cursor.moveToFirst()) {
			do {
				try {
					vt = (TimerSession) TimerSession.deserialize(cursor.getBlob(1));
				} catch (ClassNotFoundException e) {
					Log.d("Exception", "ClassNotFoundException caught in getAllAlarmsFromDB()");
				} catch (IOException e) {
					Log.d("Exception", "IOException caught in getAllAlarmsFromDB()");
				}

				result.add(vt);
			} while (cursor.moveToNext());
		}
		return result;
	}

	/**
	 * Clears the database
	 * @author Napon, Paul, Amelia
	 */
	public void deleteAllFromDB(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
		db.close();
	}

	/**
	 * Remove a VibrateTimer entry from the database that matches the given id
	 * @param TimerSession vt
	 * @author Napon, Paul, Amelia
	 */
	public void remove(TimerSession vt) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID+" =?", new String[] { String.valueOf(vt.getId()) });
		db.close();
	}

	/**
	 * Return true if the given id already exists in the DB
	 * @param int id
	 * @author Napon
	 */
	public boolean contains(int id) {
		System.out.println("query");
		SQLiteDatabase sqldb = this.getWritableDatabase();
		String Query = "Select * from " + TABLE_NAME + " where " + KEY_ID + "=" + id;
		Cursor cursor = sqldb.rawQuery(Query, null);
		if(cursor.getCount()<=0){
			return false;
		}
		return true;
	}
}
