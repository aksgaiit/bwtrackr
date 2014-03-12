package ac.aiit.bwcam.bwtracker.share.apache.impl;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import ac.aiit.bwcam.bwtracker.share.RequestException;
import android.content.Context;
import android.provider.Settings;

public abstract class PublicRequestBase extends SessionRequestBase {

	public PublicRequestBase(Context context, long sessionId) {
		super(context, sessionId);
	}
	private static String _serialId = null;
	protected synchronized String getDeviceSerial(){
		if(null == _serialId){
			_serialId = Settings.Secure.getString(this.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		}
		return _serialId;
	}
	@Override
	protected MultipartEntityBuilder processForm(MultipartEntityBuilder form) throws RequestException{
		MultipartEntityBuilder ret = super.processForm(form);
		ret.addTextBody("serial", this.getDeviceSerial(),text_plain_utf8);
		return ret;
	}

}
