package com.nubeiot.edge.core.search;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.EntityHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.gen.Tables;
import com.nubeiot.edge.core.model.gen.tables.records.TblModuleRecord;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access=AccessLevel.PUBLIC)
public final class LocalServiceSearch implements IServiceSearch {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    final static String CREATED_FROM = "from_" + Tables.TBL_MODULE.CREATED_AT.getName();
    final static String CREATED_TO = "to_" + Tables.TBL_MODULE.CREATED_AT.getName();
    final static String MODIFIED_FROM = "from_" + Tables.TBL_MODULE.MODIFIED_AT.getName();
    final static String MODIFIED_TO = "to_" + Tables.TBL_MODULE.MODIFIED_AT.getName();
    
    private final EntityHandler entityHandler;

    @Override
    public Single<JsonObject> search(RequestData requestData) throws NubeException {
        if (this.entityHandler == null) {
            throw new NubeException("Entity handler cannot be null");
        }

        logger.info("Start executing local service searching  {}", requestData.getFilter());
        return this.entityHandler
                .getExecutorSupplier().get().execute(
                        context -> filter(validateFilter(requestData.getFilter()), requestData.getPagination(), context))
                .flattenAsObservable(records -> records).flatMapSingle(record -> Single.just(record.toJson())).collect(
                        JsonArray::new, JsonArray::add).map(results -> new JsonObject().put("services", results));
    }

    JsonObject validateFilter(JsonObject filter) {
        // TODO fields name, depends object -> validate method
        JsonObject sqlData = new JsonObject(filter.getMap());
        String state = filter.getString(Tables.TBL_MODULE.STATE.getName());
        if (Strings.isNotBlank(state)) {
            try {
                sqlData.put(Tables.TBL_MODULE.STATE.getName(), State.valueOf(state));
            } catch (IllegalArgumentException e) {
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid state", e);
            }
        }

        String createdFrom = filter.getString(CREATED_FROM);
        String createdTo = filter.getString(CREATED_TO);
        if (Strings.isNotBlank(createdFrom)) {
            sqlData.put(CREATED_FROM, DateTimes.parseISO8601(createdFrom));
        }
        if (Strings.isNotBlank(createdTo)) {
            sqlData.put(CREATED_TO, DateTimes.parseISO8601(createdTo));
        }
        
        
        String modifiedFrom = filter.getString(MODIFIED_FROM);
        String modifiedTo = filter.getString(MODIFIED_TO);
        if (Strings.isNotBlank(modifiedFrom)) {
            sqlData.put(MODIFIED_FROM, DateTimes.parseISO8601(modifiedFrom));
        }
        if (Strings.isNotBlank(modifiedTo)) {
            sqlData.put(MODIFIED_TO, DateTimes.parseISO8601(modifiedTo));
        }
        sqlData.remove(Tables.TBL_MODULE.CREATED_AT.getName());
        sqlData.remove(Tables.TBL_MODULE.MODIFIED_AT.getName());
        
        return sqlData;
    }

    @SuppressWarnings("unchecked")
    private List<TblModuleRecord> filter(JsonObject filter, Pagination pagination, DSLContext context) {
        final SelectConditionStep<Record> sql = context.select().from(Tables.TBL_MODULE).where(
                DSL.field(Tables.TBL_MODULE.SERVICE_TYPE).eq(ModuleType.JAVA));
        Set<String> fieldNames = Arrays.stream(Tables.TBL_MODULE.fields()).map(Field::getName).collect(
                Collectors.toSet());
        filter.getMap().entrySet().parallelStream().filter(entry -> fieldNames.contains(entry.getKey())).forEach(
                entry -> {
                    Field field = Tables.TBL_MODULE.field(entry.getKey());
                    sql.and(field.eq(entry.getValue()));
                });
        Instant createdFrom = filter.getInstant(CREATED_FROM);
        if (Objects.nonNull(createdFrom)) {
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).gt(Date.from(createdFrom)));
        }

        Instant createdTo = filter.getInstant(CREATED_TO);
        if (Objects.nonNull(createdTo)) {
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).lt(Date.from(createdTo)));
        }
        
        Instant modifiedFrom = filter.getInstant(MODIFIED_FROM);
        if (Objects.nonNull(modifiedFrom)) {
            sql.and(DSL.field(Tables.TBL_MODULE.MODIFIED_AT).gt(Date.from(modifiedFrom)));
        }

        Instant modifiedTo = filter.getInstant(MODIFIED_TO);
        if (Objects.nonNull(modifiedTo)) {
            sql.and(DSL.field(Tables.TBL_MODULE.MODIFIED_AT).lt(Date.from(modifiedTo)));
        }

        return sql
                .limit(pagination.getPerPage()).offset(((pagination.getPage() - 1) * pagination.getPerPage())).fetchInto(
                        TblModuleRecord.class);
    }

}
