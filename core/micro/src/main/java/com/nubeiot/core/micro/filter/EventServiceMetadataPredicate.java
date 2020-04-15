package com.nubeiot.core.micro.filter;

import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.ServiceGatewayIndex.Params;
import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

class EventServiceMetadataPredicate implements MetadataPredicate {

    @Override
    public boolean test(@NonNull Record record, @NonNull JsonObject filter) {
        final String action = Strings.toString(filter.remove(Params.ACTION));
        if (Strings.isBlank(action)) {
            return true;
        }
        EventAction event = EventAction.parse(action);
        EventMethodDefinition definition = JsonData.convert(
            record.getMetadata().getJsonObject(EventMessageService.EVENT_METHOD_CONFIG), EventMethodDefinition.class);
        return definition.getMapping().stream().anyMatch(map -> map.getAction().equals(event));
    }

}
