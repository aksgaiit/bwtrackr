package ac.aiit.bwcam.bwtracker.common;

public abstract class sessionChildRecorderBase implements ISensorRecorder {
	protected FederatedSensorRecorder _parent = null;
	sessionChildRecorderBase(FederatedSensorRecorder parent){
		super();
		this._parent = parent;
	}
	
	protected abstract void _startRecording(long session) throws RecorderException;


	@Override
	public long startRecording() throws RecorderException {
		long ret = this._parent.getCurrentSession();
		this._startRecording(ret);
		return ret;
	}

}
