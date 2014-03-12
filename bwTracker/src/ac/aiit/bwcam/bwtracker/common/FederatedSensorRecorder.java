package ac.aiit.bwcam.bwtracker.common;

import java.util.HashSet;

public class FederatedSensorRecorder implements ISensorRecorder {
	private Long _session = null;
	protected HashSet<sessionChildRecorderBase> _recorders = new HashSet<sessionChildRecorderBase>();
	
	public boolean register(sessionChildRecorderBase recorder){
		return this.addremove(recorder, false);
	}
	
	private synchronized boolean addremove(sessionChildRecorderBase recorder, boolean remove){
		if(!remove){
			return this._recorders.add(recorder);
		}else{
			return this._recorders.remove(recorder);
		}
	}
	
	public boolean unregister(sessionChildRecorderBase recorder){
		return this.addremove(recorder, true);
	}

	@Override
	public void prepareRecording() throws RecorderException {
		FederatedRecorderException exp = null;
		for(ISensorRecorder r: this._recorders){
			try{
				r.prepareRecording();
			}catch(RecorderException e){
				if(null == exp){
					exp = new FederatedRecorderException();
				}
				exp.addChild(e);
			}
		}
		if(null != exp){
			throw exp;
		}
	}

	@Override
	public long startRecording() throws RecorderException {
		this._session = System.currentTimeMillis();
		FederatedRecorderException exp = null;
		for(ISensorRecorder r: this._recorders){
			try{
				r.startRecording();
			}catch(RecorderException e){
				if(null == exp){
					exp = new FederatedRecorderException();
				}
				exp.addChild(e);
			}
		}
		if(null != exp){
			throw exp;
		}
		return this._session;
	}
	protected long getCurrentSession(){
		return this._session;
	}
	@Override
	public void stopRecording() throws RecorderException {
		FederatedRecorderException exp = null;
		for(ISensorRecorder r: this._recorders){
			try{
				r.stopRecording();
			}catch(RecorderException e){
				if(null == exp){
					exp = new FederatedRecorderException();
				}
				exp.addChild(e);
			}
		}
		if(null != exp){
			throw exp;
		}
	}

	@Override
	public void close() throws RecorderException {
		FederatedRecorderException exp = null;
		for(ISensorRecorder r: this._recorders){
			try{
				r.close();
			}catch(RecorderException e){
				if(null == exp){
					exp = new FederatedRecorderException();
				}
				exp.addChild(e);
			}
		}
		if(null != exp){
			throw exp;
		}
	}

}
