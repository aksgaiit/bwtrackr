package ac.aiit.bwcam.bwtracker.ui;

import android.content.Context;
import android.view.SurfaceView;

public abstract class PreviewSurface extends SurfaceView implements
		IPreviewObject {

	public PreviewSurface(Context context) {
		super(context);
	}

}
