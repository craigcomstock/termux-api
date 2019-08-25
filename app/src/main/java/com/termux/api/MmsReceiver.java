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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.termux.api.util.TermuxApiLogger;

//import com.android.mms.transaction.PushReceiver;

/**
 * Needed to make default sms app for testing
 */
//public class MmsReceiver extends PushReceiver {

public class MmsReceiver extends BroadcastReceiver {
    private static final String TAG = MmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
	TermuxApiLogger.error("Got MMS broadcast..." + intent.getAction());

	if (intent.getAction() != "android.provider.Telephony.WAP_PUSH_DELIVER") {
	    TermuxApiLogger.error("Not WAP_PUSH_DELIVER so skip");
	}

	TermuxApiLogger.error("WAP_PUSH_DELIVER got extras="+intent.getExtras().toString());
	TermuxApiLogger.error("WAP_PUSH_DELIVER pdus="+intent.getExtras().get("pdus"));
    }
}
