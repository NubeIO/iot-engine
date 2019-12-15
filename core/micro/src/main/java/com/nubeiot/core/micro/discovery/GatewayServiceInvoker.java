package com.nubeiot.core.micro.discovery;

import java.util.Optional;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.ErrorMessageConverter;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.micro.ServiceGatewayIndex.Params;
import com.nubeiot.core.micro.ServiceKind;
import com.nubeiot.core.micro.ServiceScope;
import com.nubeiot.core.micro.filter.ByPredicate;
import com.nubeiot.core.micro.transfomer.RecordOutput;
import com.nubeiot.core.micro.transfomer.RecordTransformer.RecordView;

import lombok.NonNull;

/**
 * Helps invoking event service by delegating to {@code service gateway} finds service by name then executes with given
 * {@code event action} and {@code request data}
 *
 * @see <a href="https://en.wikipedia.org/wiki/Remote_procedure_call">Remote procedure call</a>
 */
public interface GatewayServiceInvoker extends RemoteServiceInvoker {

    /**
     * Gateway index address
     *
     * @return gateway index address
     */
    @NonNull String gatewayAddress();

    /**
     * Defines service scope to discover
     *
     * @return service scope. Default: {@link ServiceScope#ALL}
     */
    @NonNull
    default ServiceScope scope() {
        return ServiceScope.ALL;
    }

    /**
     * Defines service kind to discover
     *
     * @return service kind. Default: {@link ServiceKind#LOCAL}
     */
    @NonNull
    default ServiceKind kind() {
        return ServiceKind.LOCAL;
    }

    /**
     * Destination name
     *
     * @return destination name
     */
    @NonNull String destination();

    /**
     * Destination service pattern
     *
     * @return service pattern. Default: {@link EventPattern#REQUEST_RESPONSE}
     */
    @NonNull
    default EventPattern pattern() {
        return EventPattern.REQUEST_RESPONSE;
    }

    /**
     * Do invoke remote service
     *
     * @param action given event action
     * @param data   given request body data
     * @return single result from remote service
     * @apiNote result can be single error if remote service not found or not compatible with given event action
     * @see EventAction
     * @see RequestData#body()
     */
    default Single<JsonObject> execute(@NonNull EventAction action, JsonObject data) {
        return execute(action, RequestData.builder().body(data).build());
    }

    /**
     * Do invoke remote service
     *
     * @param action  given event action
     * @param reqData given request data
     * @return single result from remote service
     * @apiNote result can be single error if remote service not found or not compatible with given event action
     * @see EventAction
     * @see RequestData
     */
    default Single<JsonObject> execute(@NonNull EventAction action, @NonNull RequestData reqData) {
        final RequestData searchReq = RequestData.builder()
                                                 .body(new JsonObject().put(Params.IDENTIFIER, destination()))
                                                 .filter(new JsonObject().put(Params.BY, ByPredicate.BY_NAME)
                                                                         .put(Params.STATUS, Status.UP)
                                                                         .put(Params.SCOPE, scope())
                                                                         .put(Params.KIND, kind())
                                                                         .put(Params.VIEW, RecordView.TECHNICAL)
                                                                         .put(Params.ACTION, action))
                                                 .build();
        final Single<EventMessage> invoker = invoke(gatewayAddress(), EventAction.GET_ONE, searchReq);
        return invoker.flatMap(out -> out.isError()
                                      ? Single.error(notFound().apply(out.getError()))
                                      : Single.just(Optional.ofNullable(out.getData()).orElse(new JsonObject())))
                      .flatMap(json -> invoke(json.getString(RecordOutput.Fields.location), action, reqData))
                      .map(out -> out.isError() ? out.getError().toJson() : out.getData());
    }

    default Function<ErrorMessage, NubeException> notFound() {
        return msg -> ErrorMessageConverter.from(msg, ErrorCode.SERVICE_NOT_FOUND,
                                                 RemoteServiceInvoker.notFoundMessage(serviceLabel()));
    }

}
