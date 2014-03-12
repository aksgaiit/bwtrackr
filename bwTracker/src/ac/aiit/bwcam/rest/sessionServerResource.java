package ac.aiit.bwcam.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ac.aiit.bwcam.bwtracker.trackerApp;
import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.ISessionStatistics;
import ac.aiit.bwcam.bwtracker.data.ISessionStatisticsVisitor;
import ac.aiit.bwcam.bwtracker.data.SessionStatisticsBase;
import ac.aiit.bwcam.bwtracker.data.db.DBManager;
import android.content.Context;

public class sessionServerResource extends ServerResource {
	public static final String q_session_id = "sid";
	
	private static class sessionStatisticsJson extends SessionStatisticsBase{

		public sessionStatisticsJson(ISessionStatistics other) {
			super(other);
		}
		public JSONObject toJSONObject() throws JSONException{
			JSONObject ret = new JSONObject();
			ret.put("duration", this.getDuration());
			ret.put("id", this.getId());
			ret.put("epoc", this.getTime());
			return ret;
		}
	}

	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String sessionId = query.getFirstValue(q_session_id);
		if(null == sessionId){
			Context ctx = trackerApp.getApp();
			if(null != ctx){
				DBManager reader = new DBManager(ctx);
				try {
					final JSONArray items = new JSONArray();
					int num = reader
							.visitSessionStatistics(new ISessionStatisticsVisitor() {

								@Override
								public boolean visit(ISessionStatistics info)
										throws DataSourceException,
										VisitorAbortException {
									sessionStatisticsJson conv = new sessionStatisticsJson(
											info);
									try {
										items.put(conv.toJSONObject());
										return true;
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return false;
								}
							});
					JSONObject result = new JSONObject();
					result.put("items", items);
					result.put("num", num);
					return new JsonRepresentation(result);
				} catch (DataSourceException e) {
					throw new ResourceException(e);
				} catch (JSONException e) {
					throw new ResourceException(e);
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
