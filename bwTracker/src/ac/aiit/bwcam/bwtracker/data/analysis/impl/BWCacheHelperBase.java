package ac.aiit.bwcam.bwtracker.data.analysis.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BWCacheHelperBase extends SQLiteOpenHelper {
	public static final String DBNAME_Cache = "bwcache";
	public static final int DBVERSION_CUR= 2;
	
	protected Context _context = null;

	protected BWCacheHelperBase(Context context, 
			CursorFactory factory) {
		super(context, DBNAME_Cache, factory, DBVERSION_CUR);
		this._context = context;
	}

	private void createTables(SQLiteDatabase db){
		(new waveformCacheTable(db,0L)).createTable();
		(new lpfcacheTable(db,0L)).createTable();
	}
	private void dropTables(SQLiteDatabase db){
		(new waveformCacheTable(db,0L)).dropTable();
		(new lpfcacheTable(db,0L)).dropTable();
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		this.createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(1 > oldVersion){
			this.dropTables(db);
		}
		this.createTables(db);
	}

}
