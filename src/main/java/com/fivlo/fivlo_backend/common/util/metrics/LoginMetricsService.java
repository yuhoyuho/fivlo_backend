package com.fivlo.fivlo_backend.common.util.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LoginMetricsService {
    private final Counter loginSuccessCounter;
    private final Counter loginFailedCounter;

    public LoginMetricsService(MeterRegistry meterRegistry) {
        this.loginSuccessCounter = Counter.builder("fivlo.user.login.count")
            .tag("status", "success")
            .description("Number of successful user logins")
            .register(meterRegistry);

        this.loginFailedCounter = Counter.builder("fivlo.user.login.count")
            .tag("status", "failed")
            .description("Number of failed user logins")
            .register(meterRegistry);
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailed() {
        loginFailedCounter.increment();
    }
}
