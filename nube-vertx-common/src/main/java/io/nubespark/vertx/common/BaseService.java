package io.nubespark.vertx.common;

import io.reactivex.Single;

public interface BaseService<J extends BaseService> {
    Single<J> initializeService();
}
