package com.nubeiot.edge.core.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
import com.nubeiot.core.exceptions.ErrorMessage;
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
public final class PostDeploymentService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostDeploymentService.class);
    private final InstallerEntityHandler entityHandler;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.MONITOR);
    }

    @EventContractor(action = EventAction.MONITOR)
    public boolean success(@Param("result") PreDeploymentResult result, @Param("error") JsonObject error) {
        if (Objects.isNull(error) || error.isEmpty() || Strings.isBlank(result.getDeployId())) {
            errorPostDeployment(result.getServiceId(), result.getTransactionId(), result.getAction(),
                                ErrorMessage.parse(error));
            return true;
        }
        succeedPostDeployment(result.getServiceId(), result.getTransactionId(), result.getAction(),
                              result.getDeployId(), result.getTargetState());
        return true;
    }

    private void succeedPostDeployment(String serviceId, String transId, EventAction eventAction, String deployId,
                                       State targetState) {
        LOGGER.info("Handle entities after success deployment...");
        final Status status = Status.SUCCESS;
        final State state = StateMachine.instance().transition(eventAction, status, targetState);
        if (State.UNAVAILABLE == state) {
            final TblTransactionDao dao = entityHandler.getTransDao();
            LOGGER.info("Remove module id {} and its transactions", serviceId);
            dao.findOneById(transId)
               .flatMap(o -> createRemovedServiceRecord(
                   o.orElse(new TblTransaction().setTransactionId(transId).setModuleId(serviceId).setEvent(eventAction))
                    .setStatus(status)))
               .map(history -> dao)
               .flatMap(transDao -> transDao.deleteByCondition(Tables.TBL_TRANSACTION.MODULE_ID.eq(serviceId))
                                            .flatMap(ignore -> entityHandler.getModuleDao().deleteById(serviceId)))
               .subscribe();
        } else {
            Map<TableField, String> v = Collections.singletonMap(Tables.TBL_MODULE.DEPLOY_ID, deployId);
            final JDBCRXGenericQueryExecutor queryExecutor = entityHandler.genericQuery();
            queryExecutor.executeAny(c -> updateTransStatus(c, transId, status, null))
                         .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, serviceId, state, v))
                                                     .map(r2 -> r1 + r2))
                         .subscribe();
        }
    }

    //TODO: register EventBus to send message somewhere
    private void errorPostDeployment(String serviceId, String transId, EventAction action, ErrorMessage error) {
        LOGGER.error("Handle entities after error deployment...");
        final JDBCRXGenericQueryExecutor queryExecutor = entityHandler.genericQuery();
        queryExecutor.executeAny(c -> updateTransStatus(c, transId, Status.FAILED,
                                                        Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR,
                                                                                 error.toJson())))
                     .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, serviceId, State.DISABLED, null))
                                                 .map(r2 -> r1 + r2))
                     .subscribe();
    }

    private Single<ITblRemoveHistory> createRemovedServiceRecord(ITblTransaction transaction) {
        LOGGER.info("Create History record...");
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
