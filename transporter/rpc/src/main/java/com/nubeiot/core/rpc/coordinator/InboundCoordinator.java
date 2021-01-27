package com.nubeiot.core.rpc.coordinator;

import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.iotdata.IoTEntity;

/**
 * Represents for a {@code RpcClient service} that watches a particular {@code Protocol event} then notifying event to
 * external services
 *
 * @param <P> Type of IoTEntity
 * @see RpcProtocolClient
 * @see IoTEntity
 */
public interface InboundCoordinator<P extends IoTEntity> extends RpcProtocolClient<P> {}
