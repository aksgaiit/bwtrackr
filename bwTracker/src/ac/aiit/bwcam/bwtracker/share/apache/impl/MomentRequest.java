package ac.aiit.bwcam.bwtracker.share.apache.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.analysisManager;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria.accuracy;
import ac.aiit.bwcam.bwtracker.media.IFrameVisitor;
import ac.aiit.bwcam.bwtracker.media.MediaSourceException;
import ac.aiit.bwcam.bwtracker.media.VideoManager;
import ac.aiit.bwcam.bwtracker.share.IWithComment;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;

public class MomentRequest extends SessionRequestBase implements IRequest,
		IWithComment {
	protected Long _epoc = null;
	protected String _comment = null;

	public MomentRequest(Context context, long sessionId, long epoc) {
		super(context, sessionId);
		this._epoc = epoc;
	}

	@Override
	public void addComment(String content) {
		this._comment = content;
	}

	@Override
	protected String getSubpath() {
		return "/bwdata/post";
	}

	@Override
	protected MultipartEntityBuilder processForm(MultipartEntityBuilder form) throws RequestException {
		final MultipartEntityBuilder ret = super.processForm(form);
		ret.addTextBody("epoc", String.valueOf(this._epoc),text_plain_utf8);
		if(null != this._comment){
			ret.addTextBody("comment", this._comment, text_plain_utf8);
		}
		DBReader reader = new DBReader(this.getContext());
		ByTimeCriteria c = new ByTimeCriteria(this._sessionId, this._epoc, accuracy.nearest);
		c.setLimit(1L);
		analysisManager mgr = analysisManager.getInstance(this.getContext(), this._sessionId);
		try {
			reader.searchLocations(c
			, new ILocationVisitor(){
				@Override
				public boolean visit(long sessionId, long millisec,
						Location location) throws VisitorAbortException,
						DataSourceException {
					ret.addTextBody("lat", String.valueOf(location.getLatitude()), text_plain_utf8);
					ret.addTextBody("lng", String.valueOf(location.getLongitude()), text_plain_utf8);
					return true;
				}
			
			});
			reader.searchMentalStats(c, new IMentalStatsVisitor(){
				@Override
				public boolean visit(long sessinId, IBWMentalStats stat)
						throws VisitorAbortException, DataSourceException {
					ret.addTextBody("med", String.valueOf(stat.getMeditation()), text_plain_utf8);
					ret.addTextBody("att", String.valueOf(stat.getAttention()), text_plain_utf8);
					return true;
				}
			});
			mgr.traverseWaveformDetection(c, new IWaveformStatsVisitor(){

				@Override
				public void onStart(long sessionId)
						throws VisitorAbortException, DataSourceException {
				}

				@Override
				public boolean onEnd(long sessionId)
						throws VisitorAbortException, DataSourceException {
					return false;
				}

				@Override
				public boolean visit(long sessionId, IWaveFormStats stats)
						throws VisitorAbortException, DataSourceException {
					ret.addTextBody("wlen", String.valueOf(stats.getWaveLength()), text_plain_utf8);
					ret.addTextBody("wamp", String.valueOf(stats.getAmplitude()), text_plain_utf8);
					return true;
				}
			});
			VideoManager.mediaFile mf = VideoManager.getInstance()
					.getMediaFile(this._sessionId);
			if(null != mf && mf.isValid()){
				ArrayList<Long> times = new ArrayList<Long>();
				times.add(this._epoc);
				String fpath = mf.getAbsolutePath().replaceFirst("_\\d+\\.mp4$"
						, (new StringBuilder("_").append(this._epoc).append(".png")).toString());
				final FileOutputStream os = new FileOutputStream(fpath);
				try{
					if(0 < mf.visitFramesByEpoc(times, new IFrameVisitor(){
						@Override
						public boolean visitBitmap(Long offset, Long epoc,
								Bitmap bitmap) throws VisitorAbortException,
								MediaSourceException {
							return bitmap.compress(CompressFormat.PNG, 100, os);
						}
					})){
						os.flush();
						File f = new File(fpath);
						ret.addPart("bwdata-thumbnail-upload"
								, new FileBody(f
								, ContentType.create("image/png")//ContentType.MULTIPART_FORM_DATA.withCharset(Charset.forName("UTF-8"))
								, f.getName()));
					}
				} catch (MediaSourceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						os.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (DataSourceException e) {
			throw new RequestException(e);
		} catch (FileNotFoundException e) {
			throw new RequestException(e);
		}finally{
			reader.close();
			mgr.close();
		}
		return ret;
	}

}
