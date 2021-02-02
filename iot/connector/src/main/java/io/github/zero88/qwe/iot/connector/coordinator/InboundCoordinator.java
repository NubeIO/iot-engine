package io.github.zero88.qwe.iot.connector.coordinator;

import io.github.zero88.qwe.iot.connector.RpcProtocolClient;
import io.github.zero88.qwe.iot.data.IoTEntity;

/**
 * Represents for a {@code RpcClient service} that watches a particular {@code Protocol event} then notifying event to
 * external services
 *
 * @param <P> Type of IoTEntity
 * @see RpcProtocolClient
 * @see IoTEntity
 */
public interface InboundCoordinator<P extends IoTEntity> extends RpcProtocolClient<P>, Coordinator {

    Channel channel();

}
