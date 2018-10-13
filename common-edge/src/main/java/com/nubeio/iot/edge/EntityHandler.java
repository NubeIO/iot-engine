package com.nubeio.iot.edge;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jooq.Condition;
import org.jooq.DSLContext;

import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.StateMachine;
import com.nubeio.iot.edge.model.gen.Tables;
import com.nubeio.iot.edge.model.gen.tables.daos.TblModuleDao;
import com.nubeio.iot.edge.model.gen.tables.daos.TblTransactionDao;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction;
import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.ErrorMessage;
import com.nubeio.iot.share.exceptions.StateException;
import com.nubeio.iot.share.utils.DateTimes;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.MODULE)
public class EntityHandler {

    private static final Logger logger = LoggerFactory.getLogger(EntityHandler.class);
    private final Supplier<TblModuleDao> moduleDaoSupplier;
    private final Supplier<TblTransactionDao> transDaoSupplier;
    private final Supplier<JDBCRXGenericQueryExecutor> executorSupplier;

    public Single<List<TblModule>> findModulesInBootstrap() {
        return moduleDaoSupplier.get().findManyByState(Collections.singletonList(State.ENABLED));
    }

    public Single<Boolean> isFreshInstall() {
        return executorSupplier.get()
                               .execute(context -> context.fetchCount(Tables.TBL_MODULE))
                               .map(count -> count == 0);
    }

    public Single<Optional<TblModule>> findModuleByNameAndType(String serviceName, ModuleType moduleType) {
        final Condition condition = Tables.TBL_MODULE.SERVICE_NAME.eq(serviceName)
                                                                  .and(Tables.TBL_MODULE.SERVICE_TYPE.eq(moduleType));
        return moduleDaoSupplier.get().findOneByCondition(condition);
    }

    public Single<Optional<TblModule>> findModuleById(String serviceId) {
        return moduleDaoSupplier.get().findOneById(serviceId);
    }

    public Single<Optional<TblModule>> validateModuleState(String serviceId, EventType eventType) {
        logger.info("Hey: {} - {}", serviceId, eventType);
        return moduleDaoSupplier.get().findOneById(serviceId).map(o -> validateModuleState(o, eventType));
    }

    public Single<Optional<TblModule>> validateModuleState(String serviceName, ModuleType moduleType,
                                                           EventType eventType) {
        final Condition condition = Tables.TBL_MODULE.SERVICE_NAME.eq(serviceName)
                                                                  .and(Tables.TBL_MODULE.SERVICE_TYPE.eq(moduleType));
        return moduleDaoSupplier.get().findOneByCondition(condition).map(o -> validateModuleState(o, eventType));
    }

    public Single<String> handlePreDeployment(TblModule module, EventType eventType) {
        logger.info("Handle entities before do deployment...");
        return validateModuleState(module.getServiceId(), eventType).flatMap(oldOne -> {
            if (EventType.INIT == eventType || EventType.CREATE == eventType) {
                return markModuleInsert(module).flatMap(key -> createTransaction(key.getServiceId(), eventType, null));
            }
            if (EventType.UPDATE == eventType || EventType.HALT == eventType) {
                return markModuleUpdate(module, oldOne.orElseThrow(() -> new StateException("No way"))).flatMap(
                        key -> createTransaction(key.getServiceId(), eventType, oldOne.get().toJson()));
            }
            if (EventType.REMOVE == eventType) {
                return markModuleDelete(oldOne.orElseThrow(() -> new StateException("No way"))).flatMap(
                        key -> createTransaction(key.getServiceId(), eventType, oldOne.get().toJson()));
            }
            throw new UnsupportedOperationException("Unsupported event " + eventType);
        });
    }

    //TODO: register EventBus to send message somewhere
    public void succeedPostDeployment(String serviceId, String transId, EventType eventType, String deployId) {
        logger.info("Handle entities after success deployment...");
        final Status status = Status.SUCCESS;
        final State state = StateMachine.transition(eventType, status);
        if (State.UNAVAILABLE == state) {
            logger.info("Remove module id {} and its transactions", serviceId);
            transDaoSupplier.get()
                            .deleteByCondition(Tables.TBL_TRANSACTION.MODULE_ID.eq(serviceId))
                            .doOnSuccess(ignore -> moduleDaoSupplier.get().deleteById(serviceId))
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
    public void handleErrorPostDeployment(String serviceId, String transId, EventType eventType, Throwable error) {
        logger.info("Handle entities after error deployment...");
        JDBCRXGenericQueryExecutor queryExecutor = executorSupplier.get();
        queryExecutor.execute(c -> updateTransStatus(c, transId, Status.FAILED,
                                                     Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR_JSON,
                                                                              ErrorMessage.parse(error).toJson())))
                     .flatMap(r1 -> queryExecutor.execute(c -> updateModuleState(c, serviceId, State.DISABLED, null))
                                                 .map(r2 -> r1 + r2))
                     .subscribe();
    }

    private Single<String> createTransaction(String moduleId, EventType eventType, JsonObject prevState) {
        final Date now = DateTimes.now();
        final String transactionId = UUID.randomUUID().toString();
        final TblTransaction transaction = new TblTransaction().setTransactionId(transactionId)
                                                               .setModuleId(moduleId)
                                                               .setStatus(Status.WIP)
                                                               .setEvent(eventType)
                                                               .setIssuedAt(now)
                                                               .setModifiedAt(now)
                                                               .setRetry(0)
                                                               .setPrevStateJson(prevState);
        return transDaoSupplier.get().insert(transaction).map(i -> transactionId);
    }

    private Single<TblModule> markModuleInsert(TblModule module) {
        final Date now = DateTimes.now();
        return moduleDaoSupplier.get()
                                .insert(module.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                                .map(i -> module);
    }

    private Single<TblModule> markModuleUpdate(TblModule module, TblModule oldOne) {
        return moduleDaoSupplier.get()
                                .update(module.into(oldOne).setState(State.PENDING).setModifiedAt(DateTimes.now()))
                                .map(ignore -> oldOne);
    }

    private Single<TblModule> markModuleDelete(TblModule module) {
        return moduleDaoSupplier.get()
                                .update(module.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                                .map(ignore -> module);
    }

    private Optional<TblModule> validateModuleState(Optional<TblModule> findModule, EventType eventType) {
        logger.info("Validate module state...");
        StateMachine.validate(findModule, eventType, "module");
        findModule.ifPresent(module -> StateMachine.validateConflict(module.getState(), eventType,
                                                                     "module " + module.getServiceId()));
        return findModule;
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

}
