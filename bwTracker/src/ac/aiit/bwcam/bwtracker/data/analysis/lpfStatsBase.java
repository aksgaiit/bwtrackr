package ac.aiit.bwcam.bwtracker.data.analysis;

import ac.aiit.bwcam.bwtracker.data.impl.ByMillisecStatsBase;

public class lpfStatsBase extends ByMillisecStatsBase implements ILPFStats {
	protected Double _value = null;

	protected lpfStatsBase() {
		super();
	}
	public lpfStatsBase(ILPFStats other){
		super(other);
		this._value = other.getValue();
	}

	@Override
	public double getValue() {
		return this._value;
	}
}
