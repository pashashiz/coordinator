package com.ps.coordinator;

import com.hazelcast.config.Config;
import com.ps.coordinator.api.Coordinator;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;
import com.ps.coordinator.hz.CoordinatorServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoordinatorConfig {

    @Bean public Config hazelcastConfig() {
        return new Config();
    }

    @Bean(destroyMethod = "shutdown") public Coordinator coordinator() {
        return new CoordinatorServerFactory().create(hazelcastConfig());
    }

    @Bean public RegistrationAndDiscoveryServiceInteractive registrationAndDiscoveryService() {
        return coordinator().lookupRegistrationAndDiscoveryServiceInteractive();
    }
}
