package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataReader;
import ac.aiit.bwcam.bwtracker.data.IDurationVisitor;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalGroupStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.IRawdataVisitor;
import ac.aiit.bwcam.bwtracker.data.StatsValueBase;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.attTable;
import ac.aiit.bwcam.bwtracker.data.impl.BWMentalGroupStatsBase;
import ac.aiit.bwcam.bwtracker.data.impl.BWMentalStatsBase;
import ac.aiit.bwcam.bwtracker.data.impl.BWRawStatsBase;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ModerationCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBReader extends BWDBHelperBase implements IBWDataReader {

	public DBReader(Context context) {
		super(context, null);
	}

	public int getDurations(bwSessionCriteria criteria, IDurationVisitor visitor) throws DataSourceException{
		SQLiteDatabase conn = null;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				LocationTable lt = new LocationTable(conn);
				return lt.getDurations(criteria, visitor);
			}else{
				return 0;
			}
		}finally{
			if(null != conn){
				conn.close();
			}
		}
	}
	@Override
	public int searchLocations(bwSessionCriteria criteria, ILocationVisitor visitor)
			throws DataSourceException {
		SQLiteDatabase conn = null;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				LocationTable lt = new LocationTable(conn);
				return lt.searchLocations(criteria, visitor);
			}else{
				return 0;
			}
		}finally{
			if(null != conn){
				conn.close();
			}
		}
	}
	public int searchLocations(ByTimeCriteria criteria, ILocationVisitor visitor) throws DataSourceException{
		SQLiteDatabase conn = null;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				LocationTable lt = new LocationTable(conn);
				return lt.searchLocations(criteria, visitor);
			}else{
				return 0;
			}
		}finally{
			if(null != conn){
				conn.close();
			}
		}
	}

	protected static final String SQL_SELECT_MENTALSTATS_BASE
	= "select " +
			"a." + attTable.col_longValue + 
			",m." + medTable.col_longValue +
			",a." + TableHelper.col_second +
			" from " + attTable.table_name + " as a" + 
			" left join " + medTable.table_name + " as m" +
			" on (a." + TableHelper.col_session + "=m." + TableHelper.col_session +
			" and a." + TableHelper.col_second + "=m." + TableHelper.col_second +
			") where a." + TableHelper.col_session + "='%d'";
	@Override
	public int searchMentalStats(ModerationCriteria criteria,
			IMentalStatsVisitor visitor) throws DataSourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	protected static class curMentalStats extends BWMentalStatsBase{
		public curMentalStats(long sec){
			super(sec);
		}

		public curMentalStats(Cursor cur){
			super(cur.getLong(2));
			this._att = cur.getLong(0);
			this._med = cur.getLong(1);
		}
		public void setResult(Cursor cur) {
			this._att = cur.getLong(0);
			this._med = cur.getLong(1);
		}
		public void setSecond(long sec){
			this._setSecond(sec);
		}
	}
	private static final String SQL_SELECT_MENTALSTATS_BYTIME
	= SQL_SELECT_MENTALSTATS_BASE
	+ " and a.second='%d'";
	@Override
	public int searchMentalStats(ByTimeCriteria criteria, IMentalStatsVisitor visitor)throws DataSourceException{
		SQLiteDatabase conn = null;
		int ret = 0;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				long sessionId = criteria.getSessionId();
				long second = criteria.getTime();
				second -= (second % 1000);
				Cursor cur = conn.rawQuery(String.format(SQL_SELECT_MENTALSTATS_BYTIME
						, sessionId, second), null);
				if(cur.moveToFirst()){
					do{
						curMentalStats stat = new curMentalStats(cur);
						stat.setSecond(criteria.getTime());
						if(visitor.visit(criteria.getSessionId(), stat)){
							ret ++;
						}
					}while(cur.moveToNext());
				}
			}
		} catch (VisitorAbortException e) {
		}finally{
			if(null != conn){
				conn.close();
			}
		}
		return ret;
	}
	private static final String SQL_SELECT_MENTALSTATS_TABLEANDWHERECLAUSE_BASE = 
			" from " + attTable.table_name + " as a" +
			" left join " + medTable.table_name + " as m" +
			" on (a." + TableHelper.col_session + "=m." + TableHelper.col_session +
			" and a." + TableHelper.col_second + "=m." + TableHelper.col_second +
			") where a." + TableHelper.col_session + "='%d'" + 
			" and a." + TableHelper.col_second + ">='%d'" +
			" and a." + TableHelper.col_second + "<='%d'";

	private static final String SQL_SELECT_MENTALSTATS_DURATION
	= "select " +
			"avg(a." + TableHelper.col_longValue + ")" + 
			",avg(m." + TableHelper.col_longValue + ")" +
			SQL_SELECT_MENTALSTATS_TABLEANDWHERECLAUSE_BASE;
	@Override
	public int searchMentalStats(IntervalCriteria criteria,
			IMentalStatsVisitor visitor) throws DataSourceException {
		SQLiteDatabase conn = null;
		int ret = 0;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				long sessionId = criteria.getSessionId();
				Cursor cur = conn.rawQuery(String.format(SQL_SELECT_MENTALSTATS_DURATION
						, sessionId, criteria.getStartTime(), criteria.getEndTime()), null);
				if(cur.moveToFirst()){
					do{
						curMentalStats stat = new curMentalStats(criteria.getStartTime());
						stat.setResult(cur);
						if(visitor.visit(criteria.getSessionId(), stat)){
							ret ++;
						}
					}while(cur.moveToNext());
				}
			}
		} catch (VisitorAbortException e) {
		}finally{
			if(null != conn){
				conn.close();
			}
		}
		return ret;
	}
	private static final String SQL_SELECT_MENTALGROUPSTATS_DURATION
	= "select " +
			"avg(a." + TableHelper.col_longValue + ")" + 
			",avg(m." + TableHelper.col_longValue + ")" +
			",max(a." + TableHelper.col_longValue + ")" + 
			",max(m." + TableHelper.col_longValue + ")" +
			",min(a." + TableHelper.col_longValue + ")" + 
			",min(m." + TableHelper.col_longValue + ")" +
			",count(a." + TableHelper.col_longValue + ")" + 
			",count(m." + TableHelper.col_longValue + ")" +
