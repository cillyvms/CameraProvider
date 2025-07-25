/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.estrogen.cameraprovider;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class PeriodicCleanupJobService extends JobService {
    private static final String TAG = "CleanupJob";
    private Thread currentJobThread = null;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (currentJobThread != null && currentJobThread.isAlive()) {
            Log.w(TAG, "onStartJob: a cleanup thread is already running?? ignoring start");
            return false;
        }
        currentJobThread = new Thread(() -> {
            File capturesDir = CameraPhotoProvider.getCapturesDir(this);
            File[] photos = capturesDir.listFiles();
            if (photos != null) {
                long curTime = System.currentTimeMillis();
                for (File photo : photos) {
                    if (Thread.currentThread().isInterrupted()) return;
                    long age = curTime - photo.lastModified();
                    if (age > 1000*60*60*24*7 /* 7 days */) {
                        Log.d(TAG, "delete " + photo + " age: " + age);
                        //noinspection ResultOfMethodCallIgnored
                        photo.delete();
                    }
                }
            }
            jobFinished(params, false);
        });
        currentJobThread.start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (currentJobThread != null) {
            currentJobThread.interrupt();
            currentJobThread = null;
        }
        return false;
    }
}
