package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.ps.coordinator.api.Group;
import com.ps.coordinator.api.Member;
import com.ps.coordinator.api.OperationStatus;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

public class RegistrationAndDiscoveryServiceHz implements RegistrationAndDiscoveryServiceInteractive {

    private final boolean isClientMode;
    private final IMap<String, Group> groups;

    public RegistrationAndDiscoveryServiceHz(HazelcastInstance hz, boolean isClient) {
        isClientMode = isClient;
        groups = hz.getMap("groups-registry");
    }

    public void listenEvents(EventListener listener) {
        // TODO
    }

    public OperationStatus register(Member member) {
        // Atomic operation to keep consistency
        groups.lock(member.getName());
        try {
            Group group = groups.get(member.getName());
            if (group == null) {
                group = new Group(member.getName(), member.getType(), member.getEndpoint());
            }
            // Validate when member (node) joins to the existing group (cluster)
            else {
                // Member type should be the same
                if (!group.getType().equals(member.getType()))
                    return OperationStatus.createError(2,
                            "Group member (node) type should be the same as a group (cluster) type");
                // If all group members are down - we can change endpoint to the new one
                if (!group.isAvailable())
                    group.setEndpoint(member.getEndpoint());
                // But if any of members is up then new member should have the same endpoint
                else if (!group.getEndpoint().equals(member.getEndpoint()))
                    return OperationStatus.createError(2,
                            "Group member (node) endpoint should be the same as a group (cluster) endpoint");
            }
            group.getMembers().put(member.getNode(), member);
            groups.put(group.getName(), group);
        } finally {
            groups.unlock(member.getName());
        }
        return OperationStatus.createSuccessful();
    }

    public OperationStatus unregister(String name) {
        groups.remove(name);
        return OperationStatus.createSuccessful();
    }

    public OperationStatus unregister(String name, String node) {
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                group.getMembers().remove(node);
                groups.put(name, group);
            }
        } finally {
            groups.unlock(name);
        }
        return OperationStatus.createSuccessful();
    }

    @Override
    public OperationStatus setUnavailable(String name, String node) {
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                Member member = group.getMembers().get(node);
                if (member != null) {
                    member.setAvailable(false);
                    groups.put(name, group);
                }
            }
        } finally {
            groups.unlock(name);
        }
        return OperationStatus.createSuccessful();
    }

    public Group find(String name) {
        return groups.get(name);
    }

    public Member find(String name, String node) {
        Group group = groups.get(name);
        return  (group != null) ? group.getMembers().get(node) : null;
    }
}
