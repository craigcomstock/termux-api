package com.termux.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.TermuxApiLogger;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

public class MmsSendAPI {

    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {
            @Override
            public void writeResult(PrintWriter out) {
                final SmsManager smsManager = SmsManager.getDefault();
		String contentUriString = intent.getStringExtra("contentUri");
                TermuxApiLogger.error("MmsSendAPI: contentUriString="+contentUriString);
		Uri contentUri = Uri.parse(contentUriString);
                TermuxApiLogger.error("MmsSendAPI: contentUri="+contentUri);
		String locationUrl = null;
		Bundle configOverrides = new Bundle();
		/*
            val configOverrides = bundleOf(
                    Pair(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, true),
                    Pair(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE, MmsConfig.getMaxMessageSize()))

		            MmsConfig.getHttpParams()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { configOverrides.putString(SmsManager.MMS_CONFIG_HTTP_PARAMS, it) }
		*/

		// a little borrow from Transaction.kt in QKSMS app to setup sentIntent
		String MMS_SENT = "termux-api-mms-sent";
		String EXTRA_CONTENT_URI = "content_uri";
		String EXTRA_FILE_PATH = "file_path";
		//PendingIntent sentIntent = new Intent(MMS_SENT);
		//		BroadcastUtils.addClassName(context, sentIntent, MMS_SENT);
		intent.putExtra(EXTRA_CONTENT_URI, contentUriString);
	        intent.putExtra(EXTRA_FILE_PATH, contentUriString); // hmm, what is this supposed to be?
		intent.putExtra("api_method", MMS_SENT);
		intent.putExtra("content_uri", contentUriString);

		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
				
		TermuxApiLogger.error("MmsSendAPI, before calling sendMultimediaMessage()");
		smsManager.sendMultimediaMessage(context, contentUri, locationUrl, configOverrides, sentIntent);
		TermuxApiLogger.error("MmsSendAPI, after calling sendMultimediaMessage()");
            }
        });
    }
}
