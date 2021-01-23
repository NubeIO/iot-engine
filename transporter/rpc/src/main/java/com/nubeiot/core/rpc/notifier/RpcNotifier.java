package com.nubeiot.core.rpc.notifier;

import com.nubeiot.core.rpc.RpcClient;
import com.nubeiot.iotdata.IoTEntity;

/**
 * Represents for a {@code Notifier RPC client} that watches the specific {@code Protocol} event then do notify to
 * {@code Data Point service}
 *
 * @param <P> Type of IoTEntity
 * @see RpcClient
 * @see IoTEntity
 */
public interface RpcNotifier<P extends IoTEntity> extends RpcClient<P> {}
