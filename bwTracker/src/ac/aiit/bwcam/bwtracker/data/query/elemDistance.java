package ac.aiit.bwcam.bwtracker.data.query;

public class elemDistance<T> {
	private T _val = null;
	private String _name = null;
	public elemDistance(String name, T val){
		super();
		this._name = name;
		this._val = val;
	}
	public String getName(){
		return this._name;
	}
	public T getValue(){
		return this._val;
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof elemDistance<?>){
			elemDistance other = (elemDistance)o;
			if((other).getValue().getClass().equals(this._val.getClass())){
				return this._name.equals(other.getName());
			}
		}
		return super.equals(o);
	}
}
