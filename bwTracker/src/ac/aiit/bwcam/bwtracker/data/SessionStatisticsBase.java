package ac.aiit.bwcam.bwtracker.data;

public class SessionStatisticsBase implements ISessionStatistics {
	
	protected Long _id = null;
	protected Long _time = null;
	protected Long _duration = null;

	protected SessionStatisticsBase(){
		super();
	}
	protected SessionStatisticsBase(Long id, Long time, Long duration){
		super();
		this._id = id;
		this._time = time;
		this._duration = duration;
	}
	public SessionStatisticsBase(ISessionStatistics other){
		super();
		this._id = other.getId();
		this._time = other.getTime();
		this._duration = other.getDuration();
	}

	@Override
	public long getId() {
		return this._id;
	}

	@Override
	public long getTime() {
		return this._time;
	}

	@Override
	public long getDuration() {
		return this._duration;
	}

}
