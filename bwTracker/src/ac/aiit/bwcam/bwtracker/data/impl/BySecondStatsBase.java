package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IBySecondStats;

public class BySecondStatsBase implements IBySecondStats {
	protected long _second;

	protected BySecondStatsBase() {
		super();
	}
	protected BySecondStatsBase(long second){
		super();
		this._second = second;
	}
	protected BySecondStatsBase(IBySecondStats other){
		super();
		this._second = other.getSecond();
	}
	protected void _setSecond(long sec){
		this._second = sec;
	}

	@Override
	public long getSecond() {
		return this._second;
	}

}
