package com.nubeiot.edge.core.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
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

public abstract class TransactionService implements InstallerService {

    private final InstallerVerticle verticle;

    public TransactionService(@NonNull InstallerVerticle verticle) {
        this.verticle = verticle;
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
            transaction.remove("prev_system_config");
        }
        return transaction;
    }

    @Override
    public Map<EventAction, HttpMethod> map() {
        return Collections.singletonMap(EventAction.GET_ONE, HttpMethod.GET);
    }

    @Override
    public String servicePath() {
        return "/transaction";
    }

    @Override
    public String paramPath() {
        return "/:transaction_id";
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_ONE);
    }

}
