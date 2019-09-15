package com.termux.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.TermuxApiLogger;

import java.io.PrintWriter;
import java.util.ArrayList;


import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

public class MmsSendAPI {

    private static final String TAG = "MmsSendAPI";
    
    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {
            @Override
            public void writeResult(PrintWriter out) {
                String[] recipients = intent.getStringArrayExtra("recipients");
		if (recipients == null) {
		    String recipient = intent.getStringExtra("recipient");
		    if (recipient != null) recipients = new String[]{recipient};
		}
		if (recipients == null || recipients.length == 0) {
		    TermuxApiLogger.error("No recipients given");
		    return;
		}

		com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
		sendSettings.setMmsc("http://mms.msg.eng.t-mobile.com/mms/wapenc");
		sendSettings.setProxy(null);
		sendSettings.setPort(null);
		sendSettings.setUseSystemSending(true);

		Log.e( TAG, "context.getCacheDir()="+context.getCacheDir());
		
	        Transaction transaction = new Transaction(context, sendSettings);

		// TODO send raw MMS message composed by python right?
		// Or just keep it simple, text and to[] array so group messaging works :+1:
		Message message = new Message(inputString, recipients);
		String imagePath = intent.getStringExtra("image");
		if (imagePath != null) {
		    message.setImage(BitmapFactory.decodeFile(imagePath));
		}
		//                message.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.android));
		transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            }
        });
    }
}
