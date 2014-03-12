package ac.aiit.bwcam.bwtracker.data;

//note that IBWMentalStats#getAttention/Meditation returns average
// along the value within the interval issued by criteria instance.
public interface IBWMentalGroupStats{
	public IStatsValue<Long> getAttentionStat();
	public IStatsValue<Long> getMeditationStat();
}
