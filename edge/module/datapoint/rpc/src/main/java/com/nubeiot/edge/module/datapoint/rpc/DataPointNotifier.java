package com.nubeiot.edge.module.datapoint.rpc;

/**
 * Represents for a {@code Notifier RPC client} that watches the specific {@code Protocol} event then do notify to
 * {@code Data Point service}
 *
 * @param <T> Type of notifier client
 * @see DataPointRpcClient
 */
public interface DataPointNotifier<T extends DataPointNotifier> extends DataPointRpcClient<T> {}
