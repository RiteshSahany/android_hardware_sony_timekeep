/* 
 * Copyright (C) 2015 Sony Mobile Communications Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names
 *    of its contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sony.timekeep;

import java.io.FileInputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sony.timekeep.TimeKeepProperties;

public class TimeKeep extends BroadcastReceiver {
    private static final String TAG = "TimeKeep-Receiver";
    private static final String RTC_SINCE_EPOCH = "/sys/class/rtc/rtc0/since_epoch";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got intent " + intent + ", storing time delta.");

        long seconds = System.currentTimeMillis()/1000;
        long epochSince = readEpoch();
        if (epochSince < 0) {
            Log.e(TAG, "Failed to read epoch from " + RTC_SINCE_EPOCH + ", skipping store");
            return;
        }
        seconds -= epochSince;

        Log.d(TAG, "Setting adjust property to " + seconds);
        try {
            TimeKeepProperties.timeadjust(seconds);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to store time adjustment", e);
        }
    }

    private long readEpoch() {
        byte[] buffer = new byte[32];
        int read = 0;
        try (FileInputStream fis = new FileInputStream(RTC_SINCE_EPOCH)) {
            read = fis.read(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read " + RTC_SINCE_EPOCH, e);
            return -1L;
        }

        if (read > 0) {
            try {
                return Long.parseLong(new String(buffer, 0, read).trim());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Unexpected value in " + RTC_SINCE_EPOCH, e);
            }
        }

        return -1L;
    }
}
