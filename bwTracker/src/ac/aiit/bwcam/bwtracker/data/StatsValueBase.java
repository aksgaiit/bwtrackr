package ac.aiit.bwcam.bwtracker.data;

public class StatsValueBase<T> implements IStatsValue<T> {
	protected Double _average = null;
	protected T _max = null;
	protected T _min = null;
	protected Integer _count = null;

	protected StatsValueBase() {
		super();
	}
	public StatsValueBase(IStatsValue<T> other){
		super();
		this.setValue(other);
	}
	public StatsValueBase(double average, int count, T max, T min){
		super();
		this.setValue(average, count, max, min);
	}
	public void setValue(IStatsValue<T> other){
		this._average = other.getAverage();
		this._count = other.getCount();
		this._max = other.getMax();
		this._min = other.getMin();
	}
	public void setValue(double average, int count, T max, T min){
		this._average = average;
		this._count = count;
		this._max = max;
		this._min = min;
	}

	@Override
	public double getAverage() {
		return this._average;
	}

	@Override
	public int getCount() {
		return this._count;
	}

	@Override
	public T getMax() {
		return this._max;
	}

	@Override
	public T getMin() {
		return this._min;
	}

}
