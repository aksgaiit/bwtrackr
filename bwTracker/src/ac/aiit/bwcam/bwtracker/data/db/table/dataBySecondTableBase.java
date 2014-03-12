package ac.aiit.bwcam.bwtracker.data.db.table;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public abstract class dataBySecondTableBase extends sessionTableHelperBase {
	protected dataBySecondTableBase(SQLiteDatabase conn) {
		super(conn);
	}
	public dataBySecondTableBase(SQLiteDatabase conn, Long sessionId) {
		super(conn, sessionId);
	}
	private static final colInfo[] _cols = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_second,
		TableHelper.col_longValue
	};
	@Override
	public colInfo[] getColumns() {
		return _cols;
	}
	
	private static final colInfo[] _idxcols = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_second
	};
	
	@Override
	public colInfo[] getIndexColumns() {
		return _idxcols;
	}

	public void insert(long time, long value){
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, ConverterUtil.getSecond(time));
		stmt.bindLong(3, value);
//		return stmt.executeInsert();
		stmt.execute();
	}

}
