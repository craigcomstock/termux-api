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

		    /*
		    try {
			TermuxApiLogger.error("trying MMS of same message");
			// for now, also send as MMS? :p
			Settings settings = new Settings();
			settings.setMmsc("http://mms.msg.eng.t-mobile.com/mms/wapenc");
			//settings.setMmsc(settings.getMmsc());
			//settings.setProxy("");
			//settings.setPort("");
			//settings.setUseSystemSending(true);
			settings.setGroup(true); // send group messages as MMS! :)
			TermuxApiLogger.error("settings="+settings.toString());

			com.klinker.android.logger.Log.setDebug(true); // this alone will get messages going to logcat. good.

			//Settings settings = new Settings();
			//settings.setUseSystemSending(true);
			Transaction transaction = new Transaction(context, settings);
			TermuxApiLogger.error("transaction="+transaction.toString());
			Message message = new Message(inputString, recipients[0]); // TODO support multi-recipient but is that a different type of MMS message? GROUP?
			TermuxApiLogger.error("message="+message.toString());
			message.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
			//message.setImage(mBitmap); // TODO support attaching images/generic files?
			//transaction.sendNewMessage(message, threadId)
			transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);

			TermuxApiLogger.error("after sendNewMessage(), transaction="+transaction.toString());
		    } catch (Exception e) {
			e.printStackTrace();
			TermuxApiLogger.error("sending caused error:"+e);
		    }
		    */
                }

            }
        });
    }
}
