package com.nubeiot.core.utils;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.reactivex.RxHelper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorHelpers {

    public static <T> Single<T> blocking(@NonNull Vertx vertx, @NonNull Callable<T> callable) {
        return Single.fromCallable(callable).subscribeOn(RxHelper.blockingScheduler(vertx));
    }

}
