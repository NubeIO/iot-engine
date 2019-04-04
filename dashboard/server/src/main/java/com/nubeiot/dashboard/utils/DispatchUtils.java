package com.nubeiot.dashboard.utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.micro.MicroContext;

public class DispatchUtils {

    // TODO: temporary haven't tested, need its alternative
    public static Single<ResponseData> dispatchRequests(MicroContext microContext, HttpMethod method, String path,
                                                        RequestData requestData) {
        Logger logger = LoggerFactory.getLogger(DispatchUtils.class);
        int initialOffset = 5; // length of `/api/`
        if (path.length() <= initialOffset) {
            return Single.error(new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found"));
        }
        String prefix = (path.substring(initialOffset).split("/"))[0];
        logger.info("Prefix: {}", prefix);
        String newPath = path.substring(initialOffset + prefix.length());
        logger.info("New path: {}", newPath);
        return microContext
            .getClusterController()
            .executeHttpService(r -> prefix.equals(r.getMetadata().getString("api.name")), newPath, method,
                                requestData);
    }

}
