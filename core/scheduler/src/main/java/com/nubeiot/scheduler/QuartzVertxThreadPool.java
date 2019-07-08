package com.nubeiot.scheduler;

import java.util.Objects;

import org.quartz.spi.ThreadPool;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.scheduler.SchedulerConfig.WorkerPoolConfig;

import lombok.NonNull;

public final class QuartzVertxThreadPool implements ThreadPool {

    private final WorkerExecutor worker;
    private final EventController controller;
    private final WorkerPoolConfig config;
    private final String monitorAddress;

    QuartzVertxThreadPool(@NonNull Vertx vertx, String sharedKey, String monitorAddress, WorkerPoolConfig config) {
        this.worker = vertx.createSharedWorkerExecutor(config.getPoolName(), config.getPoolSize(),
                                                       config.getMaxExecuteTime(), config.getMaxExecuteTimeUnit());
        this.controller = SharedDataDelegate.getEventController(vertx, sharedKey);
        this.config = config;
        this.monitorAddress = monitorAddress;
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        if (Objects.isNull(runnable)) {
            return false;
        }
        worker.executeBlocking(future -> run(runnable, future), this::asyncResultHandler);
        return true;
    }

    @Override
    public int blockForAvailableThreads() { return 1; }

    @Override
    public void initialize() { }

    @Override
    public void shutdown(boolean waitForJobsToComplete) { worker.close(); }

    @Override
    public int getPoolSize() { return this.config.getPoolSize(); }

    @Override
    public void setInstanceId(String schedInstId) { }

    @Override
    public void setInstanceName(String schedName) { }

    private void run(Runnable runnable, Future<Object> future) {
        runnable.run();
        future.complete();
    }

    private void asyncResultHandler(AsyncResult<Object> result) {
        JsonObject data = result.failed()
                          ? ErrorMessage.parse(result.cause()).toJson()
                          : Objects.isNull(result.result()) ? null : JsonData.tryParse(result.result()).toJson();
        controller.request(DeliveryEvent.builder()
                                        .address(monitorAddress)
                                        .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                        .action(EventAction.MONITOR)
                                        .payload(data)
                                        .build());
    }

}
