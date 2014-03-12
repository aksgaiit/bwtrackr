package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class rawwaveTable extends sessionMillisecTableHelperBase {
	public rawwaveTable(SQLiteDatabase conn) {
		super(conn);
	}

	public static final String table_name="rawwave";
	@Override
	public String getTableName() {
		return table_name;
	}

	private static final colInfo[] _cols = new colInfo[]{
			TableHelper.col_session,
			TableHelper.col_millisec,
			TableHelper.col_second,
			TableHelper.col_longValue
	};
	@Override
	public colInfo[] getColumns() {
		return _cols;
	}

	public void insert(long time, long value){
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, time);
		stmt.bindLong(3, ConverterUtil.getSecond(time));
		stmt.bindLong(4, value);
//		return stmt.executeInsert();
		stmt.execute();
	}
}
