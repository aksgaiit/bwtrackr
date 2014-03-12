package ac.aiit.bwcam.bwtracker.data.db.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.db.BWDBHelperBase;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class DBUtils {
	public static void copyFile(String inputFilepath, String outputFilepath) throws DataSourceException{
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
	public static void dumpDatabase(Context ctx, String dbname, String outputFilepath) throws DataSourceException{
		copyFile(ctx.getFilesDir().getParentFile().getPath() + "/databases/"
					+ dbname, outputFilepath);
	}
	public static void restoreDatabase(Context ctx, String dbname, String inputFilePath) throws DataSourceException{
		if((new File(inputFilePath)).exists()){
			copyFile(inputFilePath
					, ctx.getFilesDir().getParentFile().getPath() + "/databases/" + dbname);
		}
	}
}
