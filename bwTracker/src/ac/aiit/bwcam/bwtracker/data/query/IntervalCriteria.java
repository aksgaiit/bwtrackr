package ac.aiit.bwcam.bwtracker.data.query;


public class IntervalCriteria extends bwSessionCriteria {
	private Long _start = null;
	private Long _end = null;

	public IntervalCriteria(Long sessionId, Long start, Long end) {
		super(sessionId);
		this._start = start;
		this._end = end;
	}
	public Long getStartTime(){
		return this._start;
	}
	public Long getEndTime(){
		return this._end;
	}

}
