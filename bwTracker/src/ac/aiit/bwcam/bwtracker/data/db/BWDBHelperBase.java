package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.data.db.table.attTable;
import ac.aiit.bwcam.bwtracker.data.db.table.blinkTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BWDBHelperBase extends SQLiteOpenHelper {
	protected Context _context = null;

	protected BWDBHelperBase(Context context, CursorFactory cf) {
		super(context, DBNAME_BW, cf, DBVERSION_CUR);
		this._context = context;
	}
	public static final String DBNAME_BW = "bwtrack";
	public static final int DBVERSION_CUR= 3;

	private void createTables(SQLiteDatabase db){
		(new rawwaveTable(db)).createTable();
		(new sigqualTable(db)).createTable();
		(new attTable(db)).createTable();
		(new medTable(db)).createTable();
		(new blinkTable(db)).createTable();
		(new eventTable(db)).createTable();
		(new LocationTable(db)).createTable();
	}
	private void dropTables(SQLiteDatabase db){
		(new rawwaveTable(db)).dropTable();
		(new sigqualTable(db)).dropTable();
		(new attTable(db)).dropTable();
		(new medTable(db)).dropTable();
		(new blinkTable(db)).dropTable();
		(new eventTable(db)).dropTable();
		(new LocationTable(db)).dropTable();
	}	
	@Override
	public void onCreate(SQLiteDatabase db) {
		this.createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(2 > oldVersion){
			this.dropTables(db);
		}
		this.createTables(db);
	}


}
