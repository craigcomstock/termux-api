package com.termux.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.telephony.SmsManager;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.TermuxApiLogger;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

public class SmsSendAPI {

    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {
            @Override
            public void writeResult(PrintWriter out) {
                final SmsManager smsManager = SmsManager.getDefault();
                String[] recipients = intent.getStringArrayExtra("recipients");

                if (recipients == null) {
                    // Used by old versions of termux-send-sms.
                    String recipient = intent.getStringExtra("recipient");
                    if (recipient != null) recipients = new String[]{recipient};
                }

		// TODO rename this to SmsMmsSendAPI?
		// or separate MmsSendAPI and let command line decide which to use?
                if (recipients == null || recipients.length == 0) {
                    TermuxApiLogger.error("No recipient given");
                } else {
		    TermuxApiLogger.error("sending recipients="+recipients.toString()+", inputString="+inputString);
		    final ArrayList<String> messages = smsManager.divideMessage(inputString);
		    for (String recipient : recipients) {
			smsManager.sendMultipartTextMessage(recipient, null, messages, null, null);
		    }
                }
            }
        });
    }
}
