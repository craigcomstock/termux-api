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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.termux.api.util.TermuxApiLogger;

/**
 * Needed to make default sms app for testing
 */
public class HeadlessSmsSendService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
	TermuxApiLogger.info("HeadlessSmsSendService.onBind() was called");
	// TODO maybe check for default sms app here? But it needs to be an "app" right?
	
        return null;
    }

}
