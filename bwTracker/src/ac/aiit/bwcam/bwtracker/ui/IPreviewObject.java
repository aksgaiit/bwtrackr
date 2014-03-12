package ac.aiit.bwcam.bwtracker.ui;

import android.view.ViewGroup;

public interface IPreviewObject {
	public String startPreview(ViewGroup parent)throws UIException;
	public void startRecording(long session) throws UIException;
	public void stopRecording();
	public void stopPreview(ViewGroup parent);

}
