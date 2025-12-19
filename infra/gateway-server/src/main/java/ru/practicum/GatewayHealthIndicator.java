package ru.practicum;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", "gateway-server")
                .withDetail("status", "UP")
                .withDetail("message", "Gateway is running and ready to route requests")
                .build();
    }
}
