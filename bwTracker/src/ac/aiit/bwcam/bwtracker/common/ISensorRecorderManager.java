package ac.aiit.bwcam.bwtracker.common;

public interface ISensorRecorderManager {
	public void log(String message);
	public void alert(String message);
	public void onConnect(ISensorRecorder recorder);
	public void onDisconnect(ISensorRecorder recorder);
}
