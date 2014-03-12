package ac.aiit.bwcam.bwtracker.share.apache.impl;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IBWDataReader;
import ac.aiit.bwcam.bwtracker.data.IBWMentalGroupStats;
import ac.aiit.bwcam.bwtracker.data.IBWMentalStats;
import ac.aiit.bwcam.bwtracker.data.ILocationVisitor;
import ac.aiit.bwcam.bwtracker.data.IMentalGroupStatsVisitor;
import ac.aiit.bwcam.bwtracker.data.IStatsValue;
import ac.aiit.bwcam.bwtracker.data.db.DBReader;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import android.content.Context;
import android.location.Location;

public class MentalStatsRequest extends PublicRequestBase {

	public MentalStatsRequest(Context context, long sessionId) {
		super(context, sessionId);
	}

	@Override
	protected String getSubpath() {
		return "/bwpub/postMental";
	}

	private static class interval_cursor{
		public long _start;
		public long _end;
		public void setNext(long nextend){
			this._start = this._end;
			this._end = nextend;
		}
	}
	private JSONObject toJSONObject(IStatsValue<Long> val) throws JSONException{
		JSONObject ret = new JSONObject();
		ret.put("ave", val.getAverage());
		ret.put("num", val.getCount());
		ret.put("min", val.getMin());
		ret.put("max", val.getMax());
		return ret;
	}
	@Override
	protected MultipartEntityBuilder processForm(MultipartEntityBuilder form)
			throws RequestException {
		MultipartEntityBuilder ret = super.processForm(form);
		final IBWDataReader reader = new DBReader(this.getContext());
		final interval_cursor cursor = new interval_cursor();
		cursor._end = cursor._start = this._sessionId;
		try {
			int num = 0;
			final JSONArray entries = new JSONArray();
			if(0 < (num = reader.searchLocations(new bwSessionCriteria(this._sessionId), new ILocationVisitor(){
				@Override
				public boolean visit(long sessionId, long millisec,
						Location location) throws VisitorAbortException,
						DataSourceException {
					cursor.setNext(millisec);
					final JSONObject part_loc = new JSONObject();
					try {
						part_loc.put("lat", location.getLatitude());
						part_loc.put("lng", location.getLongitude());
						int found = reader.searchMentalStats(
							new IntervalCriteria(_sessionId, cursor._start, cursor._end)
							, new IMentalGroupStatsVisitor(){
								@Override
								public boolean visit(long sessinId,
									IBWMentalGroupStats gstat)
									throws VisitorAbortException,
									DataSourceException {
										JSONObject entry = new JSONObject();
										try {
											entry.put("epoc", cursor._end);
											entry.put("loc", part_loc);
											entry.put("att", toJSONObject(gstat.getAttentionStat()));
											entry.put("med", toJSONObject(gstat.getMeditationStat()));
											entries.put(entry);
											return true;
										} catch (JSONException e) {
											throw new DataSourceException(e);
										}
									}
								});
						return (0 < found);
					} catch (JSONException e) {
						throw new DataSourceException(e);
					}
				}
			}))){
				ret.addTextBody("entries", entries.toString(), text_plain_utf8);
				ret.addTextBody("num", String.valueOf(num), text_plain_utf8);
			}
		} catch (DataSourceException e) {
			throw new RequestException(e);
		}
		return ret;
	}

}
