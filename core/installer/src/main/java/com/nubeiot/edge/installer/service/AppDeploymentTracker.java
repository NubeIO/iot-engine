package com.nubeiot.edge.installer.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.TableField;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.Tables;
import com.nubeiot.edge.installer.model.dto.PostDeploymentResult;
import com.nubeiot.edge.installer.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.installer.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.installer.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.installer.model.tables.pojos.TblRemoveHistory;
import com.nubeiot.edge.installer.model.tables.pojos.TblTransaction;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AppDeploymentTracker implements DeploymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentTracker.class);
    private final InstallerEntityHandler entityHandler;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.MONITOR);
    }

    @EventContractor(action = EventAction.MONITOR, returnType = Single.class)
    public Single<PostDeploymentResult> handle(@Param("result") PostDeploymentResult result) {
        Single<PostDeploymentResult> last = Status.FAILED == result.getStatus()
                                            ? handleError(result)
                                            : handleSuccess(result);
        final EventController client = sharedData(SharedDataDelegate.SHARED_EVENTBUS);
        final AppDeployer deployer = sharedData(InstallerEntityHandler.SHARED_APP_DEPLOYER_CFG);
        return last.doOnSuccess(res -> client.request(
            DeliveryEvent.from(deployer.getFinisherEvent(), new JsonObject().put("result", res.toJson()))));
    }

    private Single<PostDeploymentResult> handleSuccess(@NonNull PostDeploymentResult res) {
        LOGGER.info("INSTALLER::Handle entities after success deployment...");
        final Status status = res.getStatus();
        final State state = StateMachine.instance().transition(res.getAction(), status, res.getToState());
        final String serviceId = res.getServiceId();
        if (State.UNAVAILABLE == state) {
            final TblTransactionDao dao = entityHandler.transDao();
            LOGGER.info("INSTALLER::Removing service '{}' and its transactions...", serviceId);
            return dao.findOneById(res.getTransactionId())
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .defaultIfEmpty(new TblTransaction().setTransactionId(res.getTransactionId())
                                                          .setModuleId(serviceId)
                                                          .setEvent(res.getAction()))
                      .flatMapSingle(r -> createHistoryRecord(r.setStatus(status)))
                      .flatMap(his -> dao.deleteByCondition(Tables.TBL_TRANSACTION.MODULE_ID.eq(serviceId)))
                      .flatMap(r -> entityHandler.moduleDao().deleteById(serviceId).map(n -> r + n + 1))
                      .map(records -> PostDeploymentResult.from(res, state, records));
        }
        Map<TableField, String> v = Collections.singletonMap(Tables.TBL_MODULE.DEPLOY_ID, res.getDeployId());
        final JDBCRXGenericQueryExecutor queryExecutor = entityHandler.genericQuery();
        return queryExecutor.executeAny(c -> updateTransStatus(c, res.getTransactionId(), status, null))
                            .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, serviceId, state, v))
                                                        .map(r2 -> r1 + r2))
                            .map(records -> PostDeploymentResult.from(res, state, records));
    }

    private Single<PostDeploymentResult> handleError(@NonNull PostDeploymentResult res) {
        LOGGER.error("INSTALLER::Handle entities after error deployment...");
        final JDBCRXGenericQueryExecutor query = entityHandler.genericQuery();
        final Map<?, ?> values = Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR, res.getError());
        return query.executeAny(c -> updateTransStatus(c, res.getTransactionId(), Status.FAILED, values))
                    .flatMap(r1 -> query.executeAny(c -> updateModuleState(c, res.getServiceId(), State.DISABLED, null))
                                        .map(r2 -> r1 + r2))
                    .map(records -> PostDeploymentResult.from(res, State.DISABLED, records));
    }

    private Single<ITblRemoveHistory> createHistoryRecord(ITblTransaction transaction) {
        ITblRemoveHistory history = this.convertToHistory(transaction);
        return entityHandler.dao(TblRemoveHistoryDao.class).insert((TblRemoveHistory) history).map(i -> history);
    }

    private int updateModuleState(DSLContext context, String serviceId, State state, Map<?, ?> values) {
        return context.update(Tables.TBL_MODULE)
                      .set(Tables.TBL_MODULE.STATE, state)
                      .set(Tables.TBL_MODULE.MODIFIED_AT, DateTimes.now())
                      .set(Objects.isNull(values) ? new HashMap<>() : values)
                      .where(Tables.TBL_MODULE.SERVICE_ID.eq(serviceId))
                      .execute();
    }

    private int updateTransStatus(DSLContext context, String transId, Status status, Map<?, ?> values) {
        return context.update(Tables.TBL_TRANSACTION)
                      .set(Tables.TBL_TRANSACTION.STATUS, status)
                      .set(Tables.TBL_TRANSACTION.MODIFIED_AT, DateTimes.now())
                      .set(Objects.isNull(values) ? new HashMap<>() : values)
                      .where(Tables.TBL_TRANSACTION.TRANSACTION_ID.eq(transId))
                      .execute();
    }

    private ITblRemoveHistory convertToHistory(ITblTransaction transaction) {
        ITblRemoveHistory history = new TblRemoveHistory().fromJson(transaction.toJson());
        if (Objects.isNull(history.getIssuedAt())) {
            history.setIssuedAt(DateTimes.now());
        }
        if (Objects.isNull(history.getModifiedAt())) {
            history.setModifiedAt(DateTimes.now());
        }
        if (Objects.isNull(history.getRetry())) {
            history.setRetry(0);
        }
        return history;
    }

    @Override
    public <D> D sharedData(String dataKey) {
        return entityHandler.sharedData(dataKey);
    }

}
