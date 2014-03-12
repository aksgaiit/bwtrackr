package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.EventType;
import ac.aiit.bwcam.bwtracker.data.db.table.attTable;
import ac.aiit.bwcam.bwtracker.data.db.table.blinkTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class DBHelper extends BWDBWriterBase{
	public DBHelper(Context context) {
		super(context,null);
	}
	public void finalize(Boolean successful){
		super.finalize(successful);
		this._attTable = null;
		this._blinkTable = null;
		this._eventTable = null;
		this._locTable = null;
		this._medTable = null;
		this._rawwaveTable = null;
		this._sigqualTable = null;

	}

	public void insertRawwaveData(long time, long value){
		if(null == this.getSession()) return;
		if(null == this._rawwaveTable){
			this.initiateTable(this._rawwaveTable = new rawwaveTable(this.getConnection()));
		}
		this._rawwaveTable.insert(time, value);
	}

	public void insertSigqualData(long time, long value){
		if(null == this.getSession()) return;
		if(null == this._sigqualTable){
			this.initiateTable(this._sigqualTable = new sigqualTable(this.getConnection()));
		}
		this._sigqualTable.insert(time, value);
	}

	public void insertMedData(long time, long value){
		if(null == this.getSession()) return;
		if(null == this._medTable){
			this.initiateTable(this._medTable = new medTable(this.getConnection()));
		}
		this._medTable.insert(time, value);
	}

	public void insertAttData(long time, long value){
		if(null == this.getSession()) return;
		if(null == this._attTable){
			this.initiateTable(this._attTable = new attTable(this.getConnection()));
		}
		this._attTable.insert(time, value);
	}

	public void insertBlinkData(long time, long value){
		if(null == this.getSession()) return;
		if(null == this._blinkTable){
			this.initiateTable(this._blinkTable = new blinkTable(this.getConnection()));
		}
		this._blinkTable.insert(time, value);
	}

	@Override
	public long insertEventData(long time, EventType type)
			throws DataSourceException {
		if(null == this.getSession()) return 0;
		if(null == this._eventTable){
			this.initiateTable(this._eventTable = new eventTable(this.getConnection()));
		}
		this._eventTable.insert(time, type);
		return 1;
	}

	private rawwaveTable _rawwaveTable = null;
	private sigqualTable _sigqualTable = null;
	private attTable _attTable = null;
	private medTable _medTable = null;
	private blinkTable _blinkTable = null;
	private eventTable _eventTable = null;
	private LocationTable _locTable = null;
	@Override
	public void insertLocation(long time, Location loc)
			throws DataSourceException {
		if(null == this.getSession()) return;
		if(null == this._locTable){
			this.initiateTable(this._locTable = new LocationTable(this.getConnection()));
		}
		this._locTable.insert(time, loc);
	}
}
