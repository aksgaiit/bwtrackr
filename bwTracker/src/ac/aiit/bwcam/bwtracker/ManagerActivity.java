package ac.aiit.bwcam.bwtracker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.ISessionStatistics;
import ac.aiit.bwcam.bwtracker.data.ISessionStatisticsVisitor;
import ac.aiit.bwcam.bwtracker.data.SessionStatisticsBase;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.analysisManager;
import ac.aiit.bwcam.bwtracker.data.db.DBManager;
import ac.aiit.bwcam.bwtracker.data.db.table.sessionMillisecTableHelperBase.epocDuration;
import ac.aiit.bwcam.bwtracker.data.db.util.DBUtils;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest.IExceptionHandler;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest.IResponseHandler;
import ac.aiit.bwcam.bwtracker.share.apache.impl.MentalStatsRequest;
import ac.aiit.bwcam.bwtracker.share.apache.impl.RequestBase;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ManagerActivity extends Activity  {
	
	private ListView _dataListView = null;
	private ProgressBar _loading = null;
	
	private View _operationArea = null;
	
	private sessionStatisticsAdapter _dataList = null;
	
	private static class clickToOpenWebVisualization implements OnClickListener{
		private ManagerActivity _parent = null;

		public clickToOpenWebVisualization(ManagerActivity parent){
			super();
			this._parent = parent;
		}
		@Override
		public void onClick(View view) {
			ISessionStatistics elem = (ISessionStatistics)view.getTag();
			if(null != elem){
				this._parent._launcEEGView(elem.getId());
			}
		}
		
	}
	
	private static class sessionStatisticsCache extends SessionStatisticsBase{
		private Context _ctx = null;
		public sessionStatisticsCache(ISessionStatistics other, Context ctx){
			super(other);
			this._ctx = ctx;
			this.init_ext_values();
		}
		private Bitmap _thumbnail = null;
		private epocDuration _cacheDuration = null;
		private void init_ext_values(){
			VideoManager.mediaFile mf = VideoManager.getInstance().getMediaFile(this.getId());
			if(null != mf){
				this._thumbnail = mf.createVideoThumbnail();
			}
			analysisManager amgr = analysisManager.getInstance(this._ctx, this.getId());
			if(null != amgr){
				this._cacheDuration = amgr.getWaveFormCacheDuration(this.getId());
			}

		}
		public Bitmap getThumbnail(){
			return this._thumbnail;
		}
		public epocDuration getCacheDuration(){
			return this._cacheDuration;
		}
	}

	private static class sessionStatisticsAdapter extends ArrayAdapter<sessionStatisticsCache>{
		private LayoutInflater _inflater = null;
		private HashSet<Long> _selectedSession = null;
		private ManagerActivity _parent = null;

		public sessionStatisticsAdapter(ManagerActivity parent) {
			super(parent, R.layout.mgr_row_session);
			this._parent = parent;
			this._inflater = (LayoutInflater) parent
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this._selectedSession = new HashSet<Long>();
		}
		
		public Set<Long> getSelectedSessions(){
			return this._selectedSession;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			sessionStatisticsCache item = this.getItem(position);
			View ret = this._inflater.inflate(R.layout.mgr_row_session, parent, false);
			TextView session = (TextView) ret.findViewById(R.id.startTime);
			session.setText(ConverterUtil.getAbbreviateWhenString(item.getId()));
			
			TextView duration = (TextView) ret.findViewById(R.id.duration);
			duration.setText(String.format("%d sec.",item.getDuration()/1000));
			
			ImageView imageView = (ImageView) ret.findViewById(R.id.thumbnail);
			Bitmap thumbnail = item.getThumbnail();
			if(null != thumbnail){
				imageView.setImageBitmap(thumbnail);
			}
			
			CheckBox cb = (CheckBox)ret.findViewById(R.id.row_select);
			cb.setTag(Long.valueOf(item.getId()));
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton self,
						boolean checked) {
					if(checked){
						_selectedSession.add((Long)self.getTag());
					}else{
						_selectedSession.remove((Long)self.getTag());
					}
				}
			});
			View ch = ret.findViewById(R.id.row_clickhandle);
			ch.setTag(item);
			ch.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					ISessionStatistics elem = (ISessionStatistics)view.getTag();
					if(null != elem){
						_parent._launchMapView(elem.getId());
					}
				}
			});
			View toMapView = ret.findViewById(R.id.imageMapView);
			toMapView.setTag(item);
			toMapView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					ISessionStatistics elem = (ISessionStatistics)view.getTag();
					if(null != elem){
						_parent._launchMapView(elem.getId());
					}
				}
			});
			
			ProgressBar aprogress = (ProgressBar)ret.findViewById(R.id.aProgress);
			aprogress.setMax((int)item.getDuration());
			epocDuration dur = item.getCacheDuration();
			View pbar = ret.findViewById(R.id.progressContainer);
			ImageView toWebVisualization = (ImageView)ret.findViewById(R.id.imageWebView);
			if(null != dur && dur.getEnd() >= (dur.getStart() + item.getDuration() - 500)){//*TODO* to be revised
				toWebVisualization.setVisibility(View.VISIBLE);
				toWebVisualization.setTag(item);
				toWebVisualization.setOnClickListener(new clickToOpenWebVisualization(_parent));
				aprogress.setOnClickListener(null);
				aprogress.setVisibility(View.GONE);
				ret.findViewById(R.id.aProgressText).setVisibility(View.GONE);
			}else{
				pbar.setTag(item);
				toWebVisualization.setVisibility(View.GONE);
				pbar.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						view.setOnClickListener(null);
						ISessionStatistics stats = (ISessionStatistics)view.getTag();
						(new analysisTask(_parent, getContext(), stats, view)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0L);
					}
				});
			}
			final ImageView shareImage = (ImageView)ret.findViewById(R.id.imageShare);
			final ProgressBar shareProgress = (ProgressBar)ret.findViewById(R.id.progressShare);
			shareImage.setTag(item);
			shareImage.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					RequestBase request = new MentalStatsRequest/* new PublicMomentsRequest*/(getContext(), ((ISessionStatistics)v.getTag()).getId());
					try {
						shareImage.setVisibility(View.INVISIBLE);
						shareProgress.setVisibility(View.VISIBLE);
						request.submit(new IResponseHandler(){

							@Override
							public void onResponse(HttpResponse response) {
								Toast.makeText(getContext(), "request successfully saved!"
										, Toast.LENGTH_LONG).show();
								shareImage.setVisibility(View.VISIBLE);
								shareProgress.setVisibility(View.GONE);
							}
							
						}, new IExceptionHandler(){

							@Override
							public void onException(Throwable e) {
								Toast.makeText(getContext(), String.format("RequestExcetion %s", e.getMessage())
										, Toast.LENGTH_LONG).show();
								shareImage.setVisibility(View.VISIBLE);
								shareProgress.setVisibility(View.GONE);
							}
							
						}, null);
					} catch (RequestException e) {
						Toast.makeText(getContext(), String.format("RequestExcetion %s", e.getMessage())
								, Toast.LENGTH_LONG).show();
						shareImage.setVisibility(View.VISIBLE);
						shareProgress.setVisibility(View.GONE);
					}
					
				}
			});
			return ret;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manager);
		this._dataListView = (ListView)this.findViewById(R.id.dataList);
		this._loading = (ProgressBar)this.findViewById(R.id.waitBar);
		this._dataList = new sessionStatisticsAdapter(this);
		this._dataListView.setAdapter(this._dataList);
		this._dataListView.setLongClickable(true);
		this._dataListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterview,
					View view, int i, long l) {
				toggleOperationArea(view);
				return true;
			}
			
		});
		this._operationArea = (View)this.findViewById(R.id.operation_area);
	}
	@Override
	protected void onStart() {
		super.onStart();
		try {
			this.updateList();
		} catch (DataSourceException e) {
			Toast.makeText(
					this,
					"Error on getting sessions" + System.getProperty("line.separator")
							+ e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public void toggleOperationArea(View view){
		if(View.VISIBLE == this._operationArea.getVisibility()){
			this._operationArea.setVisibility(View.GONE);
		}else{
			this._operationArea.setVisibility(View.VISIBLE);
		}
	}
	private void alert(String txt){
		Toast.makeText(this, txt,Toast.LENGTH_LONG).show();
	}

	private void updateList() throws DataSourceException{
		this._dataList.clear();
		AsyncTask<Long, sessionStatisticsCache, Long> at = new AsyncTask<Long, sessionStatisticsCache, Long>() {

			@Override
			protected Long doInBackground(Long... params) {
				DBManager mgr = new DBManager(getApplicationContext());
				long ret = 0;
				try {
					ret = (long)mgr.visitSessionStatistics(new ISessionStatisticsVisitor() {

						@Override
						public boolean visit(ISessionStatistics info)
								throws DataSourceException,
								VisitorAbortException {
							publishProgress(new sessionStatisticsCache(info, getApplicationContext()));
							return true;
						}

					});
				}catch (DataSourceException e) {
					alert(String.format(
							"error on getting session statistics\n%s",
							e.getMessage()));
				}
				return ret;
			}

			@Override
			protected void onProgressUpdate(sessionStatisticsCache... ifos) {
				_dataList.add(ifos[0]);
				_dataList.notifyDataSetChanged();
//				_dataListView.invalidate();
			}

			@Override
			protected void onPostExecute(Long result) {
//				_dataListView.setVisibility(View.VISIBLE);
				_loading.setVisibility(View.GONE);
				if(0 >= result){
					alert("No record found.\n moving to recorder..");
					_launchRecorder();
				}
//				_dataList.notifyDataSetChanged();
			}

			@Override
			protected void onPreExecute() {
				_loading.setVisibility(View.VISIBLE);
//				_dataListView.setVisibility(View.GONE);
			}

		};
		at.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0L);
/*		DBManager mgr = new DBManager(this.getApplicationContext());
		try {
			mgr.visitSessionStatistics(new ISessionStatisticsVisitor(){

				@Override
				public boolean visit(ISessionStatistics info)
						throws DataSourceException, VisitorAbortException {
					_dataList.add(new stringfySessionStatistics(info));
					return true;
				}
				
			});
		} catch (VisitorAbortException e) {
		}
		this._dataList.notifyDataSetChanged();*/
	}

	private static final String fmt_backup_path = Environment.getExternalStorageDirectory().getPath() + "/ac.aiit.bwcam.bwtracker/%s";
	private static final String fn_db_backup = "bwtracker_db_dump.db3";
	private static final String fn_cache_backup = "bwtracker_cache.db3";

	public void dump(View view) {
		try {
			DBManager mgr = new DBManager(this.getApplicationContext());
			String path = String.format(fmt_backup_path, fn_db_backup);
			mgr.dump(path);
			DBUtils.dumpDatabase(this.getApplicationContext(), analysisManager.DBNAME_Cache
					, String.format(fmt_backup_path, fn_cache_backup));
			Toast.makeText(this, "DB dump finished\n in " + path, Toast.LENGTH_LONG)
					.show();
		} catch (DataSourceException e) {
			Toast.makeText(this, "DB dump ERROR", Toast.LENGTH_LONG).show();
		}
	}
	public void restore(View view){
		try {
			DBManager mgr = new DBManager(this.getApplicationContext());
			String path = String.format(fmt_backup_path, fn_db_backup);
			mgr.restore(path);
			DBUtils.restoreDatabase(this.getApplicationContext(), analysisManager.DBNAME_Cache
					, String.format(fmt_backup_path, fn_cache_backup));
			this.updateList();
			Toast.makeText(this, "DB restore finished\n" + path, Toast.LENGTH_LONG)
					.show();
		} catch (DataSourceException e) {
			Toast.makeText(this, "DB restore ERROR", Toast.LENGTH_LONG).show();
		}
	}
	public void merge(View view){
		//*TODO import the data in the tables of external sqlite database file.
	}
	private static abstract class deletionTask extends AsyncTask<Long[], Integer, Object>{
		protected ManagerActivity _parent = null;
		protected DBManager _mgr = null;
		protected ProgressDialog _pdialog = null;
		public deletionTask(ManagerActivity parent){
			super();
			this._parent = parent;
			this._mgr =  new DBManager(parent.getApplicationContext());
			this._pdialog = new ProgressDialog(this._parent);
		}

		@Override
		protected void onPostExecute(Object oresult) {
			if(oresult instanceof Integer){
				Integer result = (Integer)oresult;
				Toast.makeText(
						this._parent,
						String.format(
								"Processed records for %d sessions",result),
						Toast.LENGTH_LONG).show();
			}else if(oresult instanceof Exception){
				Exception e = (Exception) oresult;
				Toast.makeText(
						this._parent,
						"Error on processing records." + System.getProperty("line.separator")
								+ e.getMessage(), Toast.LENGTH_LONG).show();
			}
			try {
				this._parent.updateList();
			} catch (DataSourceException e) {
				Toast.makeText(
						this._parent,
						"Error on update list." + System.getProperty("line.separator")
								+ e.getMessage(), Toast.LENGTH_LONG).show();
			}
			this._pdialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			this._pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this._pdialog.setIndeterminate(true);
			this._pdialog.setCanceledOnTouchOutside(false);
			this._pdialog.setCancelable(false);
			this._pdialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			this._pdialog.incrementProgressBy(values[0]);
		}

		abstract protected boolean processSession(Long session) throws DataSourceException;
		abstract protected int getProgressPerSession();
		@Override
		protected Object doInBackground(Long[]... params) {
			this._pdialog.setIndeterminate(false);
			try {
				Long[] sessionIds = params[0];
				Integer num = 0;
				if(0 < sessionIds.length){
					this._pdialog.setMax(sessionIds.length * this.getProgressPerSession());
					for(Long id: sessionIds){
						if(this.processSession(id)){
							num ++;
						}
					}
				}
				return num;
			} catch (DataSourceException e) {
				return e;
			}
		}
	}

	private synchronized void deleteRecords(Collection<Long> sessionIds){
		if(null == sessionIds){
			return;
		}
		(new deletionTask(this){
			@Override
			protected boolean processSession(Long session)
					throws DataSourceException {
				this._mgr.deleteRecords(session);
				this.publishProgress(2);
				analysisManager amgr = analysisManager.getInstance(this._parent.getApplicationContext(), session);
				amgr.deleteWaveformCache();
				this.publishProgress(2);
				amgr.deleteLPFdata();
				this.publishProgress(2);
				VideoManager.mediaFile mf = VideoManager.getInstance()
						.getMediaFile(session);
				if(null != mf){
					mf.deleteFile();
				}
				this.publishProgress(1);
				return true;
			}

			@Override
			protected int getProgressPerSession() {
				return 7;
			}
		}).execute(sessionIds.toArray(new Long[sessionIds.size()]));
	}
	private AlertDialog _deleteConfirm = null;
	public void delete(View view) {
		if(!_dataList.getSelectedSessions().isEmpty()){
			if(null == this._deleteConfirm){
				   AlertDialog.Builder builder = new AlertDialog.Builder(this);

			        builder.setTitle(R.string.title_remove_alert);
			        builder.setMessage(R.string.msg_remove_selected_alert);
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteRecords(_dataList.getSelectedSessions());
//									_deleteConfirm.hide();
								}
							}
					);
			        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
//			            	_deleteConfirm.hide();
			            }
			        });
			        this._deleteConfirm = builder.create();
			}
			this._deleteConfirm.show();
		}else{
			alert("Nothing selected to be deleted.");
		}
	}
	private void _launchRecorder(){
		Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
		this.startActivity(intent);
		this.overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
	}
	private void _launchMapView(long sessionId){
		Intent intent = new Intent(this.getApplicationContext(), MapActivity.class);
		intent.putExtra(MapActivity.iext_name_session_id, sessionId);
		this.startActivity(intent);
		this.overridePendingTransition(R.anim.right_in, R.anim.left_out);
		
	}
	private void _launcEEGView(long sessionId){
		Intent intent = new Intent(this.getApplicationContext(), wbVisualizationActivity.class);
		intent.putExtra(wbVisualizationActivity.iext_name_session_id, sessionId);
		this.startActivity(intent);
		this.overridePendingTransition(R.anim.left_in, R.anim.right_out);//(R.anim.bottom_in, R.anim.top_out);
	}
	public void launchRecorder(View view){
		this._launchRecorder();
	}
	private int getPositionBySessionId(long sessionId){
		int len = this._dataList.getCount();
		while(len > 0){
			len --;
			ISessionStatistics ret = this._dataList.getItem(len);
			if(sessionId == ret.getId()){
				return len;
			}
		}
		return -1;
	}
	private static class analysisTask extends AsyncTask<Long, IWaveFormStats, Exception>{
		private ManagerActivity _parent = null;
		private ISessionStatistics _stats = null;
		private View _view = null;
		private ProgressBar _progress = null;
		private TextView _progressText = null;
		private Context _context = null;
		private long _duration = 0;
		public analysisTask(ManagerActivity parent, Context ctx, ISessionStatistics stats, View iview){
			super();
			this._parent = parent;
			this._context = ctx;
			this._stats = stats;
			this._view = iview;
			this._progress = (ProgressBar)this._view.findViewById(R.id.aProgress);
			this._progress.setIndeterminate(true);
			this._progressText = (TextView)this._view.findViewById(R.id.aProgressText);
		}
	
		@Override
		protected Exception doInBackground(Long... params) {
			this._progress.setMax((int)this._stats.getDuration());
			Long start = params[0];
			this._duration = System.currentTimeMillis();
			analysisManager mgr = analysisManager.getInstance(this._context, this._stats.getId());
			IntervalCriteria c = new IntervalCriteria(this._stats.getId(), start, null);
			if(null == start || 0 == start){
				mgr.deleteWaveformCache();
//				mgr.deleteLPFdata();
			}
			try {
				mgr.traverseWaveformDetection(c, new IWaveformStatsVisitor(){

					@Override
					public boolean visit(long sessionId, IWaveFormStats stats)
							throws VisitorAbortException, DataSourceException {
						publishProgress(stats);
						return true;
					}

					@Override
					public void onStart(long sessionId)
							throws VisitorAbortException, DataSourceException {
						_progress.setIndeterminate(false);
					}

					@Override
					public boolean onEnd(long sessionId)
							throws VisitorAbortException, DataSourceException {
						return false;
					}
					
				});
			} catch (DataSourceException e) {
				return e;
			}
			this._duration = System.currentTimeMillis() - this._duration;
			return null;
		}
		private void cleanCacheDB(){
			analysisManager mgr = analysisManager.getInstance(this._context, this._stats.getId());
			mgr.deleteWaveformCache();
			mgr.deleteLPFdata();
		}
		@Override
		protected void onCancelled() {
			this.cleanCacheDB();
		}

		@Override
		protected void onCancelled(Exception result) {
			this.cleanCacheDB();
		}

		@Override
		protected void onPostExecute(Exception e) {
			if(null != e){
				this.cleanCacheDB();
				Toast.makeText(
						this._view.getContext(),
						String.format("Error on analyzing session-%d%s%s", this._stats.getId(), System.getProperty("line.separator")
								,e.getMessage()), Toast.LENGTH_LONG).show();
			}else{
				ImageView toWebVisualization = (ImageView)this._view.findViewById(R.id.imageWebView);
				toWebVisualization.setVisibility(View.VISIBLE);
				toWebVisualization.setTag(this._view.getTag());
				toWebVisualization.setOnClickListener(new clickToOpenWebVisualization(this._parent));
				this._progress.setOnClickListener(null);
				this._progress.setVisibility(View.GONE);
				this._progressText.setVisibility(View.GONE);
				Toast.makeText(
						this._view.getContext(),
						String.format("took %d millisecs", this._duration), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onProgressUpdate(IWaveFormStats... values) {
//			if(this._progress.isIndeterminate()){
//				this._progress.setIndeterminate(false);
//			}

			int offs = (Long.valueOf(values[0].getEpocTime() - this._stats.getTime())).intValue();
			this._progress.setProgress(offs);
			this._progressText.setText(String.format("%5.1f",(float)offs / 1000.0));
			//TODO if the form is the expected one, we might better to count the ocurrance and let user know possible 
		}
	}
	private void deleteCache4Sessions(Collection<Long> sessionIds){
		if(null == sessionIds){
			return;
		}
		(new deletionTask(this){
			@Override
			protected boolean processSession(Long session)
					throws DataSourceException {
				analysisManager amgr = analysisManager.getInstance(this._parent.getApplicationContext(), session);
				amgr.deleteWaveformCache();
				this._pdialog.incrementProgressBy(1);
				amgr.deleteLPFdata();
				this._pdialog.incrementProgressBy(1);
				return true;
			}
			@Override
			protected int getProgressPerSession() {
				return 2;
			}
		}).execute(sessionIds.toArray(new Long[sessionIds.size()]));
	}
	private AlertDialog _deleteCacheConfirm = null;
	public void deleteCache(View view) {
		if(!_dataList.getSelectedSessions().isEmpty()){
			if(null == this._deleteCacheConfirm){
				   AlertDialog.Builder builder = new AlertDialog.Builder(this);

			        builder.setTitle(R.string.title_remove_alert);
			        builder.setMessage(R.string.msg_remove_selected_cache);
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteCache4Sessions(_dataList.getSelectedSessions());
								}
							}
					);
			        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            }
			        });
			        this._deleteCacheConfirm = builder.create();
			}
			this._deleteCacheConfirm.show();
		}else{
			alert("Nothing selected to be deleted.");
		}
	}
	public void launchPreference(View view){
		Intent intent = new Intent(this.getApplicationContext(), SrvSettingsActivity.class);
		this.startActivity(intent);
	}
}
