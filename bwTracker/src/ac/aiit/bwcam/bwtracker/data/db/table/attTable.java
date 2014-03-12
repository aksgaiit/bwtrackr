package ac.aiit.bwcam.bwtracker.data.db.table;

import android.database.sqlite.SQLiteDatabase;

public class attTable extends dataBySecondTableBase {

	public attTable(SQLiteDatabase conn) {
		super(conn);
	}

	public static final String table_name = "att";
	@Override
	public String getTableName() {
		return table_name;
	}
	

}
