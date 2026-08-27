package com.comeon.assignment.realitycheck.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "2m")
@ConditionalOnProperty(
        name = "scheduler-lock.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SchedulerLockConfig {

    @Bean
    public LockProvider lockProvider(
            RedisConnectionFactory connectionFactory) {

        return new RedisLockProvider(connectionFactory);
    }
}