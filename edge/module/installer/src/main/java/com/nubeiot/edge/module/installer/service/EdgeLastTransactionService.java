package com.nubeiot.edge.module.installer.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

import lombok.NonNull;

public final class EdgeLastTransactionService implements EdgeInstallerService {

    private final InstallerVerticle verticle;

    EdgeLastTransactionService(@NonNull InstallerVerticle verticle) {
        this.verticle = verticle;
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        JsonObject filter = data.getFilter();
        boolean lastTransaction = Boolean.parseBoolean(filter.getString("last"));
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getModuleId())) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Module Id cannot be blank");
        }
        if (lastTransaction) {
            return this.verticle.getEntityHandler()
                                .findOneTransactionByModuleId(transaction.getModuleId())
                                .map(o -> o.orElseThrow(() -> new NotFoundException(
                                    String.format("Not found module_id '%s'", transaction.getModuleId()))))
                                .map(this::removePrevSystemConfig)
                                .map(transactions -> new JsonObject().put("transactions",
                                                                          new JsonArray().add(transactions)));
        } else {
            return this.verticle.getEntityHandler()
                                .findTransactionByModuleId(transaction.getModuleId())
                                .flattenAsObservable(transactions -> transactions)
                                .flatMapSingle(trans -> Single.just(removePrevSystemConfig(trans.toJson())))
                                .toList()
                                .map(transactions -> new JsonObject().put("transactions", transactions));
        }
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
    public Map<EventAction, HttpMethod> map() {
        return Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET);
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
