package com.termux.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.TermuxApiLogger;

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

		Message message = new Message(inputString, recipients);
		String imagePath = intent.getStringExtra("image");
		if (imagePath != null) {
		    message.setImage(BitmapFactory.decodeFile(imagePath));
		}

		String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String destDir = MessageFormat.format("{0}/smsmms", storagePath);
		new File(destDir).mkdirs();
		String destPath = destDir + "/spool";
		// TODO recipients is a String[] array, so might need to format in some commas?
		String to = "";
		if (recipients != null) {
		    for (int i = 0; i < recipients.length; i++) {
			to += "," + recipients[i];
		    }
		    if (',' == to.charAt(0)) {
			to = to.substring(1);
		    }
		}
		String images = "";
		String[] imageNames = message.getImageNames();
		if (imageNames != null) {
		    images += "images:";
		    for (int i = 0; i < imageNames.length; i++) {
			images += "," + imageNames[i];
		    }
		    if (',' == images.charAt(0)) {
			images = images.substring(1);
		    }
		}
		String msg = DATE_FORMAT.format(new Date()) + " (self) => " + to + " " + message.getText() + " " + images + "\n";
		try {
		    File file = new File(destPath);
		    FileWriter writer = new FileWriter(file, true);
		    writer.write(msg);
		    writer.close();
		} catch (IOException ioe) {
		    TermuxApiLogger.error("Failed to write msg: "+msg);
		    ioe.printStackTrace();
		}
		transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            }
        });
    }
}
