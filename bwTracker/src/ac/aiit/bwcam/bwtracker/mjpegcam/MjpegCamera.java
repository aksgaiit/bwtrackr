/*
 * Copyright (C) 2000, Intel Corporation, all rights reserved.
 * Third party copyrights are property of their respective owners.
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistribution's of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistribution's in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The name of Intel Corporation may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the Intel Corporation or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */


package ac.aiit.bwcam.bwtracker.mjpegcam;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import ac.aiit.bwcam.bwtracker.R;
import ac.aiit.bwcam.bwtracker.SettingsActivity;
import ac.aiit.bwcam.bwtracker.ui.IPreviewObject;
import ac.aiit.bwcam.bwtracker.ui.UIException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.ViewGroup;

import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;

public class MjpegCamera extends MjpegView implements IPreviewObject {
	public MjpegCamera(Context context){
		super(context);
	}
    private boolean _cam_started = false;

	@Override
	public String startPreview(ViewGroup parent) throws UIException {
		parent.addView(this);
		try{
		    if(!this._cam_started){
				SharedPreferences prefs = SettingsActivity.getSharedPreferences(this.getContext());
				new DoRead(this).execute(String.format("http://%s:%d/?%s"
						, SettingsActivity.getWifiCamAddress(prefs)
						, SettingsActivity.getWifiCamPort(prefs)
						, SettingsActivity.getWifiCommand(prefs)));//ai-ball
		    }else{
		    	this.startPlayback();
		    }
		    return this.getResources().getString(R.string.pref_cam_wifi);
		}finally{
			
		}
	}

	@Override
	public void stopPreview(ViewGroup parent) {
		this.stopPlayback();
		parent.removeView(this);
	}
    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
    	private MjpegCamera _parent = null;
    	public DoRead(MjpegCamera parent){
    		super();
    		this._parent = parent;
    	}
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient(); 
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                 //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
        	this._parent.setSource(result);
            if(result!=null) result.setSkip(1);
            this._parent.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            this._parent.showFps(false);
        }
    }
	@Override
	public void startRecording(long session) throws UIException {
		try {
			this._startstopRecorder(session, true);
		} catch (IOException e) {
			throw new UIException(e);
		}
	}

	@Override
	public void stopRecording() {
		try {
			this._startstopRecorder(0, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
