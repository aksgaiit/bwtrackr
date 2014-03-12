package ac.aiit.bwcam.bwtracker.data.db;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IDurationVisitor;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase;
import ac.aiit.bwcam.bwtracker.data.impl.DurationBase;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria.accuracy;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;

public class LocationTable extends sessionMillisecTableHelperBase {
	public LocationTable(SQLiteDatabase conn) {
		super(conn);
	}
	@Override
	public String getTableName() {
		return table_name;
	}
	public static final String table_name = "location";

	public static final colInfo col_accuracy =new colInfo("acuracy", colType.DOUBLE);
	public static final colInfo col_altitude = new colInfo("altitude", colType.DOUBLE);
	public static final colInfo col_bearing = new colInfo("bearing", colType.DOUBLE);
	public static final colInfo col_latitude = new colInfo("latitude", colType.DOUBLE);
	public static final colInfo col_longitude = new colInfo("longitude", colType.DOUBLE);
	public static final colInfo col_speed = new colInfo("speed", colType.DOUBLE);
	
	private static final colInfo cols [] = new colInfo[]{
		TableHelper.col_session,
		TableHelper.col_millisec,
		col_accuracy,
		col_altitude,
		col_bearing,
		col_latitude,
		col_longitude,
		col_speed
	};
	@Override
	public colInfo[] getColumns() {
		return cols;
	}

	public void insert(long time, Location location){
		SQLiteStatement stmt = this.getInsertStatement();
		stmt.clearBindings();
		stmt.bindLong(1, this._sessionId);
		stmt.bindLong(2, time);
		stmt.bindDouble(3, location.getAccuracy());
		stmt.bindDouble(4, location.getAltitude());
		stmt.bindDouble(5, location.getBearing());
		stmt.bindDouble(6, location.getLatitude());
		stmt.bindDouble(7, location.getLongitude());
		stmt.bindDouble(8, location.getSpeed());
//		return stmt.executeInsert();
		stmt.execute();
	}
	private static final String SQL_SELECT_LOCATIONS_FMT_Base
	= "select " +
			LocationTable.col_millisec.getName() +
			"," + LocationTable.col_latitude.getName()+
			"," + LocationTable.col_longitude.getName() +
			"," + LocationTable.col_accuracy.getName() +
			"," + LocationTable.col_altitude.getName() +
			"," + LocationTable.col_bearing.getName() +
			"," + LocationTable.col_speed.getName() +
			" from " +
			LocationTable.table_name;
	private static final String SQL_SELECT_LOCATIONS_OrderByEpoc = 
			" order by " + col_millisec.getName();
	private static class curLocation extends Location{
	
		public curLocation(Location l) {
			super(l);
		}
		public curLocation(Cursor cur){
			super("unkown");
			this.setLatitude(cur.getDouble(1));
			this.setLongitude(cur.getDouble(2));
			this.setAccuracy(cur.getFloat(3));
			this.setAltitude(cur.getDouble(4));
			this.setBearing(cur.getFloat(5));
			this.setSpeed(cur.getFloat(6));
		}
		
	}
	private String getWhereClause(bwSessionCriteria criteria){
		return String.format(" where " + col_session.getName() + "='%d'", criteria.getSessionId());
	}
	public int searchLocations(bwSessionCriteria criteria, ILocationVisitor visitor)
		throws DataSourceException {
		int ret = 0;
		try {
			StringBuilder sb = new StringBuilder(SQL_SELECT_LOCATIONS_FMT_Base);
			sb.append(this.getWhereClause(criteria));
			sb.append(SQL_SELECT_LOCATIONS_OrderByEpoc);
			Cursor cur = this.rawQuery(sb.toString(), null);
			if (cur.moveToFirst()) {
				do {
					curLocation loc = new curLocation(cur);
					if (visitor.visit(criteria.getSessionId(), cur.getLong(0),
							loc)) {
						ret++;
					}
				} while (cur.moveToNext());
			}

		} catch (VisitorAbortException e) {
		}
		return ret;
	}
	//select * from location order by ((millisec - 1370162754020)*(millisec - 1370162754020) + (acuracy*acuracy*(1000000/(max(speed,.1)*max(speed,.1))))) asc limit 1
	private static final String order_by_nearlest_fmt =
			" order by ((" + col_millisec + " - %d)*("+ col_millisec + " - %d)" +
			" + (1000000 * " + col_accuracy + "*" + col_accuracy + "/(max(.1," + col_speed +") * max(.1,"+ col_speed +")))) asc";
	public int searchLocations(ByTimeCriteria criteria, ILocationVisitor visitor) throws DataSourceException{
		int ret = 0;
		try{
			Long limit = criteria.getLimit();
			StringBuilder sb = new StringBuilder(SQL_SELECT_LOCATIONS_FMT_Base)
			.append(this.getWhereClause(criteria))
			.append(String.format(order_by_nearlest_fmt
					, criteria.getTime(), criteria.getTime()))
			.append(null != limit ? String.format(" limit %d;", limit) : ";");

			Cursor cur = this.rawQuery(sb.toString(), null);
			if(cur.moveToFirst()){
				do{
					curLocation loc = new curLocation(cur);
					if(visitor.visit(criteria.getSessionId(), cur.getLong(0), loc)){
						ret++;
					}
				}while(cur.moveToNext());
			}
		} catch (VisitorAbortException e) {
		}
		return ret;
	}
	private static final String sql_get_millisec_duration =
			"select" +
			" max(" + LocationTable.col_millisec.getName() + ")," +
			" min("+ LocationTable.col_millisec.getName() + ")" +
			" from " + LocationTable.table_name;
	public int getDurations(bwSessionCriteria criteria, IDurationVisitor visitor) throws DataSourceException{
		try{
			StringBuilder sb = new StringBuilder(sql_get_millisec_duration);
			sb.append(this.getWhereClause(criteria));
			Cursor cur = this.rawQuery(sb.toString(), null);
			if(cur.moveToFirst()){
				DurationBase dur = new DurationBase(cur.getLong(1), cur.getLong(2));
				if(visitor.visit(dur)){
					return 1;
				}
			}
		} catch (VisitorAbortException e) {
		}
		return 0;
	}
	
}
