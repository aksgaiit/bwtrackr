package ac.aiit.bwcam.bwtracker.common;

public interface ISensorRecorder {
	public void prepareRecording() throws RecorderException;
	public long startRecording() throws RecorderException;
	public void stopRecording() throws RecorderException;
	public void close() throws RecorderException;
}
