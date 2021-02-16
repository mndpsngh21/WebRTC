package com.oney.WebRTCModule;


import java.io.IOException;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.webrtc.MediaStream;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoTrack;


public class ScreenRecorderService extends Service {
    private static final boolean DEBUG = false;
    private static final String TAG = "ScreenRecorderService";
    private static final String APP_DIR_NAME = "ScreenRecorder";
    private static final String BASE = "com.serenegiant.service.ScreenRecorderService.";
    public static final String ACTION_START = BASE + "ACTION_START";
    public static final String ACTION_STOP = BASE + "ACTION_STOP";
    public static final String ACTION_PAUSE = BASE + "ACTION_PAUSE";
    public static final String ACTION_RESUME = BASE + "ACTION_RESUME";
    public static final String ACTION_QUERY_STATUS = BASE + "ACTION_QUERY_STATUS";
    public static final String ACTION_QUERY_STATUS_RESULT = BASE + "ACTION_QUERY_STATUS_RESULT";
    public static final String EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE";
    public static final String EXTRA_QUERY_RESULT_RECORDING = BASE + "EXTRA_QUERY_RESULT_RECORDING";
    public static final String EXTRA_QUERY_RESULT_PAUSING = BASE + "EXTRA_QUERY_RESULT_PAUSING";
    private static final int NOTIFICATION = R.string.alert_description;

    private static final Object sSync = new Object();

    private MediaProjectionManager mMediaProjectionManager;
    private NotificationManager mNotificationManager;

    public ScreenRecorderService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.v(TAG, "onCreate:");
        mNotificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification(TAG);

    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (DEBUG) Log.v(TAG, "onStartCommand:intent=" + intent);

        int result = START_STICKY;
        final String action = intent != null ? intent.getAction() : null;
        if (ACTION_START.equals(action)) {
            startScreenRecord(intent);
            updateStatus();
        } else if (ACTION_STOP.equals(action) || TextUtils.isEmpty(action)) {
            stopScreenRecord();
            updateStatus();
            result = START_NOT_STICKY;
        } else if (ACTION_QUERY_STATUS.equals(action)) {
            if (!updateStatus()) {
                stopSelf();
                result = START_NOT_STICKY;
            }
        } else if (ACTION_PAUSE.equals(action)) {
            pauseScreenRecord();
        } else if (ACTION_RESUME.equals(action)) {
            resumeScreenRecord();
        }
        return result;
    }

    private boolean updateStatus() {
        final boolean isRecording = false, isPausing;
        synchronized (sSync) {

        }
        final Intent result = new Intent();
        result.setAction(ACTION_QUERY_STATUS_RESULT);
      //  result.putExtra(EXTRA_QUERY_RESULT_RECORDING, isRecording);
    //    result.putExtra(EXTRA_QUERY_RESULT_PAUSING, isPausing);
        if (DEBUG) Log.v(TAG, "sendBroadcast:isRecording=" + isRecording + ",isPausing=" + isPausing);
        sendBroadcast(result);
        return isRecording;
    }

    /**
     * start screen recording as .mp4 file
     * @param intent
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenRecord(final Intent intent) {
        synchronized (sSync) {
            ThreadUtils.runOnExecutor(() -> {
                Log.d("ScreenCapture","CallbackReceived");

                ReadableMap constraints= ServiceBridge.getServiceBridge().getConstraints();
                Callback errorCallback= ServiceBridge.getServiceBridge().getErrorCallback();
                Callback successCallback= ServiceBridge.getServiceBridge().getSuccessCallback();
                Intent intenVal= ServiceBridge.getServiceBridge().getIntent();
                WebRTCModule webRTCModule= ServiceBridge.getServiceBridge().getWebRTCModule();
                VideoTrack track = null;
                if (constraints.hasKey("video")) {
                    track = webRTCModule.createVideoTrack(constraints, new ScreenCapturerAndroid(intenVal, new MediaProjection.Callback() {}));
                }
                if (track == null) {
                    errorCallback.invoke("DOMException", "AbortError");
                    return;
                }
                String streamId = UUID.randomUUID().toString();
                MediaStream mediaStream
                    = ServiceBridge.getServiceBridge().getmFactory().createLocalMediaStream(streamId);
                WritableArray tracks = Arguments.createArray();

                mediaStream.addTrack(track);
                WritableMap track_ = Arguments.createMap();
                String trackId = track.id();
                track_.putBoolean("enabled", track.enabled());
                track_.putString("id", trackId);
                track_.putString("kind", track.kind());
                track_.putString("label", trackId);
                track_.putString("readyState", track.state().toString());
                track_.putBoolean("remote", false);
                tracks.pushMap(track_);

                Log.d(TAG, "MediaStream id: " + streamId);
                ServiceBridge.getServiceBridge().getLocalStreams().put(streamId, mediaStream);
                successCallback.invoke(streamId, tracks);
            });

        }
    }

    /**
     * stop screen recording
     */
    private void stopScreenRecord() {
        synchronized (sSync) {

        }
        stopForeground(true/*removeNotification*/);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION);
            mNotificationManager = null;
        }
        stopSelf();
    }

    private void pauseScreenRecord() {
        synchronized (sSync) {

        }
    }

    private void resumeScreenRecord() {
        synchronized (sSync) {

        }
    }


//================================================================================
    /**
     * helper method to show/change message on notification area
     * and set this service as foreground service to keep alive as possible as this can.
     * @param text
     */
    private void showNotification(final CharSequence text) {
        if (DEBUG) Log.v(TAG, "showNotification:" + text);
        NotificationChannel notificationChannel=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel= new NotificationChannel("ScreenCapture","ScreenCapture",NotificationManager.IMPORTANCE_NONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        // Set the info for the views that show in the notification panel.
         Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,"ScreenCapture")
                .setSmallIcon(R.drawable.redbox_top_border_background)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .build();
        }

        startForeground(NOTIFICATION, notification);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }


}