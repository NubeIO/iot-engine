package com.nubeiot.scheduler;

import java.util.Objects;

import org.quartz.spi.ThreadPool;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import com.nubeiot.scheduler.SchedulerConfig.WorkerPoolConfig;

import lombok.NonNull;

public final class QuartzVertxThreadPool implements ThreadPool {

    private final WorkerExecutor worker;
    private final WorkerPoolConfig config;

    QuartzVertxThreadPool(@NonNull Vertx vertx, WorkerPoolConfig config) {
        this.worker = vertx.createSharedWorkerExecutor(config.getPoolName(), config.getPoolSize(),
                                                       config.getMaxExecuteTime(), config.getMaxExecuteTimeUnit());
        this.config = config;
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        if (Objects.isNull(runnable)) {
            return false;
        }
        worker.executeBlocking(future -> run(runnable, future), null);
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

}
