package ac.aiit.bwcam.bwtracker.neuro;

import com.neurosky.thinkgear.TGDevice;

public interface ITGStateListener {
	public static class TGError{
		private String _name = null;
		private TGError(String name){
			super();
			this._name = name;
		}
		public static TGError not_found = new TGError("not_found");
		public static TGError not_paired = new TGError("not_paired"); 
	}
	public void onIdle();
	public void onConnecting();
	public void onConnected();
	public void onDisconnected();
	public void onError(TGError err);
/*
 * 					case TGDevice.STATE_IDLE:
						// this._parent.onDisconnect();
						break;
					case TGDevice.STATE_CONNECTING:
						this._parent.insertLog("Connecting...");
						break;
					case TGDevice.STATE_CONNECTED:
						this._parent.onConnect();
						break;
					case TGDevice.STATE_NOT_FOUND:
						this._parent.insertLog("Can't find");
						this._parent.onDisconnect();
						break;
					case TGDevice.STATE_NOT_PAIRED:
						this._parent.insertLog("not paired");
						break;
					case TGDevice.STATE_DISCONNECTED:
						this._parent.onDisconnect();
						break;

 */
}
