package com.nubeiot.core.http;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.event.EventModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class HttpServerContext extends UnitContext {

    private ServerInfo serverInfo;
    private EventModel uploadListenerEvent;

    HttpServerContext create(ServerInfo serverInfo, EventModel uploadListenerEvent) {
        this.serverInfo = serverInfo;
        this.uploadListenerEvent = uploadListenerEvent;
        return this;
    }

}
