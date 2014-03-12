package ac.aiit.bwcam.bwtracker.data.db.table;

import android.database.sqlite.SQLiteDatabase;

public class blinkTable extends dataByMillisecTableBase {

	public blinkTable(SQLiteDatabase conn) {
		super(conn);
	}

	@Override
	public String getTableName() {
		return "blink";
	}
	

}
