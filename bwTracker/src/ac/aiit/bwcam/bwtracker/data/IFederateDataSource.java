package ac.aiit.bwcam.bwtracker.data;

public interface IFederateDataSource {
	public long startSession()throws DataSourceException;
	public void finalize(Boolean successful) throws DataSourceException;
	
	public void insertAttData(long time, long value) throws DataSourceException;
	public void insertBlinkData(long time, long value) throws DataSourceException;
	public void insertMedData(long time, long value) throws DataSourceException;
	public void insertRawwaveData(long time, long value) throws DataSourceException;
	public void insertSigqualData(long time, long value) throws DataSourceException;

}
