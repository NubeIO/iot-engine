package com.nubeiot.edge.module.datapoint.rpc;

import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

public interface RpcProtocol {

    /**
     * Defines service protocol
     *
     * @return protocol
     * @see Protocol
     */
    @NonNull Protocol protocol();

}
