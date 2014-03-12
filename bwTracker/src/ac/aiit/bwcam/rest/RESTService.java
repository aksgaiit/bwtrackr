package ac.aiit.bwcam.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;

import ac.aiit.bwcam.file.fileServerResource;


public class RESTService {
	private RESTService(){
		super();
	}
	private Component _c = null;
	public static final int _port = 58121;
	
	private static RESTService _instance = null;
	
	public static synchronized final RESTService getInstance(){
		if(null == _instance){
			_instance = new RESTService();
		}
		return _instance;
	}
	private void _start(){
		if(null == this._c){
			this._c = new Component();
			this._c.getServers().add(Protocol.HTTP, _port);
			Router rt = new Router(this._c.getContext().createChildContext());
			rt.attach("/session", sessionServerResource.class);
			rt.attach("/raw", rawdataServerResource.class);
			rt.attach("/wform", waveformServerResource.class);
			rt.attach("/simage", thumbnailServerResource.class);
			rt.attach("/mental", mentalstatsServerResource.class);
			rt.attach("/file", fileServerResource.class);
			VirtualHost host = this._c.getDefaultHost();
			host.attach(rt);
		}
		if(!this._c.isStarted()){
			try {
				this._c.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void _stop(){
		if(null != this._c){
			try {
				this._c.stop();
			} catch (Exception e) {
			}
			this._c = null;
		}
	}
	protected synchronized void startstop(boolean stop){
		if(stop){
			this._stop();
		}else{
			this._start();
		}
	}
	public void start(){
		this.startstop(false);
	}
	public void stop(){
		this.startstop(true);
	}
	public boolean isStarted(){
		return (null != this._c && this._c.isStarted());
	}
	
	
	public static final String getExternalIPAddr() throws SocketException{
		for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
			NetworkInterface ni = en.nextElement();
			for(Enumeration<InetAddress> eia = ni.getInetAddresses(); eia.hasMoreElements();){
				InetAddress ina = eia.nextElement();
				if(!ina.isLoopbackAddress() && !ina.isLinkLocalAddress()){
					return ina.getHostAddress().toString();
				}
			}
		}
		return null;
	}

}
