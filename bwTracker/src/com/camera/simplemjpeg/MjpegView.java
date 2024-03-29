/* Copyright (C) 2000, Intel Corporation, all rights reserved.
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

package com.camera.simplemjpeg;

import java.io.IOException;

import ac.aiit.bwcam.bwtracker.mjpegcam.FrameRecorderException;
import ac.aiit.bwcam.bwtracker.mjpegcam.IFrameRecorder;
import ac.aiit.bwcam.bwtracker.mjpegcam.asyncMovieRecorder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
   
	public static final String TAG="MJPEG";
	
	public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;
    
    SurfaceHolder holder;
	Context saved_context;
    
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;    
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;    

    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

	private boolean suspending = false;
	
	private Bitmap bmp = null;
	
    protected IFrameRecorder _recorder = null;
	// image size

	public static int IMG_WIDTH=640;
	public static int IMG_HEIGHT=480;

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private String fps = "";

         
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { 
            mSurfaceHolder = surfaceHolder; 
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN)
                return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }
         
        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }
         
        private Bitmap makeFpsOverlay(Paint p) {
            Rect b = new Rect();
            p.getTextBounds(fps, 0, fps.length(), b);

            // false indentation to fix forum layout             
            Bitmap bm = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, b.width(), b.height(), p);
            p.setColor(overlayTextColor);
            c.drawText(fps, -b.left, b.bottom-b.top-p.descent(), p);
            return bm;        	 
        }

        public void run_test(){
			byte buf[] = new byte[4096];
			try {
				while (mRun) {
					mIn.read(buf);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

            int width;
            int height;
            Paint p = new Paint();
            Bitmap ovl=null;
            // Set in the surface changed method
			if(null != _recorder){
				_recorder.onEnd();
			}

            try {
	            while (mRun) {

	                Rect destRect=null;
	                Canvas c = null;

	                if(surfaceDone) {   
	                	try {
	                		if(bmp==null){
	                			bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
	                		}
	                		int ret = mIn.readMjpegFrame(bmp);

	                		if(ret == -1)
	                		{
	                			return;
	                		}


	                        destRect = destRect(bmp.getWidth(),bmp.getHeight());

	                        if(null != _recorder){
	                        	_recorder.onFrame(bmp, System.currentTimeMillis());
	                        }
	                        
	                        c = mSurfaceHolder.lockCanvas();
	                        synchronized (mSurfaceHolder) {

	                               	c.drawBitmap(bmp, null, destRect, p);

	                                if(true){//(showFps) {
	                                    p.setXfermode(mode);
	                                    if(ovl != null) {

	                                    	// false indentation to fix forum layout 	                                	 
	                                    	height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();
	                                    	width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right -ovl.getWidth();

	                                        c.drawBitmap(ovl, width, height, null);
	                                    }
	                                    p.setXfermode(null);
	                                    frameCounter++;
	                                    if((System.currentTimeMillis() - start) >= 1000) {
	                                        fps = String.valueOf(frameCounter)+"fps";
	                                        frameCounter = 0; 
	                                        start = System.currentTimeMillis();
	                                        if(ovl!=null) ovl.recycle();
	                                    	
	                                        ovl = makeFpsOverlay(overlayPaint);
	                                    }
	                                }
	                                

	                        }

	                    }catch (IOException e){ 
	                	
	                }finally { 
	                   	if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); 
	                }
	                }
	            }
			} catch (FrameRecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(null != _recorder){
					_recorder.onEnd();
				}
			}
        }
    }
    
    protected synchronized void _startstopRecorder(long session, boolean start) throws IOException{
    	if(start){
			IFrameRecorder rec = asyncMovieRecorder.getInstance(session, IMG_WIDTH, IMG_HEIGHT, 1);
			rec.onStart();
			this._recorder =  rec;
    	}else{
    		if(null != this._recorder){
    			IFrameRecorder rec = this._recorder;
    			this._recorder = null;
    			rec.onEnd();
    		}
    	}
    }

    private void init(Context context) {
    	
        //SurfaceHolder holder = getHolder();
    	holder = getHolder();
    	saved_context = context;
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }
    
    public void startPlayback() { 
        if(mIn != null) {
            mRun = true;
            if(thread==null){
            	thread = new MjpegViewThread(holder, saved_context);
            }
            thread.start();    		
        }
    }
    
    public void resumePlayback() { 
        if(suspending){
            if(mIn != null) {
                mRun = true;
                SurfaceHolder holder = getHolder();
                holder.addCallback(this);
                thread = new MjpegViewThread(holder, saved_context);		
                thread.start();
                suspending=false;
            }
        }
    }
    public void stopPlayback() { 
    	if(mRun){
    		suspending = true;
    	}
        mRun = false;
        if(thread!=null){
        	boolean retry = true;
	        while(retry) {
	            try {
	                thread.join();
	                retry = false;
	            } catch (InterruptedException e) {}
	        }
	        thread = null;
        }
        if(mIn!=null){
	        try{
	        	mIn.close();
	        }catch(IOException e){}
	        mIn = null;
        }

    }

    public void freeCameraMemory(){
    	if(mIn!=null){
    		mIn.freeCameraMemory();
    	}
    }
    
    public MjpegView(Context context, AttributeSet attrs) { 
        super(context, attrs); init(context); 
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
    	if(thread!=null){
    		thread.setSurfaceSize(w, h); 
    	}
    }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }
    
    public MjpegView(Context context) { super(context); init(context); }    
    public void surfaceCreated(SurfaceHolder holder) { surfaceDone = true; }
    public void showFps(boolean b) { showFps = b; }
    public void setSource(MjpegInputStream source) {
    	mIn = source; 
    	if(!suspending){
    		startPlayback();
    	}else{
    		resumePlayback();
    	}
    }
    public void setOverlayPaint(Paint p) { overlayPaint = p; }
    public void setOverlayTextColor(int c) { overlayTextColor = c; }
    public void setOverlayBackgroundColor(int c) { overlayBackgroundColor = c; }
    public void setOverlayPosition(int p) { ovlPos = p; }
    public void setDisplayMode(int s) { displayMode = s; }
    
    public void setResolution(int w, int h){
    	IMG_WIDTH = w;
    	IMG_HEIGHT = h;
    }
    
	public boolean isStreaming(){
		return mRun;
	}
}
