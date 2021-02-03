package io.github.zero88.qwe.iot.connector.coordinator;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.iot.connector.Subject;
import io.github.zero88.qwe.iot.connector.watcher.WatcherOption;
import io.github.zero88.qwe.protocol.HasProtocol;
import io.github.zero88.qwe.rpc.GatewayServiceInvoker;
import io.github.zero88.qwe.scheduler.model.job.EventbusJobModel;
import io.github.zero88.qwe.scheduler.model.job.QWEJobModel;
import io.github.zero88.qwe.scheduler.model.trigger.QWETriggerModel;
import io.github.zero88.qwe.scheduler.service.SchedulerRegisterArgs;
import io.github.zero88.qwe.scheduler.service.SchedulerRegisterResp;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

/**
 * Represents for an {@code coordinator service} that watches a particular {@code internal event} then notifying this
 * events to external subscribers
 */
public interface InboundCoordinator<S extends Subject> extends Coordinator<S>, HasProtocol, GatewayServiceInvoker {

    default String function() {
        return "inbound-coordinator";
    }

    /**
     * Scheduler service name
     *
     * @return scheduler service name
     */
    @Override
    @NonNull String destination();

    @EventContractor(action = "CREATE", returnType = Single.class)
    default Single<CoordinatorRegisterResult> register(@NonNull RequestData requestData) {
        return this.validateInCreation(parseInput(requestData))
                   .flatMap(input -> Single.just(input.getWatcherOption())
                                           .filter(WatcherOption::isRealtime)
                                           .flatMapSingleElement(ignore -> addRealtimeWatcher(input))
                                           .switchIfEmpty(addPollingWatcher(input)));
    }

    @Override
    @EventContractor(action = "REMOVE", returnType = Single.class)
    default Single<JsonObject> unregister(@NonNull RequestData requestData) {
        parseInput(requestData);
        return Single.just(new JsonObject());
    }

    default Single<CoordinatorInput<S>> validateInCreation(@NonNull CoordinatorInput<S> input) {
        return Single.just(input.validate());
    }

    default Single<CoordinatorInput<S>> validateInQuery(@NonNull CoordinatorInput<S> input) {
        return Single.just(input);
    }

    Single<CoordinatorRegisterResult> addRealtimeWatcher(@NonNull CoordinatorInput<S> coordinatorInput);

    Single<CoordinatorRegisterResult> addPollingWatcher(@NonNull CoordinatorInput<S> coordinatorInput);

    default Single<SchedulerRegisterResp> addPollingWatcher(@NonNull WatcherOption option, @NonNull String jobName,
                                                            @NonNull String triggerName,
                                                            @NonNull JsonObject processPayload) {
        final QWEJobModel job = EventbusJobModel.builder()
                                                .group(protocol().type())
                                                .name(jobName)
                                                .process(monitorAddress(processPayload))
                                                .callback(listenAddress())
                                                .build();
        final QWETriggerModel trigger = QWETriggerModel.from(protocol().type(), jobName, option.getTriggerOption());
        final SchedulerRegisterArgs schArgs = SchedulerRegisterArgs.builder().job(job).trigger(trigger).build();
        return this.execute(EventAction.CREATE, RequestData.builder().body(schArgs.toJson()).build().toJson())
                   .map(resp -> JsonData.from(resp, SchedulerRegisterResp.class));
    }

}
