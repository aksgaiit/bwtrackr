package ac.aiit.bwcam.bwtracker.data.db.table;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public abstract class dataByMillisecTableBase extends sessionMillisecTableHelperBase {
	public dataByMillisecTableBase(SQLiteDatabase conn) {
		super(conn);
	}

	private static final colInfo[] _cols = new colInfo[] {
			TableHelper.col_session, TableHelper.col_millisec,
			TableHelper.col_longValue };
	@Override
	public colInfo[] getColumns() {
		return _cols;
	}


	public void insert(long time, long value) {
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, time);
		stmt.bindLong(3, value);
		// return stmt.executeInsert();
		stmt.execute();
	}

}
