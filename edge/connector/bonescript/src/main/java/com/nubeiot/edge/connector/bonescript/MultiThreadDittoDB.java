package com.nubeiot.edge.connector.bonescript;

import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.connector.bonescript.model.Tables;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;

import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MultiThreadDittoDB {

    private final BoneScriptEntityHandler entityHandler;

    public JsonObject getDittoData() {
        return this.entityHandler.getTblDittoDao()
                                 .findOneById(1)
                                 .map(optional -> optional.map(TblDitto::getValue))
                                 .map(o -> o.orElseThrow(() -> new NotFoundException("Value doesn't exist on DB!")))
                                 .map(JsonObject::new)
                                 .blockingGet();
    }

    public Integer updateDittoData(JsonObject value) {
        return this.entityHandler.getQueryExecutor()
                                 .executeAny(c -> c.update(Tables.TBL_DITTO)
                                                   .set(Tables.TBL_DITTO.VALUE, value.encode())
                                                   .where(Tables.TBL_DITTO.ID.eq(1))
                                                   .execute())
                                 .blockingGet();
    }

}
