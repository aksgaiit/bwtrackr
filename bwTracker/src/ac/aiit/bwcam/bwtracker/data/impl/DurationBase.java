package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IDuration;

public class DurationBase implements IDuration{
	protected Long _start = null;
	protected Long _end = null;
	
	protected DurationBase(){
		super();
	}
	public DurationBase(IDuration other){
		super();
		this.setValue(other);
	}
	public DurationBase(long start, long end){
		super();
		this.setValue(start, end);
	}

	public void setValue(long start, long end){
		this._start = start;
		this._end = end;
	}
	public void setValue(IDuration other){
		this._start = other.getStart();
		this._end = other.getEnd();
	}
	@Override
	public long getStart() {
		return this._start;
	}

	@Override
	public long getEnd() {
		return this._end;
	}

}
