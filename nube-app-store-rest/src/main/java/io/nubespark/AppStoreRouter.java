package io.nubespark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;

import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
class AppStoreRouter {

    private static final Map<EventType, HttpMethod> HTTP_EVENT_MAPPING;
    private final List<Metadata> routers = new ArrayList<>();

    static {
        Map<EventType, HttpMethod> map = new HashMap<>();
        map.put(EventType.CREATE, HttpMethod.POST);
        map.put(EventType.UPDATE, HttpMethod.PUT);
        map.put(EventType.HALT, HttpMethod.PATCH);
        map.put(EventType.REMOVE, HttpMethod.DELETE);
        map.put(EventType.GET_ONE, HttpMethod.GET);
        map.put(EventType.GET_LIST, HttpMethod.GET);
        HTTP_EVENT_MAPPING = Collections.unmodifiableMap(map);
    }

    AppStoreRouter init() {
        addRouter(EventModel.EDGE_BIOS_TRANSACTION, "/module/transaction", "transaction_id");
        addRouter(EventModel.EDGE_BIOS_INSTALLER, "/module", "service_id");
        addRouter(EventModel.EDGE_BIOS_STATUS, "/status");
        addRouter(EventModel.EDGE_APP_INSTALLER, "/service", "service_id");
        addRouter(EventModel.EDGE_APP_TRANSACTION, "/service/transaction", "transaction_id");
        return this;
    }

    private void addRouter(EventModel eventModel, String api) {
        addRouter(eventModel, api, "id");
    }

    private void addRouter(EventModel eventModel, String api, String paramName) {
        eventModel.getEvents().parallelStream().forEach(event -> {
            final HttpMethod httpMethod = HTTP_EVENT_MAPPING.get(event);
            if (Objects.isNull(httpMethod)) {
                return;
            }
            routers.add(Metadata.builder()
                                .address(eventModel.getAddress())
                                .action(event)
                                .path(api)
                                .method(httpMethod)
                                .paramName(paramName)
                                .build());
        });
    }

    @Getter
    @Builder(builderClassName = "Builder")
    static class Metadata {

        private final String address;
        private final EventType action;
        private final String path;
        private final HttpMethod method;
        private final String paramName;
        private final Metadata afterSuccess;

        public String getPath() {
            if (EventType.GET_LIST == action || EventType.CREATE == action) {
                return path + "s";
            }
            if (EventType.GET_ONE == action || EventType.HALT == action || EventType.UPDATE == action ||
                EventType.REMOVE == action) {
                return path + "/:" + paramName;
            }
            return path;
        }

    }

}
