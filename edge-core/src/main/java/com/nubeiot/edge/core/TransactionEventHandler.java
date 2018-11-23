package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.model.gen.Tables;

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
