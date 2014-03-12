package ac.aiit.bwcam.bwtracker.data;

public interface IDataSourceListener {
	public void onOpen(IDataSource source);
	public void onClose(IDataSource source);
}
