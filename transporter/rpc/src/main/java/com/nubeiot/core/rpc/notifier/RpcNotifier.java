package com.nubeiot.core.rpc.notifier;

import io.github.zero88.msa.bp.dto.JsonData;

import com.nubeiot.core.rpc.RpcClient;

/**
 * Represents for a {@code Notifier RPC client} that watches the specific {@code Protocol} event then do notify to
 * {@code Data Point service}
 *
 * @param <P> Type of JsonData
 * @param <T> Type of notifier client
 * @see RpcClient
 */
public interface RpcNotifier<P extends JsonData, T extends RpcNotifier> extends RpcClient<P, T> {}
