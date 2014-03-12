package ac.aiit.bwcam.bwtracker.data.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DistanceCriteria<T, F> extends bwSessionCriteria implements Iterable<elemDistance<T>>{
	private F _filter = null;
	private Set<elemDistance<T>> _elems = new HashSet<elemDistance<T>>();
	private boolean _inner = true;

	public DistanceCriteria(long sessionId, F filter) {
		super(sessionId);
		this._filter = filter;
	}
	public DistanceCriteria(long sessionId, F filter, boolean inner){
		super(sessionId);
		this._filter = filter;
		this._inner = inner;
	}
	public boolean isInner(){
		return this._inner;
	}
	public F getThreshold(){
		return this._filter;
	}
	public boolean add(elemDistance<T> elem){
		return this._elems.add(elem);
	}
	@Override
	public Iterator<elemDistance<T>> iterator() {
		return this._elems.iterator();
	}
	public boolean isEmpty() {
		return _elems.isEmpty();
	}
	public int size() {
		return _elems.size();
	}
}
