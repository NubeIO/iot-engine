package com.nubeiot.core.exceptions;

/**
 * Wrap Vertx exceptions
 */
public final class EngineException extends NubeException {

    public EngineException(String message, Throwable e) {
        super(ErrorCode.ENGINE_ERROR, message, e);
    }

    public EngineException(String message) { this(message, null); }

    public EngineException(Throwable e)    { this(null, e); }

}
