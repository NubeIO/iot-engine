package com.nubeiot.scheduler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public final class SchedulerConfig implements IConfig {

    private static final String BASE_ADDRESS = "com.nubeiot.scheduler";
    private String schedulerName;
    private String registerAddress;
    private String monitorAddress;
    @JsonProperty(WorkerPoolConfig.NAME)
    private WorkerPoolConfig workerConfig;

    SchedulerConfig(String schedulerName) {
        this(schedulerName, null, null, null);
    }

    @JsonCreator
    SchedulerConfig(@JsonProperty(value = "schedulerName", required = true) String schedulerName,
                    @JsonProperty("registerAddress") String registerAddress,
                    @JsonProperty("monitorAddress") String monitorAddress,
                    @JsonProperty(WorkerPoolConfig.NAME) WorkerPoolConfig workerConfig) {
        this.schedulerName = schedulerName;
        this.registerAddress = Strings.isBlank(registerAddress)
                               ? BASE_ADDRESS + ".register." + this.schedulerName
                               : registerAddress;
        this.monitorAddress = Strings.isBlank(monitorAddress)
                              ? BASE_ADDRESS + ".monitor." + this.schedulerName
                              : monitorAddress;
        this.workerConfig = Objects.isNull(workerConfig)
                            ? new WorkerPoolConfig().injectPoolName(this.schedulerName)
                            : workerConfig.injectPoolName(this.schedulerName);
    }

    @Override
    public String name() {
        return "__scheduler__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    public static final class WorkerPoolConfig implements IConfig {

        public static final String NAME = "__schedule_worker__";
        private String poolName;
        private int poolSize = 5;
        private long maxExecuteTime = 60;
        private TimeUnit maxExecuteTimeUnit = TimeUnit.SECONDS;

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return SchedulerConfig.class;
        }

        WorkerPoolConfig injectPoolName(String poolName) {
            this.poolName = Strings.isBlank(this.poolName) ? "worker-pool-scheduler-" +
                                                             Strings.requireNotBlank(poolName) : this.poolName;
            return this;
        }

    }

}
