package com.ps.coordinator.hz;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.Coordinator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoordinatorFactory {

    private static volatile Coordinator instance;
    private static final Lock mutex = new ReentrantLock();

    public Coordinator create(Config config, boolean isClient) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(null);
        return new CoordinatorHz(hz, isClient);
    }

    public static void initForNonContainerEnvironment(Config config, boolean isClient) {
        mutex.lock();
        try {
            if (instance != null)
                throw new IllegalStateException("Cannot init Service Provider because the system already has one");
            instance = new CoordinatorFactory().create(config, isClient);
        } finally {
            mutex.unlock();
        }
    }

    public static Coordinator getInitialized() {
        if (instance == null)
            throw new IllegalStateException(
                    "For non container environment Service Provider should be initialized manually on startup");
        return instance;
    }

}
