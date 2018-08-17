package com.rslnd.rosalindandroid;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rslnd.rosalindandroid.BuildConfig;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NativeBridge implements EventObserver {
    private Context context;
    private WebView webView;
    private boolean isLoaded;
    private ArrayList<String> delayedEvents;

    static final String TAG = "NativeBridge";

    NativeBridge(Context context) {
        this.context = context;
        this.delayedEvents = new ArrayList<>();
    }

    private void unsafeEval(final String js) {
        Log.i(TAG, "Evaluating: `" + js + "`");

        new Handler(Looper.getMainLooper()).post(new Runnable () {
            @Override
            public void run () {
                webView.evaluateJavascript(js, null);
            }
        });
    }

    private String safeName (String name) {
        boolean ok = Pattern.matches("[A-Za-z/]*", name);
        if (!ok) {
            throw new IllegalArgumentException("Not evaluating unsafe name: `" + name + "`");
        } else {
            return name;
        }
    }

    private String safeValue (Object value) {
        Gson gson = new Gson();
        return gson.toJson(value);
    }

    private void setProperty(final String key, final Object value) {
        unsafeEval("window.native." + safeName(key) + " = " + safeValue(value));
    }

    private HashMap systemInfo() {
        HashMap<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("versionCode", BuildConfig.VERSION_CODE);
        systemInfo.put("versionName", BuildConfig.VERSION_NAME);
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("device", android.os.Build.DEVICE);
        systemInfo.put("model", android.os.Build.MODEL);
        systemInfo.put("product", android.os.Build.PRODUCT);
        return systemInfo;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @Override
    public void handleEvent(String event, Map payload) {
        String script = "window.native.events.emit(\"" + safeName(event) + "\", " + safeValue(payload) + ")";
        if (this.isLoaded) {
            unsafeEval(script);
        } else {
            delayedEvents.add(script);
        }
    }

    @JavascriptInterface
    public String android() {
        return Build.DEVICE;
    }

    @JavascriptInterface
    public void load() {
        /*!
         * EventEmitter v5.2.5 - git.io/ee
         * Unlicense - http://unlicense.org/
         * Oliver Caldwell - http://oli.me.uk/
         */
        StringBuilder eventEmitterJS = new StringBuilder();
        eventEmitterJS.append("!function(e){\"use strict\";function t(){}function n(e,t){for(var n=e.length;n--;)if(e[n].listener===t)return n;return-1}function r(e){return function(){return this[e].apply(this,arguments)}}function i(e){return\"function\"==typeof e||e instanceof RegExp||!(!e||\"object\"!=typeof e)&&i(e.listener)}var s=t.prototype,o=e.EventEmitter;s.getListeners=function(e){var t,n,r=this._getEvents();if(e instanceof RegExp){t={};for(n in r)r.hasOwnProperty(n)&&e.test(n)&&(t[n]=r[n])}else t=r[e]||(r[e]=[]);return t},s.flattenListeners=function(e){var t,n=[];for(t=0;t<e.length;t+=1)n.push(e[t].listener);return n},s.getListenersAsObject=function(e){var t,n=this.getListeners(e);return n instanceof Array&&(t={},t[e]=n),t||n},s.addListener=function(e,t){if(!i(t))throw new TypeError(\"listener must be a function\");var r,s=this.getListenersAsObject(e),o=\"object\"==typeof t;for(r in s)s.hasOwnProperty(r)&&-1===n(s[r],t)&&s[r].push(o?t:{listener:t,once:!1});return this},s.on=r(\"addListener\"),s.addOnceListener=function(e,t){return this.addListener(e,{listener:t,once:!0})},s.once=r(\"addOnceListener\"),s.defineEvent=function(e){return this.getListeners(e),this},s.defineEvents=function(e){for(var t=0;t<e.length;t+=1)this.defineEvent(e[t]);return this},s.removeListener=function(e,t){var r,i,s=this.getListenersAsObject(e);for(i in s)s.hasOwnProperty(i)&&-1!==(r=n(s[i],t))&&s[i].splice(r,1);return this},s.off=r(\"removeListener\"),s.addListeners=function(e,t){return this.manipulateListeners(!1,e,t)},s.removeListeners=function(e,t){return this.manipulateListeners(!0,e,t)},s.manipulateListeners=function(e,t,n){var r,i,s=e?this.removeListener:this.addListener,o=e?this.removeListeners:this.addListeners;if(\"object\"!=typeof t||t instanceof RegExp)for(r=n.length;r--;)s.call(this,t,n[r]);else for(r in t)t.hasOwnProperty(r)&&(i=t[r])&&(\"function\"==typeof i?s.call(this,r,i):o.call(this,r,i));return this},s.removeEvent=function(e){var t,n=typeof e,r=this._getEvents();if(\"string\"===n)delete r[e];else if(e instanceof RegExp)for(t in r)r.hasOwnProperty(t)&&e.test(t)&&delete r[t];else delete this._events;return this},s.removeAllListeners=r(\"removeEvent\"),s.emitEvent=function(e,t){var n,r,i,s,o=this.getListenersAsObject(e);for(s in o)if(o.hasOwnProperty(s))for(n=o[s].slice(0),i=0;i<n.length;i++)r=n[i],!0===r.once&&this.removeListener(e,r.listener),r.listener.apply(this,t||[])===this._getOnceReturnValue()&&this.removeListener(e,r.listener);return this},s.trigger=r(\"emitEvent\"),s.emit=function(e){var t=Array.prototype.slice.call(arguments,1);return this.emitEvent(e,t)},s.setOnceReturnValue=function(e){return this._onceReturnValue=e,this},s._getOnceReturnValue=function(){return!this.hasOwnProperty(\"_onceReturnValue\")||this._onceReturnValue},s._getEvents=function(){return this._events||(this._events={})},t.noConflict=function(){return e.EventEmitter=o,t},\"function\"==typeof define&&define.amd?define(function(){return t}):\"object\"==typeof module&&module.exports?module.exports=t:e.EventEmitter=t}(\"undefined\"!=typeof window?window:this||{});");
        eventEmitterJS.append("if (!window.native.events) { window.native.events = new EventEmitter() } else { console.warn('[NativeBridge] Already registered global EventEmitter') }");
        unsafeEval(eventEmitterJS.toString());

        EventBroker.getInstance().register(this);

        Log.i(TAG, "Registered event emitter inside webview");

        String clientKey = new ClientKey(context).getKey();

        setProperty("version", BuildConfig.VERSION_NAME);
        setProperty("systemInfo", systemInfo());
        setProperty("clientKey", clientKey);

        HashMap<String, String> clientKeyPayload = new HashMap<>();
        clientKeyPayload.put("clientKey", clientKey);
        handleEvent("clientKey", clientKeyPayload);

        for (String event : delayedEvents) {
            unsafeEval(event);
        }

        this.delayedEvents = new ArrayList<>();
        this.isLoaded = true;
    }
}
