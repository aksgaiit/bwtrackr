package ac.aiit.bwcam.bwtracker.ui;

import ac.aiit.bwcam.bwtracker.SettingsActivity;
import ac.aiit.bwcam.bwtracker.mjpegcam.MjpegCamera;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;

public class CameraManager implements IPreviewObject {
	private IPreviewObject _camPrev = null;
	private Context _parent = null;

	public CameraManager(Context ctx) {
		super();
		this._parent = ctx;
	}

	@Override
	public void startRecording(long session) throws UIException {
		if(null != this._camPrev){
			this._camPrev.startRecording(session);
		}
	}

	@Override
	public void stopRecording() {
		if(null != this._camPrev){
			this._camPrev.stopRecording();
		}
	}

	@Override
	public String startPreview(ViewGroup parent) throws UIException {
		if (null == this._camPrev) {
			SharedPreferences prefs = SettingsActivity.getSharedPreferences(this._parent);
			if(SettingsActivity.getWifiCamChecked(prefs)){
				this._camPrev = new MjpegCamera(this._parent);
			}else{
				this._camPrev = new MRecorderPreview(this._parent);//new CameraPreview(this);
			}
		}
		return this._camPrev.startPreview(parent);
	}

	@Override
	public void stopPreview(ViewGroup parent) {
		if(null != this._camPrev){
			this._camPrev.stopPreview(parent);
		}
	}

}
