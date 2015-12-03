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
            fireMemberEventsIfExists(event.getValue(), event.getOldValue());
            log.trace("New {} was created", event.getValue());
            for (EventListener listener : listeners.values()) {
                listener.onGroupCreated(event.getValue());
                listener.onGroupAvailable(event.getValue());
            }
        }

        @Override
        public void entryUpdated(EntryEvent<String, Group> event) {
            fireMemberEventsIfExists(event.getValue(), event.getOldValue());
            log.trace("{} was rebalanced", event.getValue());
            boolean isBecameAvailable = isGroupBecameAvailable(event.getValue(), event.getOldValue());
            if (isBecameAvailable) log.trace("{} became available", event.getValue());
            boolean isBecameUnavailable = isGroupBecameUnavailable(event.getValue(), event.getOldValue());
            if (isBecameUnavailable) log.trace("{} became unavailable", event.getValue());
            for (EventListener listener : listeners.values()) {
                listener.onGroupRebalanced(event.getValue());
                if (isBecameAvailable) listener.onGroupAvailable(event.getValue());
                if (isBecameUnavailable) listener.onGroupUnavailable(event.getValue());
            }
        }

        @Override
        public void entryRemoved(EntryEvent<String, Group> event) {
            fireMemberEventsIfExists(event.getValue(), event.getOldValue());
            log.trace("Group [{}] was removed entirely", event.getKey());
            for (EventListener listener : listeners.values()) {
                listener.onGroupUnavailable(event.getValue());
                listener.onGroupRemoved(event.getOldValue());
            }
        }

        private void fireMemberEventsIfExists(Group newGroup, Group oldGroup) {
            Set<Member> newRegistered = getNewRegisteredMembers(newGroup, oldGroup);
            Set<Member> newUnregistered = getNewUnregisteredMembers(newGroup, oldGroup);
            Set<Member> newAvailable = getNewAvailableMembers(newGroup, oldGroup);
            Set<Member> newUnavailable = getNewUnavailableMembers(newGroup, oldGroup);
            if (!newRegistered.isEmpty()) log.trace("New members {} were registered", newRegistered);
            if (!newUnregistered.isEmpty()) log.trace("Members {} were unregistered", newUnregistered);
            if (!newAvailable.isEmpty()) log.trace("Members {} became available", newAvailable);
            if (!newUnavailable.isEmpty()) log.trace("Members {} became unavailable", newUnavailable);
            for (EventListener listener : listeners.values()) {
                for (Member member : newRegistered)
                    listener.onMemberRegistered(member);
                for (Member member : newUnregistered)
                    listener.onMemberUnregistered(member);
                for (Member member : newAvailable)
                    listener.onMemberAvailable(member);
                for (Member member : newUnavailable)
                    listener.onMemberUnavailable(member);
            }
        }

        private Set<Member> getNewAvailableMembers(Group newGroup, Group oldGroup) {
            Set<Member> newMembers = new HashSet<>();
            if (newGroup != null) {
                for (Map.Entry<String, LinkedMember> entry : newGroup.getMembers().entrySet()) {
                    if (entry.getValue().isAvailable() && !isMemberAvailableInGroup(oldGroup, entry.getKey()))
                        newMembers.add(Group.createBy(newGroup, entry.getKey()));
                }
            }
            return newMembers;
        }

        private Set<Member> getNewUnavailableMembers(Group newGroup, Group oldGroup) {
            return getNewAvailableMembers(oldGroup, newGroup);
        }

        private boolean isMemberAvailableInGroup(Group group, String member) {
            return (group != null && group.getMembers().containsKey(member) && group.getMembers().get(member).isAvailable());
        }

        private Set<Member> getNewRegisteredMembers(Group newGroup, Group oldGroup) {
            Set<Member> newMembers = new HashSet<>();
            if (newGroup != null) {
                for (Map.Entry<String, LinkedMember> entry : newGroup.getMembers().entrySet()) {
                    if (oldGroup == null || oldGroup.getMembers().get(entry.getKey()) == null)
                        newMembers.add(Group.createBy(newGroup, entry.getKey()));
                }
            }
            return newMembers;
        }

        private Set<Member> getNewUnregisteredMembers(Group newGroup, Group oldGroup) {
            return getNewRegisteredMembers(oldGroup, newGroup);
        }

        private boolean isGroupBecameAvailable(Group newGroup, Group oldGroup) {
            return (newGroup != null && newGroup.isAvailable() && (oldGroup == null || !oldGroup.isAvailable()));
        }

        private boolean isGroupBecameUnavailable(Group newGroup, Group oldGroup) {
            return isGroupBecameAvailable(oldGroup, newGroup);
        }

    }

}
