package ac.aiit.bwcam.bwtracker.data.query;


public class ByTimeCriteria extends bwSessionCriteria {
	public static class accuracy{
		private String _name = null;
		private accuracy(){
			super();
		}
		private accuracy(String name){
			super();
			this._name = name;
		}
		public String getName(){
			return this._name;
		}
		public static final accuracy exactMatch = new accuracy("exactmatch");
		public static final accuracy nearest = new accuracy("nearest");
		public static final accuracy earlier = new accuracy("earlier");
		public static final accuracy later = new accuracy("later");
	}
	private accuracy _accuracy = accuracy.exactMatch;

	public ByTimeCriteria(long sessionId, long time, accuracy accuracy) {
		super(sessionId);
		this.setTime(time);
		this._accuracy = accuracy;
	}
	
	public void setTime(long time){
		this.setOffset(time - this._sessionId);
	}
	
	public long getTime(){
		return this._sessionId + this._offset;
	}
	public accuracy getAccuracy(){
		return this._accuracy;
	}

}
