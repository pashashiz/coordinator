package com.ps.coordinator.hz;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.ps.coordinator.api.Coordinator;

public class MockCoordinatorClientFactory {

    public Coordinator create(ClientConfig config) {
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(config);
        return new CoordinatorHz(hz, true);
    }

}
