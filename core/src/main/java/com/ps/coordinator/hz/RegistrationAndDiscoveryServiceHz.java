package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.ps.coordinator.api.*;
import com.ps.coordinator.api.Member;

import java.util.HashSet;
import java.util.Set;

import static com.ps.coordinator.api.utils.Assert.*;
import static com.hazelcast.query.Predicates.*;

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

    public void register(Member member) {
        if (!member.isAvailable())
            throw new IllegalStateException("Cannot register unavailable member");
        checkNull(member.getType(), "Member type");
        checkNullOrEmpty(member.getSubtype(), "Member subtype");
        checkNullOrEmpty(member.getNode(), "Member node name");
        if (member.getType() == Type.SERVICE)
            checkNullOrEmpty(member.getAddress(), "Member address");
        // Atomic operation to keep consistency
        groups.lock(member.getName());
        try {
            Group group = groups.get(member.getName());
            if (group == null) {
                group = Group.createBy(member);
            }
            // Validate when member (node) joins to the existing group (cluster)
            else {
                // Member type and getSubtype should be the same
                if (group.getType() == member.getType() && !group.getSubtype().equals(member.getSubtype()))
                    throw new IllegalArgumentException("Group member (node) type should be the same as a group (cluster) type");
                // If all group members are down - we can change endpoint to the new one
                if (!group.isAvailable())
                    group.setAddress(member.getAddress());
                // But if any of members is up then new member should have the same endpoint
                else if (!group.getAddress().equals(member.getAddress()))
                    throw new IllegalArgumentException("Group member (node) endpoint should be the same as a group (cluster) endpoint");
            }
            group.getMembers().put(member.getNode(), new LinkedMember(member.isAvailable()));
            groups.put(group.getName(), group);
        } finally {
            groups.unlock(member.getName());
        }
    }

    @Override
    public void unregister(String name, String node) {
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                group.getMembers().remove(node);
                if (group.getMembers().size() == 0)
                    groups.remove(name);
                else
                    groups.put(name, group);
            }
        } finally {
            groups.unlock(name);
        }
    }

    @Override
    public void unregister(String name) {
        groups.remove(name);
    }

    @Override
    public void setUnavailable(String name, String node) {
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
    }

    @Override
    public Group find(String name) {
        return groups.get(name);
    }

    @Override
    public Set<Group> findAll(Type type) {
        checkNull(type, "Group type");
        return new HashSet<>(groups.values(equal("type", type)));
    }

    @Override
    public Set<Group> findAll(Type type, String subtype) {
        checkNull(type, "Group type");
        checkNullOrEmpty(subtype, "Group subtype");
        return new HashSet<>(groups.values(and(equal("type", type), equal("subtype", subtype))));
    }
}
