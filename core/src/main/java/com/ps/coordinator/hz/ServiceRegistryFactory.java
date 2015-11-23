package com.ps.coordinator.hz;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.ServiceRegistry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistryFactory {

    private static volatile ServiceRegistry instance;
    private static final Lock mutex = new ReentrantLock();

    public ServiceRegistry create(Config config, boolean isClient) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(null);
        return new ServiceRegistryHz(hz, isClient);
    }

    public static void initForNonContainerEnvironment(Config config, boolean isClient) {
        mutex.lock();
        try {
            if (instance != null)
                throw new IllegalStateException("Cannot init Service Provider because the system already has one");
            instance = new ServiceRegistryFactory().create(config, isClient);
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
