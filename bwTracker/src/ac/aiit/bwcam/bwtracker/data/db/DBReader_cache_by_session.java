package ac.aiit.bwcam.bwtracker.data.db;

import java.util.TreeMap;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;
import ac.aiit.bwcam.bwtracker.data.IMentalGroupStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.StatsValueBase;
import ac.aiit.bwcam.bwtracker.data.impl.BWMentalGroupStatsBase;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ModerationCriteria;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBReader_cache_by_session extends DBReader {
	
	private TreeMap<Long, IBWMentalStats> _mentalStats = null;
	protected long _sessionId;

	public DBReader_cache_by_session(Context context, long sessionId) {
		super(context);
		this._sessionId = sessionId;
	}

	protected synchronized TreeMap<Long, IBWMentalStats> getMenatlStats(){
		if(null == this._mentalStats){
			this._mentalStats  = new TreeMap<Long, IBWMentalStats>();
			SQLiteDatabase conn = null;
			try {
				conn = this.getReadableDatabase();
				if (null != conn) {
					Cursor cur = conn
							.rawQuery(String.format(
									SQL_SELECT_MENTALSTATS_BASE,
									this._sessionId), null);
					if (cur.moveToFirst()) {
						do {
							curMentalStats stat = new curMentalStats(cur);
							_mentalStats.put(stat.getSecond(), stat);
						} while (cur.moveToNext());
					}
				}
			}finally {
				if (null != conn) {
					conn.close();//*TODO* SQLiteDatabase close in activity raises execptin in subsequent query
				}
			}
		}
		return this._mentalStats;
	}
	@Override
	public int searchMentalStats(ModerationCriteria criteria,
			IMentalStatsVisitor visitor) throws DataSourceException {
		if(this._sessionId != criteria.getSessionId()){
			throw new DataSourceException("invalid session id");
		}
		//*TODO*	no implementation in parent class..... what was the intention...	
		return super.searchMentalStats(criteria, visitor);
	}

	@Override
	public int searchMentalStats(ByTimeCriteria criteria,
			IMentalStatsVisitor visitor) throws DataSourceException {
		IBWMentalStats ms = this.getMenatlStats().get(criteria.getTime());
		try {
			if(null != ms && visitor.visit(this._sessionId, ms)){
				return 1;
			}
		} catch (VisitorAbortException e) {
		}
		return 0;
	}

	@Override
	public int searchMentalStats(IntervalCriteria criteria,
			IMentalStatsVisitor visitor) throws DataSourceException {
		int ret = 0;
		try {
			for (IBWMentalStats ms : this.getMenatlStats()
					.headMap(criteria.getEndTime() + 1)
					.tailMap(criteria.getStartTime() - 1).values()) {
				if (visitor.visit(this._sessionId, ms)) {
					ret++;
				}
			}
		} catch (VisitorAbortException e) {
		}
		return ret;
	}

	@Override
	public int searchMentalStats(IntervalCriteria criteria,
			IMentalGroupStatsVisitor visitor) throws DataSourceException {
		try{
			int count = 0;
			long total_a = 0L, total_m = 0L;
			long max_a = 0L, min_a = 0L, max_m = 0L, min_m = 0L;
			
			for(IBWMentalStats ms : this.getMenatlStats()
					.headMap(criteria.getEndTime() + 1)
					.tailMap(criteria.getStartTime() - 1).values()){
				count ++;
				long att = ms.getAttention();
				long med = ms.getMeditation();
				total_a += att;
				total_m += med;
				max_a = Math.max(max_a, att);
				min_a = Math.min(min_a, att);
				max_m = Math.max(max_m, med);
				min_m = Math.min(min_m, med);
			}
			if(0 < count && visitor.visit(this._sessionId
					, new BWMentalGroupStatsBase(
							criteria.getStartTime(), criteria.getEndTime()
							, new StatsValueBase<Long>((double)total_a / count, count, max_a, min_a)
							, new StatsValueBase<Long>((double)total_m / count, count, max_m, min_m)))){
				return 1;
			}
		} catch (VisitorAbortException e) {
		}finally{
			
		}
		return 0;
	}

}
