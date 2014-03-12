package ac.aiit.bwcam.bwtracker.data.query;


public class ModerationCriteria extends bwSessionCriteria {
	protected Long _interval = null;
	protected Long _window = null;

	public ModerationCriteria(long sessionId, long interval, long window) {
		super(sessionId);
	}
	
	public long getInterval(){
		return this._interval;
	}
	public long getWindow(){
		return this._window;
	}

}
