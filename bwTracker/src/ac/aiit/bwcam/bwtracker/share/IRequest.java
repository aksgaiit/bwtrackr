package ac.aiit.bwcam.bwtracker.share;

import org.restlet.Response;

public interface IRequest {
	public Response submit() throws RequestException;
}
