package com.termux.api.apis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;

import androidx.annotation.RequiresPermission;

import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;
import com.termux.shared.logger.Logger;

import java.io.PrintWriter;

//import com.klinker.android.logger.Log;

public class MmsSetupAPI {

	private static final String LOG_TAG = "MmsSetupAPI";

	public static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
				Logger.logDebug(LOG_TAG, "onReceive");

		ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultWriter() {
			@RequiresPermission(allOf = {Manifest.permission.RECEIVE_MMS, Manifest.permission.RECEIVE_WAP_PUSH, android.Manifest.permission.MANAGE_SUBSCRIPTION_USER_ASSOCATION})
			@Override
			public void writeResult(PrintWriter out) {
				Logger.logError("MmsSetupAPI.onReceive() called");
				new Thread(new Runnable() {
					public void run() {
						Intent intent =
								new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
						intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
								context.getPackageName());
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Logger.logError("MmsSetupAPI.onReceive() calling context.startActivity()");

						context.startActivity(intent);

						Logger.logError("MmsSetupAPI.onReceive() has started the set default sms app activity");

						//Log.setDebug(true); // this alone will get messages going to logcat. good.
						//			    Log.setPath("TermuxApi/log.txt");
					}
				}).start();
				Logger.logError("MmsSetupAPI.onReceive() started thread to launch change default app activity");

			}
		});
    }
}
