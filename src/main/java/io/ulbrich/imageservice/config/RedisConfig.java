package io.ulbrich.imageservice.config;

import io.ulbrich.imageservice.config.properties.ServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPool jedisPool(ServiceProperties serviceProperties) {
        var poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);

        return new JedisPool(poolConfig, URI.create(serviceProperties.getRedis().getUrl()), serviceProperties.getRedis().getTimeout());
    }
}
