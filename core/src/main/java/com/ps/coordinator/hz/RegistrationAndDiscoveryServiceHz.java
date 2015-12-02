package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.ps.coordinator.api.*;
import com.ps.coordinator.api.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ps.coordinator.api.utils.Assert.*;
import static com.hazelcast.query.Predicates.*;

public class RegistrationAndDiscoveryServiceHz implements RegistrationAndDiscoveryServiceInteractive {

    private final boolean isClientMode;
    private final IMap<String, Group> groups;
    private final ConcurrentMap<String, EventListener> listeners = new ConcurrentHashMap<>();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public RegistrationAndDiscoveryServiceHz(HazelcastInstance hz, boolean isClient) {
        isClientMode = isClient;
        groups = hz.getMap("groups-registry");
        groups.addEntryListener(new GroupsListener(), true);
    }

    public String addEventListener(EventListener listener) {
        log.debug("Adding new event listener {}", listener);
        String uuid = UUID.randomUUID().toString();
        listeners.put(uuid, listener);
        return uuid;
    }

    @Override
    public void removeEventListener(String id) {
        log.debug("Removing event listener by id {}", id);
        listeners.remove(id);
    }

    public void register(Member member) {
        log.debug("Registering {}...", member);
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
                log.debug("Creating new {}...", group);
            }
            // Validate when member (node) joins to the existing group (cluster)
            else {
                log.debug("Joining existing {}...", group);
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
            log.debug("Group after joining new member: {}", group);
        } finally {
            groups.unlock(member.getName());
        }
    }

    @Override
    public void unregister(String name, String node) {
        log.debug("Un-registering member [{}] of group [{}]...", node, name);
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                group.getMembers().remove(node);
                if (group.getMembers().size() == 0) {
                    log.debug("Remove group after un-registering the last member");
                    groups.remove(name);
                }
                else {
                    groups.put(name, group);
                    log.debug("Group after un-registering new member: {}", group);
                }
            }
        } finally {
            groups.unlock(name);
        }
    }

    @Override
    public void unregister(String name) {
        log.debug("Un-registering the whole group [{}]", name);
        groups.remove(name);
    }

    @Override
    public void setUnavailable(String name, String node) {
        log.debug("Disabling member [{}] of group [{}]...", node, name);
        // Atomic operation to keep consistency
        groups.lock(name);
        try {
            Group group = groups.get(name);
            if (group != null) {
                LinkedMember member = group.getMembers().get(node);
                if (member != null) {
                    member.setAvailable(false);
                    groups.put(name, group);
                    log.debug("Group after disabling member: {}", group);
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
        log.debug("Finding all groups by type [{}]", type);
        checkNull(type, "Group type");
        return new HashSet<>(groups.values(equal("type", type)));
    }

    @Override
    public Set<Group> findAll(Type type, String subtype) {
        log.debug("Finding all groups by type [{}] and subtype [{}]", type, subtype);
        checkNull(type, "Group type");
        checkNullOrEmpty(subtype, "Group subtype");
        return new HashSet<>(groups.values(and(equal("type", type), equal("subtype", subtype))));
    }

    private class GroupsListener implements EntryAddedListener<String, Group>,
            EntryUpdatedListener<String, Group>, EntryRemovedListener<String, Group> {

        @Override
        public void entryAdded(EntryEvent<String, Group> event) {
            Set<Member> registered = getRegisteredMembers(event.getValue(), event.getOldValue());
            if (!registered.isEmpty()) log.trace("New members {} were registered", registered);
            log.trace("New {} was created", event.getValue());
            for (EventListener listener : listeners.values()) {
                for (Member member : registered)
                    listener.onMemberRegistered(member);
                listener.onGroupCreated(event.getValue());
            }
        }

        @Override
        public void entryRemoved(EntryEvent<String, Group> event) {
            Set<Member> unregistered = getUnregisteredMembers(event.getValue(), event.getOldValue());
            if (!unregistered.isEmpty()) log.trace("Members {} were unregistered", unregistered);
            log.trace("Group [{}] was removed entirely", event.getKey());
            for (EventListener listener : listeners.values()) {
                for (Member member : unregistered)
                    listener.onMemberUnregistered(member);
                listener.onGroupRemoved(event.getOldValue());
            }
        }

        @Override
        public void entryUpdated(EntryEvent<String, Group> event) {
            Set<Member> registered = getRegisteredMembers(event.getValue(), event.getOldValue());
            Set<Member> unregistered = getUnregisteredMembers(event.getValue(), event.getOldValue());
            if (!registered.isEmpty()) log.trace("New members {} were registered", registered);
            if (!unregistered.isEmpty()) log.trace("Members {} were unregistered", unregistered);
            log.trace("New {} was created", event.getValue());
            for (EventListener listener : listeners.values()) {
                for (Member member : registered)
                    listener.onMemberRegistered(member);
                for (Member member : unregistered)
                    listener.onMemberUnregistered(member);
                listener.onGroupChanged(event.getValue());
            }
        }

        private Set<Member> getRegisteredMembers(Group newGroup, Group oldGroup) {
            Set<Member> newMembers = new HashSet<>();
            if (newGroup != null) {
                for (Map.Entry<String, LinkedMember> entry : newGroup.getMembers().entrySet()) {
                    if (entry.getValue().isAvailable() && !isMemberAvailableInGroup(oldGroup, entry.getKey()))
                        newMembers.add(Group.createBy(newGroup, entry.getKey()));
                }
            }
            return newMembers;
        }

        private Set<Member> getUnregisteredMembers(Group newGroup, Group oldGroup) {
            return getRegisteredMembers(oldGroup, newGroup);
        }

        private boolean isMemberAvailableInGroup(Group group, String member) {
            return (group != null && group.getMembers().containsKey(member) && group.getMembers().get(member).isAvailable());
        }

    }

}
