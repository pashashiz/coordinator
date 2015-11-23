package com.ps.coordinator.hz;

import com.ps.coordinator.api.Group;
import com.ps.coordinator.api.Member;
import com.ps.coordinator.api.OperationStatus;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class RegistrationAndDiscoveryServiceHz implements RegistrationAndDiscoveryServiceInteractive {

    private boolean isClientMode;

    public RegistrationAndDiscoveryServiceHz(boolean isClient) {
        this.isClientMode = isClient;
    }

    public void listenEvents(EventListener listener) {}

    public OperationStatus register(Member member) {
        return null;
    }

    public OperationStatus unregister(String name) {
        return null;
    }

    public OperationStatus unregister(String name, String node) {
        return null;
    }

    public Group find(String name) {
        return null;
    }

    public Member find(String name, String node) {
        return null;
    }
}
