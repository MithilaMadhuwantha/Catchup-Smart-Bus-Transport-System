package com.MMW.Catchup;

import com.squareup.otto.Bus;


public final class BusProvider {
    private static final Bus busInstance = new Bus();

    public static Bus getInstance() {
        return busInstance;
    }
}