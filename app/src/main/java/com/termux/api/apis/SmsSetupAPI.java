package com.termux.api.apis;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;

import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;
import com.termux.shared.logger.Logger;

import java.io.PrintWriter;

//import com.klinker.android.logger.Log;

public class SmsSetupAPI {

    static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
	ResultReturner.returnData(apiReceiver, intent, out -> {
		Logger.logError("SmsSetupAPI.onReceive() called");
		new Thread(new Runnable() {
			public void run() {
			    Intent intent =
				new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
			    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
					    context.getPackageName());
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    Logger.logError("SmsSetupAPI.onReceive() calling context.startActivity()");
			    
			    context.startActivity(intent);
			    
			    Logger.logError("SmsSetupAPI.onReceive() has started the set default sms app activity");

			    //Log.setDebug(true); // this alone will get messages going to logcat. good.
			    //			    Log.setPath("TermuxApi/log.txt");
			}
		    }).start();
		Logger.logError("SmsSetupAPI.onReceive() started thread to launch change default app activity");
		
	    });
    }
}
