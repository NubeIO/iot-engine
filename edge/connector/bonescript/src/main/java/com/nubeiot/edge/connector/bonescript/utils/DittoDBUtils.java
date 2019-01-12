package com.nubeiot.edge.connector.bonescript.utils;

import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.connector.bonescript.BoneScriptEntityHandler;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class DittoDBUtils {

    public static Single<JsonObject> getDittoData(BoneScriptEntityHandler entityHandler) {
        return entityHandler.getTblDittoDao()
                            .findOneById(1)
                            .map(optional -> optional.map(TblDitto::getValue))
                            .map(o -> o.orElseThrow(() -> new NotFoundException("Value doesn't exist on DB!")))
                            .map(JsonObject::new);
    }

}
