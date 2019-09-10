package com.nubeiot.core.micro.filter;

import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

final class EventServicePathPredicate implements ByPathPredicate {

    @Override
    public boolean test(@NonNull Record record, @NonNull String path) {
        EventMethodDefinition definition = JsonData.convert(
            record.getMetadata().getJsonObject(EventMessageService.EVENT_METHOD_CONFIG), EventMethodDefinition.class);
        return definition.getServicePath().equals(Urls.combinePath(path));
    }

}
