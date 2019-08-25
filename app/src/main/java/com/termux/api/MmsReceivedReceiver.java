package com.termux.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.termux.api.util.TermuxApiLogger;

public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {

    public void onMessageReceived(Context context, Uri messageUri) {
	TermuxApiLogger.error("MmsReceivedReceiver, messageUri="+messageUri.toString());
    }
    
    public void onError(Context context, String error) {
	TermuxApiLogger.error("MmsReceivedReceiver, error="+error);
    }
    
    public MmscInformation getMmscInforForReceptionAck() {
	// TODO get from system and/or user specific APN settings
	// https://support.t-mobile.com/docs/DOC-2090
	String mmscUrl = "http://mms.msg.eng.t-mobile.com/mms/wapenc";
	String mmsProxy = "";
	int proxyPort = 0; // proxy port not set
	return new MmscInformation(mmscUrl, mmsProxy, proxyPort);
    }
}
