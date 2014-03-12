package ac.aiit.bwcam.bwtracker.data.impl;

import java.util.ArrayList;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IDataSource;
import ac.aiit.bwcam.bwtracker.data.IDataSourceListener;

public abstract class DataSourceBase implements IDataSource {
	protected ArrayList<IDataSourceListener> _listeners = new ArrayList<IDataSourceListener>();

	@Override
	public void addListener(IDataSourceListener listener) {
		this._listeners.add(listener);
	}
	@Override
	public void removeListener(IDataSourceListener listener){
		this._listeners.remove(listener);
	}
	

	@Override
	public void open() throws DataSourceException{
		for(IDataSourceListener listener : this._listeners){
			listener.onOpen(this);
		}
	}

	@Override
	public void close() throws DataSourceException{
		for(IDataSourceListener listener: this._listeners){
			listener.onClose(this);
		}

	}

}
