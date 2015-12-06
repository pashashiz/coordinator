package com.ps.coordinator.api;

public interface Coordinator {

    RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService();

    RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive();

    void shutdown();

}
