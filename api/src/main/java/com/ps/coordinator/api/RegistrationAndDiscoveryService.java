package com.ps.coordinator.api;

public interface RegistrationAndDiscoveryService {

    OperationStatus register(Member member);

    OperationStatus unregister(String name);

    OperationStatus unregister(String name, String node);

    OperationStatus setUnavailable(String name, String node);

    Group find(String name);

    Member find(String name, String node);

}
