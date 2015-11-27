package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.ps.coordinator.api.*;
import com.ps.coordinator.api.Member;

import static com.ps.coordinator.api.OperationStatus.*;

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
        if (!member.isAvailable())
            throw new IllegalStateException("Cannot register unavailable member");
        if (member.getType() == Type.SERVICE) {
            if (member.subtype() == null || member.subtype().isEmpty())
                return createError(2, "Member's subtype cannot be null or empty");
            if (member.getNode() == null || member.getNode().isEmpty())
                return createError(2, "Member's node name cannot be null or empty");
            if (member.getEndpoint() == null || member.getEndpoint().isEmpty())
                return createError(2, "Member's endpoint cannot be null or empty");
        }
        // Atomic operation to keep consistency
        groups.lock(member.getName());
        try {
            Group group = groups.get(member.getName());
            if (group == null) {
                group = Group.createBy(member);
            }
            // Validate when member (node) joins to the existing group (cluster)
            else {
                // Member type and subtype should be the same
                if (group.getType() == member.getType() && !group.getSubtype().equals(member.subtype()))
                    return createError(2, "Group member (node) type should be the same as a group (cluster) type");
                // If all group members are down - we can change endpoint to the new one
                if (!group.isAvailable())
                    group.setEndpoint(member.getEndpoint());
                // But if any of members is up then new member should have the same endpoint
                else if (!group.getEndpoint().equals(member.getEndpoint()))
                    return createError(2, "Group member (node) endpoint should be the same as a group (cluster) endpoint");
            }
            group.getMembers().put(member.getNode(), new LinkedMember(member.isAvailable()));
            groups.put(group.getName(), group);
        } finally {
            groups.unlock(member.getName());
        }
        return OperationStatus.createSuccessful();
    }

    @Override
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
    public OperationStatus unregisterAll(String name) {
        groups.remove(name);
        return OperationStatus.createSuccessful();
    }

    @Override
    public OperationStatus setUnavailable(String name, String node) {
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                LinkedMember member = group.getMembers().get(node);
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

    @Override
    public Member find(String name, String node) {
        Group group = groups.get(name);
        return  (group != null) ? Group.createBy(group, node) : null;
    }

    @Override
    public Group findAll(String name) {
        return groups.get(name);
    }
}
