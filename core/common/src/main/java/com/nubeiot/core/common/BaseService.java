package com.nubeiot.core.common;

import io.reactivex.Single;

/**
 * For what??
 */
@Deprecated
public interface BaseService<J extends BaseService> {
    Single<J> initializeService();
}
