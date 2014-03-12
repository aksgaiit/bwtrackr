package ac.aiit.bwcam.bwtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SrvSettingsActivity extends Activity {
	public static final String pref_key_srv_baseUrl = "srv_url_base";
	public static final String pref_key_eeg_view_gradient = "eeg_view_gradient";

	private static final SharedPreferences getSharedPreferences(Context ctx){
		return ctx.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
	}
	public static final String getServerBaseUrl(Context ctx){
		return getSharedPreferences(ctx).getString(pref_key_srv_baseUrl, "http://localhost/op/web");
	}
	public static final boolean isEEGViewGradient(Context ctx){
		return getSharedPreferences(ctx).getBoolean(pref_key_eeg_view_gradient, false);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.srv_settings);
		final SharedPreferences prefs = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
		TextView tx_baseUrl = (TextView)this.findViewById(R.id.pref_edit_srv_base);
		tx_baseUrl.setText(getServerBaseUrl(this));
		tx_baseUrl.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Editor editor = prefs.edit();
				editor.putString(pref_key_srv_baseUrl, s.toString());
				editor.commit();
			}
		});
		TextView txUserName = (TextView)this.findViewById(R.id.tx_user_name);
		txUserName.setText(srvAuthProxy.getUserName(this));
		Switch eeg_gradient = (Switch)this.findViewById(R.id.eeg_gradient);
		eeg_gradient.setChecked(isEEGViewGradient(this));
		eeg_gradient.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton compoundbutton,
					boolean checked) {
				Editor editor = prefs.edit();
				editor.putBoolean(pref_key_eeg_view_gradient, checked);
				editor.commit();
			}
			
		});
	}
	public void authProxy(View view){
		String baseUrl = getServerBaseUrl(this);
		if(null != baseUrl){
			Intent intent = new Intent(getApplicationContext(), srvAuthProxy.class);
			intent.putExtra("base_url", baseUrl);
			startActivity(intent);
		}else{
			Toast.makeText(this, "server url is not specified",  Toast.LENGTH_LONG).show();
		}
	}
}
