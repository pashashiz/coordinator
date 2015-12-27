package com.ps.coordinator.hz;

import com.hazelcast.client.HazelcastClientNotActiveException;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;
import com.ps.coordinator.api.Type;
import com.ps.coordinator.Assert;
import com.ps.coordinator.api.Coordinator;
import com.ps.coordinator.api.Member;
import org.junit.Test;

import static org.junit.Assert.*;

public class RegistrationAndDiscoveryServiceHzTestCluster {

    @Test(timeout = 60000) public void testRegisterAndDiscoveryGraceful() throws InterruptedException {
        RegistrationAndDiscoveryServiceInteractive server = createService();
        ListenerTracker sTracker = new ListenerTracker();
        server.addEventListener(sTracker.getListener());
        RegistrationAndDiscoveryServiceInteractive client1 = createClient();
        ListenerTracker c1Tracker = new ListenerTracker();
        client1.addEventListener(c1Tracker.getListener());
        client1.register(new Member("apl-1", "node-1", Type.SERVICE, "subtype", "localhost-1"));
        assertEquals("Server should see the registered first client", "localhost-1", server.find("apl-1").getAddress());
        assertEquals("First client available event should be fired on server side", "apl-1", sTracker.takeGroupAvailable().getName());
        assertEquals("First client available event should be fired on first client side as well", "apl-1", c1Tracker.takeGroupAvailable().getName());
        RegistrationAndDiscoveryServiceInteractive client2 = createClient();
        assertEquals("Second client should see the registered first client", "localhost-1", server.find("apl-1").getAddress());
        ListenerTracker c2Tracker = new ListenerTracker();
        client2.addEventListener(c2Tracker.getListener());
        client2.register(new Member("apl-2", "node-1", Type.SERVICE, "subtype", "localhost-2"));
        assertEquals("Server should see the registered second client", "localhost-2", server.find("apl-2").getAddress());
        assertEquals("Second client available event should be fired on server side", "apl-2", sTracker.takeGroupAvailable().getName());
        assertEquals("First client should see the registered second client", "localhost-2", client1.find("apl-2").getAddress());
        assertEquals("Second client event should be fired on first client side", "apl-2", c1Tracker.takeGroupAvailable().getName());
        assertEquals("Second client event should be fired on second client side as well", "apl-2", c2Tracker.takeGroupAvailable().getName());
        client1.setUnavailable("apl-1", "node-1");
        assertFalse("Server should see that the first member should be unavailable", server.find("apl-1").isAvailable());
        assertEquals("First client unavailable event should be fired on server side", "apl-1", sTracker.takeGroupUnavailable().getName());
        assertFalse("Second client should see that the first member should be unavailable", client2.find("apl-1").isAvailable());
        assertEquals("First client unavailable event should be fired on the second client side", "apl-1", c1Tracker.takeGroupUnavailable().getName());
        client2.unregister("apl-2");
        assertNull("Server should see that the second member should be unregistered", server.find("apl-2"));
        assertEquals("Second client unavailable event should be fired on server side", "apl-2", sTracker.takeGroupRemoved().getName());
        shutdownAll(client1, client2, server);
    }

    @Test(timeout = 60000) public void testRegisterAndDiscoveryWhenOneClientCrashes() throws InterruptedException {
        RegistrationAndDiscoveryServiceInteractive server = createService();
        ListenerTracker sTracker = new ListenerTracker();
        server.addEventListener(sTracker.getListener());
        RegistrationAndDiscoveryServiceInteractive client1 = createClient();
        ListenerTracker c1Tracker = new ListenerTracker();
        client1.addEventListener(c1Tracker.getListener());
        client1.register(new Member("apl-1", "node-1", "", Type.SERVICE, "subtype", "localhost-1"));
        RegistrationAndDiscoveryServiceInteractive client2 = createClient();
        ListenerTracker c2Tracker = new ListenerTracker();
        client2.addEventListener(c2Tracker.getListener());
        client2.register(new Member("apl-2", "node-1", "", Type.SERVICE, "subtype", "localhost-2"));
        shutdownAll(client1);
        assertEquals("First client unavailable event should be fired on server side", "apl-1", sTracker.takeGroupUnavailable().getName());
        assertFalse("Server should see that the first member should be unavailable", server.find("apl-1").isAvailable());
        assertEquals("First client unavailable event should be fired on the second client side", "apl-1", c2Tracker.takeGroupUnavailable().getName());
        assertFalse("Second client should see that the first member should be unavailable", client2.find("apl-1").isAvailable());
        shutdownAll(client2, server);
    }

    @Test(timeout = 60000) public void testRegisterAndDiscoveryWhenMainServerCrashes() throws InterruptedException {
        RegistrationAndDiscoveryServiceInteractive server = createService();
        ListenerTracker sTracker = new ListenerTracker();
        server.addEventListener(sTracker.getListener());
        final RegistrationAndDiscoveryServiceInteractive client1 = createClient();
        ListenerTracker c1Tracker = new ListenerTracker();
        client1.addEventListener(c1Tracker.getListener());
        client1.register(new Member("apl-1", "node-1", "", Type.SERVICE, "subtype", "localhost-1"));
        final RegistrationAndDiscoveryServiceInteractive client2 = createClient();
        ListenerTracker c2Tracker = new ListenerTracker();
        client2.addEventListener(c2Tracker.getListener());
        client2.register(new Member("apl-2", "node-1", "", Type.SERVICE, "subtype", "localhost-2"));
        shutdownAll(server);
        Assert.assertException("First client should lose connection to the server", HazelcastClientNotActiveException.class, new Runnable() {
            @Override
            public void run() {
                client1.find("apl-2");
            }
        });
        Assert.assertException("Second client should lose connection to the server", HazelcastClientNotActiveException.class, new Runnable() {
            @Override
            public void run() {
                client2.find("apl-1");
            }
        });
        shutdownAll(client1, client2);
    }

    private RegistrationAndDiscoveryServiceInteractive createService() {
        Config config = new TestConfigFactory().createConfig();
        Coordinator coordinator = new CoordinatorServerFactory().create(config);
        return coordinator.lookupRegistrationAndDiscoveryServiceInteractive();
    }

    private RegistrationAndDiscoveryServiceInteractive createClient() {
        ClientConfig config = new TestConfigFactory().createClientConfig();
        Coordinator coordinator = new MockCoordinatorClientFactory().create(config);
        return coordinator.lookupRegistrationAndDiscoveryServiceInteractive();
    }

    private void shutdownAll(RegistrationAndDiscoveryServiceInteractive... services) {
        for (RegistrationAndDiscoveryServiceInteractive service : services)
            ((RegistrationAndDiscoveryServiceHz) service).shutdown();
    }
}
