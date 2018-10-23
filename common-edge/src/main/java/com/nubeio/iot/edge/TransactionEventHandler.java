package com.nubeio.iot.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.share.dto.RequestData;
import com.nubeio.iot.share.event.EventContractor;
import com.nubeio.iot.share.event.EventHandler;
import com.nubeio.iot.share.event.EventModel;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.NotFoundException;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

public final class TransactionEventHandler extends EventHandler {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventType> availableEvents;

    public TransactionEventHandler(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(values = EventType.GET_ONE)
    private Single<JsonObject> getOne(RequestData data) {
        String transId = data.getBody().getString(Tables.TBL_TRANSACTION.TRANSACTION_ID.getName());
        if (Strings.isBlank(transId)) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Transaction Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findTransactionById(transId)
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                    String.format("Not found transaction id '%s'", transId))));
    }

}
