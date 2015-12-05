package com.ps.coordinator.hz;

import com.hazelcast.config.Config;
import com.ps.coordinator.api.*;

import org.junit.*;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


public class RegistrationAndDiscoveryServiceHzTest {

    private static Coordinator coordinator;
    private static RegistrationAndDiscoveryServiceInteractive service;

    @BeforeClass public static void setup() {
        coordinator = new CoordinatorServerFactory().create(new Config());
        service = coordinator.lookupRegistrationAndDiscoveryServiceInteractive();
    }

    @AfterClass public static void shutdown() {
        coordinator.shutdown();
    }

    @Test public void testRegisterAndDiscoverySuccessful() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        Group group = service.find("apl");
        assertNotNull("New group should be created", group);
        assertEquals("Group type should be the same as passed", Type.SERVICE, group.getType());
        assertEquals("Group subtype should be the same as passed", "subtype", group.getSubtype());
        assertEquals("Group endpoint should be the same as passed", "localhost", group.getAddress());
        Map<String, LinkedMember> members = group.getMembers();
        assertEquals("The new group should contain only 1 member", 1, members.size());
        assertNotNull("The new group should contain passed member", members.get("node-1"));
        assertTrue("The existing member of the group should be available", members.get("node-1").isAvailable());
        service.unregister("apl");
        assertNull("Group should be unregistered", service.find("apl"));
    }

    @Test public void testJoinGroupSuccessful() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        service.register(new Member("apl", "node-2", Type.SERVICE, "subtype", "localhost"));
        Map<String, LinkedMember> members = service.find("apl").getMembers();
        assertEquals("The new group should contain only 2 members", 2, members.size());
        assertTrue("The new group should contain both joined members", members.get("node-1") != null && members.get("node-2") != null);
        service.unregister("apl");
        assertNull("Group should be unregistered", service.find("apl"));
    }

    @Test public void testJoinGroupFailed() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        assertException("The new member of the existing group with different type should be rejected",
                IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                service.register(new Member("apl", Type.RESOURCE, "subtype"));
            }
        });
        assertException("The new member of the existing group with different subtype should be rejected",
                IllegalArgumentException.class, new Runnable() {
                    @Override
                    public void run() {
                        service.register(new Member("apl", "node-3", Type.SERVICE, "other-subtype", "localhost"));
                    }
                });
        assertException("The new member of the existing group with different address should be rejected",
                IllegalArgumentException.class, new Runnable() {
                    @Override
                    public void run() {
                        service.register(new Member("apl", "node-4", Type.SERVICE, "other-subtype", "other-localhost"));
                    }
                });
        Map<String, LinkedMember> members = service.find("apl").getMembers();
        assertEquals("The existing group should contain only 1 members", 1, members.size());
        assertNotNull("The existing group should contain only first member", members.get("node-1"));
        service.unregister("apl");
        assertNull("Group should be unregistered", service.find("apl"));
    }

    @Test public void testRejoinGroupGraceful() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        service.register(new Member("apl", "node-2", Type.SERVICE, "subtype", "localhost"));
        service.setUnavailable("apl", "node-1");
        assertFalse("The first member should be unavailable", service.find("apl").getMembers().get("node-1").isAvailable());
        assertTrue("The group should be still available", service.find("apl").isAvailable());
        service.setUnavailable("apl", "node-2");
        assertFalse("The second member should be unavailable", service.find("apl").getMembers().get("node-2").isAvailable());
        assertFalse("The group should be unavailable", service.find("apl").isAvailable());
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        assertTrue("The first member should be available again", service.find("apl").getMembers().get("node-1").isAvailable());
        assertTrue("The group should be available again", service.find("apl").isAvailable());
        service.unregister("apl");
        assertNull("Group should be unregistered", service.find("apl"));
    }

    @Test public void testChangingGroupAddress() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        service.setUnavailable("apl", "node-1");
        assertFalse("The group should be unavailable", service.find("apl").isAvailable());
        service.register(new Member("apl", "node-2", Type.SERVICE, "subtype", "other-host"));
        assertTrue("The group should be available again", service.find("apl").isAvailable());
        assertEquals("The group should have a new address", service.find("apl").getAddress(), "other-host");
        service.unregister("apl");
        assertNull("Group should be unregistered", service.find("apl"));
    }

    @Test public void testUnregisterMembers() {
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        service.register(new Member("apl", "node-2", Type.SERVICE, "subtype", "localhost"));
        service.unregister("apl", "node-1");
        assertNull("The first member should be unregistered", service.find("apl").getMembers().get("node-1"));
        service.unregister("apl", "node-2");
        assertNull("The whole group should be unregistered", service.find("apl"));
    }

    @Test public void testSearch() {
        service.register(new Member("apl-1", "node-1", Type.SERVICE, "subtype-1", "localhost-1"));
        service.register(new Member("apl-2", "node-1", Type.SERVICE, "subtype-2", "localhost-2"));
        Set<Group> groupsByType = service.findAll(Type.SERVICE);
        assertEquals("Should found both registered services", 2, groupsByType.size());
        assertTrue("Found services should be valid",
                groupsByType.contains(new Group("apl-1", Type.SERVICE, "subtype-1", "localhost-1")) &&
                groupsByType.contains(new Group("apl-2", Type.SERVICE, "subtype-2", "localhost-2")));
        Set<Group> groupsBySubtype = service.findAll(Type.SERVICE, "subtype-1");
        assertEquals("Should found one registered services with specified subtype", 1, groupsBySubtype.size());
        assertTrue("Found service should be valid",
                groupsByType.contains(new Group("apl-1", Type.SERVICE, "subtype-1", "localhost-1")));
        service.unregister("apl-1");
        service.unregister("apl-2");
        assertNull("The first service should be unregistered", service.find("apl-1"));
        assertNull("The second service should be unregistered", service.find("apl-2"));
    }

    @Test(timeout = 60000) public void testEventListener() throws InterruptedException {
        ListenerTracker tracker = new ListenerTracker();
        String id = service.addEventListener(tracker.getListener());
        service.register(new Member("apl", "node-1", Type.SERVICE, "subtype", "localhost"));
        assertEquals("Group created event should be fired", "apl", tracker.takeGroupCreated().getName());
        assertEquals("Group available event should be fired", "apl", tracker.takeGroupAvailable().getName());
        assertEquals("First member registered event should be fired", "node-1", tracker.takeMemberRegistered().getNode());
        assertEquals("First member available event should be fired", "node-1", tracker.takeMemberAvailable().getNode());
        service.register(new Member("apl", "node-2", Type.SERVICE, "subtype", "localhost"));
        assertEquals("Group rebalanced event should be fired", 2, tracker.takeGroupRebalanced().getMembers().size());
        assertEquals("Second member registered event should be fired", "node-2", tracker.takeMemberRegistered().getNode());
        assertEquals("Second member available event should be fired", "node-2", tracker.takeMemberAvailable().getNode());
        service.setUnavailable("apl", "node-2");
        assertEquals("Group rebalanced event should be fired", 2, tracker.takeGroupRebalanced().getMembers().size());
        assertEquals("Second member unavailable event should be fired", "node-2", tracker.takeMemberUnavailable().getNode());
        service.unregister("apl", "node-1");
        assertEquals("First member unavailable event should be fired", "node-1", tracker.takeMemberUnavailable().getNode());
        assertEquals("First member unregistered event should be fired", "node-1", tracker.takeMemberUnregistered().getNode());
        assertEquals("Group unavailable event should be fired", "apl", tracker.takeGroupUnavailable().getName());
        service.unregister("apl");
        assertEquals("Group removed event should be fired", "apl", tracker.takeGroupRemoved().getName());
        assertEquals("Second member unregistered event should be fired", "node-2", tracker.takeMemberUnregistered().getNode());
        assertNull("The group should be unregistered", service.find("apl"));
        service.removeEventListener(id);
    }

    private <T extends Exception> void assertException(String message, Class<T> exception, Runnable function) {
        boolean isAssertionTrue = false;
        try {
            function.run();
        } catch (Exception e) {
            isAssertionTrue = e.getClass().isAssignableFrom(exception);
        }
        assertTrue(message, isAssertionTrue);
    }

}
