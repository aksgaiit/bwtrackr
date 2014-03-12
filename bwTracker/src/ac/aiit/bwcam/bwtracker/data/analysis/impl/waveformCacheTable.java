package ac.aiit.bwcam.bwtracker.data.analysis.impl;

import java.util.Collection;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria.sortOrder;
import ac.aiit.bwcam.bwtracker.data.query.elemDistance;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class waveformCacheTable extends sessionMillisecTableHelperBase {
	public waveformCacheTable(SQLiteDatabase conn, Long sessionId) {
		super(conn, sessionId);
	}

	public static final String table_name = "wformcache";

	@Override
	public String getTableName() {
		return table_name;
	}
	private static final colInfo col_wavelen = new colInfo("wlen", colType.DOUBLE);
	private static final colInfo col_amplitude = new colInfo("amp", colType.DOUBLE);
	private static final colInfo[] _cols = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_millisec,
		TableHelper.col_second,
		col_wavelen,
		col_amplitude
	};
	@Override
	public colInfo[] getColumns() {
		return _cols;
	}

	public long insert(long epoc, double waveLength, double amplitude){
		return this.insert(epoc, ConverterUtil.getSecond(epoc), waveLength, amplitude);
	}
	public long insert(long epoc, long second, double waveLength, double amplitude){
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, epoc);
		stmt.bindLong(3, second);
		stmt.bindDouble(4, waveLength);
		stmt.bindDouble(5, amplitude);
		return stmt.executeInsert();
	}
	public static class rec_value{
		public long _epoc;
		public long _second;
		public double _waveLength;
		public double _amplitude;
		public rec_value(long epoc, long second, double waveLength, double amplitude){
			super();
			this._epoc = epoc;
			this._second = second;
			this._waveLength = waveLength;
			this._amplitude = amplitude;
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
			.append(val._waveLength).append(",")
			.append(val._amplitude).append(" ");
		}
		this.executeSQL(sql.toString());
		
	}
	
	private static final String sql_fmt_traverse_data_select_base = "select " +
			col_wavelen.getName() + "," +
			col_amplitude.getName() + "," +
			TableHelper.col_millisec.getName() + "," +
			TableHelper.col_second.getName();
	private static final String sql_fmt_traverse_data_select_table =
			" from " + table_name + " where " + TableHelper.col_session.getName() + "=%d";
	private static final String sql_fmt_traverse_data = 
			sql_fmt_traverse_data_select_base + sql_fmt_traverse_data_select_table;
	private static class conv extends waveformStatsBase{
		public conv(Cursor cur){
			super();
			this._waveLength = cur.getDouble(0);
			this._amplitude = cur.getDouble(1);
			this._epoc = cur.getLong(2);
			this._second = cur.getLong(3);
		}
	}
	public int query(IntervalCriteria criteria, IWaveformStatsVisitor visitor) throws DataSourceException{
		long sessionId = criteria.getSessionId();
		StringBuilder sb = new StringBuilder(String.format(sql_fmt_traverse_data, sessionId));
		sb.append(getIntervalWhereClause(criteria));
		sb.append(getSQLFragment(criteria));
		Cursor cur = this.rawQuery(sb.toString(), null);
		int ret = 0;
		if(cur.moveToFirst()){
			try {
				do {
					conv c = new conv(cur);
					if(visitor.visit(sessionId, c)){
						ret ++;
					}
				} while (cur.moveToNext());
			} catch (VisitorAbortException e) {
			}
		}
		return ret;
	}
	public int query(ByTimeCriteria criteria, IWaveformStatsVisitor visitor)throws DataSourceException{
		long sessionId = criteria.getSessionId();
		StringBuilder sb = new StringBuilder(String.format(sql_fmt_traverse_data, sessionId));
		sb.append(String.format(" and "+ col_millisec +">=%d order by " + col_millisec + " asc limit 1",criteria.getTime()));
		Cursor cur = this.rawQuery(sb.toString(), null);
		int ret = 0;
		if(cur.moveToFirst()){
			try{
				do{
					conv c = new conv(cur);
					if(visitor.visit(sessionId, c)){
						ret++;
					}
				}while(cur.moveToNext());
			} catch (VisitorAbortException e) {
			}
		}
		return ret;
	}
	
	protected static String getDistancePowValue(DistanceCriteria<Double, Double> criteria){
		StringBuilder sb = new StringBuilder();
		boolean bfirst = true;
		for(elemDistance<Double> elem: criteria){
			if(bfirst){
				bfirst = false;
			}else{
				sb.append(" + ");
			}
			String diff = String.format("(%s - %f)", elem.getName(), elem.getValue().doubleValue());
			sb.append(String.format("(%s * %s)", diff, diff));
		}
		return sb.toString();
	}
	protected static String getDistanceWhereClause(DistanceCriteria<Double, Double> criteria, String distanceCol){
		StringBuilder sb = new StringBuilder();
		Double distance = criteria.getThreshold();
		if(!criteria.isEmpty()){
			sb.append(String.format(" and %f%s(",Math.pow(distance, 2),criteria.isInner() ? ">" : "<"));
			sb.append(null != distanceCol ? distanceCol : getDistancePowValue(criteria));
			sb.append(")");
		}
		return sb.toString();
	}
	public int query(DistanceCriteria<Double, Double> criteria, IWaveformStatsVisitor visitor) throws DataSourceException{
		long sessionId = criteria.getSessionId();
		StringBuilder sb = new StringBuilder(sql_fmt_traverse_data_select_base);
		sb.append(String.format(",(%s) as dpow", getDistancePowValue(criteria)));
		sb.append(String.format(sql_fmt_traverse_data_select_table, sessionId));
		sb.append(getDistanceWhereClause(criteria, "dpow"));
		if(null == criteria.getSortKey()){
			criteria.setSortKey("dpow");
		}
		if(null == criteria.getOrder()){
			criteria.setOrder(sortOrder.asc);
		}
		sb.append(getSQLFragment(criteria));
		
		Cursor cur = this.rawQuery(sb.toString(), null);
		int ret = 0;
		if(cur.moveToFirst()){
			try{
				do{
					conv c = new conv(cur);
					if(visitor.visit(sessionId, c)){
						ret++;
					}
				}while (cur.moveToNext());
			}catch(VisitorAbortException e){
			}
		}
		return ret;
	}
	public static final elemDistance<Double> newAmpDistanceElem(double value){
		return new elemDistance<Double>(col_amplitude.getName(), value);
	}
	public static final elemDistance<Double> newWLenDistanceElem(double value){
		return new elemDistance<Double>(col_wavelen.getName(), value);
	}
}
