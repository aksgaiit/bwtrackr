package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.data.db.table.dataBySecondTableBase;
import android.database.sqlite.SQLiteDatabase;

public class medTable extends dataBySecondTableBase {
	public medTable(SQLiteDatabase conn) {
		super(conn);
	}
	
	public static final String table_name = "med";

	@Override
	public String getTableName() {
		return table_name;
	}
}
