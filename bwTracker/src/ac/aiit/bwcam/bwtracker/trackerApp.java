package ac.aiit.bwcam.bwtracker;

import android.app.Application;

public class trackerApp extends Application {
	private static trackerApp _instance = null;
	
	public static final trackerApp getApp(){
		return _instance;
	}

	@Override
	public void onCreate() {
		_instance = this;
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		_instance = null;
		super.onTerminate();
	}

}
