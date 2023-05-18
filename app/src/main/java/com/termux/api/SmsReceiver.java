/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.termux.api;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import com.termux.api.util.TermuxApiLogger;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

/**
 * Needed to make default sms app for testing
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"); // TODO share with MMS printouts
    
    @Override
    public void onReceive(Context context, Intent intent) {
	TermuxApiLogger.info("Got SMS broadcast..." + intent.getAction());
	// Do I want to act on SMS_DELIVER or SMS_RECEIVED?
	// https://developer.android.com/reference/android/provider/Telephony.Sms.Intents#SMS_DELIVER_ACTION
	// SMS_DELIVER is sent only to main app that is expected to save the message
	// SMS_RECEIVED is ancillary for other apps
	// SMS_DELIVER has more information! :)
	if (intent.getAction() == "android.provider.Telephony.SMS_RECEIVED") {
	    // no-op
	    return;
	}

	TermuxApiLogger.info("SmsReceiver.onReceive() was called");
	
        Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");
        String body = "";
	String from = "";
	long timestamp = 0;

        for (int i = 0; i < smsExtra.length; ++i) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            body += sms.getMessageBody();
	    // TODO when do we get more than one PDU and how do we know which originating address and timestamp to use? The first one!? :p
	    TermuxApiLogger.info("pdu["+i+"], body="+sms.getMessageBody()+", from="+sms.getOriginatingAddress()+", timestamp="+sms.getTimestampMillis());
	    if (i == 0) {
		from = sms.getOriginatingAddress();
		timestamp = sms.getTimestampMillis();
	    }
        }

	if (smsExtra.length == 0) {
	    TermuxApiLogger.error("message has no PDUs so not sure how to get anything from it");
	    return;
	}

	String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	String destDir = MessageFormat.format("{0}/smsmms", storagePath);
	new File(destDir).mkdirs();
	String destPath = destDir + "/spool";
	TermuxApiLogger.error("writing SMS to filename="+destPath);
	// kinda bogus, but add a (self) as a to field to keep things inline with MMS group messages?
	String msg = DATE_FORMAT.format(timestamp) + " " + from + " => (self) " + body + "\n";
	try {
	    File file = new File(destPath);
	    FileWriter writer = new FileWriter(file, true);
	    writer.write(msg);
	    writer.close();
	} catch (IOException ioe) {
	    TermuxApiLogger.error("Failed to write msg: "+msg);
	    ioe.printStackTrace();
	}
	// TODO make notifications optional by configuration in ~/.termux/sms.config or something
	// for now, leave.

        Notification notification = new Notification.Builder(context)
                .setContentText(body)
                .setContentTitle("sms:" + from + ":")
                .setSmallIcon(R.drawable.ic_alert)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notification);

    }

}
