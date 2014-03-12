package ac.aiit.bwcam.bwtracker.data.analysis;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IStatsVisitor;

public interface IWaveformStatsVisitor extends IStatsVisitor{
	public boolean visit(long sessionId, IWaveFormStats stats) throws VisitorAbortException, DataSourceException;

}
