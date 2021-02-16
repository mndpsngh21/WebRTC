package com.oney.WebRTCModule;

import android.content.Intent;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;

import java.util.Map;

public class ServiceBridge {

    private static ServiceBridge  serviceBridge;
    private  Callback successCallback;
    private  Callback errorCallback;
    private ReadableMap constraints;
    private PeerConnectionFactory mFactory;
    private Intent intent;
    private WebRTCModule webRTCModule;
    private Map<String, MediaStream> localStreams;


    private ServiceBridge()
    {

    }

    public static ServiceBridge getServiceBridge() {
        if(serviceBridge==null)
        {
            serviceBridge = new ServiceBridge();
        }
        return serviceBridge;
    }

    public Callback getSuccessCallback() {
        return successCallback;
    }

    public void setSuccessCallback(Callback successCallback) {
        this.successCallback = successCallback;
    }

    public Callback getErrorCallback() {
        return errorCallback;
    }

    public void setErrorCallback(Callback errorCallback) {
        this.errorCallback = errorCallback;
    }

    public ReadableMap getConstraints() {
        return constraints;
    }

    public void setConstraints(ReadableMap constraints) {
        this.constraints = constraints;
    }

    public PeerConnectionFactory getmFactory() {
        return mFactory;
    }

    public void setmFactory(PeerConnectionFactory mFactory) {
        this.mFactory = mFactory;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public WebRTCModule getWebRTCModule() {
        return webRTCModule;
    }

    public void setWebRTCModule(WebRTCModule webRTCModule) {
        this.webRTCModule = webRTCModule;
    }

    public Map<String, MediaStream> getLocalStreams() {
        return localStreams;
    }

    public void setLocalStreams(Map<String, MediaStream> localStreams) {
        this.localStreams = localStreams;
    }
}
