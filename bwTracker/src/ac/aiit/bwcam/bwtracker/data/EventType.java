package ac.aiit.bwcam.bwtracker.data;

import android.util.SparseArray;

public class EventType {
	private int _id;
	private EventType(int id){
		super();
		this._id = id;
	}
	public int getId(){
		return this._id;
	}
	public static final EventType Tapped = new EventType(1);
	public static final EventType Captured = new EventType(2);
	private static final SparseArray<EventType> _types = new SparseArray<EventType>(){
		{
			put(Tapped.getId(), Tapped);
			put(Captured.getId(), Captured);
		}
	};
	public static final EventType getInstance(int id){
		return _types.get(id);
	}
}
