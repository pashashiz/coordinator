package com.ps.coordinator.api.hz;

import com.ps.coordinator.ServiceRegistry;
import com.ps.coordinator.api.RegistrationAndDiscoveryService;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

class ServiceRegistryHz implements ServiceRegistry {

    private RegistrationAndDiscoveryServiceInteractive registrationAndDiscoveryService;

    public ServiceRegistryHz() {
        registrationAndDiscoveryService = new RegistrationAndDiscoveryServiceHz();
    }

    public RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService() {
        return registrationAndDiscoveryService;
    }

    public RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive() {
        return registrationAndDiscoveryService;
    }
}
