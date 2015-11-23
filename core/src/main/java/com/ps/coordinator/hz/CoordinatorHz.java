package com.ps.coordinator.hz;

import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.Coordinator;
import com.ps.coordinator.api.RegistrationAndDiscoveryService;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class CoordinatorHz implements Coordinator {

    private RegistrationAndDiscoveryServiceInteractive registrationAndDiscoveryService;

    public CoordinatorHz(HazelcastInstance hz, boolean isClient) {
        registrationAndDiscoveryService = new RegistrationAndDiscoveryServiceHz(hz, isClient);
    }

    public RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService() {
        return registrationAndDiscoveryService;
    }

    public RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive() {
        return registrationAndDiscoveryService;
    }
}
