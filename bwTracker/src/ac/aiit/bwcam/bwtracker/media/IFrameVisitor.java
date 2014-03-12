package ac.aiit.bwcam.bwtracker.media;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import android.graphics.Bitmap;

public interface IFrameVisitor {
	public boolean visitBitmap(Long offset, Long epoc, Bitmap bitmap) throws VisitorAbortException, MediaSourceException;
}
