package com.nubeiot.edge.installer.service;

import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.interfaces.IDeployTransaction;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TransactionService implements InstallerService {

    @NonNull
    private final InstallerEntityHandler entityHandler;

    @Override
    public final String servicePath() {
        return "/transaction";
    }

    @Override
    public final String paramPath() {
        return "/:transaction_id";
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_ONE);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        final boolean includeSystemCfg = data.filter().parseBoolean("system_cfg");
        final IDeployTransaction transaction = new DeployTransaction().fromJson(data.body());
        final String transactionId = Strings.requireNotBlank(transaction.getTransactionId(),
                                                             "Transaction Id cannot be blank");
        return this.entityHandler.findTransactionById(transactionId)
                                 .map(o -> o.orElseThrow(() -> new NotFoundException(
                                     Strings.format("Not found transaction id '{0}'", transactionId))))
                                 .map(trans -> removePrevSystemConfig(trans, includeSystemCfg));
    }

    private JsonObject removePrevSystemConfig(JsonObject transaction, boolean includeSystemCfg) {
        if (!includeSystemCfg) {
            transaction.remove("prev_system_config");
        }
        return transaction;
    }

}
