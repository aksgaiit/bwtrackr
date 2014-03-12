package ac.aiit.bwcam.bwtracker;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ac.aiit.bwcam.bwtracker.R.drawable;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataWriter;
import ac.aiit.bwcam.bwtracker.data.db.DBHelper;
import ac.aiit.bwcam.bwtracker.ui.CameraManager;
import ac.aiit.bwcam.bwtracker.ui.IPreviewObject;
import ac.aiit.bwcam.bwtracker.ui.UIException;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

public class MainActivity extends Activity {
	private BluetoothAdapter bluetoothAdapter;

	private TextView _textView = null;
	private TextView _timeIndicator = null;

	private IPreviewObject _camPrev = null;
	private FrameLayout _previewContainer = null;
	
	private View _logView = null;
	private View _bw_info_detail = null;
	private View _gps_info_detail = null;
	
	private ImageView _ic_bw_qual = null;
	private ImageView _ic_gps_qual = null;
	
	private TextView _cam_name = null;

	private TextView _sigQuality = null;
	private TextView _locInfo = null;
	private TextView _rCount = null;

	private TGDevice _tgDevice = null;
	private boolean _rawEnabled = true;
	
	private ImageView _rec_button = null;
	
	private ImageView _switch_mode_view = null;

	private static class tgMsgHandler extends Handler {
		private MainActivity _parent = null;

