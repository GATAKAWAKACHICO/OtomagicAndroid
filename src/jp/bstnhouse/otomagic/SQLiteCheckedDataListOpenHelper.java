package jp.bstnhouse.otomagic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteCheckedDataListOpenHelper extends SQLiteOpenHelper{
	
	public SQLiteCheckedDataListOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public void onCreate(SQLiteDatabase db){
		db.execSQL("create table checked_data_list(facebook_id text, name text);");
		Log.d("DBçÏê¨","çÏê¨");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}
}
