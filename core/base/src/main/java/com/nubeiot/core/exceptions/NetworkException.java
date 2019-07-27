package com.nubeiot.core.exceptions;

import static com.nubeiot.core.exceptions.NubeException.ErrorCode.NETWORK_ERROR;

public final class NetworkException extends NubeException {

    public NetworkException(String message, Throwable e) { super(NETWORK_ERROR, message, e); }

    public NetworkException(String message)              { this(message, null); }

    public NetworkException(Throwable e)                 { this(null, e); }

}
