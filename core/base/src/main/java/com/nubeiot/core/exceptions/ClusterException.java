package com.nubeiot.core.exceptions;

public class ClusterException extends EngineException {

    public ClusterException(String message, Throwable e) {
        super(ErrorCode.CLUSTER_ERROR, message, e);
    }

    public ClusterException(String message) { this(message, null); }

    public ClusterException(Throwable e)    { this(null, e); }

}
