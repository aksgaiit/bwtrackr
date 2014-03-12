package ac.aiit.bwcam.bwtracker.data.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataManager;
import ac.aiit.bwcam.bwtracker.data.ISessionStatistics;
import ac.aiit.bwcam.bwtracker.data.ISessionStatisticsVisitor;
import ac.aiit.bwcam.bwtracker.data.SessionStatisticsBase;
import ac.aiit.bwcam.bwtracker.data.db.table.TableHelper;
import ac.aiit.bwcam.bwtracker.data.db.table.attTable;
import ac.aiit.bwcam.bwtracker.data.db.table.blinkTable;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager extends BWDBHelperBase implements IBWDataManager{

	public DBManager(Context context) {
		super(context, null);
	}

	private void copyFile(String inputFilepath, String outputFilepath) throws DataSourceException{
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			File f = new File(inputFilepath);
			fis = new FileInputStream(f);
			fos = new FileOutputStream(outputFilepath);
			int len = 0;
			byte[] buf = new byte[4096];
			while (-1 != (len = fis.read(buf))) {
				fos.write(buf, 0, len);
			}
			fos.flush();
		} catch (FileNotFoundException e) {
			throw new DataSourceException(e);
		} catch (IOException e) {
			throw new DataSourceException(e);
		} finally {
			try {
				if (null != fos) {
					fos.close();
				}
				if (null != fis) {
					fis.close();
				}
			} catch (IOException ioe) {
			}
		}
	}
	@Override
	public void dump(String outputFilepath)
			throws DataSourceException {
		this.copyFile(this._context.getFilesDir().getParentFile().getPath() + "/databases/"
					+ BWDBHelperBase.DBNAME_BW, outputFilepath);
	}
	@Override
	public void restore(String inputFilepath) throws DataSourceException {
		this.copyFile(inputFilepath, this._context.getFilesDir().getParentFile().getPath() + "/databases/"
					+ BWDBHelperBase.DBNAME_BW);
	}
	private static Class deletableTables[]  = {
		attTable.class
		, blinkTable.class
		, eventTable.class
		, LocationTable.class
		, medTable.class
		, rawwaveTable.class
		,sigqualTable.class	
	};
	private static Class tableConstructorTypes [] = {
			SQLiteDatabase.class
	};
	@Override
	public void deleteRecords(Long sessionId) throws DataSourceException {
		SQLiteDatabase db = this.getWritableDatabase();
		Object args [] = {db};
		for(Class clz : deletableTables){
			try {
				Constructor<TableHelper> cnstr = clz.getConstructor(tableConstructorTypes);
				TableHelper helper = (TableHelper)cnstr.newInstance(args);
				helper.deleteRecords(sessionId);
			} catch (NoSuchMethodException e) {
				throw new DataSourceException(e);
			} catch (IllegalArgumentException e) {
				throw new DataSourceException(e);
			} catch (InstantiationException e) {
				throw new DataSourceException(e);
			} catch (IllegalAccessException e) {
				throw new DataSourceException(e);
			} catch (InvocationTargetException e) {
				throw new DataSourceException(e);
			}
		}
	}

	private static final String SQL_SELECT_SESSIONS = "select " +
			TableHelper.col_session + "," +
			"min(" + TableHelper.col_millisec + ")," +
			"max(" + TableHelper.col_millisec + ") " +
			"from " + rawwaveTable.table_name +
			" group by " + TableHelper.col_session;

	private static final String SQL_SELECT_SESSIONS_FAST = "select " +
			TableHelper.col_session + "," +
			"min(" + TableHelper.col_second + ")," +
			"max(" + TableHelper.col_second + ") from " + 
			sigqualTable.table_name +
			" group by " + TableHelper.col_session;
	private static class sessionStatisticsImpl extends SessionStatisticsBase{
		public sessionStatisticsImpl(Cursor cur){
			super();
			this._id = cur.getLong(0);
			this._time = cur.getLong(1);
			this._duration = cur.getLong(2) - this._time;
		}
	}
	@Override
	public int visitSessionStatistics(ISessionStatisticsVisitor visitor)
			throws DataSourceException {
		SQLiteDatabase conn = null;
		int ret = 0;
		try{
			conn = this.getReadableDatabase();
			if(null != conn){
				conn.beginTransaction();
//				Cursor cur = conn.rawQuery(SQL_SELECT_SESSIONS, null);
				Cursor cur = conn.rawQuery(SQL_SELECT_SESSIONS_FAST, null);
				if(cur.moveToFirst()){
					do{
						sessionStatisticsImpl si = new sessionStatisticsImpl(cur);
						if(visitor.visit(si)){
							ret ++;
						}
					}while(cur.moveToNext());
				}
				conn.endTransaction();
			}
		} catch (VisitorAbortException e) {
		}finally{
			if(null != conn){
				conn.close();
			}
		}
		return ret;
	}
}

