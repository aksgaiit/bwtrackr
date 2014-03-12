package ac.aiit.bwcam.bwtracker.neuro;

public interface ITGBioDataListener {
	public void onHeartRate(long epoc, int val);
	public void onBlink(long epoc, int val);
}
