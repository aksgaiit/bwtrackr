package ac.aiit.bwcam.bwtracker;

import org.apache.http.HttpResponse;
import org.restlet.Response;

import ac.aiit.bwcam.bwtracker.share.RequestException;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest.IExceptionHandler;
import ac.aiit.bwcam.bwtracker.share.apache.IRequest.IResponseHandler;
import ac.aiit.bwcam.bwtracker.share.apache.impl.MomentRequest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class askCommentDialogFragment extends DialogFragment {
	private MomentRequest _request = null;
	private TextView _comment = null;
	public askCommentDialogFragment(){
		super();
	}
	public static askCommentDialogFragment getInstance(MomentRequest request){
		askCommentDialogFragment ret = new askCommentDialogFragment();
		ret._request = request;
		return ret;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.ask_comment_dialog, null);
		this._comment = (TextView)view.findViewById(R.id.comment_input);
		final Context ctx = getActivity().getApplicationContext();
		builder.setView(view)
		.setPositiveButton(R.string.label_done, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String comment = _comment.getText().toString();
				_request.addComment(comment);
				Response res = null;
				try {
					_request.submit(new IResponseHandler(){

						@Override
						public void onResponse(HttpResponse response) {
							Toast.makeText(ctx, "request successfully saved!"
									, Toast.LENGTH_LONG).show();
						}
						
					}, new IExceptionHandler(){

						@Override
						public void onException(Throwable e) {
							Toast.makeText(ctx, String.format("RequestExcetion %s", e.getMessage())
									, Toast.LENGTH_LONG).show();
						}
						
					}, null);
/*					if(!res.getStatus().isError()){
						Toast.makeText(getActivity(), "request successfully saved!"
								, Toast.LENGTH_LONG).show();
					}*/
				} catch (RequestException e) {
					Toast.makeText(getActivity(), String.format("RequestExcetion %s", e.getMessage())
							, Toast.LENGTH_LONG).show();
				}finally{
					if(null != res){
						res.release();
					}
				}
			}
		});
		return builder.create();
	}

}
