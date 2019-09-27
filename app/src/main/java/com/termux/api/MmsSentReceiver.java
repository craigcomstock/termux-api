package com.termux.api;

import android.content.Context;
import android.content.Intent;

import com.termux.api.util.TermuxApiLogger;

public class MmsSentReceiver extends com.klinker.android.send_message.MmsSentReceiver {
    @Override
    public void onMessageStatusUpdated(Context context, Intent intent, int resultCode) {
	TermuxApiLogger.error("MmsSentReceiver.onMessageStatusUpdated() context="+context+", intent="+intent+", resultCode="+resultCode);
    }
}
