package com.ps.coordinator.hz;

import com.hazelcast.config.Config;
import com.ps.coordinator.api.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegistrationAndDiscoveryServiceHzTest {

    private static RegistrationAndDiscoveryServiceInteractive service;

    @BeforeClass public static void setUp() {
        Coordinator coordinator = new CoordinatorFactory().create(new Config(), false);
        service = coordinator.lookupRegistrationAndDiscoveryServiceInteractive();
    }

    @Test public void testRegisterAndDiscovery() {
        OperationStatus statusOk = service.register(
                new Member("test-apl", "node-1", Type.SERVICE, "test-sub-type", "localhost"));
        Assert.assertEquals(statusOk.getStatus(), OperationStatus.Status.SUCCESSFUL);
        Assert.assertEquals(service.findAll("test-apl").getEndpoint(), "localhost");
        OperationStatus statusError = service.register(
                new Member("test-apl", "node-2", Type.SERVICE, "test-sub-type", "new-host"));
        Assert.assertEquals(statusError.getStatus(), OperationStatus.Status.ERROR);
        Assert.assertNull(service.findAll("test-apl").getMembers().get("node-2"));
        Assert.assertEquals(service.findAll("test-apl").getEndpoint(), "localhost");
    }

}
