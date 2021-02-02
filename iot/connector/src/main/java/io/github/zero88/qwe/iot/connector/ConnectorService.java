package io.github.zero88.qwe.iot.connector;
import io.github.zero88.qwe.component.HasSharedData;
import io.github.zero88.qwe.event.EventListener;
import io.github.zero88.qwe.protocol.HasProtocol;

public interface ConnectorService extends EventListener, HasProtocol, HasSharedData {

    /**
     * Defines service function name that will be used to distinguish to other services.
     *
     * @return function name
     * @apiNote It is used to generated HTTP path and Event address then it must be unique
     */
    String function();

}
