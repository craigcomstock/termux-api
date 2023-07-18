package com.termux.api.receivers;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");
        String body = "";
        String sender = "unknown";

        for (int i = 0; i < smsExtra.length; ++i) {
            // https://developer.android.com/reference/android/telephony/SmsMessage
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
            if (sms.getOriginatingAddress() != null) {
                sender = sms.getOriginatingAddress();
            }
            body += sms.getMessageBody();
            Log.w("SmsReceiver", "sender: "+sender+", body: "+sms.getMessageBody());
        }
        // TODO, keep the last sender? Or keep whichever one is not null?
        Log.w("SmsReceiver", "body: "+body);

        try {
            String fileName = Environment.getExternalStorageDirectory() + "/smsinbox.log";
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sender + " " + body);
            bw.newLine();
            bw.close();
        } catch( IOException ioe) {
            Log.w("SmsReceiver", "failed to write sms received: " + ioe);
        }
    }
}
