package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface IRawdataVisitor extends IStatsVisitor{
	public boolean visit(long sessionId, IBWRawStats stat)throws VisitorAbortException, DataSourceException;
}
