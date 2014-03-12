package ac.aiit.bwcam.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ac.aiit.bwcam.bwtracker.trackerApp;
import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;
import ac.aiit.bwcam.bwtracker.data.IByMillisecStats;
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveFormStats;
import ac.aiit.bwcam.bwtracker.data.analysis.IWaveformStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.analysisManager;
import ac.aiit.bwcam.bwtracker.data.analysis.impl.waveformStatsBase;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.impl.ByMillisecStatsBase;
import ac.aiit.bwcam.bwtracker.data.query.DistanceCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.elemDistance;
import ac.aiit.bwcam.rest.ext.IMillisecBasedStatsFragmentSetter;
import android.content.Context;

public class waveformServerResource extends ServerResource {
	public static final String q_amplitude ="ampl";
	public static final String q_wavelength = "wlen";
	public static final String q_range = "rng";
	public static final String q_ext = "ext";
	
	public static class wfs_json extends waveformStatsBase{
		public wfs_json(IWaveFormStats other) {
			super(other);
		}

		public JSONObject toJson() throws JSONException{
			JSONObject ret = new JSONObject();
			ret.put("amp", this.getAmplitude());
			ret.put("epoc", this.getEpocTime());
			ret.put("wlen", this.getWaveLength());
			return ret;
		}
	}
	public static class MentalFragmentSetter implements IMillisecBasedStatsFragmentSetter{
		private DBReader _reader = null;
		private Long _sessionId = null;
		public MentalFragmentSetter(Long sessionId, DBReader reader){
			super();
			this._reader = reader;
			this._sessionId = sessionId;
		}
		@Override
		public void setFragments(IByMillisecStats stats, final JSONObject target,
				final String path) throws DataSourceException {
			IntervalCriteria c = new IntervalCriteria(this._sessionId, stats.getEpocTime(), stats.getEpocTime() + 1000);
			this._reader.searchMentalStats(c, new IMentalStatsVisitor(){

				@Override
				public boolean visit(long sessinId, IBWMentalStats stat)
						throws VisitorAbortException, DataSourceException {
					try{
						JSONObject ret = new JSONObject();
//						ret.put("sec", stat.getSecond());
						ret.put("att", stat.getAttention());
						ret.put("med", stat.getMeditation());	
						target.put(path, ret);
					} catch (JSONException e) {
						throw new DataSourceException(e);
					}
					return true;
				}
			});
		}
	}
	public static class extDataFactory{
		private DBReader _reader = null;
		public DBReader getReader(){
			return this._reader;
		}
		public extDataFactory(Context ctx){
			super();
			this._reader = new DBReader(ctx);
		}
		
		protected Map<String, IMillisecBasedStatsFragmentSetter> _setters = new HashMap<String, IMillisecBasedStatsFragmentSetter>();

		public void registerSetter(String key, IMillisecBasedStatsFragmentSetter setter){
			this._setters.put(key, setter);
		}
		public void setFragments(IByMillisecStats stats, JSONObject target)throws DataSourceException{
			for(Map.Entry<String,IMillisecBasedStatsFragmentSetter> entry : this._setters.entrySet()){
				entry.getValue().setFragments(stats, target, entry.getKey());
			}
		}
	}

	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String s_session = query.getFirstValue(restConstants.q_sessionId);
		if(null != s_session){
			String s_ampl = query.getFirstValue(q_amplitude);
			String s_wlen = query.getFirstValue(q_wavelength);
			String s_range = query.getFirstValue(q_range);
			String s_exts = query.getFirstValue(q_ext);
			String s_limit = query.getFirstValue(restConstants.q_limit);
			Context ctx = trackerApp.getApp();
			if(null != ctx){
				Long sessionId = Long.valueOf(s_session);
				analysisManager mgr = analysisManager.getInstance(ctx, sessionId);
				final extDataFactory extf = new extDataFactory(ctx);
				if(null != s_exts){
					for(String ext : s_exts.split(",")){
						if("mental".equals(ext)){
							extf.registerSetter(ext, new MentalFragmentSetter(sessionId, extf._reader));//*TODO*
						}
					}
				}
				if(null != s_ampl && null != s_wlen){
					Double range = null != s_range ? Double.valueOf(s_range) : 100.0;
					DistanceCriteria<Double, Double> criteria = new DistanceCriteria(sessionId, range);
					mgr.addAmpElement(criteria, Double.valueOf(s_ampl));
					mgr.addWlenElement(criteria, Double.valueOf(s_wlen));
					if(null != s_limit){
						criteria.setLimit(Long.valueOf(s_limit));
					}
					try{
						final JSONArray items = new JSONArray();

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
									items.put(itm);
									return true;
								} catch (JSONException e) {
									throw new DataSourceException(e);
								}
							}
							
						});
						JSONObject result = new JSONObject();
						result.put("items", items);
//						result.put("sessionId", sessionId);
						result.put("num", num);
//						return new EncodeRepresentation(Encoding.GZIP,new JsonRepresentation(result));
						return new JsonRepresentation(result);
					} catch (JSONException e) {
						throw new ResourceException(e);
					} catch (DataSourceException e) {
						throw new ResourceException(e);
					}
				}else{
					this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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
