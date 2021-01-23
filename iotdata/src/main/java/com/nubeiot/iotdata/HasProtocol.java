package com.nubeiot.iotdata;

import lombok.NonNull;

public interface HasProtocol {

    /**
     * Declares IoT protocol
     *
     * @return IoT protocol
     * @see Protocol
     */
    @NonNull Protocol protocol();

}
