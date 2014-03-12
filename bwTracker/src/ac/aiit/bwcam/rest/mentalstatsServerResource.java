package ac.aiit.bwcam.rest;

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
import ac.aiit.bwcam.bwtracker.data.IMentalStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import android.content.Context;

public class mentalstatsServerResource extends ServerResource {
	private JSONObject toJson(IBWMentalStats stats) throws JSONException{
		JSONObject ret = new JSONObject();
		ret.put("sec", stats.getSecond());
		ret.put("att", stats.getAttention());
		ret.put("med", stats.getMeditation());
		return ret;
	}
	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String s_session = query.getFirstValue(restConstants.q_sessionId);
		if (null != s_session) {
			String s_start = query.getFirstValue(restConstants.q_start);
			String s_duration = query.getFirstValue(restConstants.q_duration);
			String s_limit = query.getFirstValue(restConstants.q_limit);
			Context ctx = trackerApp.getApp();
			if (null != ctx) {
				DBReader reader = new DBReader(ctx);
				if(null != s_start){
					try{
						final JSONArray items = new JSONArray();
						Long start = Long.valueOf(s_start);
						IntervalCriteria criteria = new IntervalCriteria(Long.valueOf(s_session),
								start, start + (null != s_duration ? Long.valueOf(s_duration) : 1));
						if(null != s_limit){
							Long limit = Long.valueOf(s_limit);
							criteria.setLimit(limit);
						}
						int num = reader.searchMentalStats(criteria, new IMentalStatsVisitor(){
							@Override
							public boolean visit(long sessinId, IBWMentalStats stat)
									throws VisitorAbortException, DataSourceException {
								try {
									items.put(toJson(stat));
									return true;
								} catch (JSONException e) {
									throw new DataSourceException(e);
								}
							}
						});
						JSONObject ret = new JSONObject();
						ret.put("num", num);
						ret.put("items", items);
						return new EncodeRepresentation(Encoding.GZIP,new JsonRepresentation(ret));
					} catch (DataSourceException e) {
						throw new ResourceException(e);
					} catch (JSONException e) {
						throw new ResourceException(e);
					}
				}else{
					this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			} else {
				this.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
			}
		} else {
			this.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "no session id");
		}
		return new EmptyRepresentation();
	}

}
