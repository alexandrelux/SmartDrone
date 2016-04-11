package com.example.circles;

import java.util.EventListener;

public interface NewsUpdateListener extends EventListener {
    public void onComplete();
    public void Reset();
    public void onError(Throwable error);
}
