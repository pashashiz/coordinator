package com.ps.coordinator.api;

import java.util.Set;

public interface RegistrationAndDiscoveryService {

    void register(Member member);

    void unregister(String name, String node);

    void unregister(String name);

    void setUnavailable(String name, String node);

    Group find(String name);

    Set<Group> findAll(Type type);

    Set<Group> findAll(Type type, String subtype);

}
