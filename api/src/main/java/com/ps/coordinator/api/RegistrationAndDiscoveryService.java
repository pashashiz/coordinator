package com.ps.coordinator.api;

public interface RegistrationAndDiscoveryService {

    OperationStatus register(Member member);

    OperationStatus unregister(String name, String node);

    OperationStatus unregisterAll(String name);

    OperationStatus setUnavailable(String name, String node);

    Member find(String name, String node);

    Group findAll(String name);

}
