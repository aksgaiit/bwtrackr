package ac.aiit.bwcam.bwtracker.data.db.table;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public abstract class sessionTableHelperBase extends TableHelper {
	protected Long _sessionId = null;
	protected sessionTableHelperBase(SQLiteDatabase conn){
		super(conn);
	}
	public sessionTableHelperBase(SQLiteDatabase conn, Long sessionId){
		super(conn);
		this.setSessionId(sessionId);
	}
	public void setSessionId(Long sessionId){
		this._sessionId = sessionId;
	}
	
	public void deleteRecords(){
		if(null != this._sessionId){
			this.deleteRecords(this._sessionId);
		}
	}
	private static final String sql_fmt_check_session = "select count(1) from %s" +
			" where " + col_session.getName() + "=?";
	private SQLiteStatement _checkSessionDataStatement = null;
	public boolean hasSessionData(){
		return this.hasSessionData(this._sessionId);
	}
	protected boolean hasSessionData(long session){
		this.opencloseCheckSessionDataStatement(true);
		this._checkSessionDataStatement.clearBindings();
		this._checkSessionDataStatement.bindLong(1, session);
		return 0 < this._checkSessionDataStatement.simpleQueryForLong();
	}
	private synchronized SQLiteStatement opencloseCheckSessionDataStatement(boolean open){
		if(!open && null != this._checkSessionDataStatement){
			this._checkSessionDataStatement.close();
			this._checkSessionDataStatement = null;
		}else if(open && null == this._checkSessionDataStatement){
			this._checkSessionDataStatement
			= this.compileStatement(String.format(sql_fmt_check_session, this.getTableName()));
		}
		return this._checkSessionDataStatement;
	}
	@Override
	public void closeWorkingStmt() {
		super.closeWorkingStmt();
		this.opencloseCheckSessionDataStatement(false);
	}

}
