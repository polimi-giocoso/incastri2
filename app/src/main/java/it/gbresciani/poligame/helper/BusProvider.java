package it.gbresciani.poligame.helper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class BusProvider {

    private static final Bus BUS = new BearBus(ThreadEnforcer.ANY);

    public BusProvider() {}

    public static Bus getInstance() {
        return BUS;
    }
}
