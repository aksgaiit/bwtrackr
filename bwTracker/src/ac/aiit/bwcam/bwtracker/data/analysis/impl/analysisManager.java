package ac.aiit.bwcam.bwtracker.data.analysis.impl;

import java.util.ArrayList;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataReader;
import ac.aiit.bwcam.bwtracker.data.IBWRawStats;
import ac.aiit.bwcam.bwtracker.data.IRawdataVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IBWAnalysisReader;
import ac.aiit.bwcam.bwtracker.data.analysis.ILPFStats;
import ac.aiit.bwcam.bwtracker.data.analysis.ILPFStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.lpfStatsBase;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase.epocDuration;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import ac.aiit.bwdata.common.HandlerException;
import ac.aiit.bwdata.lpf.Butterworth;
import ac.aiit.bwdata.lpf.IBWDataFilter;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class analysisManager extends BWCacheHelperBase implements IBWAnalysisReader{
	
	//*TODO* some DI framework would better be used to switch LPF implementations, hopefully.

	protected analysisManager(Context context) {
		super(context, null);
	}
	
	protected Long _sessionId = null;
	
	public Long getSession(){
		return this._sessionId;
	}
	public static final analysisManager getInstance(Context context, long sessionId){
		analysisManager ret = new analysisManager(context);
		ret._sessionId = Long.valueOf(sessionId);
		return ret;
	}
	private SQLiteDatabase _db = null;
	private SQLiteDatabase getConnection(){
		if(null == this._db){
			this._db = this.getWritableDatabase();
		}
		return this._db;
	}
	
	private static class conv extends lpfStatsBase{
		protected conv(){
			super();
		}
		public static final conv getInstance(IBWRawStats bwstats, IBWDataFilter df) throws HandlerException{
			conv ret = new conv();
			ret._epoc = bwstats.getEpocTime();
			ret._second = bwstats.getSecond();
			ret._value = df.filter((double)bwstats.getValue());
			return ret;
		}
	}
	
	public void deleteLPFdata(){
		lpfcacheTable table = new lpfcacheTable(this.getConnection(), this.getSession());
		table.deleteRecords();
	}
	private static final long _buf_max_rows = 500;
	private class lpfcacheTable_buffered extends lpfcacheTable{
		private ArrayList<lpfcacheTable.rec_value> _cache;
		private long _limit;
		public lpfcacheTable_buffered(SQLiteDatabase conn, Long sessionId) {
			super(conn, sessionId);
			this.init();
		}
		private void init(){
			this._cache = new ArrayList<lpfcacheTable.rec_value>();
			this._limit = _buf_max_rows;
		}
		public synchronized void flush(){
			if(null != this._cache && !this._cache.isEmpty()){
				this.insert(this._cache);
				this.init();
			}
		}
		@Override
		public synchronized void insert(long epoc, long second, double value) {
			this._cache.add(new lpfcacheTable.rec_value(epoc, second, value));
			this._limit--;
			if(0  >= this._limit){
				this.flush();
			}
		}
	}
	@Override
	public int traverseLPFdata(IntervalCriteria criteria,
			ILPFStatsVisitor visitor) throws DataSourceException {
		long sessionId = criteria.getSessionId();
		final lpfcacheTable_buffered table = new lpfcacheTable_buffered(this.getConnection(), sessionId);
		try{
			if(!table.hasSessionData()){
				final ILPFStatsVisitor v = visitor;
				final IBWDataReader reader = new DBReader(this._context);
				final IBWDataFilter df = new Butterworth(512, 4.5);
				IRawdataVisitor irdv = new IRawdataVisitor() {
					@Override
					public boolean visit(long sessionId, IBWRawStats stat)
							throws VisitorAbortException, DataSourceException {
						try {
							conv c = conv.getInstance(stat, df);
							table.insert(c.getEpocTime(), c.getSecond(), c.getValue());
							return v.visit(sessionId, c);
						} catch (HandlerException e) {
							throw new DataSourceException(e);
						}
					}

					@Override
					public void onStart(long sessionId)
							throws VisitorAbortException, DataSourceException {
						v.onStart(sessionId);
					}

					@Override
					public boolean onEnd(long sessionId)
							throws VisitorAbortException, DataSourceException {
						table.flush();
						return v.onEnd(sessionId);
					}
				};
				return reader.traverseRawdata(criteria, irdv);
			}else{
				return table.traverseData(criteria, visitor);
			}
		}finally{
			table.closeWorkingStmt();
		}
	}
	public epocDuration getLPFdataDuration(long sessionId){
		lpfcacheTable table = new lpfcacheTable(this.getConnection(), sessionId);
		return table.getDuration();
	}
	private static class waveformDetection implements ILPFStatsVisitor{

		private IWaveformStatsVisitor _visitor = null;
		private Long _lastInflectionTime = null;
		private Double _lastInflectionValue = null;
		private ILPFStats _prevValue = null;
		private double _prevDelta = 0;
		private waveformCacheTable_buffered _table = null;
		public waveformDetection(IWaveformStatsVisitor visitor, waveformCacheTable_buffered table){
			super();
			this._visitor = visitor;
			this._table = table;
		}
		private static final Double exp_diff = 50.0;
		private static final Long exp_duration = 50L;
		private double getAccuracy(long duration, double distance){		
			return Math.sqrt(Math.pow(duration - exp_duration, 2) + Math.pow(distance - exp_diff, 2));
		}
		private static class wfelem extends waveformStatsBase{
			public wfelem(long epoc, long second, long duration, double distance){
				super();
				this._epoc = epoc;
				this._second = second;
				this._amplitude = distance / 2;
				this._waveLength = duration * 2.0;
			}
		}
		private boolean processStat(long sessionId, ILPFStats stat) throws VisitorAbortException, DataSourceException{
			wfelem elem =  new wfelem(stat.getEpocTime(), stat.getSecond()
					, stat.getEpocTime() - this._lastInflectionTime, stat.getValue() - this._lastInflectionValue);
			if(null != this._table
					&& 0 < this._table.insert(elem.getEpocTime(), elem.getSecond(), elem.getWaveLength(), elem.getAmplitude())){
				return this._visitor.visit(sessionId, elem);
			}
			return false;
		}
		@Override
		public boolean visit(long sessionId, ILPFStats stat)
				throws VisitorAbortException, DataSourceException {
			double delta = stat.getValue() - (null != this._prevValue ? this._prevValue.getValue()
					: (null != this._lastInflectionValue ? this._lastInflectionValue : 0));
			boolean ret = false;//*TODO* if we'd like to count the number of the processed filtered elements,
			///we might set this default value to true.
			if(0 > (delta * this._prevDelta)){//inflection point
				if(null != this._lastInflectionTime && null != this._lastInflectionValue){
//					double accuracy = this.getAccuracy(Math.abs(stat.getEpocTime() - this._lastInflectionTime)
//							, Math.abs(stat.getValue() - this._lastInflectionValue));
					//*TODO* if we'd like to catch events when this inflection starts,
					/// we need to use this._lastInflectionTimr for the 1st argument for wfelem constructor..
					ret = this.processStat(sessionId, this._prevValue);
				}
				this._lastInflectionTime = stat.getEpocTime();
				this._lastInflectionValue = stat.getValue();
			}
			this._prevValue = stat;
			if(0.0 != delta){
				this._prevDelta = delta;
			}
			return ret;
		}
		@Override
		public void onStart(long sessionId) throws VisitorAbortException,
				DataSourceException {
			this._visitor.onStart(sessionId);
		}
		@Override
		public boolean onEnd(long sessionId)  throws VisitorAbortException, DataSourceException{
			boolean ret = false;
			if(null != this._prevValue){
				ret = this.processStat(sessionId, this._prevValue);
			}
			this._table.flush();
			return this._visitor.onEnd(sessionId) || ret;
		}
		
	}
	public static class waveformCacheTable_buffered extends waveformCacheTable{
		private ArrayList<waveformCacheTable.rec_value> _cache = null;
		private long _rest;
		
		private void init(){
			this._cache = new ArrayList<waveformCacheTable.rec_value>();
			this._rest = _buf_max_rows;
		}
		public synchronized void flush(){
			if(null != this._cache && !this._cache.isEmpty()){
				this.insert(this._cache);
				this.init();
			}
		}

		public waveformCacheTable_buffered(SQLiteDatabase conn, Long sessionId) {
			super(conn, sessionId);
			this.init();
		}

		@Override
		public synchronized long insert(long epoc, long second, double waveLength,
				double amplitude) {
			this._cache.add(new rec_value(epoc,second,waveLength,amplitude));
			this._rest--;
			if(0 >= this._rest){
				this.flush();
			}
			return 1;
		}
		
	}
	protected waveformCacheTable_buffered getCacheTable(bwSessionCriteria criteria){
		return new waveformCacheTable_buffered(this.getConnection(),criteria.getSessionId());
	}
	@Override
	public int traverseWaveformDetection(IntervalCriteria criteria,
			IWaveformStatsVisitor visitor) throws DataSourceException {
		waveformCacheTable_buffered table = this.getCacheTable(criteria);
		try{
			if(!table.hasSessionData()){
				waveformDetection detector = new waveformDetection(visitor, table);
				return this.traverseLPFdata(criteria, detector);
			}else{
				return table.query(criteria, visitor);
			}
		}finally{
			table.closeWorkingStmt();
		}
	}
	public int traverseWaveformDetection(ByTimeCriteria criteria,
			IWaveformStatsVisitor visitor) throws DataSourceException{
		waveformCacheTable_buffered table = this.getCacheTable(criteria);
		return table.query(criteria, visitor);
	}
	public void deleteWaveformCache(){
		waveformCacheTable table = new waveformCacheTable(this.getConnection(), this.getSession());
		table.deleteRecords();
	}
	public epocDuration getWaveFormCacheDuration(long sessionId){
		waveformCacheTable table = new waveformCacheTable(this.getConnection(), sessionId);
		return table.getDuration();
	}
	
	public void addAmpElement(DistanceCriteria<Double, Double> criteria, double value){
		criteria.add(waveformCacheTable.newAmpDistanceElem(value));
	}
	public void addWlenElement(DistanceCriteria<Double, Double> criteria, double value){
		criteria.add(waveformCacheTable.newWLenDistanceElem(value));
	}

	@Override
	public int traverseWaveformDetection(
			DistanceCriteria<Double, Double> criteria,
			IWaveformStatsVisitor visitor) throws DataSourceException {
		waveformCacheTable table = this.getCacheTable(criteria);
		return table.query(criteria, visitor);
	}

}
