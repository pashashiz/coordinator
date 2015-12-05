package com.ps.coordinator.hz;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.Coordinator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoordinatorServerFactory {

    private static volatile Coordinator instance;
    private static final Lock mutex = new ReentrantLock();

    public Coordinator create(Config config) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        return new CoordinatorHz(hz, false);
    }

    public static void initForNonContainerEnvironment(Config config) {
        mutex.lock();
        try {
            if (instance != null)
                throw new IllegalStateException("Cannot init Service Provider because the system already has one");
            instance = new CoordinatorServerFactory().create(config);
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
