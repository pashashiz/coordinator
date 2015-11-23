package com.ps.coordinator.api;

import com.ps.coordinator.api.RegistrationAndDiscoveryService;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public interface Coordinator {

    RegistrationAndDiscoveryService lookupRegistrationAndDiscoveryService();

    RegistrationAndDiscoveryServiceInteractive lookupRegistrationAndDiscoveryServiceInteractive();

}
