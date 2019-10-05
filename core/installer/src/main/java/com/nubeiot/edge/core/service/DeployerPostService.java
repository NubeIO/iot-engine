package com.nubeiot.edge.core.service;

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

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.PreDeploymentResult;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblRemoveHistory;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DeployerPostService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployerPostService.class);
    private final InstallerEntityHandler entityHandler;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.MONITOR);
    }

    @EventContractor(action = EventAction.MONITOR, returnType = Single.class)
    public Single<Integer> success(@Param("result") PreDeploymentResult result, @Param("error") JsonObject error) {
        if (Objects.isNull(error) || error.isEmpty() || Strings.isBlank(result.getDeployId())) {
            return handleError(result.getServiceId(), result.getTransactionId(), result.getAction(), error);
        }
        return handleSuccess(result.getServiceId(), result.getTransactionId(), result.getAction(), result.getDeployId(),
                             result.getTargetState());
    }

    private Single<Integer> handleSuccess(String service, String transId, EventAction action, String deployId,
                                          State targetState) {
        LOGGER.info("Handle entities after success deployment...");
        final Status status = Status.SUCCESS;
        final State state = StateMachine.instance().transition(action, status, targetState);
        if (State.UNAVAILABLE == state) {
            final TblTransactionDao dao = entityHandler.getTransDao();
            LOGGER.info("Removing service '{}' and its transactions...", service);
            return dao.findOneById(transId)
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .defaultIfEmpty(
                          new TblTransaction().setTransactionId(transId).setModuleId(service).setEvent(action))
                      .flatMapSingle(r -> createHistoryRecord(r.setStatus(status)))
                      .doOnSuccess(his -> LOGGER.info("History record for service '{}' and transaction '{}' is created",
                                                      his.getModuleId(), his.getTransactionId()))
                      .flatMap(history -> dao.deleteByCondition(Tables.TBL_TRANSACTION.MODULE_ID.eq(service)))
                      .doOnSuccess(nr -> LOGGER.info("Service '{}' is removed", service))
                      .flatMap(ignore -> entityHandler.getModuleDao().deleteById(service))
                      .doOnSuccess(n -> LOGGER.info("{} transaction records for service '{}' are removed", n, service));
        }
        Map<TableField, String> v = Collections.singletonMap(Tables.TBL_MODULE.DEPLOY_ID, deployId);
        final JDBCRXGenericQueryExecutor queryExecutor = entityHandler.genericQuery();
        return queryExecutor.executeAny(c -> updateTransStatus(c, transId, status, null))
                            .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, service, state, v))
                                                        .map(r2 -> r1 + r2));
    }

    private Single<Integer> handleError(String serviceId, String transId, EventAction action, JsonObject error) {
        LOGGER.error("Handle entities after error deployment...");
        final JDBCRXGenericQueryExecutor query = entityHandler.genericQuery();
        final Map<?, ?> values = Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR, error);
        return query.executeAny(c -> updateTransStatus(c, transId, Status.FAILED, values))
                    .flatMap(r1 -> query.executeAny(c -> updateModuleState(c, serviceId, State.DISABLED, null))
                                        .map(r2 -> r1 + r2));
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
            history.setIssuedAt(DateTimes.now());
        }
        if (Objects.isNull(history.getRetry())) {
            history.setRetry(0);
        }
        return history;
    }

}
