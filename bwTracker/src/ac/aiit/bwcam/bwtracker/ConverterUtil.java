package ac.aiit.bwcam.bwtracker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConverterUtil {
	public static final long getSecond(long millisec){
		return (millisec - (millisec % 1000));
	}
	private static final SimpleDateFormat _sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	private static final SimpleDateFormat _ssdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final SimpleDateFormat _sssdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
	private static final SimpleDateFormat _tdf = new SimpleDateFormat("HH:mm:ss.SSS");

	public static final String getTimeString(Date date){
		return _tdf.format(date);
	}
	public static final String getTimeString(long time){
		Date tmp = new Date(time);
		return getTimeString(tmp);
	}
	public static final String getWhenString(long time){
		Date tmp = new Date();
		tmp.setTime(time);
		return getWhenString(tmp);
	}
	public static final String getWhenString(Date date){
		return _sdf.format(date);
	}
	public static final String getShortWhenString(long time){
		Date tmp = new Date();
		tmp.setTime(time);
		return getShortWhenString(tmp);
	}
	public static final String getShortWhenString(Date date){
		return _ssdf.format(date);
	}
	public static final String getAbbreviateWhenString(long time){
		Date tmp = new Date(time);
		return getAbbreviateWhenString(tmp);
	}
	public static final String getAbbreviateWhenString(Date date){
		return _sssdf.format(date);
	}
	
}
