package com.ps.coordinator;

import com.ps.coordinator.api.ServiceRegistry;
import com.ps.coordinator.hz.ServiceRegistryHz;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistryFactory {

    private static volatile ServiceRegistry instance;
    private static final Lock mutex = new ReentrantLock();

    public ServiceRegistry create(boolean isClient) {
        return new ServiceRegistryHz(isClient);
    }

    public static void initForNonContainerEnvironment(boolean isClient) {
        mutex.lock();
        try {
            if (instance != null)
                throw new IllegalStateException("Cannot init Service Provider because the system already has one");
            instance = new ServiceRegistryFactory().create(isClient);
        } finally {
            mutex.unlock();
        }
    }

    public static ServiceRegistry getInitialized() {
        if (instance == null)
            throw new IllegalStateException(
                    "For non container environment Service Provider should be initialized manually on startup");
        return instance;
    }

}
