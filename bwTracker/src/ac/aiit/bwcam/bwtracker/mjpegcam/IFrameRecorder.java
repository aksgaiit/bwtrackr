package ac.aiit.bwcam.bwtracker.mjpegcam;

import android.graphics.Bitmap;

public interface IFrameRecorder {
	public void onStart();
	public void onFrame(Bitmap bmp, long curtime) throws FrameRecorderException;
	public void onEnd();

}
