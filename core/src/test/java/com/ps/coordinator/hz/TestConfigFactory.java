package com.ps.coordinator.hz;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;

public class TestConfigFactory {

    public Config createConfig() {
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        return config;
    }

    public ClientConfig createClientConfig() {
        ClientConfig config = new ClientConfig();
        config.setProperty("hazelcast.logging.type", "slf4j");
        return config;
    }

}
