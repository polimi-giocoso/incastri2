package it.gbresciani.poligame.helper;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Extends Otto Bus to extend it with the abilty to post on the main thread
 */
public class BearBus extends Bus {

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public BearBus() {
        super();
    }

    public BearBus(String identifier) {
        super(identifier);
    }

    public BearBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public BearBus(ThreadEnforcer enforcer, String identifier) {
        super(enforcer, identifier);
    }

    @Override public void post(final Object event) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            super.post(event);
        }else{
            mainThread.post(new Runnable() {
                @Override public void run() {
                    post(event);
                }
            });
        }
    }
}
