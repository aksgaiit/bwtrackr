package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IBWRawStats;

public class BWRawStatsBase extends ByMillisecStatsBase implements IBWRawStats {
	protected Long _value = null;

	protected BWRawStatsBase() {
		super();
	}
	public BWRawStatsBase(IBWRawStats other){
		super(other);
		this._value = Long.valueOf(other.getValue());
	}

	@Override
	public long getValue() {
		return this._value;
	}
}
