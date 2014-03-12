package ac.aiit.bwcam.bwtracker.common;

import java.util.ArrayList;
import java.util.Iterator;

public class FederatedRecorderException extends RecorderException implements Iterable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4083560592292331851L;
	private ArrayList<RecorderException> _exceptions = null;
	public boolean addChild(RecorderException exp){
		return this._exceptions.add(exp);
	}
	public Iterator<RecorderException> iterator() {
		return _exceptions.iterator();
	}

}