		public tgMsgHandler(MainActivity parent) {
			super();
			this._parent = parent;
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case TGDevice.MSG_STATE_CHANGE:

					switch (msg.arg1) {
					case TGDevice.STATE_IDLE:
						// this._parent.onDisconnect();
						break;
					case TGDevice.STATE_CONNECTING:
						this._parent.insertLog("Connecting...");
						break;
					case TGDevice.STATE_CONNECTED:
						this._parent.onConnect();
						break;
					case TGDevice.STATE_NOT_FOUND:
						this._parent.insertLog("Can't find");
						this._parent.deleteDevice();
						break;
					case TGDevice.STATE_NOT_PAIRED:
						this._parent.insertLog("not paired");
						break;
					case TGDevice.STATE_DISCONNECTED:
						this._parent.onDisconnect();
						break;
					}

					break;
				case TGDevice.MSG_POOR_SIGNAL:
					// signal = msg.arg1;
					// this._parent.tv.append(String.format("%s - PoorSignal:%d\n",
					// getWhenString(msg), msg.arg1));
					if(null != this._parent._writeHelper){
						this._parent._writeHelper.insertSigqualData(
								this._parent.getEpocTime(msg), msg.arg1);
					}
					this._parent.updateStatus(msg.arg1);
					this._parent.setBWQuality(100 > msg.arg1, null);
					break;
				case TGDevice.MSG_RAW_DATA:
					// raw1 = msg.arg1;
					// tv.append(String.format("%s - Got raw:%d\n",
					// getWhenString(msg), msg.arg1));
					if(null != this._parent._writeHelper){					
						this._parent._writeHelper.insertRawwaveData(
							this._parent.getEpocTime(msg), msg.arg1);
					}
					break;
				case TGDevice.MSG_HEART_RATE:
					this._parent.insertLog(String.format("%s - Heart rate:%d",
							this._parent.getWhenString(msg), msg.arg1));
					break;
				case TGDevice.MSG_ATTENTION:
					// att = msg.arg1;
					// this._parent.tv.append(String.format("%s - Attention:%d",
					// this._parent.getWhenString(msg), msg.arg1));
					// Log.v("HelloA", "Attention: " + att + "");
					if(null != this._parent._writeHelper){
						this._parent._writeHelper.insertAttData(
								this._parent.getEpocTime(msg), msg.arg1);
					}
					break;
				case TGDevice.MSG_MEDITATION:
					if(null != this._parent._writeHelper){
						this._parent._writeHelper.insertMedData(
								this._parent.getEpocTime(msg), msg.arg1);
					}
					break;
				case TGDevice.MSG_BLINK:
					// this._parent.tv.append(String.format("%s - Blink:%d",
					// this._parent.getWhenString(msg),
					// msg.arg1));
					if(null != this._parent._writeHelper){
						this._parent._writeHelper.insertBlinkData(
								this._parent.getEpocTime(msg), msg.arg1);
					}
					break;
				case TGDevice.MSG_RAW_COUNT:
					// this._parent.tv.append(String.format("%s - Raw Count:%d",
					// this._parent.getWhenString(msg),msg.arg1));
					this._parent.setBWQuality(null, 400 < msg.arg1);
					this._parent._rCount.setText(String.format("%d", msg.arg1));
					break;
				case TGDevice.MSG_LOW_BATTERY:
					Toast.makeText(this._parent.getApplicationContext(),
							"Low battery!", Toast.LENGTH_SHORT).show();
					break;
				case TGDevice.MSG_RAW_MULTI:
					// TGRawMulti rawM = (TGRawMulti)msg.obj;
					// tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
				default:
					break;
				}
			} catch (DataSourceException e) {
				e.printStackTrace();
			} finally {

			}
		}
	};

	private IBWDataWriter _writeHelper = null;

	private Long _logStarted = null;

	public void alert(String message){
		Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	private void updateStatus(int sigQual) {
		Long logstarted = this._logStarted;
		if(null != logstarted){
			this._timeIndicator.setText(String
					.valueOf((System.currentTimeMillis() - logstarted) / 1000) + " sec.");
		}
		this._sigQuality.setText(String.valueOf(sigQual));
	}

	private void insertLog(String newLine) {
		this._textView.setText(newLine + System.getProperty("line.separator")
				+ this._textView.getText());
	}
	
	private ask_con_dialog _ask_con_dialog = null;
	public static class ask_con_dialog extends DialogFragment{
		private MainActivity _parent = null;
		public void setParent(MainActivity parent){
			this._parent = parent;
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog ret = new Dialog(getActivity(), R.style.Theme_Translucent);
			ret.setContentView(R.layout.ask_reconnect_layout);
			ret.setCanceledOnTouchOutside(false);
			ret.setCancelable(false);
			ret.setTitle("No EEG sensor available..");
			ImageButton btn = (ImageButton)ret.findViewById(R.id.try_connect);
			btn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					_parent.startDevice();
				}
			});
			return ret;
		}
		
	}
	private synchronized void _ask_reconnect(){
		if(null == this._ask_con_dialog){
			this._ask_con_dialog = new ask_con_dialog();
			this._ask_con_dialog.setParent(this);
		}
		this._ask_con_dialog.show(getFragmentManager(), "ask_con_dialog");
	}

	private void enterRecording(){
		this._rec_button.setImageResource(R.drawable.ic_stop);
		this._switch_mode_view.setEnabled(false);
	}
	private void leaveRecording(){
		this._rec_button.setImageResource(R.drawable.ic_rec);
		this._switch_mode_view.setEnabled(true);
	}
	protected void onConnect() {
		this.insertLog(String.format("Connected.@%s",
				ConverterUtil.getWhenString(new Date())));
		this._tgDevice.start();
		this._rec_button.setEnabled(true);
		this._rec_button.setImageResource(R.drawable.ic_rec);
		this._locInfo.setText("0");
		this.setBWQuality(1);
	}

	protected void onDisconnect() throws DataSourceException {
		this.insertLog(String.format("Disconnected.@%s",
				ConverterUtil.getWhenString(new Date())));
		_deviceTimeDiff = null;
		this._rec_button.setImageResource(R.drawable.ic_rec_disabled);
		this._rec_button.setEnabled(false);
		this.setBWQuality(0);
		this.deleteDevice();
		this.startClock();
		this._ask_reconnect();
	}

	private IBWDataWriter getWriteHelper() {
		if (null == _writeHelper) {
			// switch the logger here//
			this._writeHelper = new DBHelper(this.getApplicationContext());
			
			// this._writeHelper = new CSVFileWriteHelper(
			// Environment.getExternalStorageDirectory().getPath());//new
			// DBHelper(this.getApplicationContext());
			//this._writeHelper = new uiDataTracker(this._textView);//new
			// CSVFileWriteHelper(
			// Environment.getExternalStorageDirectory().getPath());//new
			// DBHelper(this.getApplicationContext());
		}
		return _writeHelper;
	}

	private long _curSession = 0;

	private long startLogging() throws DataSourceException {
		this._rec_button.setEnabled(false);
		this.stopClock();
		IBWDataWriter writeHelper = this.getWriteHelper();
		this._curSession = writeHelper.startSession();
		this._logStarted = Long.valueOf(System.currentTimeMillis());
		this.insertLog(String
				.format("Starting session %d...", this._curSession));
		try {
			this._camPrev.startRecording(this._curSession);
			this.enterRecording();
			this._rec_button.setEnabled(true);
			return this._curSession;
		} catch (UIException e) {
			throw new DataSourceException(e);
		}
	}

	private void stopLogging() throws DataSourceException {
		if(null != this._logStarted && 0 != this._curSession){
			this._rec_button.setEnabled(false);
			this.insertLog("stop logging...");
			this._logStarted = null;
			this._curSession = 0;
			AsyncTask<IPreviewObject, Object, Boolean> task = new AsyncTask<IPreviewObject, Object, Boolean>(){
				@Override
				protected Boolean doInBackground(IPreviewObject... params) {
					IPreviewObject previewObject = params[0];
					if(null != previewObject){
						previewObject.stopRecording();
					}
					return true;
				}
				@Override
				protected void onPostExecute(Boolean result) {
					if(result){
						leaveRecording();
						_rec_button.setEnabled(true);
						startClock();
						insertLog("stopped");
					}else{
						Toast.makeText(
								getApplicationContext(),
								"stop preview failed", Toast.LENGTH_LONG).show();
					}
				}
			};
			if(null != this._writeHelper){
				IBWDataWriter writeHelper = this._writeHelper;
				this._writeHelper = null;
				writeHelper.finalize(true);
			}
			task.execute(this._camPrev);
		}
	}
	
	private void startDevice(){
		if(null != this._ask_con_dialog){
			this._ask_con_dialog.dismiss();
		}
		if (null == this._tgDevice){
			this.insertLog("creating device..");
			this.createDevice();
		}
		int state = this._tgDevice.getState();
		if(TGDevice.STATE_CONNECTING != state && TGDevice.STATE_CONNECTED != state) {
			this.insertLog("connecting device..");
			this._tgDevice.connect(this._rawEnabled);
		}else if(TGDevice.STATE_IDLE == state){
			this.insertLog("resuming device..");
			this._tgDevice.start();
		}
	}
	
	private void stopDevice(){
		if(null != this._tgDevice){
			this.insertLog("stopping device..");
			this._tgDevice.stop();
		}
	}
	

	private void createDevice() {
		if (null == this.bluetoothAdapter) {
			this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (this.bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		} else {
			if (null == this._tgDevice) {
				/* create the TGDevice */
				this._tgDevice = new TGDevice(this.bluetoothAdapter,
						new tgMsgHandler(this));
			}
		}
	}
	private void deleteDevice(){
		if(null != this._tgDevice){
			this.insertLog("removing device..");
			this._tgDevice.stop();
			this._tgDevice.close();
			this._tgDevice = null;
		}
	}

	private boolean _clockStarted = false;
	private Handler _clockHandler = null;
	private Runnable _ticker = null;

	private void startClock() {
		if (null == this._clockHandler) {
			this._clockHandler = new Handler();
		}
		if (null == this._ticker) {
			this._ticker = new Runnable() {

				@Override
				public void run() {
					if (_clockStarted) {
						long milisec = System.currentTimeMillis();
						_timeIndicator.setText(ConverterUtil
								.getTimeString(milisec));
						long test = SystemClock.uptimeMillis();
						_clockHandler.postAtTime(_ticker, test
								+ (1000 - (test % 1000)));
					}
				}
			};
		}
		if (!this._clockStarted) {
			this._clockStarted = true;
			this._ticker.run();
		}
	}

	private void stopClock() {
		this._clockStarted = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this._textView = (TextView) findViewById(R.id.rotateinfo);
		this._textView.setText("");
		this._timeIndicator = (TextView) this.findViewById(R.id.time_indicator);
		this._previewContainer = (FrameLayout) this.findViewById(R.id.previewContainer);
		this._sigQuality = (TextView) this.findViewById(R.id.sigQualTrack);
		this._locInfo = (TextView) this.findViewById(R.id.locationTrack);
		this._rCount = (TextView) this.findViewById(R.id.rawCountTrack);
		
		this._cam_name = (TextView)this.findViewById(R.id.camera_name);
		
		this._camPrev = new CameraManager(this);
		this._rec_button = (ImageView)this.findViewById(R.id.btn_rec);
		
		this._toggleVisibility(this._logView = this.findViewById(R.id.logView));
		this._toggleVisibility(
				this._bw_info_detail = this.findViewById(R.id.bw_info_detail)
				);
		this._toggleVisibility(this._gps_info_detail = this.findViewById(R.id.gps_info_detail));
		
		this._ic_bw_qual = (ImageView)this.findViewById(R.id.ic_bw_qual);
		this._ic_gps_qual = (ImageView)this.findViewById(R.id.ic_gps_qual);
		
		this._switch_mode_view = (ImageView)this.findViewById(R.id.switch_mode_view);
		
		this.createDevice();
		this.startClock();
	}

	@Override
	protected void onPause() {
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		try {
			this.stopLogging();
		} catch (DataSourceException e) {
			this.alert("error on stopLogging" + e.getMessage());
		}
		this.stopLocationListener();
		this.deleteDevice();
		this._camPrev.stopPreview(this._previewContainer);
		this._rec_button.setImageResource(R.drawable.ic_rec_disabled);
		this._rec_button.setEnabled(false);
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		try {
			String cam_name = this._camPrev.startPreview(this._previewContainer);
			if(null != cam_name){
				this._cam_name.setText(cam_name);
			}
		} catch (UIException e) {
			this.insertLog("error on camera preview");
		}
		this.startDevice();
		this.startLocationListener();
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	@Override
	protected void onDestroy() {
		this.deleteDevice();
		super.onDestroy();
	}

	private static Long _deviceTimeDiff = null;

	protected long getEpocTime(Message msg) {
		long mw = msg.getWhen();
		if (null == _deviceTimeDiff) {
			_deviceTimeDiff = System.currentTimeMillis() - mw;
		}
		return mw + _deviceTimeDiff;
	}

	protected final String getWhenString(Message msg) {
		return ConverterUtil.getWhenString(this.getEpocTime(msg));
	}

	public synchronized void doStuff(View view) {
		try {
			if(null == this._writeHelper || null == this._writeHelper.getSession()){
				this.startLogging();
			} else {
				this.stopLogging();
			}
		} catch (DataSourceException e) {
			Toast.makeText(
					this,
					"Operation failed." + System.getProperty("line.separator")
							+ e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	private void _toggleVisibility(View view){
		if(View.VISIBLE == view.getVisibility()){
			view.setVisibility(View.INVISIBLE);
		}else{
			view.setVisibility(View.VISIBLE);
		}
	}
	
	private void _toggleVisibility_org(View view){
		AlphaAnimation anim = null;
		if(0 == view.getAlpha()){
			anim = new AlphaAnimation(0.0f,1.0f);
		}else{
			anim = new AlphaAnimation(1.0f,0.0f);
		}
		anim.setDuration(400);
		anim.setFillAfter(true);
		view.startAnimation(anim);
	}
	
	public synchronized void toggleLogView(View view){
		this._toggleVisibility(this._logView);
	}
	public synchronized void toggle_bw_info_detail(View view){
		this._toggleVisibility(this._bw_info_detail);
	}
	public synchronized void toggle_gps_info_detail(View view){
		this._toggleVisibility(this._gps_info_detail);
	}

	private static class locationListener implements LocationListener {
		private MainActivity _parent = null;
		private LocationManager _manager = null;
		private Criteria _criteria = null;
		private static final long _listener_min_interval = 5000;

		public locationListener(MainActivity parent) {
			super();
			this._parent = parent;
			Criteria c = new Criteria();
//			c.setAccuracy(Criteria.ACCURACY_FINE);
			c.setPowerRequirement(Criteria.POWER_MEDIUM);
//			c.setSpeedRequired(true);
//			c.setBearingRequired(true);
//			c.setAltitudeRequired(true);
			c.setCostAllowed(true);
			this._criteria = c;
		}

		protected LocationManager getLocationManager() {
			if (null == this._manager) {
				this._manager = (LocationManager) this._parent
						.getSystemService(Context.LOCATION_SERVICE);
			}
			return this._manager;
		}

		public void connect() {
			String provider = this.getLocationManager().getBestProvider(this._criteria, true);
			if(null != provider){
				Location loc = this.getLocationManager().getLastKnownLocation(provider);
				if(null != loc && System.currentTimeMillis() < loc.getTime() + 10000){
					this.onLocationChanged(loc);
				}
			}else{
				this._parent.alert("No location provider detected." + System.getProperty("line.separator") + "Check GPS setting.");
			}
			this.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, _listener_min_interval, 0, this);
//			this.getLocationManager().requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, this);
			this.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, _listener_min_interval, 0, this);
//			this.getLocationManager().requestLocationUpdates(5000, 0,this._criteria, this, null);
		}

		public void release() {
			this.getLocationManager().removeUpdates(this);
		}

		private static long _durLimit = 120000L;
		private static long _accurDeltaLimit = 200;
		
		private Handler _handler = new Handler(){

			private Location lastLocation = null;
			
			private synchronized void updateLocation(Location location){
				if(null == this.lastLocation
						|| this.lastLocation.getAccuracy() < location.getAccuracy()
						|| this.lastLocation.getProvider().equals(location.getProvider())
						|| (this.lastLocation.getTime() + _durLimit) < location.getTime()
						|| (!((this.lastLocation.getAccuracy() - _accurDeltaLimit) > location.getAccuracy())) 
						){
					long time = location.getTime();
					if(null != _parent._writeHelper && null != _parent._writeHelper.getSession()){
						try {
							_parent._writeHelper.insertLocation(time, location);
						} catch (DataSourceException e) {
							_parent.insertLog(String.format("exception: %s",
									e.getMessage()));
						}
					}
					_parent.incrementCounter(_parent._locInfo);
					_parent._locInfo.setBackgroundColor(Color.TRANSPARENT);
					_parent.setGPSQuality(Math.max(3 - (int)Math.floor(location.getAccuracy() / 10.0), 0));
				}
			}
			@Override
			public void handleMessage(Message msg) {
				if(MSG_LOCATION == msg.what){
					Location location = (Location)msg.obj;
					if(null != location){
						this.updateLocation(location);
					}
				}
			}
			
		};

		private static final int MSG_LOCATION = 1;
		@Override
		public void onLocationChanged(Location location) {
			/*
			 * this code should be available from API level 17 or above//_ak
			 * if(0 == this._initialTimeInMillis){ this._initialTimeInMillis =
			 * System.currentTimeMillis(); this._initialEstimatedTimeInNanos =
			 * location.getElapsedRealtimeNanos(); } long time =
			 * this._initialTimeInMillis + location.getElapsedRealtimeNanos() -
			 * this._initialEstimatedTimeInNanos;
			 */
			Message.obtain(this._handler, MSG_LOCATION, location).sendToTarget();
		}

		@Override
		public void onProviderDisabled(String provider) {
			this._parent.insertLog(String.format("provider disabled: %s",
					provider));
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			this._parent.insertLog(String.format("status changed: %s, %d",
					provider, status));
		}
	}

	private locationListener _locListener = null;

	protected void startLocationListener() {
		if (null == this._locListener) {
			this._locListener = new locationListener(this);
		}
		this._locListener.connect();
		this._locInfo.setText("0");
		this._locInfo.setBackgroundColor(Color.RED);
		this.setGPSQuality(0);
	}

	protected void stopLocationListener() {
		if (null != this._locListener) {
			this._locListener.release();
		}
	}

	private static final HashMap<Integer, Integer>_level_bw_qual_map = new HashMap<Integer, Integer>(){
		private static final long serialVersionUID = 273970599618427634L;
		{
			put(1, R.drawable.ic_bw_01);
			put(2, R.drawable.ic_bw_02);
			put(3, R.drawable.ic_bw_03);
		}
	};
	private boolean _quality_ok = false;
	private boolean _datafreq_ok = false;
	private void setBWQuality(Boolean q_ok, Boolean f_ok){
		if(null != q_ok){
			this._quality_ok = q_ok;
		}
		if(null != f_ok){
			this._datafreq_ok = f_ok;
		}
		this.setBWQuality(1 + (this._quality_ok ? 1 : 0) + (this._datafreq_ok ? 1 : 0));
	}
	private synchronized void setBWQuality(Integer level){
		Integer id = _level_bw_qual_map.get(level);
		if(null == id){
			id = R.drawable.ic_bw_00;
			this._ic_bw_qual.setBackgroundResource(R.drawable.bkg);
		}else{
			this._ic_bw_qual.setBackgroundDrawable(null);
		}
		this._ic_bw_qual.setImageResource(id);
	}
	
	private static final HashMap<Integer, Integer> _level_gps_qual_map = new HashMap<Integer, Integer>(){
		private static final long serialVersionUID = 2434179612994776108L;
		{
			put(1, R.drawable.ic_gps_01);
			put(2, R.drawable.ic_gps_02);
			put(3, R.drawable.ic_gps_03);
		}
	};
	private static final Timer _gps_qual_update_timer = new Timer();
	private TimerTask _gps_last_update = null;
	private synchronized void setGPSQuality(final Integer level){
		Integer id = _level_gps_qual_map.get(level);
		if(null == id){
			id = R.drawable.ic_gps_00;
			this._ic_gps_qual.setBackgroundResource(R.drawable.bkg);
		}else{
			this._ic_gps_qual.setBackgroundDrawable(null);
		}
		this._ic_gps_qual.setImageResource(id);
		if(null != this._gps_last_update){
			this._gps_last_update.cancel();
		}
		this._gps_last_update = new TimerTask() {
			@Override
			public void run() {
				Handler refresh = new Handler(Looper.getMainLooper());
				refresh.post(new Runnable(){

					@Override
					public void run() {
						if (0 < level) {
							setGPSQuality(level - 1);
						} else {
							setGPSQuality(level);
						}
					}
					
				});
			}
		};
		this._gps_qual_update_timer.schedule(this._gps_last_update, 10000);
	}
	private int incrementCounter(TextView tv) {
		String text = tv.getText().toString();
		Integer val = 0;
		if (0 < text.length()) {
			val = Integer.valueOf(text);
		}
		val++;
		tv.setText(String.valueOf(val));
		return val;
	}
	public void gotoManager(View view){
		this.finish();
		this.overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
	}

    private boolean _cam_started = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.options_menu, menu);
    	return true;
	}
	private void _launchCamPreference(){
		Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
		this.startActivity(intent);		
	}
	public void launchCamPreference(View view){
		this._launchCamPreference();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.cam_settings:
			this._launchCamPreference();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
