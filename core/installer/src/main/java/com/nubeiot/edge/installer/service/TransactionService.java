package com.nubeiot.edge.installer.service;

import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.installer.model.tables.pojos.TblTransaction;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TransactionService implements InstallerService {

    @NonNull
    private final InstallerEntityHandler entityHandler;

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> getOne(RequestData data) {
        boolean systemCfg = data.filter().parseBoolean("system_cfg");
        ITblTransaction transaction = new TblTransaction().fromJson(data.body());
        if (Strings.isBlank(transaction.getTransactionId())) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Transaction Id cannot be blank");
        }
        return this.entityHandler.findTransactionById(transaction.getTransactionId())
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

}
