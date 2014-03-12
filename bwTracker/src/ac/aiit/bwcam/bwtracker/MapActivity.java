package ac.aiit.bwcam.bwtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataReader;
import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.analysisManager;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.db.DBReader_cache_by_session;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria.accuracy;
import ac.aiit.bwcam.bwtracker.media.IFrameVisitor;
import ac.aiit.bwcam.bwtracker.media.MediaSourceException;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity {
	public static final String iext_name_session_id = "sessionId";

	private GoogleMap _map = null;
	private View _infoview = null;
	private LatLngBounds.Builder _bounds = null;
	private Long _sessionId = null;
	private ProgressBar _progressBar = null;
	private ToggleButton _toggleButton = null;
	private Switch _markerSwitch = null;
	private ImageView _image_marker = null;

	private void setMarkerClickHandler() {
		this._map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				String title = marker.getTitle();// TBD
				LatLng ll = marker.getPosition();
				String snpt = marker.getSnippet();
				String txts[] = snpt.split(",");
				if (txts.length > 1) {
					Long[] vals = new Long[txts.length];
					for (int i = 0; i < txts.length; i++) {
						vals[i] = Long.valueOf(txts[i]);
					}
					Intent intent = new Intent(getApplicationContext(),
							infopanelActivity.class);
					intent.putExtra(infopanelActivity.iext_name_session,
							vals[0]);
					intent.putExtra(infopanelActivity.iext_name_epoc, vals[1]);
					intent.putExtra(infopanelActivity.iext_name_latitude,
							Double.toString(ll.latitude));
					intent.putExtra(infopanelActivity.iext_name_longitude,
							Double.toString(ll.longitude));
					intent.putExtra(infopanelActivity.iext_name_title, title);
					startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private void setInfoWindowAdapter() {
		this._map.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoContents(Marker marker) {
				final String title = marker.getTitle();
				final LatLng ll = marker.getPosition();
				final String snpt = marker.getSnippet();
				if (null == _infoview) {
					_infoview = getLayoutInflater().inflate(
							R.layout.info_window_layout, null);
				}

				View v = _infoview;
				TextView txtitle = (TextView) v
						.findViewById(R.id.map_info_title);
				txtitle.setText(title);
				TextView txlatlng = (TextView) v
						.findViewById(R.id.map_info_label_latlng);
				txlatlng.setText(String.format("lat/lng(%f,%f)", ll.latitude,
						ll.longitude));
				String txts[] = snpt.split(",");
				if (txts.length > 1) {
					Long[] vals = new Long[txts.length];
					for (int i = 0; i < txts.length; i++) {
						vals[i] = Long.valueOf(txts[i]);
					}
					VideoManager.mediaFile mf = VideoManager.getInstance()
							.getMediaFile(vals[0]);
					if (null != mf && mf.isValid()) {
						VideoView video = (VideoView) v
								.findViewById(R.id.map_info_video);
						Long epoc = vals[1];
						Long start = mf.getStartEpoc();
						final int offs = (int) (epoc - start);

						video.setVisibility(View.VISIBLE);
						video.setVideoPath(mf.getAbsolutePath());
						video.seekTo(offs);
						video.start();
					}
					return _infoview;
				} else {
					return null;
				}
			}

			@Override
			public View getInfoWindow(Marker marker) {
				return null;
			}
		});
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Intent intent = this.getIntent();
		this._progressBar = (ProgressBar) this.findViewById(R.id.waitBar);
		this._toggleButton = (ToggleButton)this.findViewById(R.id.bt_toggle);
		this._image_marker = (ImageView)this.findViewById(R.id.image_marker);
		this._toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean checked) {
				for(Marker m : _cur_markers){
					m.setVisible(checked);
				}
				_markerSwitch.setEnabled(checked);
			}
		});
		
		this._markerSwitch = (Switch) this.findViewById(R.id.sw_markers);
		this._markerSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean checked) {
				if(checked){
					if(null == _eeg_markers){
						drawEEGMarkers(_sessionId);
					}
					_cur_markers = _eeg_markers;
				}else{
					_cur_markers = _flat_markers;
				}
				for(Marker m : _flat_markers){
					m.setVisible(_cur_markers == _flat_markers);
				}
				for(Marker m : _eeg_markers){
					m.setVisible(_cur_markers == _eeg_markers);
				}
				
				///TODO* switch the current list and show the selected one and hide the other.
			}
		});
		
		
		try {
			MapsInitializer.initialize(this.getApplicationContext());
			this._sessionId = intent.getLongExtra(iext_name_session_id, 0);
			if (null != this._sessionId && 0 < this._sessionId) {
				MapFragment mapf = (MapFragment) this.getFragmentManager()
						.findFragmentById(R.id.map);
				this._map = mapf.getMap();
				// this.setInfoWindowAdapter();
				this.setMarkerClickHandler();
				// this.drawMarkers(sessionId,true);
			}
		} catch (GooglePlayServicesNotAvailableException e) {
			alert(String.format("error on MapsInitializer\n%s", e.getMessage()));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (null != this._sessionId && 0 < this._sessionId) {
			this.drawMarkersAsync(this._sessionId, true);
			this._toggleButton.setChecked(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	private static class drawLineEntry {
		public LatLng start = null;
		public LatLng end = null;
		public IBWMentalStats mentatlStats = null;

		public drawLineEntry(LatLng start, LatLng end, IBWMentalStats mstats) {
			super();
			this.start = start;
			this.end = end;
			this.mentatlStats = mstats;
		}

		public void draw(GoogleMap map, double ave_attention,
				double ave_meditation, double std_attention,
				double std_meditation) {
			int color = 0xc0000000;
			if (null != this.mentatlStats) {
				// color += Math.round(this.mentatlStats.getAttention() * 255 /
				// 100) * 0x10000;
				// color += Math.round(this.mentatlStats.getMeditation() * 255 /
				// 100);
				color += Math.max(0,
						Math.min(255, (Math.round((this.mentatlStats
								.getAttention() - ave_attention)
								* 50
								/ std_attention + 128)))) * 0x10000;
				color += Math.max(0,
						Math.min(255, (Math.round((this.mentatlStats
								.getMeditation() - ave_attention)
								* 50
								/ std_meditation + 128))));
			} else {
				color = 0xc02ca9e1;
			}
			map.addPolyline((new PolylineOptions()).add(this.start)
					.add(this.end).color(color).width(10).geodesic(true));
		}
	}

	private static class linedrawer {
		private ArrayList<drawLineEntry> dlentries = new ArrayList<drawLineEntry>();
		private long _sum_attention = 0;
		private long _sum_meditation = 0;
		private long _count_stats = 0;

		public linedrawer() {
			super();
		}

		public boolean add(LatLng start, LatLng end, IBWMentalStats mstats) {
			if (null != mstats) {
				this._sum_attention += mstats.getAttention();
				this._sum_meditation += mstats.getMeditation();
				this._count_stats++;
			}
			return dlentries.add(new drawLineEntry(start, end, mstats));
		}

		public void draw(GoogleMap map) {
			long count = (0 != this._count_stats ? this._count_stats : 1);
			double ave_attention = this._sum_attention / count;
			double ave_meditation = this._sum_meditation / count;
			double var_attention = 0;
			double var_meditation = 0;
			for (drawLineEntry dl : dlentries) {
				if (null != dl.mentatlStats) {
					var_attention += Math.pow(
							ave_attention - dl.mentatlStats.getAttention(), 2);
					var_meditation += Math
							.pow(ave_meditation
									- dl.mentatlStats.getMeditation(), 2);
				}
			}
			var_attention = var_attention / count;
			var_meditation = var_meditation / count;
			double std_attention = Math.sqrt(var_attention);
			double std_meditation = Math.sqrt(var_meditation);
			for (drawLineEntry dl : dlentries) {
				dl.draw(map, ave_attention, ave_meditation, std_attention,
						std_meditation);
			}
		}
	}

	private LatLng _lastPos = null;
	private Long _lastMillisec = null;

	private static HashMap<Float, BitmapDescriptor> _defaultMarkers = new HashMap<Float, BitmapDescriptor>();

	private static final synchronized BitmapDescriptor getDefaultMarker(
			Float hue) {
		BitmapDescriptor ret = _defaultMarkers.get(hue);
		if (null == ret) {
			ret = BitmapDescriptorFactory.defaultMarker(hue);
			_defaultMarkers.put(hue, ret);
		}
		return ret;
	}

	private static class drawMarkerParam {
		public long _sessionId;
		public boolean _colorline;

		public drawMarkerParam(long sid, boolean cl) {
			super();
			this._sessionId = sid;
			this._colorline = cl;
		}
	}

	private void drawMarkersAsync(long sessionId, boolean colorline) {
		drawMarkerParam prm = new drawMarkerParam(sessionId, colorline);
		AsyncTask<drawMarkerParam, MarkerOptions, Integer> at = new AsyncTask<drawMarkerParam, MarkerOptions, Integer>() {

			@Override
			protected void onProgressUpdate(MarkerOptions... values) {
				MarkerOptions mo = values[0];
				_flat_markers.add(_map.addMarker(mo));
			}

			@Override
			protected Integer doInBackground(drawMarkerParam... params) {
				drawMarkerParam param = params[0];
				return drawMarkers(param._sessionId, param._colorline,
						new IMarkerWriter() {
							@Override
							public boolean onMarkerArrived(MarkerOptions mo) {
								publishProgress(mo);
								return true;
							}
						});
			}

			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				_progressBar.setVisibility(View.GONE);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				_progressBar.setVisibility(View.VISIBLE);
			}

		};
		at.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, prm);
	}

	private ArrayList<Marker> _flat_markers = new ArrayList<Marker>();
	private ArrayList<Marker> _eeg_markers = null;
	private ArrayList<Marker> _cur_markers = _flat_markers;

	private void drawEEGMarkers(long sessionId){
		this._toggleButton.setEnabled(false);
		this._markerSwitch.setEnabled(false);
		this._eeg_markers = new ArrayList<Marker>();
		final analysisManager mgr = analysisManager.getInstance(this.getApplicationContext(), sessionId);
		final DBReader_cache_by_session lmgr = new DBReader_cache_by_session(getApplicationContext(), sessionId);
		final VideoManager.mediaFile mf = VideoManager.getInstance()
				.getMediaFile(sessionId);
		final ByTimeCriteria croffset = new ByTimeCriteria(this._sessionId, this._sessionId, accuracy.nearest);
		croffset.setLimit(1L);
		Double range = 100.0;
		DistanceCriteria<Double, Double> criteria = new DistanceCriteria(sessionId, range);
		mgr.addAmpElement(criteria, Double.valueOf(100));
		mgr.addWlenElement(criteria, Double.valueOf(200));
		criteria.setLimit(Long.valueOf(30));
		final AsyncTask<DistanceCriteria<Double, Double>, MarkerOptions, DataSourceException> task  = new AsyncTask<DistanceCriteria<Double, Double>, MarkerOptions, DataSourceException>(){

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				_progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onPostExecute(DataSourceException result) {
				_progressBar.setVisibility(View.GONE);
				if(null != result){
					alert(String.format("Exception(%s): %s", result.getClass().getName(), result.getMessage()));
				}else{
					// TODO Auto-generated method stub
				}
				_toggleButton.setEnabled(true);
				_markerSwitch.setEnabled(true);
				super.onPostExecute(result);
			}

			@Override
			protected void onProgressUpdate(MarkerOptions... values) {
				///draw marker
				_eeg_markers.add(_map.addMarker(values[0]));
				super.onProgressUpdate(values);
			}

			@Override
			protected DataSourceException doInBackground(
					DistanceCriteria<Double, Double>... params) {
				DistanceCriteria<Double, Double> dc = params[0];
				try {
					mgr.traverseWaveformDetection(dc, new IWaveformStatsVisitor(){
						@Override
						public void onStart(long sessionId)
								throws VisitorAbortException, DataSourceException {
						}
						@Override
						public boolean onEnd(long sessionId)
								throws VisitorAbortException, DataSourceException {
							return true;
						}
						@Override
						public boolean visit(long sessionId, final IWaveFormStats stats)
								throws VisitorAbortException, DataSourceException {
							croffset.setTime(stats.getEpocTime());
							if(0 < lmgr.searchLocations(croffset, new ILocationVisitor(){
								@Override
								public boolean visit(long sessionId, long millisec,
										Location location)
										throws VisitorAbortException,
										DataSourceException {
									Bitmap bitmap = mf.getFrameByEpoc(stats.getEpocTime());
									Bitmap scaled = Bitmap
											.createScaledBitmap(bitmap,
													80, 80, false);
									MarkerOptions mo = new MarkerOptions()
									.position(new LatLng(location.getLatitude(), location.getLongitude()))
									.icon(BitmapDescriptorFactory
											.fromBitmap(scaled))
									.snippet(String.format("%d,%d"
											, sessionId, millisec))
									.title(String.format("%s (%f)"
											, ConverterUtil.getShortWhenString(millisec), location.getAccuracy()));
									publishProgress(mo);
									return true;
								}				
							})){
								return true;
							}else{
								return false;
							}
						}
					});
				} catch (DataSourceException e) {
					return e;
				}
				return null;
			}
			
		};
		task.execute(criteria);


	}
	public void toggleMarkers(View view) {
		if (this._flat_markers.size() > 0) {
			boolean visibility = !(this._flat_markers.get(0).isVisible());
			for (Marker m : this._flat_markers) {
				m.setVisible(visibility);
			}
		}
	}

	private static interface IMarkerWriter {
		public boolean onMarkerArrived(MarkerOptions mo);
	}
	private int drawMarkers(long sessionId, boolean colorline,
			final IMarkerWriter mwriter) {
		final BitmapDescriptor red = getDefaultMarker(BitmapDescriptorFactory.HUE_RED);
		final BitmapDescriptor green = getDefaultMarker(BitmapDescriptorFactory.HUE_GREEN);
		final BitmapDescriptor azure = getDefaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		this._flat_markers.clear();

		final IBWDataReader reader = new DBReader_cache_by_session(getApplicationContext(), sessionId);
		this._bounds = new LatLngBounds.Builder();
		this._lastPos = null;
		this._lastMillisec = null;
		final boolean colorByMental = colorline;
		int ret = 0;
		try {
			int points = 0;
			final HashMap<Long, MarkerOptions> markers = new HashMap<Long, MarkerOptions>();
			final linedrawer ldrawer = new linedrawer();
			VideoManager.mediaFile mf = VideoManager.getInstance()
					.getMediaFile(sessionId);
			long time = System.currentTimeMillis();
			if (0 < (points = reader.searchLocations(new bwSessionCriteria(
					sessionId), new ILocationVisitor() {
				@Override
				public boolean visit(long sessionId, long millisec,
						Location location) throws VisitorAbortException,
						DataSourceException {
					if (0 < millisec && 20 > location.getAccuracy()) {
						// /*TODO*
						// need to guess the appropriate position here instead
						// of just filtering by accuracy..
						final LatLng loc = new LatLng(location.getLatitude(),
								location.getLongitude());
						if (true){//(8 > location.getAccuracy()) {
							// /*TODO* need to put as candidate, sort by some
							// interests and show the final candidates in
							// visible area.
							markers.put(
									Long.valueOf(millisec),
									(new MarkerOptions())
											.position(loc)
											.snippet(
													String.format("%d,%d",
															sessionId, millisec))
											// .snippet(loc.toString())
											.title(String
													.format("%s (%f)",
															ConverterUtil
																	.getShortWhenString(millisec),
															location.getAccuracy())));
							// _map.addMarker(
							// .icon(null == _lastPos ? red : azure));
						}
						if (null != _lastPos) {
							if (!colorByMental
									|| 0 == reader.searchMentalStats(
											new IntervalCriteria(sessionId,
													_lastMillisec, millisec),
											new IMentalStatsVisitor() {
												@Override
												public boolean visit(
														long sessinId,
														IBWMentalStats stat)
														throws VisitorAbortException,
														DataSourceException {
													ldrawer.add(_lastPos, loc,
															stat);
													// int color = 0xc0001000;
													// color +=
													// Math.round(stat.getAttention()
													// * 255 / 100) * 0x10000;
													// color +=
													// Math.round(stat.getMeditation()
													// * 255 / 100);
													// _map.addPolyline((new
													// PolylineOptions())
													// .add(_lastPos).add(loc)
													// .color(color).width(10)
													// .geodesic(true));
													return true;
												}
											})) {
								// _map.addPolyline((new PolylineOptions())
								// .add(_lastPos).add(loc)
								// .color(0xc02ca9e1).width(10)
								// .geodesic(true));
								ldrawer.add(_lastPos, loc, null);
							}
						}
						_lastPos = loc;
						_lastMillisec = millisec;
						_bounds.include(loc);
						return true;
					}
					return false;
				}
			}))) {
				final long dur = System.currentTimeMillis() - time;
				final int pts = points;
				this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						_map.moveCamera(CameraUpdateFactory.newLatLngBounds(
								_bounds.build(), 40));

						ldrawer.draw(_map);

						if (1 < pts) {
							/*
							 * last position marker _map.addMarker((new
							 * MarkerOptions()) .position(_lastPos)
							 * .title(ConverterUtil
							 * .getShortWhenString(_lastMillisec))
							 * .snippet(_lastPos.toString()).icon(green));
							 */
						}
						alert(String.format("took %d msec", dur));
					}

				});
				if (null != mf && mf.isValid()) {
					try {
						Set<Long> epocs = markers.keySet();
						int interval = Math.round(epocs.size() / 30);
						if(0 >= interval){
							interval = 1;
						}
						int count = 0;
						ArrayList<Long> epcs = new ArrayList<Long>();
						for(Long epc : epocs){
							if(0 == count % interval){
								epcs.add(epc);
							}
							count++;
						}
						ret = mf.visitFramesByEpoc(epcs,
								new IFrameVisitor() {

									@Override
									public boolean visitBitmap(Long offset,
											Long epoc, Bitmap bitmap)
											throws VisitorAbortException,
											MediaSourceException {
										final MarkerOptions mo = markers
												.get(epoc);
										if (null != mo) {
											Bitmap scaled = Bitmap
													.createScaledBitmap(bitmap,
															80, 80, false);
											mo.icon(BitmapDescriptorFactory
													.fromBitmap(scaled));
											return mwriter.onMarkerArrived(mo);
										}
										return false;
									}
								});
					} catch (final MediaSourceException e) {
						this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								alert(String.format(
										"error on getting maker icons\n%s",
										e.getMessage()));
							}

						});
					}
				} else {
					for (MarkerOptions mo : markers.values()) {
						mo.icon(null == _lastPos ? red : azure);
						ret += (mwriter.onMarkerArrived(mo) ? 1 : 0);
					}
				}

			}
		} catch (DataSourceException e) {
			alert(String.format("error on drawing markers\n%s", e.getMessage()));
		}
		return ret;
	}

	private void alert(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
	}

}
