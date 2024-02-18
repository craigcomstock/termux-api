package com.termux.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HeadlessSmsSendService extends Service {
    public IBinder onBind(Intent intent) {
        return null; // no op
    }
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // no op
        return super.onStartCommand(intent, flags, startId);
    }
}
