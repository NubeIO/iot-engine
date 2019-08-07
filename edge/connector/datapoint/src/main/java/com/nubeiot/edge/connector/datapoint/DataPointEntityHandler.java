package com.nubeiot.edge.connector.datapoint;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Configuration;

import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.pojos.Device;

import lombok.NonNull;

class DataPointEntityHandler extends EntityHandler {

    public static final String BUILTIN_DATA = "BUILTIN_DATA";

    public DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.POINT);
    }

    @Override
    public Single<EventMessage> initData() {
        return setupData();
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.initial(EventAction.MIGRATE));
    }

    Single<EventMessage> setupData() {
        return Optional.ofNullable((JsonObject) this.getSharedDataFunc().apply(BUILTIN_DATA))
                       .map(builtinData -> Single.merge(
                           ReflectionClass.stream(Device.class.getPackage().getName(), VertxPojo.class)
                                          .filter(c -> builtinData.containsKey(c.getName().toLowerCase()))
                                          .map(c -> insert(builtinData, c))
                                          .collect(Collectors.toList()))
                                                 .buffer(5)
                                                 .reduce(0, (i, r) -> i + r.stream().reduce(0, Integer::sum))
                                                 .map(r -> EventMessage.success(EventAction.INIT,
                                                                                new JsonObject().put("records", r))))
                       .orElseGet(() -> Single.just(
                           EventMessage.success(EventAction.INIT, new JsonObject().put("records", 0))));
    }

    private Single<Integer> insert(@NonNull JsonObject builtinData, @NonNull Class<VertxPojo> pojoClass) {
        final Object data = builtinData.getValue(pojoClass.getName().toLowerCase());
        return findDao(pojoClass).map(dao -> insert(dao, pojoClass, data)).orElseGet(() -> Single.just(0));
    }

    @SuppressWarnings("unchecked")
    private Single<Integer> insert(AbstractVertxDAO dao, @NonNull Class<VertxPojo> pojoClass, @NonNull Object data) {
        if (data instanceof JsonObject || data instanceof Map) {
            return ((Single<Integer>) dao.insert(
                ReflectionClass.createObject(pojoClass).fromJson(JsonData.tryParse(data).toJson()))).doOnSuccess(
                r -> logger.info("Insert {} records in {}", r, pojoClass.getSimpleName()));
        }
        if (data instanceof JsonArray || data instanceof Collection) {
            final Stream<Object> stream = data instanceof JsonArray
                                          ? ((JsonArray) data).stream()
                                          : ((Collection) data).stream();
            return ((Single<Integer>) dao.insert(stream.filter(o -> o instanceof JsonObject || o instanceof Map)
                                                       .map(o -> ReflectionClass.createObject(pojoClass)
                                                                                .fromJson(
                                                                                    JsonData.tryParse(data).toJson()))
                                                       .collect(Collectors.toList()))).doOnSuccess(
                r -> logger.info("Insert {} records in {}", r, pojoClass.getSimpleName()));
        }
        return Single.just(0);
    }

    @SuppressWarnings("unchecked")
    private Optional<AbstractVertxDAO> findDao(@NonNull Class<VertxPojo> pojoClass) {
        try {
            final Class daoClass = Class.forName(pojoClass.getSimpleName() + "Dao");
            if (ReflectionClass.assertDataType(daoClass, AbstractVertxDAO.class)) {
                return Optional.ofNullable((AbstractVertxDAO) getDao(daoClass));
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Not found DAO of pojo {}", e, pojoClass);
        }
        return Optional.empty();
    }

}
