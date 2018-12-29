package com.nubeiot.edge.core;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jooq.DSLContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.model.gen.Tables;
import com.nubeiot.edge.core.model.gen.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.gen.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.gen.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.gen.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.gen.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblTransaction;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.MODULE)
public final class EntityHandler {

    private static final Logger logger = LoggerFactory.getLogger(EntityHandler.class);
    private final Supplier<TblModuleDao> moduleDaoSupplier;
    private final Supplier<TblTransactionDao> transDaoSupplier;
    private final Supplier<TblRemoveHistoryDao> historyDaoSupplier;
    private final Supplier<JDBCRXGenericQueryExecutor> executorSupplier;

    public Single<List<TblModule>> getModulesWhenBootstrap() {
        return moduleDaoSupplier.get().findManyByState(Collections.singletonList(State.ENABLED));
    }

    public Single<Boolean> isFreshInstall() {
        return executorSupplier.get()
                               .execute(context -> context.fetchCount(Tables.TBL_MODULE))
                               .map(count -> count == 0);
    }

    public Single<Optional<JsonObject>> findModuleById(String serviceId) {
        return moduleDaoSupplier.get().findOneById(serviceId).map(optional -> optional.map(ITblModule::toJson));
    }

    public Single<Optional<JsonObject>> findTransactionById(String transactionId) {
        return transDaoSupplier.get()
                               .findOneById(transactionId)
                               .flatMap(optional -> optional.isPresent()
                                                    ? Single.just(Optional.of(optional.get().toJson()))
                                                    : this.findHistoryTransactionById(transactionId));
    }

    Single<PreDeploymentResult> handlePreDeployment(TblModule module, EventAction event) {
        logger.info("Handle entities before do deployment...");
        return validateModuleState(module.getServiceId(), event).flatMap(o -> {
            if (EventAction.INIT == event || EventAction.CREATE == event) {
                module.setState(State.NONE);
                return markModuleInsert(new TblModule(module)).flatMap(
                        key -> createTransaction(key.getServiceId(), event, module.toJson()).map(
                                transId -> createPreDeployResult(module, transId, event, module.getState())));
            }
            final TblModule oldOne = o.orElseThrow(() -> new NotFoundException(""));
            final boolean isUpdated = EventAction.UPDATE == event;
            if (isUpdated || EventAction.HALT == event) {
                return markModuleModify(module, new TblModule(oldOne), isUpdated).flatMap(
                        key -> createTransaction(key.getServiceId(), event, oldOne.toJson()).map(
                                transId -> createPreDeployResult(key, transId, event, oldOne.getState())));
            }
            if (EventAction.REMOVE == event) {
                return markModuleDelete(new TblModule(oldOne)).flatMap(
                        key -> createTransaction(key.getServiceId(), event, oldOne.toJson()).map(
                                transId -> createPreDeployResult(key, transId, event, oldOne.getState())));
            }
            throw new UnsupportedOperationException("Unsupported event " + event);
        });
    }

    private PreDeploymentResult createPreDeployResult(TblModule module, String transactionId, EventAction event,
                                                      State prevState) {
        return PreDeploymentResult.builder()
                                  .transactionId(transactionId)
                                  .action(event)
                                  .prevState(prevState)
                                  .serviceId(module.getServiceId())
                                  .deployId(module.getDeployId())
                                  .deployCfg(module.getDeployConfigJson())
                                  .build();
    }

    private Single<Optional<TblModule>> validateModuleState(String serviceId, EventAction eventAction) {
        logger.info("Validate {}::::{} ...", serviceId, eventAction);
        return moduleDaoSupplier.get()
                                .findOneById(serviceId)
                                .map(o -> validateModuleState(o.orElse(null), eventAction));
    }

