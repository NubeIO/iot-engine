package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.workflow.TaskExecutionContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EntityRuntimeContext<P extends VertxPojo> implements TaskExecutionContext<P> {

    @NonNull
    private final RequestData originReqData;
    @NonNull
    private final EventAction originReqAction;
    @NonNull
    private final EntityMetadata metadata;
    private final P data;
    private final Throwable throwable;

}
