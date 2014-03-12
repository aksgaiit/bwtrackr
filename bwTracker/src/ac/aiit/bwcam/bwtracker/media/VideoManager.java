package ac.aiit.bwcam.bwtracker.media;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ac.aiit.bwcam.bwtracker.common.VisitorAbortException;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore.Images.Thumbnails;

public class VideoManager {
	public static class mediaFile{
		private File _file = null;
		private Long _session = null;
		private Long _start = null;
		protected mediaFile(File file){
			super();
			this._file = file;
			this.init();
		}
		private static final Pattern _extfilter = Pattern.compile("\\.mp4$");
		private long[] parseFilename(File f){
			Matcher m = _extfilter.matcher(f.getName());
			String[] telems = m.replaceAll("").split("_");
			long[] ret = new long[telems.length];
			for(int i = 0; i < telems.length ; i++){
				ret[i] = Long.valueOf(telems[i]);
			}
			return ret;
		}
		private void init(){
			long[] vals = this.parseFilename(this._file);
			if(vals.length > 1){
				this._session = vals[0];
				this._start = vals[1];
			}
		}
		public Long getSession(){
			return this._session;
		}
		public Long getStartEpoc(){
			return this._start;
		}
		public String getAbsolutePath(){
			return this._file.getAbsolutePath();
		}
		public synchronized void deleteFile(){
			if(null != this._file){
				if(this._file.exists()){
					this._file.delete();
				}
				this._file = null;
			}
		}
		public boolean isValid(){
			return (null != this._file && null != this._session && null != this._start);
		}
		private MediaMetadataRetriever getRetriever(){
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(this._file.getAbsolutePath());
			return retriever;
		}
		public Bitmap getFrameByEpoc(long epoc){
			return this.getRetriever().getFrameAtTime(1000 * (epoc - this._start));
		}
		public int visitFramesByEpoc(Collection<Long> epocs, IFrameVisitor visitor) throws MediaSourceException{
			MediaMetadataRetriever retriever = this.getRetriever();
			int ret = 0;
			try{
				for(long epoc : epocs){
					long offs = epoc - this._start;
					Bitmap bmp = retriever.getFrameAtTime(1000 * offs);//(1000 * 1000 * 10);//
					if(null != bmp){
						if(visitor.visitBitmap(offs, epoc, bmp)){
							ret++;
						}
					}
				}
			} catch (VisitorAbortException e) {
			}
			return ret;
		}
		public Bitmap createVideoThumbnail(){
			return this.createVideoThumbnail(Thumbnails.MICRO_KIND);
		}
		public Bitmap createVideoThumbnail(int kind){
			return ThumbnailUtils.createVideoThumbnail(this.getAbsolutePath(), kind);
		}
	}
	private static final VideoManager _instance = new VideoManager();
	private VideoManager(){
		super();
		init();
	}
	public static final VideoManager getInstance(){
		return _instance;
	}
	private File _videoFolder = null;
	private void init(){
		if(null == _videoFolder){
			String path = String.format("%s/ac.aiit.bwcam.bwtracker"
					,Environment.getExternalStorageDirectory().getPath());
			File folder = new File(path);
			if(!folder.exists()){
				folder.mkdir();
			}
			_videoFolder = folder;
		}
	}
	protected File getVideoFolder(){
		return this._videoFolder;
	}
	public final String generateFileName(long session, long startmillis){
		File folder = this.getVideoFolder();
		return String.format("%s/%d_%d.mp4"
				,folder.getAbsolutePath()
				, session
				, startmillis);
	}
	public final String generateFileName4Session(long session){
		return generateFileName(session, (new Date()).getTime());
	}
	public final File createFile(long session, long startmillis)throws IOException{
		File ret = new File(this.generateFileName(session, startmillis));
		if(!ret.exists()){
			ret.createNewFile();
		}
		return ret;
	}
	public final File createFile4Session(long session) throws IOException{
		return this.createFile(session, (new Date()).getTime());
	}
	public final mediaFile getMediaFile(long session){
		final Pattern p = Pattern.compile(String.format("^%d_\\d+\\.mp4",session));
		File[] files = this.getVideoFolder().listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				Matcher m = p.matcher(name);
				return m.matches();
			}
		});
		if(null != files && files.length > 0 && null != files[0]){
			return new mediaFile(files[0]);
		}
		return null;
	}

}
