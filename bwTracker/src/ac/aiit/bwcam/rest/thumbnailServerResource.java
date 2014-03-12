package ac.aiit.bwcam.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ac.aiit.bwcam.bwtracker.trackerApp;
import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.media.IFrameVisitor;
import ac.aiit.bwcam.bwtracker.media.MediaSourceException;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class thumbnailServerResource extends ServerResource {
	
	public static final String q_width = "wdth";
	public static final String q_height = "hght";

	public static class thumbnailOutputRepresentation extends OutputRepresentation{
		private Bitmap _bitmap = null;
		
		private static final Map<MediaType, CompressFormat> _mtmap = new HashMap<MediaType, CompressFormat>(){
			private static final long serialVersionUID = -2110409093478307234L;
			{
				put(MediaType.IMAGE_ALL, CompressFormat.PNG);
				put(MediaType.IMAGE_PNG, CompressFormat.PNG);
				put(MediaType.IMAGE_JPEG, CompressFormat.JPEG);
			}
		};
		private thumbnailOutputRepresentation(MediaType mediaType) {
			super(mediaType);
		}
		public static final thumbnailOutputRepresentation getInstance(MediaType mediaType){
			if(_mtmap.containsKey(mediaType)){
				return new thumbnailOutputRepresentation(mediaType);
			}
			return null;
		}
		public void setBitmap(Bitmap bitmap){
			this._bitmap = bitmap;
		}

		@Override
		public void write(OutputStream os) throws IOException {
			this._bitmap.compress(_mtmap.get(this.getMediaType()), 100, os);
			os.flush();
		}
		
	}
	public static final synchronized thumbnailOutputRepresentation getThumbnailOutputRepresentation(Context ctx
			, long sessionId, long start
			, final Integer width, final Integer height) throws ResourceException{
		final thumbnailOutputRepresentation ret = thumbnailOutputRepresentation
				.getInstance(MediaType.IMAGE_PNG);
		try {
			ArrayList<Long> times = new ArrayList<Long>();
			times.add(start);
			VideoManager.mediaFile mf = VideoManager.getInstance()
					.getMediaFile(Long.valueOf(sessionId));
			mf.visitFramesByEpoc(times, new IFrameVisitor() {

				@Override
				public boolean visitBitmap(Long offset, Long epoc, Bitmap bitmap)
						throws VisitorAbortException, MediaSourceException {
					if (null != width || null != height) {
						ret.setBitmap(Bitmap.createScaledBitmap(bitmap, width,
								height, false));
					} else {
						ret.setBitmap(bitmap);
					}
					throw new VisitorAbortException("done");
				}

			});
			if (null != ret._bitmap) {
				return ret;
			} else {
				return null;
			}
		} catch (MediaSourceException e) {
			throw new ResourceException(e);
		}
	}
	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String s_session = query.getFirstValue(restConstants.q_sessionId);
		if(null != s_session){
			String start = query.getFirstValue(restConstants.q_start);
			final String s_width = query.getFirstValue(q_width);
			final String s_height = query.getFirstValue(q_height);
			Context ctx = trackerApp.getApp();
			if(null != ctx && null != start){
				Integer width = null, height = null;
				if(null != s_width || null != s_height){
					width = (null != s_width ? Integer.valueOf(s_width) : Integer.valueOf(s_height));
					height = (null != s_height ? Integer.valueOf(s_height) : Integer.valueOf(s_width));
				}
				thumbnailOutputRepresentation ret = getThumbnailOutputRepresentation(ctx
						, Long.valueOf(s_session), Long.valueOf(start)
						, width, height);
				if(null != ret){
					return ret;
				}else{
					this.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "out of bound epoc");
				}
			}else{
				this.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		}else{
			this.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "no session id");
		}
		return new EmptyRepresentation();
	}

}
