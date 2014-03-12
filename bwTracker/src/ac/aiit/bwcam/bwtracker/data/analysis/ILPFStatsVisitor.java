package ac.aiit.bwcam.bwtracker.data.analysis;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IStatsVisitor;

public interface ILPFStatsVisitor extends IStatsVisitor{
	public boolean visit(long sessionId, ILPFStats stat) throws VisitorAbortException, DataSourceException;

}
