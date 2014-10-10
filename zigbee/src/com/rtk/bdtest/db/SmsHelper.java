package com.rtk.bdtest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsHelper extends SQLiteOpenHelper {
	//短信数据库 初期版本
	private final static String DATABASE_NAME="zigbee_db2"; 
	private final static int DATABASE_VERSION=6;
	private final static String TABLE_NAME="zigbee_sms"; 
	public final static String SMS_ID="_id2";
	private static final String SMS_NAME = "name";
	private static final String SMS_TIME = "time";
	private static final String  SMS_TEXT = "message";
	private static final String   SMS_SEND = "issend";

	public SmsHelper(Context context ) {
		super(context, DATABASE_NAME,null, DATABASE_VERSION); 
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql="Create table "+TABLE_NAME+"("
				                               +SMS_ID+" integer primary key autoincrement," 
				                               +SMS_NAME+" text ," 
				                               +SMS_TIME+" text," 
				                               +SMS_SEND+" text," 
				                               +SMS_TEXT+" text );"; 
		db.execSQL(sql); 
	}
	public Cursor select() { 
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null, null, null, null, null, " _id2 desc");
		return cursor;
	} 
	
	public Cursor select(String name) { 
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null, SMS_NAME + "=?", new String[]{String.valueOf(name)}, null, null, " _id2 desc");
		return cursor;
	} 
	
	public Cursor selectsend(String issend) { 
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null, SMS_SEND + "=?", new String[]{String.valueOf(issend)}, null, null, " _id2 desc");
		return cursor;
	} 
	
	public Cursor select(String name , String issend) {
		SQLiteDatabase db=this.getReadableDatabase(); 
		Cursor cursor=db.query(TABLE_NAME, null, SMS_NAME + "=?"+ " AND " + SMS_SEND +"=?", new String[]{String.valueOf(name),issend}, null, null, " _id2 desc");
		return cursor;
	}
	
	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		// TODO Auto-generated method stub
		String sql=" DROP TABLE IF EXISTS "+TABLE_NAME; db.execSQL(sql); onCreate(db); 
		
	} 
	
	public long insert(String name, String time, String message,String issend)
	{ 
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(SMS_NAME, name); 
		cv.put(SMS_TIME,time); 		
		cv.put(SMS_TEXT, message); 	
		cv.put(SMS_SEND, issend);
		long row=db.insert(TABLE_NAME, null, cv); 
		return row; 
		}
	
	public void delete(String name) { 
		SQLiteDatabase db=this.getWritableDatabase();
		String where=SMS_NAME+"=?";
		String[] whereValue={name}; db.delete(TABLE_NAME, where, whereValue); }
	
}
