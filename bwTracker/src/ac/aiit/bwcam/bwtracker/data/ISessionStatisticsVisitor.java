package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface ISessionStatisticsVisitor {
	public boolean visit(ISessionStatistics info) throws DataSourceException, VisitorAbortException;
}