//			",total(a." + TableHelper.col_longValue + ")" + 
//			",total(m." + TableHelper.col_longValue + ")" +
			SQL_SELECT_MENTALSTATS_TABLEANDWHERECLAUSE_BASE;
	private static class curMentalGroupStats extends BWMentalGroupStatsBase{
		public curMentalGroupStats(long start, long end){
			super(start, end);
		}
		public void setResult(Cursor cur) {
			this._att = new StatsValueBase<Long>(
					cur.getDouble(0),
					cur.getInt(6),
					cur.getLong(2),
					cur.getLong(4)
					);
			this._med = new StatsValueBase<Long>(
					cur.getDouble(1),
					cur.getInt(7),
					cur.getLong(3),
					cur.getLong(5));
		}
	}
	@Override
	public int searchMentalStats(IntervalCriteria criteria,
			IMentalGroupStatsVisitor visitor) throws DataSourceException {
		SQLiteDatabase conn = null;
		int ret = 0;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				long sessionId = criteria.getSessionId();
				long start = criteria.getStartTime();
				long end = criteria.getEndTime();
				Cursor cur = conn.rawQuery(String.format(SQL_SELECT_MENTALGROUPSTATS_DURATION
						, sessionId, start, end), null);
				if(cur.moveToFirst()){
					do{
						curMentalGroupStats stat = new curMentalGroupStats(start, end);
						stat.setResult(cur);
						if(visitor.visit(criteria.getSessionId(), stat)){
							ret ++;
						}
					}while(cur.moveToNext());
				}
			}
		} catch (VisitorAbortException e) {
		}finally{
			if(null != conn){
				conn.close();
			}
		}
		return ret;
	}
	
	private static final String sql_get_raw_data = "select "
			+ rawwaveTable.col_longValue 
			+ ", " + rawwaveTable.col_second
			+ ", " + rawwaveTable.col_millisec
			+ " from " + rawwaveTable.table_name
			+ " where " + rawwaveTable.col_session + "='%d'";
	private static final String sqlwhere_rawwave_start=
			" and "+ rawwaveTable.col_millisec + " >= '%d'";
	private static final String sqlwhere_rawwave_end=
			" and " + rawwaveTable.col_millisec + " < '%d'";
	private static final String sqlopt_limit = 
			" limit %d";
	private static final String sqlopt_offset =
			" offset %d";
	private static class curRawStats extends BWRawStatsBase{
		public curRawStats(Cursor cur){
			super();
			this._value = cur.getLong(0);
			this._second = cur.getLong(1);
			this._epoc = cur.getLong(2);
		}
	}
	@Override
	public int traverseRawdata(IntervalCriteria criteria,
			IRawdataVisitor visitor) throws DataSourceException {
		SQLiteDatabase conn = null;
		int ret = 0;
		long sessionId = criteria.getSessionId();
		try{
			visitor.onStart(sessionId);
			conn = this.getReadableDatabase();
			if(null != conn){
				StringBuilder sb = new StringBuilder(String.format(sql_get_raw_data, sessionId));
				Long start = criteria.getStartTime();
				if(null != start){
					sb.append(String.format(sqlwhere_rawwave_start, start));
				}
				Long end = criteria.getEndTime();
				if(null != end){
					sb.append(String.format(sqlwhere_rawwave_end, end));
				}
				Long limit = criteria.getLimit();
				if(null != limit){
					sb.append(String.format(sqlopt_limit, limit));
				}
				Long offset = criteria.getOffset();
				if(null != offset){
					sb.append(String.format(sqlopt_offset, offset));
				}
				Cursor cur = conn.rawQuery(sb.toString(), null);
				if(cur.moveToFirst()){
					do{
						curRawStats stat = new curRawStats(cur);
						if(visitor.visit(criteria.getSessionId(), stat)){
							ret ++;
						}
					}while(cur.moveToNext());//&&ret < 10000);
				}
			}
		} catch (VisitorAbortException e) {
		}finally{
			try {
				if(visitor.onEnd(sessionId)){
					ret++;
				}
			} catch (VisitorAbortException e) {
			}
			if(null != conn){
				conn.close();
			}
		}
		return ret;
	}

}
