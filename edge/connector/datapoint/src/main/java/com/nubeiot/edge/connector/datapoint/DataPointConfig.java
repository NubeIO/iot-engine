package com.nubeiot.edge.connector.datapoint;

import java.time.Duration;
import java.util.HashMap;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.edge.connector.datapoint.policy.CleanupPolicy;
import com.nubeiot.edge.connector.datapoint.policy.OldestCleanupPolicy;
import com.nubeiot.edge.connector.datapoint.service.HistoryDataService;
import com.nubeiot.edge.connector.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.model.tables.interfaces.IMeasureUnit;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.scheduler.trigger.CronTriggerModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

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
    private LowdbMigration lowdbMigration = new LowdbMigration();
    @JsonProperty(PublisherConfig.NAME)
    private PublisherConfig publisherConfig;
    private BuiltinData builtinData;
    @JsonProperty(CleanupPolicyConfig.NAME)
    private CleanupPolicyConfig policyConfig;

    static DataPointConfig def() {
        return new DataPointConfig(new LowdbMigration(), new PublisherConfig(), BuiltinData.def(),
                                   CleanupPolicyConfig.def());
    }

    static DataPointConfig def(@NonNull BuiltinData builtinData) {
        return new DataPointConfig(new LowdbMigration(), new PublisherConfig(), builtinData, CleanupPolicyConfig.def());
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
    public static final class LowdbMigration implements JsonData, Shareable {

        private boolean enabled = false;
        private String path;

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    public static final class PublisherConfig extends AbstractEnumType implements IConfig {

        static final String NAME = "__publisher__";

        private boolean enabled = false;
        private JsonObject location;

        PublisherConfig() {
            super("");
        }

        @JsonCreator
        PublisherConfig(@JsonProperty("type") String type, @JsonProperty("location") JsonObject location) {
            super(type);
            this.location = location;
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

    }


    @Getter
    public static final class BuiltinData extends HashMap<String, Object> implements JsonData {

        public static BuiltinData def() {
            final BuiltinData bd = new BuiltinData();
            bd.put("measure_unit", ReflectionField.streamConstants(DataType.class, DataType.class)
                                                  .map(dt -> new MeasureUnit(dt.toJson()))
                                                  .map(IMeasureUnit::toJson)
                                                  .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
            return bd;
        }

    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CleanupPolicyConfig implements IConfig {

        private static final String NAME = "__cleanup_policy__";
        private boolean enabled = false;
        private DeliveryEvent process;
        private TriggerModel triggerModel;
        private CleanupPolicy policy;

        static CleanupPolicyConfig def() {
            final DeliveryEvent event = DeliveryEvent.builder()
                                                     .address(HistoryDataService.class.getName())
                                                     .pattern(EventPattern.REQUEST_RESPONSE)
                                                     .action(EventAction.BATCH_DELETE)
                                                     .build();
            final TriggerModel trigger = CronTriggerModel.builder()
                                                         .expr("0 0 0 ? * SUN *")
                                                         .tz("Australia/Sydney")
                                                         .name("historyData")
                                                         .group("cleanup")
                                                         .build();
            return new CleanupPolicyConfig(true, event, trigger,
                                           new OldestCleanupPolicy(100, PointExtension.REQUEST_KEY,
                                                                   Duration.ofDays(30)));
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() {
            return DataPointConfig.class;
        }

    }

}
