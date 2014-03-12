package ac.aiit.bwcam.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ac.aiit.bwcam.bwtracker.ManagerActivity;
import android.os.Environment;

public class fileServerResource extends ServerResource {

	private static class fileManager{
		public static class tmpFile{
			public long size;
			public File file;
			public tmpFile(File f, int len){
				super();
				this.file = f;
				this.size = len;
			}
		}
		private ConcurrentMap<String, tmpFile> _files = new ConcurrentHashMap<String, tmpFile>();
		private fileManager(){
			super();
		}
		private static fileManager _instance = new fileManager();
		public static final fileManager getInstance(){
			return _instance;
		}
		public void removeAll() throws IOException{
			for(String name: this._files.keySet()){
				this._getremove(name, true);
			}
		}
		public tmpFile get(String name)throws IOException{
			return this._getremove(name, false);
		}
		public void remove(String name) throws IOException{
			this._getremove(name, true);
		}
		private synchronized tmpFile _getremove(String name, boolean remove) throws IOException{
			tmpFile tf = this._files.get(name);
			if(!remove && null == tf){
				File fl = File.createTempFile(ManagerActivity.class.getName(), "", new File(Environment.getExternalStorageDirectory().getPath() + "/ac.aiit.bwcam.bwtracker"));
				InputStream fis = null;
				FileOutputStream fos = null;
				try{
					fis = fileServerResource.class.getResourceAsStream(name);
					fos = new FileOutputStream(fl);
					int len = 0, total = 0;;
					byte[] buf = new byte[4096];
					while (-1 != (len = fis.read(buf))) {
						total += len;
						fos.write(buf, 0, len);
					}
					fos.flush();
					this._files.put(name, (tf = new tmpFile(fl, total)));
				}finally{
					if(null != fis){
						try {
							fis.close();
						} catch (IOException e) {
						}
					}
					if(null != fos){
						try {
							fos.close();
						} catch (IOException e) {
						}
					}
				}
			}
			if(remove && null != tf){
				tf.file.delete();
			}
			return tf;
		}
	}
	private static class fileOutputRepresentation extends OutputRepresentation{
		private String _name = null;
		public fileOutputRepresentation(String name, MediaType mediaType) {
			super(mediaType);
			this._name = name;
		}

		@Override
		public void write(OutputStream os) throws IOException {
			BufferedInputStream fis = null;
			try{
				fis = new BufferedInputStream(fileServerResource.class.getResourceAsStream(this._name));
				byte[] buf = new byte[8192];
				int len = 0, total = 0;
				while (-1 != (len = fis.read(buf))) {
					total += len;
					os.write(buf, 0, len);
				}
				this.setSize(total);
//				os.flush();
//				os.close();
			}finally{
				if(null != fis){
					try{
						fis.close();
					}catch(IOException e){
					}
				}
			}
		}
	}

	@Override
	protected Representation get() throws ResourceException {
		Form query = this.getQuery();
		String name = query.getFirstValue("name");
		if(null != name){
			MediaType mtype = MediaType.TEXT_HTML;
			if(name.endsWith(".js")){
				mtype = MediaType.APPLICATION_JAVASCRIPT;
			}else if(name.endsWith("json")){
				mtype = MediaType.APPLICATION_JSON;
			}
//			return new EncodeRepresentation(Encoding.GZIP, new FileRepresentation(fileServerResource.class.getResource(name).toString(), mtype, 0));
/*			try {
				ac.aiit.bwcam.file.fileServerResource.fileManager.tmpFile tf = fileManager.getInstance().get(name);
				FileRepresentation ret = new FileRepresentation(tf.file, mtype, 0);
				ret.setSize(tf.size);
				return new EncodeRepresentation(Encoding.GZIP, ret);
			} catch (IOException e) {
				throw new ResourceException(e);
			}*/
			
			fileOutputRepresentation ret =  new fileOutputRepresentation(name, mtype);
			ret.setCharacterSet(CharacterSet.UTF_8);
			return new EncodeRepresentation(Encoding.GZIP,ret);
		}else{
			this.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new EmptyRepresentation();
		}
	}
}
