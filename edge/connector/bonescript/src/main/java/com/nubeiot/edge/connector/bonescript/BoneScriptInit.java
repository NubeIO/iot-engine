package com.nubeiot.edge.connector.bonescript;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bonescript.functions.InitPassword;
import com.nubeiot.edge.connector.bonescript.functions.InitPins;
import com.nubeiot.edge.connector.bonescript.model.tables.daos.TblDittoDao;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoneScriptInit implements Supplier<Single<JsonObject>> {

    private static final Logger logger = LoggerFactory.getLogger(BoneScriptInit.class);
    private static final String DITTO_TEMPLATE_RESOURCE_FILE = "ditto/ditto_template.json";
    private final TblDittoDao tblDittoDao;

    public static JsonObject initDittoTemplate() {
        String dittoTemplate = Strings.convertToString(
            Reflections.staticClassLoader().getResourceAsStream(DITTO_TEMPLATE_RESOURCE_FILE));
        final String deviceId = UUID.randomUUID().toString();
        return new JsonObject(dittoTemplate.replaceAll("<uuid>", deviceId));
    }

    @Override
    public Single<JsonObject> get() {
        logger.info("Fresh Ditto Template instantiation...");
        LocalDateTime now = DateTimes.nowUTC();
        TblDitto tblDitto = new TblDitto().setCreatedAt(now)
                                          .setStartedAt(now)
                                          .setValue(new InitPassword().apply(new InitPins().apply(initDittoTemplate()))
                                                                      .toString());

        return tblDittoDao.insert(tblDitto)
                          .map(ignore -> new JsonObject().put("message",
                                                              "Successfully created the template JSON of ditto"));
    }

}
