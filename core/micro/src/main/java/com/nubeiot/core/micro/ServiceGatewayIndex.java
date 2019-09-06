package com.nubeiot.core.micro;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.micro.transfomer.RecordTransformer;
import com.nubeiot.core.micro.transfomer.RecordTransformer.RecordView;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ServiceGatewayIndex implements EventListener {

    @NonNull
    private final MicroContext context;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(@NonNull RequestData requestData) {
        JsonObject filter = requestData.getFilter();
        ServiceDiscoveryController controller = getController(filter);
        RecordView view = RecordView.parse((String) filter.remove("view"));
        return controller.getRx()
                         .rxGetRecord(record -> predicate(filter).test(record))
                         .map(RecordTransformer.create(view)::transform)
                         .switchIfEmpty(Single.error(new NotFoundException("Not found")));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(@NonNull RequestData requestData) {
        JsonObject filter = requestData.getFilter();
        ServiceDiscoveryController controller = getController(filter);
        RecordTransformer transformer = RecordTransformer.create(RecordView.END_USER);
        return controller.getRx()
                         .rxGetRecords(record -> predicate(filter).test(record))
                         .flatMap(records -> Observable.fromIterable(records).map(transformer::transform).toList())
                         .map(records -> new JsonObject().put("apis", new JsonArray(records)));
    }

    private ServiceDiscoveryController getController(JsonObject filter) {
        ServiceDiscoveryKind scope = ServiceDiscoveryKind.parse((String) filter.remove("kind"));
        return ServiceDiscoveryKind.LOCAL == scope ? context.getLocalController() : context.getClusterController();
    }

    private Predicate<Record> predicate(JsonObject filter) {
        String type = filter.getString("type");
        Status status = Objects.isNull(filter.getString("status"))
                        ? Status.UP
                        : Functions.getIfThrow(() -> Status.valueOf(filter.getString("status").toUpperCase()))
                                   .orElse(Status.UP);
        ServiceScope scope = ServiceScope.parse((String) filter.remove("scope"));
        return byScope(scope, type).and(r -> r.getStatus().equals(status)).and(r -> r.match(filter));
    }

    private Predicate<Record> byScope(@NonNull ServiceScope scope, final String type) {
        if (Strings.isNotBlank(type)) {
            return record -> record.getType().equals(type);
        }
        return scope.getPredicate();
    }

}
