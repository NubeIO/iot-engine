package com.nubeiot.dashboard.helpers;

import com.nubeiot.dashboard.utils.MongoResourceUtils;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class SiteHelper {

    private static String LOGO_SM = "logo_sm";
    private static String LOGO_MD = "logo_md";

    public static Single<JsonObject> buildAbs(MongoClient mongoClient, String host, JsonObject site, String mediaRoot) {
        return MongoResourceUtils.putAbsPath(mongoClient, host, site, LOGO_SM, mediaRoot)
                                 .flatMap(ignored -> MongoResourceUtils.putAbsPath(mongoClient, host, site, LOGO_MD,
                                                                                   mediaRoot))
                                 .map(ignored -> site);
    }

}
