package com.nubeiot.core.common.utils.response;

import io.vertx.reactivex.ext.web.RoutingContext;

import com.nubeiot.core.common.utils.StringUtils;

/**
 * Created by topsykretts on 4/26/18.
 */
public class ResponseUtils {
    public static String CONTENT_TYPE = "content-type";
    public static String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    public static String buildAbsoluteUri(RoutingContext ctx, String location) {
        if (StringUtils.isNull(location)) {
            return "";
        }
        return "http://" + ctx.request().host() + location;
    }
}
