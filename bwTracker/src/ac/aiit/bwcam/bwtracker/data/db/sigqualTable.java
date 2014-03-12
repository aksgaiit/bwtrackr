package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.data.db.table.dataBySecondTableBase;
import android.database.sqlite.SQLiteDatabase;

public class sigqualTable extends dataBySecondTableBase {
	public sigqualTable(SQLiteDatabase conn) {
		super(conn);
	}
	public static final String table_name = "sigqual";

	@Override
	public String getTableName() {
		return table_name;
	}
}
