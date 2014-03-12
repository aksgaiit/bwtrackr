package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;

public class BWMentalStatsBase extends BySecondStatsBase implements IBWMentalStats {
	
	protected long _att;
	protected long _med;

	protected BWMentalStatsBase() {
		super();
	}
	protected BWMentalStatsBase(long sec){
		super(sec);
	}
	public BWMentalStatsBase(long sec, long att, long med){
		super(sec);
		this._att = att;
		this._med = med;
	}
	public BWMentalStatsBase(IBWMentalStats other){
		super(other);
		this._att = other.getAttention();
		this._med = other.getMeditation();
	}

	@Override
	public long getAttention() {
		return this._att;
	}

	@Override
	public long getMeditation() {
		return this._med;
	}

}
