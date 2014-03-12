package ac.aiit.bwcam.bwtracker.data.analysis;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;

public interface IBWAnalysisReader {
	public int traverseLPFdata(IntervalCriteria criteria, ILPFStatsVisitor visitor) throws DataSourceException;
	public int traverseWaveformDetection(IntervalCriteria criteria, IWaveformStatsVisitor visitor) throws DataSourceException;
	public int traverseWaveformDetection(DistanceCriteria<Double, Double> criteria, IWaveformStatsVisitor visitor) throws DataSourceException;
}
