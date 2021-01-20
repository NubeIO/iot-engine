package com.nubeiot.core.rpc.notifier;

import com.nubeiot.core.rpc.RpcClient;
import com.nubeiot.iotdata.IoTEntity;

/**
 * Represents for a {@code Notifier RPC client} that watches the specific {@code Protocol} event then do notify to
 * {@code Data Point service}
 *
 * @param <P> Type of JsonData
 * @param <T> Type of notifier client
 * @see RpcClient
 */
public interface RpcNotifier<P extends IoTEntity, T extends RpcNotifier> extends RpcClient<P, T> {}
