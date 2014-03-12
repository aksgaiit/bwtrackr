package ac.aiit.bwcam.bwtracker;

import java.net.SocketException;
import java.util.List;

import ac.aiit.bwcam.bwtracker.neuro.ITGStateListener.TGError;
import ac.aiit.bwcam.rest.RESTService;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.neurosky.thinkgear.TGDevice;

public class wbVisualizationActivity extends Activity  implements SensorEventListener{
	public static final String iext_name_session_id = "sessionId";
	
	private WebView _webView = null;
	private TextView _msgText = null;
	private ProgressBar _attGraph = null;
	private ProgressBar _medGraph = null;
	private Long _sessionId = null;
	private boolean _enableSensor = false;
	
	private SensorManager _manager = null;
	
	private RESTService _rest = null;

	public static class messageTracker{
		private wbVisualizationActivity _parent = null;
		public messageTracker(wbVisualizationActivity parent){
			super();
			this._parent = parent;
		}
		@JavascriptInterface
		public void onThumbnailSelect(String s_epoc){
			this._parent.showInfoPanel(Long.valueOf(s_epoc));
		}
		@JavascriptInterface
		public void onSensorReady(){
			this._parent.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					_parent.moveToCenter();
				}
				
			});
			this._parent.setSensorReady(true);
		}
	}
	private void moveToCenter(){
		int height = this._webView.getHeight()/2;//TODO*get metrics of body in webview
		int width = this._webView.getWidth()/2;//TODO*get metrics of body in webview
		this._webView.scrollTo(Math.max((600 - width)/2, 0), Math.max((600 - height)/2, 0));
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webvis);
		this._sessionId = this.getIntent().getLongExtra(iext_name_session_id,0);
		this._manager = (SensorManager)getSystemService(SENSOR_SERVICE);
		if(null != this._sessionId && 0 < this._sessionId){
			this._webView = (WebView)this.findViewById(R.id.webView1);
			this._webView.getSettings().setJavaScriptEnabled(true);
			this._webView.getSettings().setAppCacheEnabled(true);
			this._webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			this._webView.getSettings().setUseWideViewPort(true);
			this._webView.getSettings().setLoadWithOverviewMode(true);
			this._webView.setWebViewClient(new WebViewClient());
			this._webView.addJavascriptInterface(new messageTracker(this), "__msgbroker");
			this._msgText = (TextView)this.findViewById(R.id.wvAddress);
		}
		this._attGraph = (ProgressBar)this.findViewById(R.id.attGraph);
		this._attGraph.setMax(100);
		this._attGraph.setRotation(180);
		this._attGraph.setEnabled(false);
		this._medGraph = (ProgressBar)this.findViewById(R.id.medGraph);
		this._medGraph.setMax(100);
		this._medGraph.setEnabled(false);
		ToggleButton toggleSensor = (ToggleButton)this.findViewById(R.id.toggleSensor);
		this._enableSensor = toggleSensor.isChecked();
		toggleSensor.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				_enableSensor = isChecked;				
			}
		});
		
		this._rest = RESTService.getInstance();
		if(!this._rest.isStarted()){
			this._rest.start();
		}
	}
	
	
	@Override
	protected void onDestroy() {
		if(null != this._rest){
			this._rest.stop();
			this._rest = null;
		}

		super.onDestroy();
	}


	private synchronized void onMentalStateChanged(){
		if(null != this._curAttention && null != this._curMeditation){
			this._attGraph.setProgress(this._curAttention);
			this._medGraph.setProgress(this._curMeditation);
			if(this._sensorReady && this._enableSensor){
				this._webView.loadUrl(String.format("javascript:__handleStats({med:%d,att:%d});", this._curMeditation, this._curAttention));
			}
			this._curAttention = this._curMeditation = null;
		}
	}
	private Integer _curAttention = null;
	private void setAttention(int val){
		this._curAttention = Integer.valueOf(val);
		this.onMentalStateChanged();
	}
	private Integer _curMeditation = null;
	private void setMeditation(int val){
		this._curMeditation = Integer.valueOf(val);
		this.onMentalStateChanged();
	}
	private void onBlink(int val){
		//TODO*load blink effect on the page.
	}
	private void showInfoPanel(Long epoc){
		Intent intent = new Intent(getApplicationContext(), infopanelActivity.class);
		intent.putExtra(infopanelActivity.iext_name_session, this._sessionId);
		intent.putExtra(infopanelActivity.iext_name_epoc, epoc);
//		intent.putExtra(infopanelActivity.iext_name_latitude, Double.toString(ll.latitude));
//		intent.putExtra(infopanelActivity.iext_name_longitude, Double.toString(ll.longitude));
		intent.putExtra(infopanelActivity.iext_name_title, "test");
		this.startActivity(intent);
	}

	private boolean _sensorReady = false;
	public void setSensorReady(boolean flag){
		this._sensorReady = flag;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this._webView.clearCache(true);
		boolean isgradient = SrvSettingsActivity.isEEGViewGradient(this.getApplicationContext());
//		this._webView.loadUrl(String.format("http://localhost:58121/file?name=css3d_periodictable.html#sid=%d",this._sessionId));
		this._webView.loadUrl(String.format("http://localhost:58121/file?name=bw_bforce.html#sid=%d",this._sessionId) + (isgradient ? "&cls=gradient" : ""));
		if(null != _msgText){
			try{
				_msgText.setText(String.format("%s s=%d", RESTService.getExternalIPAddr(), this._sessionId));
			}catch(SocketException e){
				_msgText.setText("IP error");
			}
		}
	}
	private TGDevice _tgDevice = null;
	private BluetoothAdapter bluetoothAdapter = null;
	private boolean _tgAutoConnect = false;
	private boolean _tgInflight = false;
	private boolean _tgAutoConnecting = false;

	protected void onTGConnect(){
		this._tgInflight = false;
		if(null != this._tgDevice){
			this._tgDevice.start();
		}
	}
	protected synchronized void onTGDisconnect(){
		this._tgInflight = false;
		if(null != this._tgDevice){
			this._tgDevice.stop();
			this._tgDevice.close();
			this._tgDevice = null;
		}
		if(this._tgAutoConnect && !this._tgAutoConnecting){
			this._tgAutoConnecting = true;
			(new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
					_tgAutoConnecting = false;
					startTGAM();
				}
			})).start();
		}
	}
	protected void onTGError(TGError err){
		this._tgInflight = false;
		this._tgAutoConnect = false;
		if(null != this._tgDevice){
			this._tgDevice.stop();
			this._tgDevice.close();
		}
	}
	public void tryTGAM(View view){
		this._tgAutoConnect = true;
		this.startTGAM();
	}
	private synchronized void startTGAM(){
		if (null == this.bluetoothAdapter) {
			this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		if (this.bluetoothAdapter == null) {
			// Alert user that Bluetooth is not available
			Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG)
					.show();
//			finish();
			return;
		} else {
			if (null == this._tgDevice) {
				/* create the TGDevice */
				this._tgDevice = new TGDevice(this.bluetoothAdapter,
						new Handler() {
							public void handleMessage(Message msg) {
								try {
									switch (msg.what) {
									case TGDevice.MSG_STATE_CHANGE:
										switch (msg.arg1) {
										case TGDevice.STATE_IDLE:
											// this._parent.onDisconnect();
											break;
										case TGDevice.STATE_CONNECTED:
											onTGConnect();
											break;
										case TGDevice.STATE_NOT_FOUND:
											onTGError(TGError.not_found);
											break;
										case TGDevice.STATE_NOT_PAIRED:
											onTGError(TGError.not_paired);
											break;
										case TGDevice.STATE_DISCONNECTED:
											onTGDisconnect();
											break;
										}

										break;
									case TGDevice.MSG_POOR_SIGNAL:
										// SIGQUALITY
										break;
									case TGDevice.MSG_ATTENTION:
										setAttention(msg.arg1);
										break;
									case TGDevice.MSG_MEDITATION:
										setMeditation(msg.arg1);
										break;
									case TGDevice.MSG_BLINK:
										onBlink(msg.arg1);
										break;
									case TGDevice.MSG_LOW_BATTERY:
										// alert
										break;
									default:
										break;
									}
								} finally {

								}
							}

						});
			}
			if(!this._tgInflight){
				this._tgInflight = true;
				this._tgDevice.connect(false);//no raw data required here.
			}
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		this._manager.unregisterListener(this);
		this._tgAutoConnect = false;
		this.onTGDisconnect();
	}
	private void registerSensor(){
		Sensor sensor = this._manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		//TODO* no gravity sensor...
		if(null != sensor){
			this._manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		}else{
			sensor = this._manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if(null != sensor){
				this._manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
			}
		}
	}
	private Integer _orientation = null;
	private Integer _rotation = null;
	@Override
	protected void onResume() {
		super.onResume();
		this._orientation = this.getResources().getConfiguration().orientation;
		this._rotation = this.getWindowManager().getDefaultDisplay().getRotation();

		this.registerSensor();
		this._tgAutoConnect = true;
		this.startTGAM();
	}
	@Override
	protected void onStop() {
		super.onStop();
		RESTService rest = RESTService.getInstance();
		if(rest.isStarted()){
			rest.stop();
		}
	}
	
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(R.anim.right_in, R.anim.left_out);//(R.anim.top_in, R.anim.bottom_out);
	}
	private float[] _prevVals = null;
	private float[] _initVals = null;
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	private long __lasttick = 0;//in nano sec
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (false//event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				|| !this._sensorReady
				|| 600000 > event.timestamp - __lasttick) return;
		this.__lasttick = event.timestamp;
		int type = event.sensor.getType();
		float rev = ((Configuration.ORIENTATION_LANDSCAPE == this._orientation && (Surface.ROTATION_270 == this._rotation || Surface.ROTATION_180 == this._rotation))
				||(Configuration.ORIENTATION_PORTRAIT == this._orientation && (Surface.ROTATION_180 == this._rotation || Surface.ROTATION_90 == this._rotation))) ? -1.0f : 1.0f;
		if(Sensor.TYPE_GRAVITY == type || Sensor.TYPE_ACCELEROMETER == type){
			if(null == this._initVals){
				this._initVals = event.values.clone();
			}
			Float gx = null;
			Float gy = null;
			if(Configuration.ORIENTATION_LANDSCAPE == this._orientation){
				gx = -1 * rev * event.values[1];
				gy = -1 * rev * event.values[0];
			}else{
				gx =  rev * event.values[0];
				gy = -1 * rev * event.values[1];
			}
			this._webView.loadUrl(String.format("javascript:__handleRotation({p:%s,r:%s});",gy.toString(), gx.toString()));
		}
	}
	public void launchPreference(View view){
		Intent intent = new Intent(this.getApplicationContext(), SrvSettingsActivity.class);
		this.startActivity(intent);
	}
}
