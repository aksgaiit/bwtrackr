package ac.aiit.bwcam.bwtracker.data.ui;

import java.util.Date;

import ac.aiit.bwcam.bwtracker.ConverterUtil;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.EventType;
import ac.aiit.bwcam.bwtracker.data.IBWDataWriter;
import android.location.Location;
import android.widget.TextView;

public class uiDataTracker implements IBWDataWriter {
	private TextView _textView = null;
	
	public uiDataTracker(TextView textview){
		super();
		this._textView = textview;
	}

	private Long _session = null;
	@Override
	public Long startSession() throws DataSourceException {
		this._textView.setText("");
		return (this._session = (new Date()).getTime());
	}

	@Override
	public Long getSession() {
		return this._session;
	}

	@Override
	public void finalize(Boolean successful) throws DataSourceException {
		this.printLastRawCount();
	}

	@Override
	public void insertAttData(long time, long value) throws DataSourceException {
		if(null == this.getSession()) return;
		this.insertLog(String.format("%s: attention: %d", ConverterUtil.getWhenString(time), value ));
	}

	@Override
	public void insertBlinkData(long time, long value)
			throws DataSourceException {
		if(null == this.getSession()) return;
		this.insertLog(String.format("%s: blink: %d", ConverterUtil.getWhenString(time), value ));
	}

	@Override
	public void insertMedData(long time, long value) throws DataSourceException {
		if(null == this.getSession()) return;
		this.insertLog(String.format("%s: meditation: %d", ConverterUtil.getWhenString(time), value ));
	}

	private long _rdCount = 0;
	private long _lastSecond = ConverterUtil.getSecond((new Date()).getTime());
	private void printLastRawCount(){
		this.insertLog(String.format("%s: rowwave data count: %d", ConverterUtil.getWhenString(this._lastSecond), this._rdCount ));
	}
	@Override
	public void insertRawwaveData(long time, long value)
			throws DataSourceException {
		if(null == this.getSession()) return;
		Long sec = ConverterUtil.getSecond(time);
		if(sec != this._lastSecond){
			this.printLastRawCount();
			this._rdCount = 0;
			this._lastSecond = sec;
		}else{
			this._rdCount++;
		}
	}

	private void insertLog(String newLine){
		this._textView.setText(newLine + System.getProperty("line.separator")
				+ this._textView.getText().subSequence(0, 1000));
	}

	@Override
	public void insertSigqualData(long time, long value)
			throws DataSourceException {
		if(null == this.getSession()) return;
		this.insertLog(String.format("%s: signal quality: %d", ConverterUtil.getWhenString(time), value ));
	}

	@Override
	public long insertEventData(long time, EventType type)
			throws DataSourceException {
		return 0;
		
	}

	@Override
	public void insertLocation(long time, Location loc)
			throws DataSourceException {
		// TODO Auto-generated method stub
		
	}

}
