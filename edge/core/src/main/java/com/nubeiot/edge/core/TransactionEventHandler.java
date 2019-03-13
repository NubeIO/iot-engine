package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

import lombok.Getter;
import lombok.NonNull;

public final class TransactionEventHandler implements EventHandler {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventAction> availableEvents;

    public TransactionEventHandler(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getTransactionId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Transaction Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findTransactionById(transaction.getTransactionId())
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                String.format("Not found transaction id '%s'", transaction.getTransactionId()))));
    }

}
