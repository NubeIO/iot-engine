package com.nubeiot.edge.module.installer;

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
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

import lombok.Getter;
import lombok.NonNull;

public final class LastTransactionEventHandler implements EventHandler {

    private final EdgeVerticle verticle;
    @Getter
    private final List<EventAction> availableEvents;

    LastTransactionEventHandler(@NonNull EdgeVerticle verticle, @NonNull EventModel eventModel) {
        this.verticle = verticle;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        boolean lastTransaction = "true".equals(filter.getString("last"));
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getModuleId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Module Id cannot be blank");
        }
        if (lastTransaction) {
            return this.verticle.getEntityHandler()
                                .findOneTransactionByModuleId(transaction.getModuleId())
                                .map(o -> o.orElseThrow(() -> new NotFoundException(
                                    String.format("Not found module_id '%s'", transaction.getModuleId()))));
        } else {
            return this.verticle.getEntityHandler()
                                .findTransactionByModuleId(transaction.getModuleId())
                                .map(transactions -> new JsonObject().put("transactions", transactions));
        }
    }

}
