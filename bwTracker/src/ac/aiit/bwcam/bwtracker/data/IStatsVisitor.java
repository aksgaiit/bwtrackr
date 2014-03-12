package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface IStatsVisitor {
	public void onStart(long sessionId) throws VisitorAbortException, DataSourceException;
	public boolean onEnd(long sessionId) throws VisitorAbortException, DataSourceException;
}
