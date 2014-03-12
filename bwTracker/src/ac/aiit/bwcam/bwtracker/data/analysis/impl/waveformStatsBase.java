package ac.aiit.bwcam.bwtracker.data.analysis.impl;

import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.impl.ByMillisecStatsBase;

public class waveformStatsBase extends ByMillisecStatsBase implements IWaveFormStats {
	protected Double _waveLength = null;
	protected Double _amplitude = null;

	protected waveformStatsBase() {
		super();
	}
	public waveformStatsBase(IWaveFormStats other){
		super(other);
		this._waveLength = other.getWaveLength();
		this._amplitude = other.getAmplitude();
	}
	@Override
	public double getWaveLength() {
		return this._waveLength;
	}

	@Override
	public double getAmplitude() {
		return this._amplitude;
	}

}
