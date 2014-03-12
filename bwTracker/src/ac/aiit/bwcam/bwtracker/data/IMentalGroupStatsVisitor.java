package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface IMentalGroupStatsVisitor {
	boolean visit(long sessinId, IBWMentalGroupStats gstat)throws VisitorAbortException, DataSourceException;
}