    //TODO: register EventBus to send message somewhere
    public void succeedPostDeployment(String serviceId, String transId, EventAction eventAction, String deployId) {
        logger.info("Handle entities after success deployment...");
        final Status status = Status.SUCCESS;
        final State state = StateMachine.instance().transition(eventAction, status);
        if (State.UNAVAILABLE == state) {
            logger.info("Remove module id {} and its transactions", serviceId);
            transDaoSupplier.get()
                            .findOneById(transId)
                            .flatMap(o -> this.createRemovedServiceRecord(o.orElse(
                                    new TblTransaction().setTransactionId(transId)
                                                        .setModuleId(serviceId)
                                                        .setEvent(eventAction)).setStatus(status)))
                            .map(history -> transDaoSupplier.get())
                            .flatMap(transDao -> transDao.deleteByCondition(
                                    Tables.TBL_TRANSACTION.MODULE_ID.eq(serviceId))
                                                         .flatMap(ignore -> moduleDaoSupplier.get()
                                                                                             .deleteById(serviceId)))
                            .subscribe();
        } else {
            JDBCRXGenericQueryExecutor queryExecutor = executorSupplier.get();
            queryExecutor.execute(c -> updateTransStatus(c, transId, status, null))
                         .flatMap(r1 -> queryExecutor.execute(c -> updateModuleState(c, serviceId, state,
                                                                                     Collections.singletonMap(
                                                                                             Tables.TBL_MODULE.DEPLOY_ID,
                                                                                             deployId)))
                                                     .map(r2 -> r1 + r2))
                         .subscribe();
        }
    }

    //TODO: register EventBus to send message somewhere
    public void handleErrorPostDeployment(String serviceId, String transId, EventAction action, Throwable error) {
        logger.error("Handle entities after error deployment...", error);
        JDBCRXGenericQueryExecutor queryExecutor = executorSupplier.get();
        queryExecutor.execute(c -> updateTransStatus(c, transId, Status.FAILED,
                                                     Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR_JSON,
                                                                              ErrorMessage.parse(error).toJson())))
                     .flatMap(r1 -> queryExecutor.execute(c -> updateModuleState(c, serviceId, State.DISABLED, null))
                                                 .map(r2 -> r1 + r2))
                     .subscribe();
    }

    private Single<String> createTransaction(String moduleId, EventAction action, JsonObject prevState) {
        logger.debug("Create new transaction for {}::::{}...", moduleId, action);
        logger.debug("Previous module state: {}", prevState.encode());
        final Date now = DateTimes.now();
        final String transactionId = UUID.randomUUID().toString();
        final TblTransaction transaction = new TblTransaction().setTransactionId(transactionId)
                                                               .setModuleId(moduleId)
                                                               .setStatus(Status.WIP)
                                                               .setEvent(action)
                                                               .setIssuedAt(now)
                                                               .setModifiedAt(now)
                                                               .setRetry(0)
                                                               .setPrevStateJson(prevState);
        return transDaoSupplier.get().insert(transaction).map(i -> transactionId);
    }

    private Single<TblModule> markModuleInsert(TblModule module) {
        logger.debug("Mark service {} to create...", module.getServiceId());
        final Date now = DateTimes.now();
        return moduleDaoSupplier.get()
                                .insert(module.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                                .map(i -> module);
    }

    private Single<TblModule> markModuleModify(TblModule module, TblModule oldOne, boolean isUpdated) {
        //TODO: handleEvent merge data
        logger.debug("Mark service {} to modify...", module.getServiceId());
        final TblModule into = isUpdated ? module.into(oldOne) : oldOne.into(module);
        return moduleDaoSupplier.get()
                                .update(into.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                                .map(ignore -> oldOne);
    }

    private Single<TblModule> markModuleDelete(TblModule module) {
        logger.debug("Mark service {} to delete...", module.getServiceId());
        return moduleDaoSupplier.get()
                                .update(module.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                                .map(ignore -> module);
    }

    private Optional<TblModule> validateModuleState(TblModule findModule, EventAction eventAction) {
        logger.info("StateMachine is validating...");
        StateMachine.instance().validate(findModule, eventAction, "service");
        if (Objects.nonNull(findModule)) {
            StateMachine.instance()
                        .validateConflict(findModule.getState(), eventAction, "service " + findModule.getServiceId());
            return Optional.of(findModule);
        }
        return Optional.empty();
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

    private Single<TblRemoveHistory> createRemovedServiceRecord(TblTransaction transaction) {
        logger.info("Create History record...");
        final TblRemoveHistory history = this.convertToHistory(transaction);
        return historyDaoSupplier.get().insert(history).map(i -> history);
    }

    private TblRemoveHistory convertToHistory(TblTransaction transaction) {
        final TblRemoveHistory history = (TblRemoveHistory) new TblRemoveHistory().fromJson(transaction.toJson());
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

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return historyDaoSupplier.get()
                                 .findOneById(transactionId)
                                 .map(optional -> optional.map(ITblRemoveHistory::toJson));
    }

}
