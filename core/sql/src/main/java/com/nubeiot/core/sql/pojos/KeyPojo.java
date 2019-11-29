package com.nubeiot.core.sql.pojos;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
public final class KeyPojo {

    private final VertxPojo request;
    private final Object key;
    private final VertxPojo pojo;

}
