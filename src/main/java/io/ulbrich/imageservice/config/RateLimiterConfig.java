package io.ulbrich.imageservice.config;

import io.ulbrich.imageservice.config.properties.ServiceProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RateLimiterConfig {
    private final long defaultRate;
    private Map<Long, Long> endpointGroupRates;

    public RateLimiterConfig(ServiceProperties serviceProperties) {
        this.endpointGroupRates = serviceProperties.getRateLimitGroups().getEndpointGroupRates();
        this.defaultRate = serviceProperties.getRateLimitGroups().getDefaultRate();
    }

    public long getRateForEndpointGroup(long endpointGroupId) {
        return endpointGroupRates.getOrDefault(endpointGroupId, defaultRate);
    }

    public Map<Long, Long> getEndpointGroupRates() {
        return endpointGroupRates;
    }

    public void setEndpointGroupRates(Map<Long, Long> endpointGroupRates) {
        this.endpointGroupRates = endpointGroupRates;
    }
}
