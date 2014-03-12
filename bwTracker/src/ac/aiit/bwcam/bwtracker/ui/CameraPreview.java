package ac.aiit.bwcam.bwtracker.ui;

import java.io.File;
import java.io.IOException;

import ac.aiit.bwcam.bwtracker.R;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup;

public class CameraPreview extends PreviewSurface implements Callback {
	private Camera _cam = null;
	private MediaRecorder _recorder = null;
	public CameraPreview(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
		if(null != this._cam){
//			Camera.Parameters params = this._cam.getParameters();
//	        params.setPreviewSize(width, height);
//	        this._cam.setParameters(params);
//	        this._cam.startPreview();
		}
		
	}
	//http://developer.android.com/reference/android/media/MediaRecorder.html
	private void initRecorder(int camId){
		this._recorder = new MediaRecorder();
		this._cam.unlock();
		this._recorder.setCamera(this._cam);

/*		CamcorderProfile profile = CamcorderProfile.get(camId,
				CamcorderProfile.QUALITY_HIGH);
		if (profile != null) {
			this._recorder.setProfile(profile);

		} else {
			// default to basic H263 and AMR_NB if profile not found
			this._recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			this._recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
		}*/
	}

	private File _tmpFile = null;
	private String _destFilename = null;
	private void _startRecording(long session) throws IllegalStateException, IOException{
		VideoManager vm = VideoManager.getInstance();
		this._tmpFile = vm.createFile4Session(session);
		this._recorder.setOutputFile(this._tmpFile.getAbsolutePath());
		this._recorder.prepare();
		this._destFilename = vm.generateFileName4Session(session);
		this._recorder.start();
	}
	private void _stopRecording(){
		if(null != this._tmpFile){
			if(null != this._recorder){
				this._recorder.stop();
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
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		try {
			int numCameras = Camera.getNumberOfCameras();
			
			CameraInfo camInfo = new CameraInfo();
			int lastCamId= -1;
			for(int i=0; i < numCameras;i++){
				lastCamId = i;
				Camera.getCameraInfo(lastCamId, camInfo);
				if(CameraInfo.CAMERA_FACING_BACK == camInfo.facing){
					break;
				}
			}
			if(lastCamId >= 0){
				this._cam = Camera.open(lastCamId);
			}else{
				this._cam = Camera.open();//don't know what happens..
			}
			this._cam.setDisplayOrientation(90);
			this._cam.setPreviewDisplay(arg0);
			this.initRecorder(lastCamId);
			if(null != this._inQueueSession){
				this._startRecording(this._inQueueSession);
				this._inQueueSession = null;
			}
		} catch (IOException e) {
			if(null != this._cam){
				this._cam.release();
				this._cam = null;
			}
			//TODO* notify the error
		}
		
	}
	private Long _inQueueSession = null;
	public String startPreview(ViewGroup parent) throws UIException{
		try{
			parent.addView(this);
			return this.getResources().getString(R.string.pref_cam_builtin);
		} catch (IllegalStateException e) {
			throw new UIException(e);
		}
	}
	public void stopPreview(ViewGroup parent){
		parent.removeView(this);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(null != this._cam){
			this._cam.release();
		}
		this.releaseRecorder();
	}
	@Override
	public void startRecording(long session) throws UIException {
		try{
			if(null != this._cam){
				this._startRecording(session);
			}
			this._inQueueSession = Long.valueOf(session);
		} catch (IllegalStateException e) {
			throw new UIException(e);
		} catch (IOException e) {
			throw new UIException(e);
		}
	}
	@Override
	public void stopRecording() {
		if(null != this._cam){
			this._stopRecording();
		}
		this._inQueueSession = null;
	}
}
