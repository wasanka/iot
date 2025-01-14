package com.mufg.pex.messaging.processes;

import com.mufg.pex.messaging.util.Cache;

import java.util.TimerTask;

public class ClientExpiryThread extends TimerTask {

    @Override
    public void run() {

        Cache.getInstance().clearExpiredClients();
    }
}
