package com.nubeiot.edge.connector.datapoint;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Table;

import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityAuditHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.pojos.Device;
import com.nubeiot.iotdata.model.tables.pojos.DeviceEquip;
import com.nubeiot.iotdata.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.model.tables.pojos.Network;
import com.nubeiot.iotdata.model.tables.pojos.Point;
import com.nubeiot.iotdata.model.tables.pojos.Thing;
import com.nubeiot.iotdata.model.tables.pojos.Transducer;

import lombok.NonNull;

class DataPointEntityHandler extends EntityHandler implements EntityAuditHandler {

    public static final String BUILTIN_DATA = "BUILTIN_DATA";

    private static final Map<Class<? extends VertxPojo>, Integer> DEPENDENCIES = initDependencies();

    DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    private static Map<Class<? extends VertxPojo>, Integer> initDependencies() {
        Map<Class<? extends VertxPojo>, Integer> map = new HashMap<>();
        map.put(MeasureUnit.class, 10);
        map.put(Device.class, 10);
        map.put(Equipment.class, 10);
        map.put(Transducer.class, 10);
        map.put(Network.class, 20);
        map.put(Thing.class, 20);
        map.put(DeviceEquip.class, 30);
        map.put(Point.class, 40);
        return map;
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.POINT);
    }

    @Override
    public Single<EventMessage> initData() {
        Map<Table, Field<UUID>> map = new HashMap<>();
        map.put(Tables.DEVICE, Tables.DEVICE.ID);
        map.put(Tables.EQUIPMENT, Tables.EQUIPMENT.ID);
        map.put(Tables.NETWORK, Tables.NETWORK.ID);
        map.put(Tables.POINT, Tables.POINT.ID);
        map.put(Tables.TRANSDUCER, Tables.TRANSDUCER.ID);
        createDefaultUUID(map);
        return initDataFromConfig();
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.initial(EventAction.MIGRATE));
    }

    private Single<EventMessage> initDataFromConfig() {
        return Optional.ofNullable((JsonObject) this.getSharedDataFunc().apply(BUILTIN_DATA))
                       .map(data -> Single.merge(
                           ReflectionClass.stream(Device.class.getPackage().getName(), VertxPojo.class,
                                                  ReflectionClass.publicClass())
                                          .filter(c -> data.containsKey(jsonKey(c)))
                                          .sorted(Comparator.comparingInt(o -> DEPENDENCIES.getOrDefault(o, 999)))
                                          .map(c -> insert(data, c))
                                          .collect(Collectors.toList()))
                                          .buffer(5)
                                          .reduce(0, (i, r) -> i + r.stream().reduce(0, Integer::sum))
                                          .map(r -> EventMessage.success(EventAction.INIT,
                                                                         new JsonObject().put("records", r))))
                       .orElseGet(() -> Single.just(
                           EventMessage.success(EventAction.INIT, new JsonObject().put("records", 0))));
    }

    private String jsonKey(Class<VertxPojo> c) {
        return Strings.toSnakeCaseLC(c.getSimpleName());
    }

    private Single<Integer> insert(@NonNull JsonObject builtinData, @NonNull Class<VertxPojo> pojoClass) {
        final Object data = builtinData.getValue(jsonKey(pojoClass));
        return findDao(pojoClass).map(dao -> insert(dao, pojoClass, data)).orElseGet(() -> Single.just(0));
    }

    @SuppressWarnings("unchecked")
    private Single<Integer> insert(AbstractVertxDAO dao, @NonNull Class<VertxPojo> pojoClass, @NonNull Object data) {
        if (parsable(pojoClass, data)) {
            return ((Single<Integer>) dao.insert(
                EntityAuditHandler.addCreationAudit(true, parse(pojoClass, data), "SYSTEM_INITIATOR"))).doOnSuccess(
                initLog(pojoClass));
        }
        if (data instanceof JsonArray || data instanceof Collection) {
            final Stream<Object> stream = data instanceof JsonArray
                                          ? ((JsonArray) data).stream()
                                          : ((Collection) data).stream();
            return ((Single<Integer>) dao.insert(stream.filter(o -> parsable(pojoClass, o))
                                                       .map(o -> parse(pojoClass, o))
                                                       .map(pojo -> EntityAuditHandler.addCreationAudit(true, pojo,
                                                                                                        "SYSTEM_INITIATOR"))
                                                       .collect(Collectors.toList()))).doOnSuccess(initLog(pojoClass));
        }
        return Single.just(0);
    }

    private boolean parsable(@NonNull Class<VertxPojo> pojoClass, @NonNull Object data) {
        return data instanceof JsonObject || data instanceof Map || pojoClass.isInstance(data);
    }

    private Consumer<Integer> initLog(@NonNull Class<VertxPojo> pojoClass) {
        return r -> logger.info("Inserted {} record(s) in {}", r, pojoClass.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private Optional<AbstractVertxDAO> findDao(@NonNull Class<VertxPojo> pojoClass) {
        try {
            final Class daoClass = Class.forName(pojoClass.getName().replaceAll("pojos", "daos") + "Dao");
            if (ReflectionClass.assertDataType(daoClass, AbstractVertxDAO.class)) {
                return Optional.ofNullable((AbstractVertxDAO) getDao(daoClass));
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Not found DAO of pojo {}", e, pojoClass);
        }
        return Optional.empty();
    }

}
