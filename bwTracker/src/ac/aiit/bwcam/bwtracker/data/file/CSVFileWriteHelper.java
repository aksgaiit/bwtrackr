package ac.aiit.bwcam.bwtracker.data.file;

import java.io.IOException;
import java.util.Date;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.EventType;
import ac.aiit.bwcam.bwtracker.data.IBWDataWriter;
import android.location.Location;

public class CSVFileWriteHelper implements IBWDataWriter{
	protected String _folderPath = null;
	protected Long _sessionId = null;
	public CSVFileWriteHelper(String basepath){
		super();
		this._folderPath = basepath;
	}
	protected String getBasePath() throws DataSourceException{
		if(null != this._sessionId){
			return String.format("%s/bwt_%d_", this._folderPath, this._sessionId);
		}else{
			throw new DataSourceException("seeeion id is not specified");
		}
	}
	private static final String _dtfmtTwoNum = "%d,%d" + System.getProperty("line.separator");
	private static final String _dtfmtThreeNum = "%d,%d,%d" + System.getProperty("line.separator");
	
	private static abstract class helperBasedFileDataSource extends fileDataSource{
		CSVFileWriteHelper _parent = null;
		public helperBasedFileDataSource(CSVFileWriteHelper parent){
			super();
			this._parent = parent;
		}
		protected abstract String getSubPath();
		@Override
		protected String _createPath() throws DataSourceException {
			return this._parent.getBasePath() + this.getSubPath();
		}
		
	}
	
	private static class attWriter extends helperBasedFileDataSource{
		public attWriter(CSVFileWriteHelper parent) {
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException{
			try {
				this._writer.write(String.format(_dtfmtTwoNum
						, ConverterUtil.getSecond(time)
						, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "att.csv";
		}

	}
	private attWriter _att = null;
	public void insertAttData(long time, long value) throws DataSourceException{
		if(null == this._sessionId){
			return;
		}
		if(null == this._att){
			this._att = new attWriter(this);
			this._att.open();
		}
		this._att.insert(time, value);
	}
	
	private static class blinkWriter extends helperBasedFileDataSource{
		public blinkWriter(CSVFileWriteHelper parent){
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException {
			try {
				this._writer.write(String.format(_dtfmtTwoNum
						,time, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "blink.csv";
		}
	}
	private blinkWriter _blink = null;
	public void insertBlinkData(long time, long value)throws DataSourceException{
		if(null == this._sessionId){
			return;
		}
		if(null == this._blink){
			this._blink = new blinkWriter(this);
			this._blink.open();
		}
		this._blink.insert(time, value);
	}
	
	private static class medWriter extends helperBasedFileDataSource{
		public medWriter(CSVFileWriteHelper parent){
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException {
			try {
				this._writer.write(String.format(_dtfmtTwoNum
						, ConverterUtil.getSecond(time)
						, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "med.csv";
		}
	}
	private medWriter _med = null;
	public void insertMedData(long time, long value) throws DataSourceException{
		if(null == this._sessionId){
			return;
		}
		if(null == this._med){
			this._med = new medWriter(this);
			this._med.open();
		}
		this._med.insert(time, value);
	}
	
	private static class rawwaveWriter extends helperBasedFileDataSource{
		public rawwaveWriter(CSVFileWriteHelper parent){
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException {
			try {
				this._writer.write(String.format(_dtfmtThreeNum
						,time
						, ConverterUtil.getSecond(time)
						, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "rawwave.csv";
		}
	}
	private rawwaveWriter _rawwave = null;
	public void insertRawwaveData(long time, long value)throws DataSourceException{
		if(null == this._sessionId){
			return;
		}
		if(null == this._rawwave){
			this._rawwave = new rawwaveWriter(this);
			this._rawwave.open();
		}
		this._rawwave.insert(time, value);
	}
	
	private static class sigqualWriter extends helperBasedFileDataSource{
		public sigqualWriter(CSVFileWriteHelper parent){
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException {
			try {
				this._writer.write(String.format(_dtfmtTwoNum
						,ConverterUtil.getSecond(time)
						, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "sigqual.csv";
		}
	}
	private sigqualWriter _sigqual = null;
	public void insertSigqualData(long time, long value)throws DataSourceException{
		if(null == this._sessionId){
			return;
		}
		if(null == this._sigqual){
			this._sigqual = new sigqualWriter(this);
			this._sigqual.open();
		}
		this._sigqual.insert(time, value);
	}
	private static class eventWriter extends helperBasedFileDataSource{
		public eventWriter(CSVFileWriteHelper parent){
			super(parent);
		}

		@Override
		public void insert(long time, long value) throws DataSourceException {
			try {
				this._writer.write(String.format(_dtfmtTwoNum
						,time
						, value));
			} catch (IOException e) {
				throw new DataSourceException(e);
			}
		}

		@Override
		protected String getSubPath() {
			return "events.csv";
		}
	}
	private eventWriter _eventWriter = null;
	@Override
	public long insertEventData(long time, EventType type)
			throws DataSourceException {
		if(null == this._sessionId) return 0;
		if(null == this._eventWriter){
			this._eventWriter = new eventWriter(this);
			this._eventWriter.open();
		}
		this._eventWriter.insert(time, type.getId());
		return 1;
	}
	@Override
	public void insertLocation(long time, Location loc)
			throws DataSourceException {
		// TODO Auto-generated method stub
		
	}
	public Long startSession(){
		this._sessionId = (new Date()).getTime();
		return this._sessionId;
	}
	 
	@Override
	public Long getSession() {
		return this._sessionId;
	}
	public void finalize(Boolean successful){
		this._sessionId = null;
		if(null != this._att){
			try {
				this._att.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._att = null;
		}
		if(null != this._blink){
			try {
				this._blink.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._blink = null;
		}
		if(null != this._med){
			try {
				this._med.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._med = null;
		}
		if(null != this._rawwave){
			try {
				this._rawwave.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._rawwave = null;
		}
		if(null != this._sigqual){
			try {
				this._sigqual.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._sigqual = null;
		}
		if(null != this._eventWriter){
			try {
				this._eventWriter.close();
			} catch (DataSourceException e) {
				e.printStackTrace();
			}
			this._eventWriter = null;
		}
	}

}
