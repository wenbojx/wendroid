package com.ece594bb.geopan;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataHelper {

   private static final String DATABASE_PATH = "/data/data/com.ece594bb.geopan/databases/";
   private static final String DATABASE_NAME = "geopan.db";
   private static final int DATABASE_VERSION = 1;
   private static final String TABLE_NAME = "picture_info"; 

   private Context context;
   private SQLiteDatabase db;

   private SQLiteStatement insertStmt;
   private static final String INSERT = "insert into "  + TABLE_NAME + 
   		"(project_name, lat, lon, file_path) values (?, ?, ?, ?)";

   public DataHelper(Context context) 
   {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
      this.insertStmt = this.db.compileStatement(INSERT);
   }

   public long insert(String project_name, String lat, String lon, String file_path) 
   {
      this.insertStmt.bindString(1, project_name);
      this.insertStmt.bindString(2, lat);
      this.insertStmt.bindString(3, lon);
      this.insertStmt.bindString(4, file_path);
      return this.insertStmt.executeInsert();
   }

   public void deleteAll() 
   {
      this.db.delete(TABLE_NAME, null, null);
   }

   public List<String> selectAll() 
   {
      List<String> list = new ArrayList<String>();
      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "project_name", "lat", "lon", "file_path" }, 
        null, null, null, null, null);
      if (cursor.moveToFirst()) {
         do {	        	 
            list.add(cursor.getString(0));
            list.add(cursor.getString(1)); 
            list.add(cursor.getString(2)); 
            list.add(cursor.getString(3)); 
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }
   
   
   public List<String> selectCoordinates() 
   {
      List<String> list = new ArrayList<String>();
      Cursor cursor = this.db.rawQuery("select lat, lon from " + TABLE_NAME + " where project_name like '%1%';", null);
      if (cursor.moveToFirst()) {
         do {	        	 
            list.add(cursor.getString(0));
            list.add(cursor.getString(1));  
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }
   
   public void deletePicture(String filename)
   {
	   this.db.delete(TABLE_NAME, "project_name = " + filename, null);
	   return;
   }

   private static class OpenHelper extends SQLiteOpenHelper 
   {

      OpenHelper(Context context) 
      {
    	  	super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT (0), project_name TEXT, lat TEXT, lon TEXT, file_path TEXT)");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }


   
	public void copyDataBase() throws IOException 
	{

		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DATABASE_PATH + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}
	
	

}
