package ac.aiit.bwcam.bwtracker.neuro;

import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

public interface ITGDataListener {
	public void onSignalQuality(long epoc, int val);
	public void onRawData(long epoc, int val);
	
	
	
/*
 * 				case TGDevice.MSG_POOR_SIGNAL:
					// signal = msg.arg1;
					// this._parent.tv.append(String.format("%s - PoorSignal:%d\n",
					// getWhenString(msg), msg.arg1));
					this._parent._writeHelper.insertSigqualData(
							this._parent.getEpocTime(msg), msg.arg1);
					this._parent.updateStatus(msg.arg1);
					break;
				case TGDevice.MSG_RAW_DATA:
					// raw1 = msg.arg1;
					// tv.append(String.format("%s - Got raw:%d\n",
					// getWhenString(msg), msg.arg1));
					this._parent._writeHelper.insertRawwaveData(
							this._parent.getEpocTime(msg), msg.arg1);
					break;
				case TGDevice.MSG_HEART_RATE:
					this._parent.insertLog(String.format("%s - Heart rate:%d",
							this._parent.getWhenString(msg), msg.arg1));
					break;
				case TGDevice.MSG_ATTENTION:
					// att = msg.arg1;
					// this._parent.tv.append(String.format("%s - Attention:%d",
					// this._parent.getWhenString(msg), msg.arg1));
					// Log.v("HelloA", "Attention: " + att + "");
					this._parent._writeHelper.insertAttData(
							this._parent.getEpocTime(msg), msg.arg1);
					break;
				case TGDevice.MSG_MEDITATION:
					this._parent._writeHelper.insertMedData(
							this._parent.getEpocTime(msg), msg.arg1);
					break;
				case TGDevice.MSG_BLINK:
					// this._parent.tv.append(String.format("%s - Blink:%d",
					// this._parent.getWhenString(msg),
					// msg.arg1));
					this._parent._writeHelper.insertBlinkData(
							this._parent.getEpocTime(msg), msg.arg1);
					break;
				case TGDevice.MSG_RAW_COUNT:
					// this._parent.tv.append(String.format("%s - Raw Count:%d",
					// this._parent.getWhenString(msg),msg.arg1));
					break;
				case TGDevice.MSG_LOW_BATTERY:
					Toast.makeText(this._parent.getApplicationContext(),
							"Low battery!", Toast.LENGTH_SHORT).show();
					break;
				case TGDevice.MSG_RAW_MULTI:
					// TGRawMulti rawM = (TGRawMulti)msg.obj;
					// tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);

 */
}
