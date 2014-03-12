package ac.aiit.bwcam.bwtracker.data.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IInsertDataSource;
import ac.aiit.bwcam.bwtracker.data.impl.DataSourceBase;

public abstract class fileDataSource extends DataSourceBase implements IInsertDataSource {

	protected BufferedWriter _writer = null;
	private String _path = null;

	protected fileDataSource() {
		super();
	}
	
	protected abstract String _createPath() throws DataSourceException;
	private synchronized String getSourcePath() throws DataSourceException{
		if(null == this._path){
			this._path = this._createPath();
		}
		return this._path;
	}

	@Override
	public void open() throws DataSourceException {
		super.open();
		try{
			if(null == this._writer){
				this._writer = new BufferedWriter(new FileWriter(this.getSourcePath()));
			}
		} catch (IOException e) {
			throw new DataSourceException(e);
		}finally{
			//
		}

	}

	@Override
	public void close() throws DataSourceException {
		super.close();
		try{
			if(null != this._writer){
				this._writer.flush();
			}
		} catch (IOException e) {
			new DataSourceException(e);
		}finally{
			if(null != this._writer){
				try {
					this._writer.close();
				} catch (IOException e) {
				}
			}
		}

	}

}
