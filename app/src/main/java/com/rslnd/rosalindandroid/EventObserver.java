package com.rslnd.rosalindandroid;

import java.util.Map;

public interface EventObserver {
    void handleEvent(String event, Map payload);
}
