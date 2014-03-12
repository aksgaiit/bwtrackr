package ac.aiit.bwcam.bwtracker.share.apache;

import org.apache.http.HttpResponse;

import ac.aiit.bwcam.bwtracker.share.RequestException;

public interface IRequest {
	public static interface IResponseHandler{
		public void onResponse(HttpResponse response);
	}
	public static interface IExceptionHandler{
		public void onException(Throwable e);
	}
	public static interface IProgressHandler{
		public void onProgress(long offs, long total);
	}
	public void submit(IResponseHandler rhandler, IExceptionHandler ehandler, IProgressHandler phandler) throws RequestException;
}
