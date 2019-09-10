package com.nubeiot.core.micro;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.micro.filter.RecordPredicate;
import com.nubeiot.core.micro.transfomer.RecordTransformer;
import com.nubeiot.core.micro.transfomer.RecordTransformer.RecordView;
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
        final JsonObject filter = requestData.getFilter();
        final ServiceDiscoveryController controller = getController(filter);
        final RecordView view = RecordView.parse((String) filter.remove("view"));
        final String identifier = Optional.ofNullable(requestData.body())
                                          .map(body -> body.getString(RecordPredicate.IDENTIFIER))
                                          .orElse(null);
        filter.put(RecordPredicate.IDENTIFIER, Strings.requireNotBlank(identifier, "Missing record identifier"));
        return controller.getRx().rxGetRecord(RecordPredicate.filter(filter, EventAction.GET_ONE))
                         .map(RecordTransformer.create(view)::transform)
                         .switchIfEmpty(Single.error(new NotFoundException("Not found")));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(@NonNull RequestData requestData) {
        JsonObject filter = requestData.getFilter();
        ServiceDiscoveryController controller = getController(filter);
        RecordTransformer transformer = RecordTransformer.create(RecordView.END_USER);
        return controller.getRx().rxGetRecords(RecordPredicate.filter(filter, EventAction.GET_LIST))
                         .flatMap(records -> Observable.fromIterable(records).map(transformer::transform).toList())
                         .map(records -> new JsonObject().put("apis", new JsonArray(records)));
    }

    private ServiceDiscoveryController getController(JsonObject filter) {
        ServiceDiscoveryKind scope = ServiceDiscoveryKind.parse((String) filter.remove("kind"));
        return ServiceDiscoveryKind.LOCAL == scope ? context.getLocalController() : context.getClusterController();
    }

}
