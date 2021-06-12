package io.ulbrich.imageservice.service;

import io.ulbrich.imageservice.config.RateLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.LocalDateTime;

@Service
public class RateLimitServiceImpl implements RateLimitService {
    private static final Logger LOG = LoggerFactory.getLogger(RateLimitServiceImpl.class);

    private final JedisPool jedisPool;
    private final RateLimiterConfig rateLimiterConfig;

    public RateLimitServiceImpl(JedisPool jedisPool, RateLimiterConfig rateLimiterConfig) {
        this.jedisPool = jedisPool;
        this.rateLimiterConfig = rateLimiterConfig;
    }

    @Override
    public boolean isRateLimited(String identifier, long group) throws JedisConnectionException {
        String key = identifier + ":" + group + ":" + LocalDateTime.now().getHour();
        long alreadyUsed = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.get(key);
            if (response != null) {
                alreadyUsed = Long.parseLong(response);
            }
            LOG.debug(key + "-> " + alreadyUsed);

            if (alreadyUsed >= rateLimiterConfig.getRateForEndpointGroup(group)) {
                LOG.debug("Rate limited {} for endpoint group {}", identifier, group);
                return true;
            }

            Transaction transaction = jedis.multi();
            transaction.incr(key);
            transaction.expire(key, 3600L);
            transaction.exec();
        }
        return false;
    }
}
