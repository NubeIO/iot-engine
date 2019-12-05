package com.nubeiot.edge.module.datapoint.rpc;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

interface RpcProtocol {

    /**
     * Defines service protocol
     *
     * @return protocol
     * @see Protocol
     */
    @NonNull Protocol protocol();

    /**
     * Defines service name by protocol in request header
     *
     * @return request service name
     * @see Headers#X_REQUEST_BY
     */
    @NonNull
    default String requestBy() {
        return "service/" + protocol().type();
    }

}
