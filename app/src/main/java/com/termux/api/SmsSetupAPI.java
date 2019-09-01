package com.termux.api;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;

import com.termux.api.util.ResultReturner;
import com.termux.api.util.TermuxApiLogger;

import java.io.PrintWriter;

import com.klinker.android.logger.Log;

public class SmsSetupAPI {

    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
	ResultReturner.returnData(apiReceiver, intent, out -> {
		TermuxApiLogger.error("SmsSetupAPI.onReceive() called");
		new Thread(new Runnable() {
			public void run() {
			    Intent intent =
				new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
			    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
					    context.getPackageName());
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    TermuxApiLogger.error("SmsSetupAPI.onReceive() calling context.startActivity()");
			    
			    context.startActivity(intent);
			    
			    TermuxApiLogger.error("SmsSetupAPI.onReceive() has started the set default sms app activity");

			    Log.setDebug(true); // this alone will get messages going to logcat. good.
			    //			    Log.setPath("TermuxApi/log.txt");
			}
		    }).start();
		TermuxApiLogger.error("SmsSetupAPI.onReceive() started thread to launch change default app activity");
		
	    });
    }
}
