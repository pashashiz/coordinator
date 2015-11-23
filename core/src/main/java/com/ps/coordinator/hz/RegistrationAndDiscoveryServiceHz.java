package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.ps.coordinator.api.Group;
import com.ps.coordinator.api.Member;
import com.ps.coordinator.api.OperationStatus;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class RegistrationAndDiscoveryServiceHz implements RegistrationAndDiscoveryServiceInteractive {

    private final boolean isClientMode;
    private final IMap<String, Group> groupRegistry;

    public RegistrationAndDiscoveryServiceHz(HazelcastInstance hz, boolean isClient) {
        isClientMode = isClient;
        groupRegistry = hz.getMap("groups-registry");
    }

    public void listenEvents(EventListener listener) {}

    public OperationStatus register(Member member) {
        Group group = new Group(member.getName(), member.getType(), member.getEndpoint());
        group.getMembers().put(member.getNode(), member);
        groupRegistry.put(member.getName(), group);
        return OperationStatus.createSuccessful();
    }

    public OperationStatus unregister(String name) {
        return null;
    }

    public OperationStatus unregister(String name, String node) {
        return null;
    }

    public Group find(String name) {
        return groupRegistry.get(name);
    }

    public Member find(String name, String node) {
        return null;
    }
}
