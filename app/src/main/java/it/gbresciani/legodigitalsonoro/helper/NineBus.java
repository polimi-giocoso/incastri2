package it.gbresciani.legodigitalsonoro.helper;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Extends Otto Bus to extend it with the abilty to post on the main thread
 */
public class NineBus extends Bus {

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public NineBus() {
        super();
    }

    public NineBus(String identifier) {
        super(identifier);
    }

    public NineBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public NineBus(ThreadEnforcer enforcer, String identifier) {
        super(enforcer, identifier);
    }

    @Override public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mainThread.post(new Runnable() {
                @Override public void run() {
                    NineBus.super.post(event);
                }
            });
        }
    }
}
