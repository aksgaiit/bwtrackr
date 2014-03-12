package ac.aiit.bwcam.bwtracker.share.apache.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import ac.aiit.bwcam.bwtracker.SrvSettingsActivity;
import ac.aiit.bwcam.bwtracker.srvAuthProxy;
import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest;
import android.content.Context;
import android.os.AsyncTask;

public abstract class RequestBase implements IRequest {
	public static final ContentType text_plain_utf8 = ContentType.TEXT_PLAIN.withCharset(Charset.forName("UTF-8"));
	public static final ContentType text_html_utf8 = ContentType.TEXT_HTML.withCharset(Charset.forName("UTF-8"));
	protected abstract MultipartEntityBuilder processForm(MultipartEntityBuilder form) throws RequestException;
	abstract protected String getSubpath();

	private Context _context = null;
	public RequestBase(Context context){
		super();
		this._context = context;
	}

	public Context getContext() {
		return this._context;
	}
	
	private long _total = 0;
	private long _offs = 0;
	private Throwable _lastException = null;
	public Throwable getLastException(){
		return this._lastException;
	}
	

	@Override
	public void submit(final IResponseHandler rhandler, final IExceptionHandler ehandler, final IProgressHandler phandler) throws RequestException {
		this._total = 0;
		this._offs = 0;
		String url = String.format("%s/api.php%s?apiKey=%s",
				SrvSettingsActivity.getServerBaseUrl(this._context),
				this.getSubpath(), srvAuthProxy.getApiKey(this._context));

		AsyncTask<String, Integer, HttpResponse> task = new AsyncTask<String, Integer, HttpResponse>(){

			@Override
			protected void onProgressUpdate(Integer... values) {
				if(null != phandler){
					_offs += values[1];
					phandler.onProgress(_offs, _total);
				}
			}

			@Override
			protected HttpResponse doInBackground(String... params) {
				String url = params[0];
				HttpParams hprms = new BasicHttpParams();
				hprms.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				HttpClient client = new DefaultHttpClient(hprms);
				HttpPost post = new HttpPost(url);
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				builder.setCharset(Charset.forName("UTF-8"));
				try {
					builder = processForm(builder);
					HttpEntity he = builder.build();
					_total = he.getContentLength();
/*					class ProgressiveEntity implements HttpEntity {
						@Override
						public void consumeContent() throws IOException {
							he.consumeContent();
						}
						@Override
						public InputStream getContent() throws IOException,
								IllegalStateException {
							return he.getContent();
						}
						@Override
						public Header getContentEncoding() {
							return he.getContentEncoding();
						}
						@Override
						public long getContentLength() {
							return he.getContentLength();
						}
						@Override
						public Header getContentType() {
							return he.getContentType();
						}
						@Override
						public boolean isChunked() {
							return he.isChunked();
						}
						@Override
						public boolean isRepeatable() {
							return this.isRepeatable();
						}
						@Override
						public boolean isStreaming() {
							return he.isStreaming();
						}
						@Override
						public void writeTo(OutputStream outstream)
								throws IOException {
							he.writeTo(new FilterOutputStream(outstream) {
								@Override
								public void close() throws IOException {
									this.out.close();
								}

								@Override
								public void flush() throws IOException {
									this.out.close();
								}

								@Override
								public void write(byte[] buffer, int offset,
										int length) throws IOException {
									this.out.write(buffer, offset, length);
									publishProgress(length);
								}

								@Override
								public void write(int oneByte)
										throws IOException {
									this.out.write(oneByte);
								}

								@Override
								public void write(byte[] buffer)
										throws IOException {
									this.out.write(buffer);
								}
							});
						}
					}*/
					post.setEntity(he);//new ProgressiveEntity());
					HttpResponse ret = client.execute(post);
					return ret;
				} catch (ClientProtocolException e) {
					_lastException = e;
					this.cancel(true);
				} catch (IOException e) {
					_lastException = e;
					this.cancel(true);
				} catch (RequestException e) {
					_lastException = e;
					this.cancel(true);
				}
				return null;
			}
			@Override
			protected void onPostExecute(HttpResponse result) {
				if(null != rhandler && null != result){
					rhandler.onResponse(result);
				}
			}
		};
		task.execute(url);
		if(task.isCancelled()){
			if(null != this._lastException && null != ehandler){
				ehandler.onException(this._lastException);
			}
		}
		
	}

}
