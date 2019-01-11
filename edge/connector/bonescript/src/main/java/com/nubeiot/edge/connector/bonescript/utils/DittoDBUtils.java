package com.nubeiot.edge.connector.bonescript.utils;

import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.connector.bonescript.model.Tables;
import com.nubeiot.edge.connector.bonescript.model.tables.daos.TblDittoDao;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class DittoDBUtils {

    public static Single<JsonObject> getDittoData(TblDittoDao tblDittoDao) {
        return tblDittoDao.findOneById(1)
                          .map(optional -> optional.map(TblDitto::getValue))
                          .map(o -> o.orElseThrow(() -> new NotFoundException("Value doesn't exist on DB!")))
                          .map(JsonObject::new);
    }

    public static int updateDittoData(DSLContext context, JsonObject value) {
        return context.update(Tables.TBL_DITTO)
                      .set(Tables.TBL_DITTO.VALUE, value.encode())
                      .where(Tables.TBL_DITTO.ID.eq(1))
                      .execute();
    }

}
