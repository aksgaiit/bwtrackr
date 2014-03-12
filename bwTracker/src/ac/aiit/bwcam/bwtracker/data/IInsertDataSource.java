package ac.aiit.bwcam.bwtracker.data;

public interface IInsertDataSource extends IDataSource{
	public void insert(long time, long value) throws DataSourceException;

}
