package ac.aiit.bwcam.bwtracker;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;

public class SettingsActivity  extends Activity {

	private static final boolean default_wfcam_checked = false;
	private static final String key_wfcam_checked = "wfcam_checked";
	private static final String default_addr = "192.168.2.1";
	private static final String key_addr ="wfcam_addr";
	private static final Integer default_port = 80;
	private static final String key_port = "wfcam_port";
	private static final String default_command = "action=stream";
	private static final String key_command = "wfcam_command";
	
	private static final String key_incam_recsize = "incam_recsize";
	private static final int camcoderprofile_default = CamcorderProfile.QUALITY_480P;
	
	public static final SharedPreferences getSharedPreferences(Context ctx){
		return ctx.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
	}
	public static final boolean getWifiCamChecked(SharedPreferences prefs){
		return prefs.getBoolean(key_wfcam_checked, default_wfcam_checked);
	}
	public static final String getWifiCamAddress(SharedPreferences prefs){
		return prefs.getString(key_addr, default_addr);
	}
	public static final Integer getWifiCamPort(SharedPreferences prefs){
		return prefs.getInt(key_port, default_port);
	}
	public static final String getWifiCommand(SharedPreferences prefs){
		return prefs.getString(key_command, default_command);
	}
	public static final Integer getIncamCorderProfileId(SharedPreferences prefs){
		return prefs.getInt(key_incam_recsize, camcoderprofile_default);
	}
	
	private static final HashMap<Integer, Integer> _radioid_recsize = new HashMap<Integer, Integer>(){
		private static final long serialVersionUID = 7999050416741282774L;
		{
			put(R.id.rz_480p, CamcorderProfile.QUALITY_480P);
			put(R.id.rz_720p, CamcorderProfile.QUALITY_720P);
			put(R.id.rz_1080p, CamcorderProfile.QUALITY_1080P);
		}
	};
	private static final HashMap<Integer, Integer> _recsize_radioid = new HashMap<Integer, Integer>(){
		private static final long serialVersionUID = -8968249818077059609L;
		{
			put(CamcorderProfile.QUALITY_480P, R.id.rz_480p);
			put(CamcorderProfile.QUALITY_720P, R.id.rz_720p);
			put(CamcorderProfile.QUALITY_1080P, R.id.rz_1080p);
		}
	};
	private void initIncamScreenSettings(final SharedPreferences prefs){
		RadioGroup rg = (RadioGroup)this.findViewById(R.id.incam_recsize_rgroup);
		Integer profid = getIncamCorderProfileId(prefs);
		Integer radioid = _recsize_radioid.get(profid);
		rg.check(radioid);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				prefs.edit()
				.putInt(key_incam_recsize, _radioid_recsize.get(Integer.valueOf(checkedId)))
				.commit();
			}
		});
	}
	private void initTabs(){
		TabHost tabhost = (TabHost) ((ViewGroup)this.findViewById(android.R.id.content)).getChildAt(0);
		tabhost.setup();
		tabhost.addTab(tabhost.newTabSpec("tab1").setIndicator(
				this.getResources().getString(R.string.pref_cam_builtin)).setContent(R.id.tab1));
		tabhost.addTab(tabhost.newTabSpec("tab2").setIndicator(
				this.getResources().getString(R.string.pref_cam_wifi)).setContent(R.id.tab2));
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		this.initTabs();
		final SharedPreferences prefs = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
		this.initIncamScreenSettings(prefs);
		CheckBox cb = (CheckBox)this.findViewById(R.id.check_use_wifi);
		cb.setChecked(getWifiCamChecked(prefs));
		cb.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				CheckBox v = (CheckBox)arg0;
				Editor editor = prefs.edit();
				editor.putBoolean(key_wfcam_checked, v.isChecked());
				editor.commit();
			}
			
		});
		TextView tv = (TextView)this.findViewById(R.id.pref_edit_addr);
		tv.setText(getWifiCamAddress(prefs));
		tv.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence value, int arg1, int arg2,
					int arg3) {
				Editor editor = prefs.edit();
				editor.putString(key_addr, value.toString());
				editor.commit();
			}
		});
		tv = (TextView)this.findViewById(R.id.pref_edit_port);
		tv.setText(String.valueOf(getWifiCamPort(prefs)));
		tv.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
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
				editor.putInt(key_port, Integer.valueOf(s.toString()));
				editor.commit();
			}
		});
		tv = (TextView)this.findViewById(R.id.pref_edit_command);
		tv.setText(getWifiCommand(prefs));
		tv.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
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
				editor.putString(key_command, s.toString());
				editor.commit();
			}
		});
	}


}
