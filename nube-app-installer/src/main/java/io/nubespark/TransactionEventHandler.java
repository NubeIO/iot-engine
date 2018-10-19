package io.nubespark;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.event.IEventHandler;
import com.nubeio.iot.share.event.RequestData;
import com.nubeio.iot.share.exceptions.NotFoundException;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public final class TransactionEventHandler implements IEventHandler {

    private final EdgeVerticle verticle;
    private final Map<EventType, Function<RequestData, Single<JsonObject>>> mapping = new HashMap<>();

    public TransactionEventHandler(EdgeVerticle verticle) {
        this.verticle = verticle;
        EventModel.EDGE_APP_TRANSACTION.getEvents()
                                       .forEach(eventType -> mapping.put(eventType,
                                                                         data -> this.factory(eventType, data)));
    }

    public Single<JsonObject> handle(EventType eventType, RequestData data) throws NubeException {
        return mapping.get(eventType).apply(data);
    }

    private Single<JsonObject> getOne(RequestData data) {
        String transId = data.getBody().getString(Tables.TBL_TRANSACTION.TRANSACTION_ID.getName());
        if (Strings.isBlank(transId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Transaction Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .getTransDaoSupplier()
                            .get()
                            .findOneById(transId)
                            .map(o -> o.orElseThrow(
                                    () -> new NotFoundException(String.format("Not found service id '%s'", transId)))
                                       .toJson());
    }

    //TODO To be removed. Use reflection
    private Single<JsonObject> factory(EventType event, RequestData data) {
        if (EventType.GET_ONE == event) {
            return getOne(data);
        }
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Unsupported action " + event);
    }

}
