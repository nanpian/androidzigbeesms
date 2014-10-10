package com.rtk.bdtest.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

public class DbDeviceHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME="zigbee_db"; 
	private final static int DATABASE_VERSION=6;
	private final static String TABLE_NAME="zigbee_device"; 
	public final static String DEVICE_ID="_id";
	public final static String DEVICE_BID = "bindid";
	public final static String DEVICE_SORTID="sort_id";
	private static final String DEVICE_NAME = "name";
	private static final String DEVICE_ADDRESS_ = "address";
	private static final String DEVICE_TYPE = "type";
	private static final String DEVICE_PADDRESS = "parentaddress";

	public DbDeviceHelper(Context context ) {
		super(context, DATABASE_NAME,null, DATABASE_VERSION); 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql="Create table "+TABLE_NAME+"("
				                               +DEVICE_ID+" integer primary key autoincrement," 
				                               +DEVICE_SORTID+" integer ," 
				                               +DEVICE_NAME+" text," 
				                               +DEVICE_BID+" text ," 
				                               +DEVICE_ADDRESS_+" text ," 
				                               +DEVICE_TYPE+" integer ," 
				                               +DEVICE_PADDRESS+" text);"; 
		db.execSQL(sql); 
	}
	
	public Cursor select() { 
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null, null, null, null, null, " _id desc");
		return cursor;
	} 
	
	public Cursor select(String name) { 
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null,  DEVICE_NAME+ "=?", new String[]{String.valueOf(name)}, null, null, " _id desc");
		return cursor;
	} 

	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		// TODO Auto-generated method stub
		String sql=" DROP TABLE IF EXISTS "+TABLE_NAME; db.execSQL(sql); onCreate(db); 
		
	} 
	
	public void update(String name, String bindid) {
		SQLiteDatabase db=this.getWritableDatabase(); String where=DEVICE_NAME+"=?";
		String[] whereValue={name}; ContentValues cv=new ContentValues(); 
		cv.put(DEVICE_BID,  bindid); 
		db.update(TABLE_NAME, cv, where, whereValue); 
		}
	
	public long insert(String name, String bindid, String type ,String address, String parentaddress)
	{ 
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(DEVICE_NAME, name); 
		cv.put(DEVICE_BID,bindid); 		
		cv.put(DEVICE_TYPE, type); 		
		cv.put(DEVICE_ADDRESS_, address); 	
		cv.put(DEVICE_PADDRESS, parentaddress); 	
		long row=db.insert(TABLE_NAME, null, cv); 
		return row; 
		}
	
	public void delete(String name) { 
		SQLiteDatabase db=this.getWritableDatabase();
		String where=DEVICE_NAME+"=?";
		String[] whereValue={name}; db.delete(TABLE_NAME, where, whereValue); }

}
