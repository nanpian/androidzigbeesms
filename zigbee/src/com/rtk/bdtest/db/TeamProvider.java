/**
 * 
 */ 
package com.rtk.bdtest.db; 
/** 
 * @author 作者 E-mail: zdwxxx@qq.com
 * @version 创建时间：2014-12-8 下午3:40:16 
 * 类说明 
 */

import com.rtk.bdtest.util.Device;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class TeamProvider extends ContentProvider {
	private DbPersonHelper mOpenHelper;
	private final static String DATABASE_NAME = "zigbee_db4";
	private final static int DATABASE_VERSION = 6;
	private final static String Tag = "TeamProvider";

	public final static String PERSON_ID = "_id";
	private static final String PERSON_NAME = "name";
	public final static String PERSON_BID = "id";
	public final static String PERSON_TYPE = "typep";
	private static final String PERSON_RANK = "rank";
	private static final String PERSON_JOB = "job";
	public final static String PERSON_YEAR = "year";
	public final static String PERSON_SEX = "sex";
	private static final String PERSON_BEIZHU = "beizhu";
	private static final String DEVICE_ADDRESS_ = "address";
	private static final String DEVICE_TYPE = "device_type";
	private static final String DEVICE_PADDRESS = "parentaddress";
	private static final String  DEVICE_BANJI ="banji";
	private static final String DEVICE_STATUS = "status";
	// 授权“域名”,必须唯一，且与AndroidManifest里面注册的须一致
	private final static String AUTHORITY = "Personxxx";
	private static final UriMatcher URI_MATCHER;
	private final static String TABLE_NAME = "zigbee_team";
	private static final int PERSONS = 1;
	private static final int PERSON = 2;
	private SQLiteDatabase db;
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/zigbee_team");  
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		// 先将各项注册进去，才能在后面用到后进行匹配
		URI_MATCHER.addURI(AUTHORITY, TABLE_NAME, PERSONS);
		URI_MATCHER.addURI(AUTHORITY, TABLE_NAME + "/#", PERSON);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		db = mOpenHelper.getWritableDatabase();
		int num = 0;
		switch (URI_MATCHER.match(uri)) {
		case PERSONS:
			num = db.delete(TABLE_NAME, selection, selectionArgs);
			break;
		case PERSON:
			long id = ContentUris.parseId(uri);
			String where = "id=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and" + selection;
			}
			num = db.delete(TABLE_NAME, where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri:" + uri);
		}
	    getContext().getContentResolver().notifyChange(uri, null);
		return num;
	}
   /*
	public void delete(Uri uri, String name) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String where = PERSON_NAME + "=?";
		String[] whereValue = { name };
		db.delete(TABLE_NAME, where, whereValue);
	}*/

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (URI_MATCHER.match(uri)) {
		case PERSONS:// //数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
			return "vnd.android.cursor.dir/personprovider.person";
		case PERSON:// 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
			return "vnd.android.cursor.item/personprovider.person";
		default:
			throw new IllegalArgumentException("Unknow Uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		db = mOpenHelper.getWritableDatabase();
		long rowid;
		switch (URI_MATCHER.match(uri)) {
		case PERSONS: // 向表中添加新纪录并返回其行号
			Log.i(Tag,"insert into persons");
			rowid = db.insert(TABLE_NAME, null, values);
		    getContext().getContentResolver().notifyChange(uri, null);
			return ContentUris.withAppendedId(uri, rowid);
		default:
			throw new IllegalArgumentException("Unknow Uri:" + uri);
		}
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.w("pdb", "run here");
		// 这块搞了我大半天，所有东西都搞定了，就是表建不出来，原来SqliteHelper的OnCreate()方法不会被自动调用，只有使用getReadableDatabase()后才会新建　　　　
		// 表
		mOpenHelper = new DbPersonHelper(getContext());
		mOpenHelper.getReadableDatabase();
		mOpenHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		db = mOpenHelper.getReadableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case PERSONS:
			return db.query(TABLE_NAME, projection, selection, selectionArgs,
					null, null, "_id desc");
		case PERSON:
			long id = ContentUris.parseId(uri);
			String where = "id=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and" + selection;
			}
			return db.query(TABLE_NAME, projection, where, selectionArgs, null,
					null, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown Uri:" + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		db = mOpenHelper.getWritableDatabase();
		int num;
		Log.i(Tag,"update ittttttt!!!");
		switch (URI_MATCHER.match(uri)) {
		case PERSONS:
			num = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case PERSON:
			long id = ContentUris.parseId(uri);
			String where = "id=" + id;
			if (selection != null && !"".equals(selection)) {
				where = where + " and" + selection;
			}
			num = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknow Uri" + uri);
		}
	    getContext().getContentResolver().notifyChange(uri, null);
		return num;
	}

	public class DbPersonHelper extends SQLiteOpenHelper {

		public DbPersonHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			String sql = "Create table " + TABLE_NAME + "(" + PERSON_ID
					+ " integer primary key autoincrement," + PERSON_NAME
					+ " text," + PERSON_BID + " VARCHAR UNIQUE," + PERSON_TYPE
					+ " text," + PERSON_RANK + " text ," + PERSON_JOB
					+ " text," + PERSON_YEAR + " text ," + PERSON_SEX
					+ " text," + PERSON_BEIZHU + " text ," + DEVICE_BANJI
					+ " text," + DEVICE_STATUS + " text ," + DEVICE_ADDRESS_
					+ " text ," + DEVICE_TYPE + " integer ," + DEVICE_PADDRESS
					+ " text);";
			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			// TODO Auto-generated method stub
			String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
			db.execSQL(sql);
			onCreate(db);
		}

		public Cursor select() {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
					" _id desc");
			return cursor;
		}

		public Cursor select(String name) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_NAME, null, PERSON_NAME + "=?",
					new String[] { String.valueOf(name) }, null, null,
					" _id desc");
			return cursor;
		}
		
		public Cursor select(String id,String xx) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_NAME, null, PERSON_ID + "=?",
					new String[] { String.valueOf(id) }, null, null,
					" _id desc");
			return cursor;
		}

		public long insert(String name, String bindid, String type,
				String address, String parentaddress) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(PERSON_NAME, name);
			cv.put(PERSON_BID, bindid);
			cv.put(DEVICE_TYPE, type);
			cv.put(DEVICE_ADDRESS_, address);
			cv.put(DEVICE_PADDRESS, parentaddress);
			long row = db.insert(TABLE_NAME, null, cv);
			return row;
		}

		public void insert(Device user) {// 插入数据
			SQLiteDatabase sdb = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(PERSON_NAME, user.getDeviceName());
			values.put(PERSON_BID, user.getDeviceID());
			values.put(PERSON_TYPE, user.getDeviceName());
			values.put(PERSON_RANK, user.getDeviceID());
			values.put(PERSON_JOB, user.getDeviceName());
			values.put(PERSON_YEAR, user.getDeviceID());
			values.put(PERSON_SEX, user.getDeviceName());
			values.put(PERSON_BEIZHU, user.getDeviceID());
			sdb.insert(TABLE_NAME, null, values);
			sdb.close();
		}

		public void update(Device user, int id) {// 更新数据
			SQLiteDatabase sdb = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(PERSON_NAME, user.getDeviceName());
			values.put(PERSON_BID, user.getDeviceID());
			values.put(PERSON_TYPE, user.getDeviceName());
			values.put(PERSON_RANK, user.getDeviceID());
			values.put(PERSON_JOB, user.getDeviceName());
			values.put(PERSON_YEAR, user.getDeviceID());
			values.put(PERSON_SEX, user.getDeviceName());
			values.put(PERSON_BEIZHU, user.getDeviceID());
			sdb.update("user", values, "id=?",
					new String[] { String.valueOf(id) });
			sdb.close();
		}

		public void update(String name, String bindid) {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = PERSON_NAME + "=?";
			String[] whereValue = { name };
			ContentValues cv = new ContentValues();
			cv.put(PERSON_BID, bindid);
			db.update(TABLE_NAME, cv, where, whereValue);
		}

		public void delete(String name) {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = PERSON_NAME + "=?";
			String[] whereValue = { name };
			db.delete(TABLE_NAME, where, whereValue);
		}

	}
}

 