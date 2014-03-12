package ac.aiit.bwcam.bwtracker.data;

import android.location.Location;

public interface IBWDataWriter {
	public Long startSession()throws DataSourceException;
	public Long getSession();
	public void finalize(Boolean successful) throws DataSourceException;
	
	public void insertAttData(long time, long value) throws DataSourceException;
	public void insertBlinkData(long time, long value) throws DataSourceException;
	public void insertMedData(long time, long value) throws DataSourceException;
	public void insertRawwaveData(long time, long value) throws DataSourceException;
	public void insertSigqualData(long time, long value) throws DataSourceException;
	public long insertEventData(long time, EventType type) throws DataSourceException;
	
	public void insertLocation(long time, Location loc) throws DataSourceException;

}
