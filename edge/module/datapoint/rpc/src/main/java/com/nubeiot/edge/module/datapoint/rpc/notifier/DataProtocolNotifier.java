package com.nubeiot.edge.module.datapoint.rpc.notifier;

import com.nubeiot.edge.module.datapoint.rpc.DataProtocolRpcClient;

/**
 * Represents for a {@code Notifier RPC client} that watches the specific {@code Protocol} event then do notify to
 * {@code Data Point service}
 *
 * @param <T> Type of notifier client
 * @see DataProtocolRpcClient
 */
public interface DataProtocolNotifier<T extends DataProtocolNotifier> extends DataProtocolRpcClient<T> {}
