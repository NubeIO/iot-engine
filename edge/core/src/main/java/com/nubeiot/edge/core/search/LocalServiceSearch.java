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
import lombok.NonNull;

public final class LocalServiceSearch implements IServiceSearch {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private EntityHandler entityHandler;

    public LocalServiceSearch(@NonNull EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    @Override
    public Single<JsonObject> search(RequestData requestData) throws NubeException {
        logger.info("Start executing local service searching  {}", requestData.getFilter());
        return this.entityHandler.getExecutorSupplier()
                                 .get()
                                 .execute(context -> filter(validateFilter(requestData.getFilter()), requestData.getPagination(), context))
                                 .flattenAsObservable(records -> records)
                                 .flatMapSingle(record -> Single.just(record.toJson()))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put("services", results));
    }

    JsonObject validateFilter(JsonObject filter) {
        //TODO fields name, depends object -> validate method
    	JsonObject sqlData = new JsonObject(filter.getMap());
        String state = filter.getString(Tables.TBL_MODULE.STATE.getName());
        if (Strings.isNotBlank(state)) {
            try {
            	sqlData.put(Tables.TBL_MODULE.STATE.getName(), State.valueOf(state));
            } catch (IllegalArgumentException e) {
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid state", e);
            }
        }

        //TODO from/to + "_" + table fields name
        String from = filter.getString("from");
        String to = filter.getString("to");
        if (Strings.isNotBlank(from)) {
        	sqlData.put("from", DateTimes.parseISO8601(from));
        }
        if (Strings.isNotBlank(to)) {
        	sqlData.put("to", DateTimes.parseISO8601(to));
        }
        return sqlData;
    }

    @SuppressWarnings("unchecked")
    private List<TblModuleRecord> filter(JsonObject filter, Pagination pagination, DSLContext context) {
        final SelectConditionStep<Record> sql = context.select()
                                                       .from(Tables.TBL_MODULE)
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
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).gt(Date.from(from)));
        }

        final Instant to = filter.getInstant("to");
        if (Objects.nonNull(to)) {
            sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).lt(Date.from(to)));
        }

        return sql.limit(pagination.getPerPage())
                  .offset(((pagination.getPage() - 1) * pagination.getPerPage()))
                  .fetchInto(TblModuleRecord.class);
    }

}
