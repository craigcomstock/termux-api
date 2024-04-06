package com.termux.api.receivers;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver {
    // TODO share with MmsReceivedReceiverImpl.java the logging to spool and message formats

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");
        String body = "";
        String sender = "unknown";

        Log.w("SmsReceiver", "CRAIG: onReceive has " + smsExtra.length + " smsExtra items");
        for (int i = 0; i < smsExtra.length; ++i) {
            // https://developer.android.com/reference/android/telephony/SmsMessage
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            Log.w("SmsReceiver", "smsExtra["+i+"]=" + sms);
            if (sms.getOriginatingAddress() != null) {
                sender = sms.getOriginatingAddress();
            }
            body += sms.getMessageBody();
            Log.w("SmsReceiver", "sender: "+sender+", body: "+sms.getMessageBody());
        }
        // TODO, keep the last sender? Or keep whichever one is not null?
        Log.w("SmsReceiver", "body: "+body);

        try {
            String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String destDir = MessageFormat.format("{0}/sms", storagePath);
            new File(destDir).mkdirs();
            String destPath = destDir + "/spool";
            FileWriter fw = new FileWriter(destPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            TelephonyManager tmgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String to = tmgr.getLine1Number();
            bw.write(new Date().getTime()/1000 + " " + sender + " " + to + " " + body);
            bw.newLine();
            bw.close();
        } catch( IOException ioe) {
            Log.w("SmsReceiver", "failed to write sms received: " + ioe);
        }
    }
}
