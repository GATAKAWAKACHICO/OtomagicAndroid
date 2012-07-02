package jp.bstnhouse.otomagic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SQLitePlaylistOpenHelper extends SQLiteOpenHelper{
	public SQLitePlaylistOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table playlist(user_id text, user_name text, user_profile_img text, artist_name text, json text);");
		Log.d("DBçÏê¨","çÏê¨");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}
}
