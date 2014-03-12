package ac.aiit.bwcam.bwtracker.neuro;

public interface ITGBWDataListener {
	public void onSignalQuality(long epoc, int val);
	public void onRawData(long epoc, int val);
	public void onRawDataCount(long epoc, int val);
}
