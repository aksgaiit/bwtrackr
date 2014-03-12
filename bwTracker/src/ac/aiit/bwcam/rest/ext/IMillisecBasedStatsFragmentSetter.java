package ac.aiit.bwcam.rest.ext;

import org.json.JSONObject;

import ac.aiit.bwcam.bwtracker.data.DataSourceException;
import ac.aiit.bwcam.bwtracker.data.IByMillisecStats;

public interface IMillisecBasedStatsFragmentSetter {
	public void setFragments(IByMillisecStats stats, JSONObject target, String path) throws DataSourceException;
}
