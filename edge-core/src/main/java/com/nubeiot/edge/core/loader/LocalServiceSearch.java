package com.nubeiot.edge.core.loader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.EntityHandler;
import com.nubeiot.edge.core.model.gen.Tables;
import com.nubeiot.edge.core.model.gen.tables.records.TblModuleRecord;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.NonNull;

public class LocalServiceSearch implements IServiceSearch {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    private EntityHandler entityHandler;

    public LocalServiceSearch(@NonNull EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    @Override
    public Single<JsonObject> search(RequestData requestData) throws NubeException {
        JsonObject filter = requestData.getFilter();
        String stateParam = filter.getString(Tables.TBL_MODULE.STATE.getName());

        State state;

        if (Strings.isBlank(stateParam)) {
            state = null;
        } else {
            try {
                state = State.valueOf(stateParam);
            } catch (Exception ex) {
                logger.warn("Parsing state: {} error: ", stateParam, ex);
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid state", ex);
            }
        }
        Date fromDate;
        String from = filter.getString("from");

        if (Strings.isBlank(from)) {
            fromDate = null;
        } else {
            try {
                fromDate = sdf.parse(from);
            } catch (ParseException e) {
                logger.warn("Invalid from date :{}", from, e);
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid from date", e);
            }
        }

        Date toDate;
        String to = filter.getString("to");
        if (Strings.isBlank(to)) {
            toDate = null;
        } else {
            try {
                toDate = sdf.parse(to);
            } catch (ParseException e) {
                logger.warn("Invalid to date :{}", to, e);
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid to date", e);
            }
        }

        Pagination pagination = requestData.getPagination();
        
        logger.info("Start executing local service searching  {}", requestData.getFilter());
        return this.entityHandler.getExecutorSupplier().get().execute(context -> {
            SelectConditionStep<Record> sql = context.select().from(Tables.TBL_MODULE).where(
                DSL.field(Tables.TBL_MODULE.SERVICE_TYPE).eq(ModuleType.JAVA));
            if (state != null) {
                sql = sql.and(DSL.field(Tables.TBL_MODULE.STATE).equal(state));
            }

            if (fromDate != null) {
                sql = sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).gt(fromDate));
            }

            if (toDate != null) {
                sql = sql.and(DSL.field(Tables.TBL_MODULE.CREATED_AT).lt(toDate));
            }

            return sql
                .limit(pagination.getPerPage()).offset(
                    ((pagination.getPage() - 1) * pagination.getPerPage())).fetchInto(TblModuleRecord.class);
        }).flattenAsObservable(records -> records).flatMapSingle(record -> Single.just(record.toJson())).collect(
            JsonArray::new, JsonArray::add).map(results -> new JsonObject().put("services", results));
    }

}
