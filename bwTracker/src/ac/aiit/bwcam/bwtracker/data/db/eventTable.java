package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.data.EventType;
import ac.aiit.bwcam.bwtracker.data.db.table.dataByMillisecTableBase;
import android.database.sqlite.SQLiteDatabase;

public class eventTable extends dataByMillisecTableBase {
	public eventTable(SQLiteDatabase conn) {
		super(conn);
	}

	@Override
	public String getTableName() {
		return "event";
	}
	public void insert(long time, EventType type){
		super.insert(time, type.getId());
	}
}
