package io.github.zero88.qwe.iot.connector.command;

import java.util.Collections;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.iot.connector.ConnectorServiceApis;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.utils.Urls;
import io.vertx.core.http.HttpMethod;

import lombok.NonNull;

public interface CommanderApis extends Commander, ConnectorServiceApis {

    default String fullServicePath() {
        return Urls.combinePath(function(), protocol().type().toLowerCase(), servicePath(), commandType());
    }

    @Override
    default String paramPath() {
        return null;
    }

    @Override
    default @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.SEND, HttpMethod.POST));
    }

}
