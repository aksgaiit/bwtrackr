package ac.aiit.bwcam.bwtracker.neuro;

public interface ITGMentalStateListener {
	public void onAttention(long epoc, int val);
	public void onMeditation(long epoc, int val);

}
