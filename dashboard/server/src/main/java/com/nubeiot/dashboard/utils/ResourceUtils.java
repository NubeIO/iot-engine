package com.nubeiot.dashboard.utils;

import javax.validation.constraints.NotNull;

import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.base.Urls;

/**
 * @deprecated
 */
public class ResourceUtils {

    public static String buildAbsolutePath(@NotNull String host, @NotNull String mediaRoot,
                                           @NotNull String relativePath) {
        return Urls.optimizeURL(RegisterScheme.getInstance().getHttpScheme().getScheme() + "://" + host,
                                Urls.combinePath(mediaRoot, relativePath));
    }

}
