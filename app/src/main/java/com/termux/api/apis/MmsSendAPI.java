package com.termux.api.apis;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;
import com.termux.shared.logger.Logger;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

public class MmsSendAPI {

    private static final String TAG = "MmsSendAPI";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"); // TODO share with MMS printouts

    public static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {
            @RequiresPermission(allOf = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS })
            @Override
            public void writeResult(PrintWriter out) {
                String[] recipients = intent.getStringArrayExtra("recipients");
                if (recipients == null) {
                    String recipient = intent.getStringExtra("recipient");
                    if (recipient != null) recipients = new String[]{recipient};
                }
                if (recipients == null || recipients.length == 0) {
                    Logger.logError("No recipients given");
                    return;
                }

                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc("http://mms.msg.eng.t-mobile.com/mms/wapenc");
                sendSettings.setProxy(null);
                sendSettings.setPort(null);
                sendSettings.setUseSystemSending(true);

                Log.e( TAG, "context.getCacheDir()="+context.getCacheDir());

                Transaction transaction = new Transaction(context, sendSettings);

                Message message = new Message(inputString, recipients);
// TODO other types of extras?
// send two extras: 1) mime type 2) file url
                String imagePath = intent.getStringExtra("image");
                if (imagePath != null) {
                    message.setImage(BitmapFactory.decodeFile(imagePath));
                }

                try {
                    transaction.sendNewMessage(message);
                } catch(Exception e) {
                    e.printStackTrace();
                    Logger.logError("Exception sending messages: "+message);
                }
// TODO how to track success or not? retries?
            }
        });
    }
}
