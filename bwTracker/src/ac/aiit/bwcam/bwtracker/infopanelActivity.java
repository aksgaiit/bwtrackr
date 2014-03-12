package ac.aiit.bwcam.bwtracker;

import org.restlet.Response;

import ac.aiit.bwcam.bwtracker.media.VideoManager;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.impl.MomentRequest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class infopanelActivity extends Activity {
	public static final String iext_name_session = "session";
	public static final String iext_name_epoc = "epoc";
	public static final String iext_name_latitude = "lat";
	public static final String iext_name_longitude = "lng";
	public static final String iext_name_title = "title";
	
	private TextView _title = null;
	private TextView _snippet = null;
	private VideoView _video = null;
	private TextView _v_offs = null;
	private ProgressBar _p_loading = null;
	private LinearLayout _container = null;
	
	private Long _session = null;
	private Long _epoc = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_window_layout);
		this._title = (TextView)this.findViewById(R.id.map_info_title);
		this._snippet = (TextView)this.findViewById(R.id.map_info_label_latlng);
		this._video = (VideoView)this.findViewById(R.id.map_info_video);
		this._v_offs = (TextView)this.findViewById(R.id.map_info_video_offs);
		this._container = (LinearLayout)this.findViewById(R.id.map_info_container);
		this._p_loading = (ProgressBar)this.findViewById(R.id.if_waitBar);
		
		Intent intent = this.getIntent();
		this._session = intent.getLongExtra(iext_name_session, 0);
		this._epoc = intent.getLongExtra(iext_name_epoc, 0);
		String s_lat = intent.getStringExtra(iext_name_latitude);
		Double lat = null;
		if(null != s_lat){
			lat = Double.parseDouble(s_lat);
		}
		String s_lng = intent.getStringExtra(iext_name_longitude);
		Double lng = null;
		if(null != s_lng){
			lng = Double.parseDouble(s_lng);
		}
		String title = intent.getStringExtra(iext_name_title);
		
		if(0 < this._session && 0 < this._epoc){
			this._title.setText(title);
			this._snippet.setText(
					(null != lat && null != lng) ? String.format("lat/lng(%f,%f)", lat, lng) : ""
					);
			VideoManager.mediaFile mf = VideoManager.getInstance().getMediaFile(this._session);
			if(null != mf && mf.isValid()){
				Long start = mf.getStartEpoc();
				int offs = (int)(this._epoc - start);
				this._video.setVideoPath(mf.getAbsolutePath());
				this._video.seekTo(offs);
				final Window w = this.getWindow();
				this._video.setOnPreparedListener(new OnPreparedListener (){

					@Override
					public void onPrepared(MediaPlayer mp) {
						Display display = getWindowManager().getDefaultDisplay();
						int maxHeight = display.getHeight() -  _title.getHeight() - _snippet.getHeight();
						int maxWidth = display.getWidth();
						int ht = mp.getVideoHeight();
						int wt = mp.getVideoWidth();
						float hr = (float)maxHeight / (float)ht;
						float wr = (float)maxWidth / (float)wt;
						float rt = hr;
						boolean resizeht = true;
						if(Math.abs(hr - 1) > Math.abs(wr - 1) ){
							rt = wr;
							resizeht = false;
						}
//						w.setLayout(resizeht ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT, resizeht ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT);
//						_container.setScaleX(rt);
//						_container.setScaleY(rt);
//						_video.setScaleX(rt);
//						_video.setScaleY(rt);
						hideLoading();
						_video.start();
					}
					
				});
			}
		}else{
			Toast.makeText(this, "invalid session or offset", Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}
	
	private void showLoading(){
		this._p_loading.setVisibility(View.VISIBLE);
		this._p_loading.bringToFront();
	}
	private void hideLoading(){
		this._p_loading.setVisibility(View.GONE);
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
						long milisec = _video.getCurrentPosition();
						_v_offs.setText(Long.toString(milisec / 1000));
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
	protected void onStart() {
		super.onStart();
		this._video.setZOrderOnTop(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.stopClock();
		this._video.pause();
		this.showLoading();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.hideLoading();
		this._video.resume();
		this.startClock();
	}
	public void shareMoment(View view){
		MomentRequest req = new MomentRequest(this.getApplicationContext(), this._session, this._epoc);
		askCommentDialogFragment df = askCommentDialogFragment.getInstance(req);
		df.show(this.getFragmentManager(), "Share with comment");
	}
}
