package ac.aiit.bwcam.bwtracker.data.analysis;

import ac.aiit.bwcam.bwtracker.data.IByMillisecStats;

public interface IWaveFormStats extends IByMillisecStats{
	public double getWaveLength();//estimated
	public double getAmplitude();//estimated
}
