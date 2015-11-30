package com.ps.coordinator.hz;

import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.Coordinator;
import com.ps.coordinator.api.RegistrationAndDiscoveryService;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class CoordinatorHz implements Coordinator {

    private RegistrationAndDiscoveryServiceInteractive registrationAndDiscoveryService;
    private HazelcastInstance hz;

    public CoordinatorHz(HazelcastInstance hz, boolean isClient) {
        this.hz = hz;
        registrationAndDiscoveryService = new RegistrationAndDiscoveryServiceHz(hz, isClient);
    }

    public RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService() {
        return registrationAndDiscoveryService;
    }

    public RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive() {
        return registrationAndDiscoveryService;
    }

    @Override
    public void shutdown() {
        hz.shutdown();
    }
}
