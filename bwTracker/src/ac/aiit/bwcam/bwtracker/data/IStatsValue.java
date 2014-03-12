package ac.aiit.bwcam.bwtracker.data;

public interface IStatsValue<T> {
	public double getAverage();
	public int getCount();
	public T getMax();
	public T getMin();
}
