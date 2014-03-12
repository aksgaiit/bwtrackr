package ac.aiit.bwcam.bwtracker.share.apache.impl;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IByMillisecStats;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.analysisManager;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria.accuracy;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.rest.waveformServerResource.MentalFragmentSetter;
import ac.aiit.bwcam.rest.waveformServerResource.extDataFactory;
import ac.aiit.bwcam.rest.waveformServerResource.wfs_json;
import ac.aiit.bwcam.rest.ext.IMillisecBasedStatsFragmentSetter;
import android.content.Context;
import android.location.Location;

public class PublicMomentsRequest extends PublicRequestBase {

	private Double _range = 10.0;
	private Double _amp = 100.0;
	private Double _wlen = 100.0;

	public PublicMomentsRequest(Context context, long sessionId) {
		super(context, sessionId);
	}

	@Override
	protected String getSubpath() {
		return "/bwpub/post";
	}
	
	public void setDistanceRange(double range){
		this._range = range;
	}
	private static class LocationFragmentSetter implements IMillisecBasedStatsFragmentSetter{
		private DBReader _reader = null;
		private Long _sessionId = null;
		public LocationFragmentSetter(Long sessionId, DBReader reader){
			super();
			this._reader = reader;
			this._sessionId = sessionId;
		}
		@Override
		public void setFragments(IByMillisecStats stats, final JSONObject target,
				final String path) throws DataSourceException {
			ByTimeCriteria c = new ByTimeCriteria(this._sessionId, stats.getEpocTime(), accuracy.nearest);
			c.setLimit(1L);
			this._reader.searchLocations(c
			, new ILocationVisitor(){
				@Override
				public boolean visit(long sessionId, long millisec,
						Location location) throws VisitorAbortException,
						DataSourceException {
					JSONObject ret = new JSONObject();
					try {
						ret.put("lat", location.getLatitude());
						ret.put("lng", location.getLongitude());
						target.put(path, ret);
						return true;
					} catch (JSONException e) {
						throw new DataSourceException(e);
					}
				}
			});

		}
	}
	@Override
	protected MultipartEntityBuilder processForm(MultipartEntityBuilder form)
			throws RequestException {
		MultipartEntityBuilder ret = super.processForm(form);
		analysisManager mgr = analysisManager.getInstance(this.getContext(), this._sessionId);
		DistanceCriteria<Double, Double> criteria = new DistanceCriteria<Double, Double>(this._sessionId, this._range);
		mgr.addAmpElement(criteria, this._amp);
		mgr.addWlenElement(criteria, this._wlen);
		final extDataFactory extf = new extDataFactory(this.getContext());
		extf.registerSetter("mental", new MentalFragmentSetter(this._sessionId, extf.getReader()));
		extf.registerSetter("loc", new LocationFragmentSetter(this._sessionId, extf.getReader()));
//		criteria.setLimit(Long.valueOf(s_limit));
		
		///TODO to be revised!!!!!
		try{
			final JSONArray entries = new JSONArray();
			int num = mgr.traverseWaveformDetection(criteria, new IWaveformStatsVisitor(){

				@Override
				public void onStart(long sessionId)
						throws VisitorAbortException,
						DataSourceException {
				}

				@Override
				public boolean onEnd(long sessionId)
						throws VisitorAbortException,
						DataSourceException {
					return false;
				}

				@Override
				public boolean visit(long sessionId,
						IWaveFormStats stats)
						throws VisitorAbortException,
						DataSourceException {
					wfs_json conv = new wfs_json(stats);
					try {
						JSONObject itm =conv.toJson();
						extf.setFragments(stats, itm);
						entries.put(itm);
//						ret.add("entry", itm.toString());
						return true;
					} catch (JSONException e) {
						throw new DataSourceException(e);
					}
				}
			});
			ret.addTextBody("entries", entries.toString(),text_plain_utf8);
			ret.addTextBody("num", String.valueOf(num),text_plain_utf8);
		} catch (DataSourceException e) {
			throw new RequestException(e);
		}
		return ret;
	}

}
