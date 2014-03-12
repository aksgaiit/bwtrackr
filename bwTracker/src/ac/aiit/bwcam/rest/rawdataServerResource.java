package ac.aiit.bwcam.rest;

import java.util.ArrayList;
import java.util.List;

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
import ac.aiit.bwcam.bwtracker.data.IBWDataReader;
import ac.aiit.bwcam.bwtracker.data.IBWRawStats;
import ac.aiit.bwcam.bwtracker.data.IRawdataVisitor;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.impl.BWRawStatsBase;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwdata.common.HandlerException;
import ac.aiit.bwdata.lpf.Butterworth;
import ac.aiit.bwdata.lpf.IBWDataFilter;
import android.content.Context;

public class rawdataServerResource extends ServerResource {
	public static final String q_type = "type";
	public static final String q_filter = "fltr";
	
	public static class rawwave_items {
		private List<IBWRawStats> _items = new ArrayList<IBWRawStats>();
		public long getNum(){
			return this._items.size();
		}
		public List<IBWRawStats> getItems(){
			return this._items;
		}
		public boolean addItem(IBWRawStats item){
			return this._items.add(item);
		}
	}
	private static class rawstat_json extends BWRawStatsBase{
		
		public rawstat_json(IBWRawStats other) {
			super(other);
		}

		public JSONObject toJsonObject() throws JSONException{
			JSONObject ret = new JSONObject();
			ret.put("epoc", this.getEpocTime());
			ret.put("value", this.getValue());
			return ret;
		}
	}

	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String s_session = query.getFirstValue(restConstants.q_sessionId);
		if(null != s_session){
			String type = query.getFirstValue(q_type);
			String start = query.getFirstValue(restConstants.q_start);
			String duration = query.getFirstValue(restConstants.q_duration);
			String filter = query.getFirstValue(q_filter);
			String s_offs = query.getFirstValue(restConstants.q_offset);
			String s_limit = query.getFirstValue(restConstants.q_limit);
			
			Context ctx = trackerApp.getApp();
			if(null != ctx){
				Long lstrt = (null != start ? Long.valueOf(start) : null);
				Long lend = (null != duration ? Long.valueOf(duration) + lstrt : null);
				final IBWDataReader reader = new DBReader(ctx);
				final JSONArray items = new JSONArray();
				try {
					IRawdataVisitor visitor = null;
					if("lpfbw".equals(filter)){
						final IBWDataFilter df = new Butterworth(512, 4.5);
						visitor = new IRawdataVisitor(){

							@Override
							public boolean visit(long sessionId,
									IBWRawStats stat)
									throws VisitorAbortException,
									DataSourceException {
								rawstat_json conv = new rawstat_json(stat);
								try{
									JSONObject itm = conv.toJsonObject();
									itm.put("f_value", df.filter((double)conv.getValue()));
									items.put(itm);
								} catch (JSONException e) {
									throw new DataSourceException(e);
								} catch (HandlerException e) {
									throw new DataSourceException(e);
								}
								return false;
							}

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
						};						
					}else{
						visitor = new IRawdataVisitor(){

							@Override
							public boolean visit(long sessionId, IBWRawStats stat)
									throws VisitorAbortException, DataSourceException {
								rawstat_json conv = new rawstat_json(stat);
								try {
									items.put(conv.toJsonObject());
									return true;
								} catch (JSONException e) {
									throw new DataSourceException(e);
								}
							}

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
						};
					}
					int num = reader.traverseRawdata(
							new IntervalCriteria(Long.valueOf(s_session), lstrt, lend)
					, visitor);
					JSONObject result = new JSONObject();
					result.put("items", items);
//					result.put("sessionId", sessionId);
					result.put("num", num);
					return new EncodeRepresentation(Encoding.GZIP,new JsonRepresentation(result));
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
