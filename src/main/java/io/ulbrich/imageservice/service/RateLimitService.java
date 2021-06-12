package io.ulbrich.imageservice.service;

import redis.clients.jedis.exceptions.JedisConnectionException;

public interface RateLimitService {
    boolean isRateLimited(String identifier, long group) throws JedisConnectionException;
}
