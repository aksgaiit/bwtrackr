package ac.aiit.bwcam.bwtracker.data.db;

import java.util.ArrayList;

import ac.aiit.bwcam.bwtracker.data.IBWDataWriter;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionTableHelperBase;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public abstract class BWDBWriterBase extends BWDBHelperBase  implements IBWDataWriter{
	private Long _sessionId = null;

	protected BWDBWriterBase(Context context, CursorFactory cf) {
		super(context, cf);
	}
	private ArrayList<sessionTableHelperBase> _tableManaged = new ArrayList<sessionTableHelperBase>();

	protected void initiateTable(sessionTableHelperBase helper){
		helper.setSessionId(this._sessionId);
		this._tableManaged.add(helper);
	}
	private SQLiteDatabase _db = null;
	protected SQLiteDatabase getConnection(){
		if(null == this._db){
			this._db = this.getWritableDatabase();
		}
		return this._db;
	}
	public Long startSession(){
		SQLiteDatabase db = this.getConnection();
		db.beginTransaction();
		this._sessionId = System.currentTimeMillis();
		return this._sessionId;
	}
	@Override
	public Long getSession() {
		return this._sessionId;
	}
	public void finalize(Boolean successful){
		this._sessionId = null;
		for(TableHelper helper: this._tableManaged){
			helper.closeWorkingStmt();
		}
		this._tableManaged.clear();
		if(null != this._db){
			if(successful){
				this._db.setTransactionSuccessful();
			}
			this._db.endTransaction();
			this._db.close();
			this._db = null;
		}
	}

}
