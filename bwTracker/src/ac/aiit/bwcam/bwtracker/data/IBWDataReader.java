package ac.aiit.bwcam.bwtracker.data;

import ac.aiit.bwcam.bwtracker.data.query.ByTimeCriteria;
import ac.aiit.bwcam.bwtracker.data.query.bwSessionCriteria;
import ac.aiit.bwcam.bwtracker.data.query.IntervalCriteria;
import ac.aiit.bwcam.bwtracker.data.query.ModerationCriteria;


public interface IBWDataReader {
	public int traverseRawdata(IntervalCriteria criteria, IRawdataVisitor visitor) throws DataSourceException;
	public int searchLocations(bwSessionCriteria criteria, ILocationVisitor visitor) throws DataSourceException;
	public int searchMentalStats(ModerationCriteria criteria, IMentalStatsVisitor visitor)throws DataSourceException;
	public int searchMentalStats(ByTimeCriteria criteria, IMentalStatsVisitor visitor)throws DataSourceException;
	public int searchMentalStats(IntervalCriteria criteria, IMentalStatsVisitor visitor)throws DataSourceException;
	public int searchMentalStats(IntervalCriteria criteria, IMentalGroupStatsVisitor visitor)throws DataSourceException;
}
