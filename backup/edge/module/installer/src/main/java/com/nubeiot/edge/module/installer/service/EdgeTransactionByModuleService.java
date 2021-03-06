package com.nubeiot.edge.module.installer.service;

import java.util.Collection;
import java.util.Collections;

import io.github.zero88.utils.Strings;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.installer.model.tables.pojos.TblTransaction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EdgeTransactionByModuleService implements EdgeInstallerService {

    @NonNull
    private final InstallerEntityHandler entityHandler;

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.filter();
        boolean lastTransaction = Boolean.parseBoolean(filter.getString("last"));
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getModuleId())) {
            throw new IllegalArgumentException("Service id is mandatory");
        }
        if (lastTransaction) {
            return this.entityHandler.findOneTransactionByModuleId(transaction.getModuleId())
                                     .map(o -> o.orElseThrow(() -> new NotFoundException(
                                         String.format("Not found service id '%s'", transaction.getModuleId()))))
                                     .map(this::removePrevSystemConfig)
                                     .map(transactions -> new JsonObject().put("transactions",
                                                                               new JsonArray().add(transactions)));
        }
        return this.entityHandler.findTransactionByModuleId(transaction.getModuleId())
                                 .flattenAsObservable(transactions -> transactions)
                                 .flatMapSingle(trans -> Single.just(removePrevSystemConfig(trans.toJson())))
                                 .toList()
                                 .map(transactions -> new JsonObject().put("transactions", transactions));
    }

    private JsonObject removePrevSystemConfig(JsonObject transaction) {
        transaction.remove("prev_system_config");
        return transaction;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singleton(EventAction.GET_LIST);
    }

    @Override
    public String servicePath() {
        return "/:module_id/transactions";
    }

    @Override
    public String paramPath() {
        return null;
    }

}
