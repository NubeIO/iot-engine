package com.nubeiot.core.http;

import com.nubeiot.core.component.UnitContext;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class HttpServerContext extends UnitContext {

    private ServerInfo serverInfo;

    HttpServerContext create(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        return this;
    }

}
