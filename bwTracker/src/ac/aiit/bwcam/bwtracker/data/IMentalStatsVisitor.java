package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface IMentalStatsVisitor {
	public boolean visit(long sessinId, IBWMentalStats stat)throws VisitorAbortException, DataSourceException;
}
