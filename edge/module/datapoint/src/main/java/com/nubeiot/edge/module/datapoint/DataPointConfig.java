package com.nubeiot.edge.module.datapoint;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.auth.Credential;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.scheduler.DataJobDefinition;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.unit.DataType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class DataPointConfig implements IConfig {

    static final String NAME = "__datapoint__";
    @JsonProperty(LowdbMigration.NAME)
    private LowdbMigration lowdbMigration = new LowdbMigration();
    @JsonProperty(DataSyncConfig.NAME)
    private DataSyncConfig dataSyncConfig;
    @JsonProperty(BuiltinData.NAME)
    private BuiltinData builtinData;
    @JsonProperty("__data_scheduler__")
    private List<DataJobDefinition> jobs;

    static DataPointConfig def() {
        return new DataPointConfig(new LowdbMigration(), DataSyncConfig.def(), BuiltinData.def(),
                                   DataJobDefinition.def());
    }

    static DataPointConfig def(@NonNull BuiltinData builtinData) {
        return new DataPointConfig(new LowdbMigration(), DataSyncConfig.def(), builtinData, DataJobDefinition.def());
    }

    static DataPointConfig def(@NonNull BuiltinData builtinData, @NonNull DataSyncConfig config) {
        return new DataPointConfig(new LowdbMigration(), config, builtinData, DataJobDefinition.def());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    public static final class LowdbMigration implements IConfig, Shareable {

        static final String NAME = "__lowdb_migration__";
        private boolean enabled = false;
        private String path;

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

    }


    @Getter
    @NoArgsConstructor
    @Setter(value = AccessLevel.PACKAGE)
    public static final class DataSyncConfig implements IConfig {

        public static final String NAME = "__data_sync__";
        public static final String USER_AGENT = "nubeio.edge.datapoint";
        private String type;
        private boolean enabled;
        private Credential credential;
        private JsonObject clientConfig;

        @JsonCreator
        DataSyncConfig(@JsonProperty("type") String type, @JsonProperty("enabled") boolean enabled,
                       @JsonProperty("credential") Credential credential,
                       @JsonProperty("clientConfig") JsonObject clientConfig) {
            this.type = type;
            this.enabled = enabled;
            this.credential = credential;
            this.clientConfig = clientConfig;
        }

        public static DataSyncConfig def() {
            JsonObject cfg = HttpClientConfig.create(USER_AGENT, HostInfo.builder().build()).toJson();
            return new DataSyncConfig("DITTO", false, null, cfg);
        }

        public static DataSyncConfig enabled(@NonNull Credential credential, @NonNull HostInfo hostInfo) {
            JsonObject cfg = HttpClientConfig.create(USER_AGENT, hostInfo).toJson();
            return new DataSyncConfig("DITTO", true, credential, cfg);
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

        static JsonObject update(@NonNull JsonObject cfg, @NonNull String version, @NonNull UUID deviceId) {
            JsonObject userAgent = new JsonObject().put("userAgent", USER_AGENT + "/" + version + " " +
                                                                     UUID64.uuidToBase64(deviceId));
            return cfg.mergeIn(new JsonObject().put("clientConfig", userAgent), true);
        }

    }


    @Getter
    public static final class BuiltinData extends HashMap<String, Object> implements IConfig {

        static final String NAME = "__builtin_data__";

        public static BuiltinData def() {
            final BuiltinData bd = new BuiltinData();
            bd.put(MeasureUnitMetadata.INSTANCE.singularKeyName(), DataType.available()
                                                                           .map(dt -> new MeasureUnit(dt.toJson()))
                                                                           .map(p -> JsonPojo.from(p).toJson())
                                                                           .collect(JsonArray::new, JsonArray::add,
                                                                                    JsonArray::addAll));
            return bd;
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

    }

}
