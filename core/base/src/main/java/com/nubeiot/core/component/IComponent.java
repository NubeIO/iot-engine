package com.nubeiot.core.component;

import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.Future;

/**
 * Represents small and independent component that integrate with verticle.
 */
public interface IComponent {

    void start(Future<Void> startFuture) throws NubeException;

    void start() throws NubeException;

    void stop() throws NubeException;

    void stop(Future<Void> future) throws NubeException;

}
