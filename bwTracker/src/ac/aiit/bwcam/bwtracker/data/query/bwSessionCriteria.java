package ac.aiit.bwcam.bwtracker.data.query;

import android.util.SparseArray;

public class bwSessionCriteria {
	protected Long _sessionId = null;
	protected Long _offset = null;
	protected Long _limit = null;
	protected sortOrder _order = null;
	protected String _sortKey = null;
	
	public static class sortOrder{
		private int _id;
		private String _name = null;
		private sortOrder(int id, String name){
			super();
			this._id = id;
			this._name = name;
		}
		public int getId(){
			return this._id;
		}
		public static final sortOrder asc = new sortOrder(1, "asc");
		public static final sortOrder desc = new sortOrder(2, "desc");
		private static SparseArray<sortOrder> _types = new SparseArray<sortOrder>(){
			{
				put(asc.getId(), asc);
				put(desc.getId(), desc);
			}
		};
		public static final sortOrder getInstance(int id){
			return _types.get(id);
		}
		@Override
		public String toString() {
			return this._name;
		}
		
	}
	public bwSessionCriteria(long sessionId){
		super();
		this._sessionId = sessionId;
	}
	public long getSessionId(){
		return this._sessionId;
	}
	public Long getOffset(){
		return this._offset;
	}
	public Long getLimit(){
		return this._limit;
	}
	public Long setOffset(Long offs){
		Long ret = this._offset;
		this._offset = offs;
		return ret;
	}
	public Long setLimit(Long limit){
		Long ret = this._limit;
		this._limit = limit;
		return ret;
	}
	public sortOrder setOrder(sortOrder order){
		sortOrder ret = this._order;
		this._order = order;
		return ret;
	}
	public sortOrder getOrder(){
		return this._order;
	}
	
	public String setSortKey(String key){
		String ret = this._sortKey;
		this._sortKey = key;
		return ret;
	}
	public String getSortKey(){
		return this._sortKey;
	}
}
