package ac.aiit.bwcam.bwtracker.mjpegcam;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import ac.aiit.bwcam.bwtracker.media.VideoManager;
import android.graphics.Bitmap;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class asyncMovieRecorder implements IFrameRecorder, Runnable {
	private FFmpegFrameRecorder _recorder = null;
	private boolean _running = false;
	private boolean _tobereleased = false;
	private boolean _recording = false;
	//new asyncMovieRecorder(VideoManager.getInstance().generateFileName4Session(System.currentTimeMillis()), IMG_WIDTH, IMG_HEIGHT, 1);

	private File _tmpfile = null;
	private String _dstfilename = null;
	private Long _session = null;
			
	protected asyncMovieRecorder(long session){
		super();
		this._session = Long.valueOf(session);
	}
	public static final asyncMovieRecorder getInstance(long session, int imageWidth, int imageHeight, int audioChannels) throws IOException{
		asyncMovieRecorder ret = new asyncMovieRecorder(session);
		ret._tmpfile = VideoManager.getInstance().createFile4Session(session);
		ret.init(ret._tmpfile.getAbsolutePath(), imageWidth, imageHeight, audioChannels);
		return ret;
	}
	protected void init(String filename, int imageWidth, int imageHeight, int audioChannels){
		this._recorder = new FFmpegFrameRecorder(filename, imageWidth, imageHeight, audioChannels);
		this._recorder.setVideoCodec(13);
		this._recorder.setFormat("mp4");
        this._recorder.setFrameRate(15);
	}
	
	public class FrameEntry {
		private long _nanooffs;
		private long _epoc;
		private ByteBuffer _buffer = null;
		FrameEntry(Bitmap image, long nanooffs, long epoc){
			super();
			this._nanooffs = nanooffs;
			this._epoc = epoc;
			this._buffer = ByteBuffer.allocate(image.getByteCount());
	        image.copyPixelsToBuffer(this._buffer);
	        this._buffer.rewind();
		}
		public long getNanoOffs(){
			return this._nanooffs;
		}
		public ByteBuffer getImageBuffer(){
			return this._buffer;
		}
		public long getEpoc(){
			return this._epoc;
		}
	}
	
	private ArrayBlockingQueue<FrameEntry> _queue = new ArrayBlockingQueue<FrameEntry>(100);
	
	public void put(Bitmap bmp, long curtime, long starttime) throws InterruptedException{
        this._queue.put(new FrameEntry(bmp, 1000 * (curtime - starttime), curtime));
	}
	public void stop() throws Exception{
		if(this._recording){
			this._recording = false;
		}else{
			this._recorder.stop();
		}
	}
	private void _release() throws Exception{
		this._recorder.release();
		this._recorder = null;
	}
	public void release() throws Exception{
		if(this._running){
			this._tobereleased = true;
		}else{
			this._release();
		}
	}

	private static int IMG_WIDTH = 640;
	private static int IMG_HEIGHT = 480;
	@Override
	public void run() {
		try {
			this._running = true;
			IplImage yuvIplimage = IplImage.create(IMG_WIDTH, IMG_HEIGHT,  IPL_DEPTH_8U, 4);
			FrameEntry entry = null;
			this._recorder.start();
			this._recording = true;
			while(null != entry || this._recording){
					entry = this._queue.poll(10, TimeUnit.MILLISECONDS);
					if(null != entry){
						if(null != this._recorder){
							this._recorder.setTimestamp(entry.getNanoOffs());
							try {
						        yuvIplimage.getByteBuffer().put(entry.getImageBuffer());
								this._recorder.record(yuvIplimage);
								if(null == this._dstfilename){
									this._dstfilename = VideoManager.getInstance().generateFileName(this._session, entry.getEpoc());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}finally{
							}
						}
					}
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				this._recorder.stop();
			} catch (Exception e) {
			}
			if(this._tobereleased){
				try {
					this._release();
				} catch (Exception e) {
				}
				this._tobereleased = false;
			}
			if(null != this._tmpfile){
				if(null != this._dstfilename){
					this._tmpfile.renameTo(new File(this._dstfilename));
					this._dstfilename = null;

				}
				this._tmpfile = null;
			}
			this._running = false;
		}
	}

	private long _lasttime = 0;
	private Long _starttime = null;
	private double _fdur = 0;
	@Override
	public void onStart() {
        double rcfps = this._recorder.getFrameRate();
       	this._fdur = 1000.0 / rcfps;
        this._lasttime = 0;
        this._starttime = null;
        Thread th = new Thread(this);
		th.start();
	}

	
	@Override
	public void onFrame(Bitmap bmp, long curtime) throws FrameRecorderException{
		if(null == this._starttime){
			this._starttime = Long.valueOf(curtime);
		}
        if(curtime > (this._lasttime + this._fdur)){
        	this._lasttime = curtime;
        	try {
				this.put(bmp, curtime, this._starttime);
			} catch (InterruptedException e) {
				throw new FrameRecorderException(e);
			}
        }
	}

	@Override
	public void onEnd() {
		try {
			this.stop();
			this.release();
		} catch (Exception e) {
		}
	}

}
