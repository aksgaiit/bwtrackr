package ac.aiit.bwcam.bwtracker.data;

public interface IDataSource {
	public void addListener(IDataSourceListener listener);
	public void removeListener(IDataSourceListener listener);
	public void open() throws DataSourceException;
	public void close() throws DataSourceException;
}
