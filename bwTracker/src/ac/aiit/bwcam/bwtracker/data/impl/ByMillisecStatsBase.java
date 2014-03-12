package ac.aiit.bwcam.bwtracker.data.impl;

import ac.aiit.bwcam.bwtracker.data.IByMillisecStats;

public class ByMillisecStatsBase extends BySecondStatsBase implements
		IByMillisecStats {
	protected Long _epoc = null;
	protected ByMillisecStatsBase() {
		super();
	}

	protected ByMillisecStatsBase(IByMillisecStats other) {
		super(other);
		this._epoc = other.getEpocTime();
	}

	@Override
	public long getEpocTime() {
		return this._epoc;
	}

}
