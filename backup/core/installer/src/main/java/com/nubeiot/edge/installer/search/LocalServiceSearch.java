package com.nubeiot.edge.installer.search;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import io.github.zero88.utils.DateTimes;
import io.github.zero88.utils.DateTimes.Iso8601Parser;
import io.github.zero88.utils.Strings;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.Tables;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;
import com.nubeiot.edge.installer.model.tables.records.TblModuleRecord;

import lombok.NonNull;

public final class LocalServiceSearch implements IServiceSearch {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstallerEntityHandler entityHandler;

    public LocalServiceSearch(@NonNull InstallerEntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    @Override
    public Single<JsonObject> search(RequestData requestData) throws NubeException {
        logger.info("Start executing local service searching {}", requestData.filter());
        return this.entityHandler.genericQuery()
                                 .executeAny(
                                     context -> filter(validateFilter(requestData.filter()), requestData.pagination(),
                                                       context))
                                 .flattenAsObservable(records -> records)
                                 .flatMapSingle(this::excludeData)
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put("services", results));
    }

    private Single<JsonObject> excludeData(TblModuleRecord rec) {
        final JsonObject cfg = PreDeploymentResult.filterOutSensitiveConfig(rec.getServiceId(), rec.getAppConfig());
        return Single.just(rec.setAppConfig(cfg).setSystemConfig(null).toJson());
    }

    private JsonObject validateFilter(JsonObject filter) {
        //TODO fields name, depends object -> validate method
        JsonObject sqlData = new JsonObject(filter.getMap());
        String state = filter.getString(Tables.TBL_MODULE.STATE.getName().toLowerCase());
        if (Strings.isNotBlank(state)) {
            try {
                sqlData.put(Tables.TBL_MODULE.STATE.getName().toLowerCase(), State.valueOf(state));
            } catch (IllegalArgumentException e) {
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid state", e);
            }
        }

        //TODO from/to + "_" + table fields name https://github.com/NubeIO/iot-engine/issues/244
        String from = filter.getString("from");
        String to = filter.getString("to");
        if (Strings.isNotBlank(from)) {
            sqlData.put("from", Iso8601Parser.parse(from));
        }
        if (Strings.isNotBlank(to)) {
            sqlData.put("to", Iso8601Parser.parse(to));
        }
        return sqlData;
    }

    @SuppressWarnings( {"unchecked", "rawtypes"})
    private List<TblModuleRecord> filter(JsonObject filter, Pagination pagination, DSLContext context) {
        SelectConditionStep<TblModuleRecord> sql = context.selectFrom(Tables.TBL_MODULE)
                                                          .where(DSL.field(Tables.TBL_MODULE.SERVICE_TYPE)
                                                                    .eq(ModuleType.JAVA));
        Set<String> fieldNames = Arrays.stream(Tables.TBL_MODULE.fields())
                                       .map(Field::getName)
                                       .collect(Collectors.toSet());
        filter.getMap()
              .entrySet()
              .parallelStream()
              .filter(entry -> fieldNames.contains(entry.getKey()))
              .forEach(entry -> {
                  Field field = Tables.TBL_MODULE.field(entry.getKey());
                  sql.and(field.eq(entry.getValue()));
              });
        final Instant from = filter.getInstant("from");
        if (Objects.nonNull(from)) {
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).gt(DateTimes.from(from)));
        }

        final Instant to = filter.getInstant("to");
        if (Objects.nonNull(to)) {
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).lt(DateTimes.from(to)));
        }

        return sql.limit(pagination.getPerPage())
                  .offset(((pagination.getPage() - 1) * pagination.getPerPage()))
                  .fetchInto(TblModuleRecord.class);
    }

}
