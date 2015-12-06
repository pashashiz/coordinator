package com.ps.coordinator.hz;

import com.hazelcast.core.*;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hazelcast.query.Predicates.*;
import static com.ps.coordinator.api.utils.Assert.*;

public class RegistrationAndDiscoveryServiceHz implements RegistrationAndDiscoveryServiceInteractive {

    private final HazelcastInstance instance;
    private final IMap<String, Group> groups;
    private final ConcurrentMap<String, EventListener> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public RegistrationAndDiscoveryServiceHz(HazelcastInstance hz, boolean isClient) {
        instance = hz;
        if (!isClient) {
            instance.getCluster().addMembershipListener(new ClusterMembershipListener());
            instance.getClientService().addClientListener(new ClientMembershipListener());
        }
        groups = instance.getMap("groups-registry");
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
        notNull(member.getType(), "Member type");
        notNullOrEmpty(member.getSubtype(), "Member subtype");
        notNullOrEmpty(member.getNode(), "Member node name");
        if (member.getType() == Type.SERVICE)
            notNullOrEmpty(member.getAddress(), "Member address");
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
            // Use current owner if not specified
            if (member.getOwner() == null || member.getOwner().isEmpty())
                member.setOwner(instance.getLocalEndpoint().getUuid());
            // Add new member
            group.getMembers().put(member.getNode(), new LinkedMember(member.getOwner(), member.isAvailable()));
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
    public Set<Group> findAll() {
        return new HashSet<>(groups.values());
    }

    @Override
    public Set<Group> findAll(Type type) {
        log.debug("Finding all groups by type [{}]", type);
        notNull(type, "Group type");
        return new HashSet<>(groups.values(equal("type", type)));
    }

    @Override
    public Set<Group> findAll(Type type, String subtype) {
        log.debug("Finding all groups by type [{}] and subtype [{}]", type, subtype);
        notNull(type, "Group type");
        notNullOrEmpty(subtype, "Group subtype");
        return new HashSet<>(groups.values(and(equal("type", type), equal("subtype", subtype))));
    }

    protected Set<Group> findAllByOwner(String owner) {
        log.debug("Finding all groups by owner [{}]", owner);
        notNullOrEmpty(owner, "Owner UUID");
        Aggregation<String, Group, Set<Group>> aggregation = Aggregations.distinctValues();
        return groups.aggregate(new OwnerFilter(owner), aggregation);
    }

    protected void shutdown() {
        instance.shutdown();
    }

    private static class OwnerFilter extends Supplier<String, Group, Group> {

        private String owner;

        public OwnerFilter(String owner) {
            this.owner = owner;
        }

        @Override
        public Group apply(Map.Entry<String, Group> entry) {
            for (LinkedMember member : entry.getValue().getMembers().values()) {
                if (member.getOwner().equals(owner))
                    return entry.getValue();
            }
            return null;
        }
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
                listener.onGroupUnavailable(event.getOldValue());
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

    private class ClusterMembershipListener implements MembershipListener {

        public void memberAdded(MembershipEvent membershipEvent) {
            log.debug("New server's node added: " + membershipEvent);
        }

        public void memberRemoved(final MembershipEvent membershipEvent) {
            log.debug("Existing server's node disconnected: " + membershipEvent);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    trySetUnavailableLostNodes(membershipEvent.getMember().getUuid());
                }
            });
        }

        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            log.debug("Server node's attribute changed: " + memberAttributeEvent);
        }
    }

    private class ClientMembershipListener implements ClientListener {

        @Override
        public void clientConnected(Client client) {
            log.debug("Connected a new client {}", client);
        }

        @Override
        public void clientDisconnected(final Client client) {
            log.debug("Disconnected an existing client {} " + client);
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    trySetUnavailableLostNodes(client.getUuid());
                }
            });
        }
    }

    private boolean trySetUnavailableLostNodes(String owner) {
        log.debug("Try to set unavailable all nodes owned by {}...", owner);
        boolean success = false;
        for (Group lost : findAllByOwner(owner))
            success = success | trySetUnavailableLostNodes(lost, owner);
        return success;
    }

    private boolean trySetUnavailableLostNodes(Group suspectGroup, String owner) {
        log.debug("Try to set unavailable all suspected nodes in {} owned by {}...",  suspectGroup, owner);
        // Atomic operation to keep consistency
        groups.lock(suspectGroup.getName());
        try {
            Group group = groups.get(suspectGroup.getName());
            if (group != null) {
                Set<String> nodes = retrieveNodesByOwner(group, owner);
                for (String node : nodes) {
                    log.debug("Found node [{}] in {} which lost its owner: {}, set it unavailable", node, group);
                    LinkedMember member = group.getMembers().get(node);
                    member.setAvailable(false).setOwner(null);
                    groups.put(group.getName(), group);
                }
                if (!nodes.isEmpty()) {
                    log.debug("Group after setting unavailable all nodes owned by {}...", owner);
                    return true;
                }
            }
        } finally {
            groups.unlock(suspectGroup.getName());
        }
        return false;
    }

    private Set<String> retrieveNodesByOwner(Group group, String owner) {
        HashSet<String> nodes = new HashSet<>();
        for (Map.Entry<String, LinkedMember> entry : group.getMembers().entrySet()) {
            LinkedMember member = entry.getValue();
            if (member != null && member.getOwner().equals(owner) && member.isAvailable())
                nodes.add(entry.getKey());
        }
        return nodes;
    }

}
