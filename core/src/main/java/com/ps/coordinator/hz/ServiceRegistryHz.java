package com.ps.coordinator.hz;

import com.ps.coordinator.api.ServiceRegistry;
import com.ps.coordinator.api.RegistrationAndDiscoveryService;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class ServiceRegistryHz implements ServiceRegistry {

    private RegistrationAndDiscoveryServiceInteractive registrationAndDiscoveryService;

    public ServiceRegistryHz(boolean isClient) {
        registrationAndDiscoveryService = new RegistrationAndDiscoveryServiceHz(isClient);
    }

    public RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService() {
        return registrationAndDiscoveryService;
    }

    public RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive() {
        return registrationAndDiscoveryService;
    }
}
