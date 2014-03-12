package ac.aiit.bwcam.bwtracker.ui;

import java.io.File;
import java.io.IOException;

import ac.aiit.bwcam.bwtracker.R;
import ac.aiit.bwcam.bwtracker.SettingsActivity;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup;

public class MRecorderPreview extends PreviewSurface implements Callback {
	
	private MediaRecorder _recorder = null;
	private Camera _cam = null;

	public MRecorderPreview(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	private void initCam(){
		if(null == this._cam){
			this._cam = Camera.open(0);
		}
	}
	private void releaseCam(){
		if(null != this._cam){
			this._cam.setPreviewCallback(null);
			this._cam.stopPreview();
			this._cam.release();
			this._cam = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if(null != this._recorder){
//			Camera.Parameters params = this._cam.getParameters();
//	        params.setPreviewSize(width, height);
//	        this._cam.setParameters(params);
//	        this._cam.startPreview();
		}
		
	}
	private void initRecorder(){
		if(null == this._recorder){
			this._recorder = new MediaRecorder();
			this._recorder.setOnErrorListener(new OnErrorListener(){

				@Override
				public void onError(MediaRecorder mr, int what, int extra) {
					if(MediaRecorder.MEDIA_ERROR_SERVER_DIED == what){
						_recorder.release();
						_recorder = null;
					}
					
				}
			});
		}
	}

	private File _tmpFile = null;
	private String _destFilename = null;
	private void _prepareRecording(long session) throws IOException{
		this.initRecorder();//in case previous recording finished with error
		if(null != this._cam){
			this._cam.unlock();
			this._recorder.setCamera(this._cam);
		}
		this._recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		this._recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		CamcorderProfile prof = CamcorderProfile.get(SettingsActivity.getIncamCorderProfileId(SettingsActivity.getSharedPreferences(this.getContext())));
		if(MediaRecorder.OutputFormat.MPEG_4 != prof.fileFormat){
			prof.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
		}
		prof.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
		this._recorder.setProfile(prof);
//		this._recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//		this._recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);		
//		this._recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//		this._recorder.setVideoFrameRate(15);//30
//		this._recorder.setVideoSize(prof.videoFrameWidth, prof.videoFrameHeight);
//		this._recorder.setVideoEncodingBitRate(3000000);//someone makes this value to 15000000
//		this._recorder.setOrientationHint(90);//tbd

		this._recorder.setPreviewDisplay(this.getHolder().getSurface());
		this._tmpFile = VideoManager.getInstance().createFile4Session(session);
		this._recorder.setOutputFile(this._tmpFile.getAbsolutePath());

		this._recorder.prepare();
	}
	private void _startRecording(long session) throws IllegalStateException, IOException{
		this._prepareRecording(session);

		this._recorder.start();
		this._destFilename = VideoManager.getInstance().generateFileName4Session(session);
	}
	private void _stopRecording(){
		if(null != this._tmpFile){
			if(null != this._recorder){
				try{
					this._recorder.stop();
				}catch(IllegalStateException e){
				}finally{
					this._recorder.reset();
					//refresh --just configuring,preparing in next time causes runtime error//
					this._cam.lock();
					this.releaseRecorder();
					this.initRecorder();
				}
			}
			if(null != this._destFilename){
				this._tmpFile.renameTo(new File(this._destFilename));
				this._destFilename = null;
			}
			this._tmpFile = null;
		}
		
	}
	private void releaseRecorder(){
		if(null != this._recorder){
			this._recorder.release();
			this._recorder = null;
		}
	}
	private Long _inQueueSession = null;
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.initCam();
		this.initRecorder();

		try{
			this._cam.setPreviewDisplay(holder);
			this._cam.startPreview();
			if(null != this._inQueueSession){
				this._startRecording(this._inQueueSession);
				this._inQueueSession = null;
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		this.releaseRecorder();
		this.releaseCam();
	}

	@Override
	public String startPreview(ViewGroup parent) throws UIException {
		try{
			parent.addView(this);
			return this.getResources().getString(R.string.pref_cam_builtin);
		} catch (IllegalStateException e) {
			throw new UIException(e);
		} 
		
	}

	@Override
	public void stopPreview(ViewGroup parent) {
		parent.removeView(this);
	}

	@Override
	public void startRecording(long session) throws UIException {
		try {
			if(null != this._recorder){
				this._startRecording(session);
			}else{
				this._inQueueSession = Long.valueOf(session);
			}
		} catch (IllegalStateException e) {
			throw new UIException(e);
		} catch (IOException e) {
			throw new UIException(e);
		}
	}

	@Override
	public void stopRecording() {
		if(null != this._recorder){
			this._stopRecording();
		}
		this._inQueueSession = null;
	}

}
