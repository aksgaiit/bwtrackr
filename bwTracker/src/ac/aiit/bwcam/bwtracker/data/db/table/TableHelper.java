package ac.aiit.bwcam.bwtracker.data.db.table;

import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria.sortOrder;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public abstract class TableHelper {
	public static final colInfo col_session = new colInfo("session", colType.LONG);
	public static final colInfo col_millisec = new colInfo("millisec", colType.LONG);
	public static final colInfo col_second = new colInfo("second", colType.LONG);
	public static final colInfo col_longValue = new colInfo("value", colType.LONG);
	public static final colInfo col_doubleValue = new colInfo("value", colType.DOUBLE);
	
	protected static final String sql_fmt_where_start =
			" and " + col_millisec.getName() + ">=%d";
	protected static final String sql_fmt_where_end = 
			" and " + col_millisec.getName() + "<%d";
	protected static final String sql_fmt_sort_key = 
			" order by %s";
	protected static final String sqlopt_limit = 
			" limit %d";
	protected static final String sqlopt_offset =
			" offset %d";

	protected static final String getIntervalWhereClause(IntervalCriteria criteria){
		StringBuilder sb = new StringBuilder();
		Long start = criteria.getStartTime();
		if(null != start){
			sb.append(String.format(sql_fmt_where_start, start));
		}
		Long end = criteria.getEndTime();
		if(null != end){
			sb.append(String.format(sql_fmt_where_end, end));
		}
		return sb.toString();
	}
	protected static final String getSQLFragment(bwSessionCriteria criteria){
		StringBuilder sb = new StringBuilder();
		String sortkey = criteria.getSortKey();
		if(null != sortkey){
			sb.append (String.format(sql_fmt_sort_key, sortkey));
		}
		sortOrder order = criteria.getOrder();
		if(null != order){
			sb.append(" " + order.toString());
		}
		Long limit = criteria.getLimit();
		if(null != limit){
			sb.append(String.format(sqlopt_limit, limit));
		}
		Long offset = criteria.getOffset();
		if(null != offset){
			sb.append(String.format(sqlopt_offset));
		}
		return sb.toString();
	}
	
	public static class colType{
		private String _name = null;
		private colType(String name){
			super();
			this._name = name;
		}
		public String getName(){
			return this._name;
		}
		public static final colType BLOB = new colType("blob");
		public static final colType DOUBLE = new colType("double");
		public static final colType LONG = new colType("long");
		public static final colType STRING = new colType("string");
	}
	public static class colInfo{
		@Override
		public String toString() {
			return this.getName();
		}
		private String _name = null;
		private colType _type = null;
		public colInfo(String name, colType type){
			super();
			this._name = name;
			this._type = type;
		}
		public String getName(){
			return this._name;
		}
		public colType getType(){
			return this._type;
		}
	}
	private SQLiteStatement _insstmt = null;
	private SQLiteDatabase _conn = null;
	public TableHelper(SQLiteDatabase conn){
		super();
		this._conn = conn;
	}
	public synchronized void closeWorkingStmt(){
		if(null != this._insstmt){
			this._insstmt.close();
			this._insstmt = null;
		}
	}
	protected void beginTransaction(){
		this._conn.beginTransaction();
	}
	protected void endTransaction(){
		this._conn.endTransaction();
	}
	protected void executeSQL(String sql){
		this._conn.execSQL(sql);
	}
	protected SQLiteStatement compileStatement(String sql){
		return this._conn.compileStatement(sql);
	}
	protected Cursor rawQuery(String sql, String[] args){
		return this._conn.rawQuery(sql, args);
	}
	public abstract String getTableName();
	public abstract colInfo[] getColumns();
	public abstract colInfo[] getIndexColumns();
	public void createTable(){
		StringBuilder sb = new StringBuilder(String.format("create table if not exists %s (", this.getTableName()));
		boolean empty = true;
		for(colInfo cinfo : this.getColumns()){
			if(empty){
				empty = false;
			}else{
				sb.append(",");
			}
			sb.append(String.format("%s %s", cinfo.getName(), cinfo.getType().getName()));
		}
		sb.append(");");
		colInfo[] indexCols = this.getIndexColumns();
		if(null != indexCols && 0 < indexCols.length){
			for(colInfo iinfo : indexCols){
				sb.append(String.format("create index %s_%s on %s (%s asc);"
						, this.getTableName(), iinfo.getName(), this.getTableName(), iinfo.getName() ));
			}
		}
		this._conn.execSQL(sb.toString());
	}
	public void dropTable(){
		StringBuilder sb = new StringBuilder(String.format("drop table if exists %s;", this.getTableName()));
		colInfo[] indexCols = this.getIndexColumns();
		if(null != indexCols && 0 < indexCols.length){
			for(colInfo iinfo : indexCols){
				sb.append(String.format("drop index %s_%s;"
						, this.getTableName(), iinfo.getName()));
			}
		}
		this._conn.execSQL(sb.toString());
	}
	
	public void deleteRecords(Long sessionId){
		this._conn.execSQL(String.format(
				"delete from %s" + (null != sessionId ? String.format(" where session=%d", sessionId) : ""), this.getTableName()));
	}
	protected SQLiteStatement getInsertStatement(){//would better be synchronized, leaving as it is to improve performance.
		if(null == this._insstmt){
			StringBuilder sb = new StringBuilder(String.format("insert into %s (", this.getTableName()));
			StringBuilder sbval = new StringBuilder(" values(");
			boolean empty = true;
			for(colInfo cinfo: this.getColumns()){
				if(empty){
					empty = false;
				}else{
					sb.append(",");
					sbval.append(",");
				}
				sb.append(cinfo.getName());
				sbval.append("?");
			}
			sb.append(")");
			sbval.append(")");
			this._insstmt = this._conn.compileStatement(sb.toString() + sbval.toString());
		}
		return this._insstmt;
	}

	public String joinColumnNames(String delim){
		StringBuilder sb = new StringBuilder();
		boolean empty = true;
		for(colInfo ci: this.getColumns()){
			if(empty){
				empty=false;
			}else{
				sb.append(delim);
			}
			sb.append(ci.getName());
		}
		return sb.toString();
	}
}
