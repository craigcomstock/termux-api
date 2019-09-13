package com.termux.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.termux.api.util.TermuxApiLogger;

//public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {
public class MmsReceivedReceiver extends BroadcastReceiver {
    
    public final void onReceive(final Context context, final Intent intent) {
        TermuxApiLogger.error("MmsReceivedReceiver, context="+context+", intent="+intent);
	for (String key : intent.getExtras().keySet()) {
	    TermuxApiLogger.error("MmsReceivedReceiver, extra["+key+"]="+intent.getExtras().get(key));
	}
	// contentTypeParameters={Charset=4}
	byte[] data = intent.getByteArrayExtra("data");
	TermuxApiLogger.error("MmsReceivedReceiver, data="+data);
	TermuxApiLogger.error("MmsReceivedReceiver, new String(data)="+new String(data));
    }

    /*
    public void onMessageReceived(Context context, Uri messageUri) {
	TermuxApiLogger.error("MmsReceivedReceiver, context="+context+", messageUri="+messageUri);
    }
    
    public void onError(Context context, String error) {
	TermuxApiLogger.error("MmsReceivedReceiver, context="+context+", error="+error);
    }
    
    public MmscInformation getMmscInforForReceptionAck() {
	// TODO get from system and/or user specific APN settings
	// https://support.t-mobile.com/docs/DOC-2090
	String mmscUrl = "http://mms.msg.eng.t-mobile.com/mms/wapenc";
	String mmsProxy = "";
	int proxyPort = 0; // proxy port not set
	return new MmscInformation(mmscUrl, mmsProxy, proxyPort);
    }
    */
}
