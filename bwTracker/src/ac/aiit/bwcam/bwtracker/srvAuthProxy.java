package ac.aiit.bwcam.bwtracker;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class srvAuthProxy extends Activity {
	private WebView _webView = null;
	
	private static final String pref_key_srv_apiKey = "srv_api_key";
	private static final String pref_key_srv_apiBase = "srv_api_base";
	private static final String pref_key_srv_userName = "srv_user_name";
	private static final String pref_key_srv_userId = "srv_user_id";
	
	private static final SharedPreferences getSharedPreferences(Context ctx){
		return ctx.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
	}
	public static final String getApiKey(Context ctx){
		return getSharedPreferences(ctx).getString(pref_key_srv_apiKey, null);
	}
	public static final String getApiBase(Context ctx){
		return getSharedPreferences(ctx).getString(pref_key_srv_apiBase, null);
	}
	public static final String getUserName(Context ctx){
		return getSharedPreferences(ctx).getString(pref_key_srv_userName, null);
	}
	public static final String getUserId(Context ctx){
		return getSharedPreferences(ctx).getString(pref_key_srv_userId, null);
	}
	private void alert(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	public static class authHandler{
		private srvAuthProxy _parent = null;
		public authHandler(srvAuthProxy parent){
			super();
			this._parent = parent;
		}
		@JavascriptInterface
		public void onCredentialError(String sjson){
			try {
				JSONObject json = new JSONObject(sjson);
				this._parent.alert(json.getString("msg"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		@JavascriptInterface
		public void onCredentialReady(String sjson){
			try {
				JSONObject json = new JSONObject(sjson);
				String apiKey = json.getString("apiKey");
				String apiBase = json.getString("apiBase");
				String userName = json.getString("name");
				String userId = json.getString("id");
				
				SharedPreferences prefs = getSharedPreferences(this._parent);
				Editor editor = prefs.edit();
				if(null != apiKey){
					editor.putString(pref_key_srv_apiKey, apiKey);
				}
				if(null != apiBase){
					editor.putString(pref_key_srv_apiBase, apiBase);
				}
				if(null != userName){
					editor.putString(pref_key_srv_userName, userName);
				}
				if(null != userId){
					editor.putString(pref_key_srv_userId, userId);
				}
				editor.commit();
				this._parent.alert("credential was successfully stored!");
				this._parent.finish();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = this.getIntent();
		String baseUrl = intent.getStringExtra("base_url");
		if(null != baseUrl){
			if(baseUrl.endsWith("/")){
				baseUrl.substring(0, baseUrl.length() - 1);
			}
			this._webView.loadUrl(baseUrl + "/bwt/authProxy");
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.srv_auth_proxy);
		this._webView = (WebView)this.findViewById(R.id.wv_auth_prxy);
		this._webView.getSettings().setJavaScriptEnabled(true);
		this._webView.getSettings().setAppCacheEnabled(true);
		this._webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		this._webView.setWebViewClient(new WebViewClient());
		this._webView.addJavascriptInterface(new authHandler(this), "__msgbroker");
		//attach listener
		//check loaded url
		this._webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.endsWith("bwt/authProxy")){
					view.loadUrl("javascript:(function(){if(window['pingCredential']){pingCredential();}})()");
				}
			}
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				alert(String.format("Received page load error %d\n%s\n%s",errorCode,description,failingUrl));
				finish();
			}
		});
	}
	public void closeActivity(View view){
		this.finish();
	}

}
