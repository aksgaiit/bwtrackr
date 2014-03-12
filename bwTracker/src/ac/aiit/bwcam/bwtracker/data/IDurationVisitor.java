package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;

public interface IDurationVisitor {
	public boolean visit(IDuration duration)throws VisitorAbortException, DataSourceException;

}
