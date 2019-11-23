package com.nubeiot.core.component;

public interface ApplicationProbe {

    /**
     * Check is able to serve requests.
     *
     * @return {@code true} if application is able to serve requests, otherwise is {@code false}
     */
    boolean readiness();

    /**
     * Health check for an application is running, but unable to make progress
     *
     * @return {@code true} if application is healthy
     */
    boolean liveness();

}
