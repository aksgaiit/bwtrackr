package ac.aiit.bwcam.bwtracker.share.apache.impl;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest;
import android.content.Context;

public abstract class SessionRequestBase extends RequestBase implements IRequest {
	protected Long _sessionId = null;

	public SessionRequestBase(Context context, long sessionId) {
		super(context);
		this._sessionId = sessionId;
	}

	@Override
	protected MultipartEntityBuilder processForm(MultipartEntityBuilder form) throws RequestException{
		form.addTextBody("session", String.valueOf(this._sessionId), RequestBase.text_plain_utf8);
		return form;
	}
}
