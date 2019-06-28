package com.nubeiot.edge.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

import lombok.Getter;
import lombok.NonNull;

public final class TransactionEventListener implements EventListener {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventAction> availableEvents;

    public TransactionEventListener(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        JsonObject filter = data.getFilter();
        boolean systemCfg = Boolean.parseBoolean(filter.getString("system_cfg"));
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getTransactionId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Transaction Id cannot be blank");
        }
        return this.verticle.getEntityHandler()
                            .findTransactionById(transaction.getTransactionId())
                            .map(o -> o.orElseThrow(() -> new NotFoundException(
                                Strings.format("Not found transaction id '{0}'", transaction.getTransactionId()))))
                            .map(trans -> removePrevSystemConfig(trans, systemCfg));
    }

    private JsonObject removePrevSystemConfig(JsonObject transaction, boolean systemCfg) {
        if (!systemCfg) {
            // TODO: replace with POJO constant later
            transaction.remove("prev_system_config");
        }
        return transaction;
    }

}
