package com.ps.coordinator.api;

import java.util.List;

public interface RegistrationAndDiscoveryService {

    void register(Member member);

    void unregister(String name, String node);

    void unregister(String name);

    void setUnavailable(String name, String node);

    Group find(String name);

    List<Group> findAll(Type type);

    List<Group> findAll(Type type, String subtype);

}
