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
        service.register(new Member("test-apl", "master", "test", "localhost"));
        Assert.assertEquals(service.find("test-apl").getEndpoint(), "localhost");
    }

}