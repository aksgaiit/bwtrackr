package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IBWMentalGroupStats;
import ac.aiit.bwcam.bwtracker.data.IStatsValue;

public class BWMentalGroupStatsBase extends DurationBase implements
		IBWMentalGroupStats {
	protected IStatsValue<Long> _att = null;
	protected IStatsValue<Long> _med = null;

	protected BWMentalGroupStatsBase(long start, long end){
		super(start, end);
	}
	public BWMentalGroupStatsBase(long start, long end, IStatsValue<Long> att, IStatsValue<Long> med){
		super(start, end);
		this._att = att;
		this._med = med;
	}

	public BWMentalGroupStatsBase(BWMentalGroupStatsBase other) {
		super(other);
		this._att = other.getAttentionStat();
		this._med = other.getMeditationStat();
	}
	@Override
	public IStatsValue<Long> getAttentionStat() {
		return this._att;
	}
	@Override
	public IStatsValue<Long> getMeditationStat() {
		return this._med;
	}
}
