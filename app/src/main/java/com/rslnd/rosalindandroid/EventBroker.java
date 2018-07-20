package com.rslnd.rosalindandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventBroker {
    private static EventBroker instance;
    private static final String TAG = "EventBroker";
    private List<EventObserver> observers;

    private EventBroker() {
        this.observers = new ArrayList<>();
    }

    public static EventBroker getInstance() {
        if (instance == null) {
            instance = new EventBroker();
        }

        return instance;
    }

    public void emit(String event, Map payload) {
        Log.i(TAG, "Emit " + event + ": " + payload);

        for (EventObserver obj : observers) {
            obj.handleEvent(event, payload);
        }
    }

    public void register(EventObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }
}
