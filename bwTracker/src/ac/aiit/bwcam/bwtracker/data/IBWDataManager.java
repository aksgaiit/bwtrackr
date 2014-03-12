package ac.aiit.bwcam.bwtracker.data;




public interface IBWDataManager {
	public void dump(String outputFilepath) throws DataSourceException;
	public void restore(String inputFilepath) throws DataSourceException;
	public void deleteRecords(Long sessionId)throws DataSourceException;
	public int visitSessionStatistics(ISessionStatisticsVisitor visitor) throws DataSourceException;
}
