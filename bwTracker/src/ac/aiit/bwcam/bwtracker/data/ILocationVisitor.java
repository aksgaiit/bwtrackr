package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import android.location.Location;

public interface ILocationVisitor {
	public boolean visit(long sessionId, long millisec, Location location) throws VisitorAbortException, DataSourceException;
}
