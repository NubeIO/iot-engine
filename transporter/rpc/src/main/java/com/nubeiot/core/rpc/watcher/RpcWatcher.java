package com.nubeiot.core.rpc.watcher;

import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.iotdata.IoTEntity;

/**
 * Represents for a {@code protocol watcher} is based on {@code RpcClient} that watches a particular {@code Protocol
 * event} then do notify to external service
 *
 * @param <P> Type of IoTEntity
 * @see RpcProtocolClient
 * @see IoTEntity
 */
public interface RpcWatcher<P extends IoTEntity> extends RpcProtocolClient<P> {}
