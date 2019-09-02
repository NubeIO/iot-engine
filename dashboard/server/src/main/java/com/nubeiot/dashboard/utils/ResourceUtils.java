package com.nubeiot.dashboard.utils;

import javax.validation.constraints.NotNull;

import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.base.Urls;

import lombok.NonNull;

/**
 * @deprecated
 */
public class ResourceUtils {

    public static String buildAbsolutePath(@NotNull String host, @NonNull String mediaDir, @NotNull String file) {
        return Urls.optimizeURL(RegisterScheme.getInstance().getHttpScheme().getScheme() + "://" + host,
                                Urls.combinePath(mediaDir, file));
    }

}
