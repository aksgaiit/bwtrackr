package ac.aiit.bwcam.bwtracker.data.db.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class sessionMillisecTableHelperBase extends
		sessionTableHelperBase {

	protected sessionMillisecTableHelperBase(SQLiteDatabase conn) {
		super(conn);
	}

	protected sessionMillisecTableHelperBase(SQLiteDatabase conn, Long sessionId) {
		super(conn, sessionId);
	}
	private static final colInfo[] _idxcols = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_millisec
	};
	@Override
	public colInfo[] getIndexColumns() {
		return _idxcols;
	}
	public static class epocDuration{
		private Long _start = null;
		private Long _end = null;
		private epocDuration(Long start, Long end){
			super();
			this._start = start;
			this._end = end;
		}
		private epocDuration(Cursor cursor){
			super();
			this._end = cursor.getLong(0);
			this._start = cursor.getLong(1);
		}
		public Long getStart(){
			return this._start;
		}
		public long getEnd(){
			return this._end;
		}
	}
	private static final String sql_fmt_get_duration =
			"select max(" + TableHelper.col_millisec.getName() + "), min(" + TableHelper.col_millisec
			+ ") from %s where " + TableHelper.col_session.getName() + "=%d";
	public epocDuration getDuration(){
		Cursor cur = this.rawQuery(String.format(sql_fmt_get_duration
				, this.getTableName(), this._sessionId), null);
		epocDuration ret = null;
		if(cur.moveToFirst()){
			ret = new epocDuration(cur);
			if(ret._start == 0 || ret._end == 0){//returns 0 in result set when no entry matches with the criteria...
				ret = null;
			}
		}
		return ret;
	}


}
