package ac.aiit.bwcam.bwtracker.data.analysis.impl;

import java.util.Collection;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.analysis.ILPFStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.lpfStatsBase;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class lpfcacheTable extends sessionMillisecTableHelperBase {
	
	public lpfcacheTable(SQLiteDatabase conn, Long sessionId) {
		super(conn, sessionId);
	}

	public static final String table_name = "lpfcache";

	@Override
	public String getTableName() {
		return table_name;
	}
	private static final colInfo[] _cols = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_millisec,
		TableHelper.col_second,
		TableHelper.col_doubleValue
		};
	@Override
	public colInfo[] getColumns() {
		return _cols;
	}
	public void insert(long epoc, double value){
		this.insert(epoc, ConverterUtil.getSecond(epoc), value);
	}
	public void insert(long epoc, long second, double value){
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, epoc);
		stmt.bindLong(3, second);
		stmt.bindDouble(4, value);
//		return stmt.executeInsert();
		stmt.execute();
	}
	public static class rec_value{
		public long _epoc;
		public long _second;
		public double _value;
		public rec_value(long epoc, long second, double value){
			super();
			this._epoc = epoc;
			this._second = second;
			this._value = value;
		}
	}
	public void insert(Collection<rec_value> values){
		//they say 500 is the maximum number of the rows to be inserted in one time.
		StringBuilder sql = new StringBuilder("insert into ");
		sql.append(this.getTableName());
		boolean first = true;
		for(rec_value val : values){
			if(first){
				first = false;
			}else{
				sql.append(" union all");
			}
			sql.append(" select ")
			.append(this._sessionId).append(",")
			.append(val._epoc).append(",")
			.append(val._second).append(",")
			.append(val._value).append(" ");
		}
		this.executeSQL(sql.toString());
	}
	
	private static final String sql_fmt_traverse_data = 
			"select " + col_doubleValue.getName() + "," +
			col_millisec.getName() + "," +
			col_second.getName() + " from " + table_name + 
			" where " + col_session.getName() + "=%d";
	private static class conv extends lpfStatsBase{
		public conv(Cursor cur){
			super();
			this._value = cur.getDouble(0);
			this._epoc = cur.getLong(1);
			this._second = cur.getLong(2);
		}
	}
	public int traverseData(IntervalCriteria criteria, ILPFStatsVisitor visitor) throws DataSourceException{
		long sessionId = criteria.getSessionId();
		int ret = 0;
		try {
			visitor.onStart(sessionId);
			StringBuilder sb = new StringBuilder(String.format(
					sql_fmt_traverse_data, sessionId));
			sb.append(getIntervalWhereClause(criteria));
			sb.append(getSQLFragment(criteria));

			Cursor cur = this.rawQuery(sb.toString(), null);
			if (cur.moveToFirst()) {
				do {
					conv c = new conv(cur);
					if (visitor.visit(sessionId, c)) {
						ret++;
					}
				} while (cur.moveToNext());
			}
		} catch (VisitorAbortException e) {
		}finally{
			try {
				if(visitor.onEnd(sessionId)){
					ret++;
				}
			} catch (VisitorAbortException e) {
			}
		}
		return ret;
	}
}
