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
import com.napontaratan.vibernate.model.TimerUtils;

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
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
	public void addToDB(TimerSession timerSession) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			values.put(KEY_ID, timerSession.getId());
			values.put(KEY_ALARM, TimerSession.serialize(timerSession));
		} catch (Exception e) {
			e.printStackTrace();
		}

		db.insert(TABLE_NAME, null, values);
		db.close(); 
	}

	public void updateTimerInDB(TimerSession timerSession) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			values.put(KEY_ALARM, TimerSession.serialize(timerSession));
		} catch (Exception e) {
		    e.printStackTrace();
		}

		db.update (TABLE_NAME, values, "id="+timerSession.getId(), null);
		db.close();
	}

	/**
	 * Retrieve all VibrateTimer objects in the database
	 * @return List<VibrateTimer>
	 * @author Napon, Paul, Amelia
	 */
	public List<TimerSession> getAllTimers() {

		List<TimerSession> result = new LinkedList<TimerSession>();
		String query = "SELECT  * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		TimerSession timerSession = null;

		if (cursor.moveToFirst()) {
			do {
				try {
					timerSession = (TimerSession) TimerSession.deserialize(cursor.getBlob(1));
					result.add(timerSession);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
    					db.delete(TABLE_NAME, null, null);
					break;
				} catch (IOException e) {
					e.printStackTrace();
				} 
			} while (cursor.moveToNext());
		}
		return result;
	}

	/**
	 * Remove a VibrateTimer entry from the database that matches the given id
	 * @param TimerSession timerSession
	 * @author Napon, Paul, Amelia
	 */
	public void remove(TimerSession timerSession) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID+" =?", new String[] { String.valueOf(timerSession.getId()) });
		db.close();
	}

	/**
	 * Return true if the given id already exists in the DB
	 * @param int id
	 * @author Napon
	 */
	public boolean contains(int id) {
		SQLiteDatabase sqldb = this.getWritableDatabase();
		String Query = "Select * from " + TABLE_NAME + " where " + KEY_ID + "=" + id;
		Cursor cursor = sqldb.rawQuery(Query, null);
		return cursor.getCount() > 0;
	}

}
