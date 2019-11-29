package com.nubeiot.core.micro.discovery;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.exceptions.ServiceNotFoundException;
import com.nubeiot.core.micro.ServiceGatewayIndex.Params;
import com.nubeiot.core.micro.ServiceKind;
import com.nubeiot.core.micro.ServiceScope;
import com.nubeiot.core.micro.transfomer.RecordTransformer.RecordView;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Helps invoking event service by delegating to {@code service gateway} finds service by name then executes with given
 * {@code event action} and {@code request data}
 */
public interface RemoteServiceInvoker {

    /**
     * Gateway index address
     *
     * @return gateway index address
     */
    @NonNull String gatewayAddress();

    /**
     * Request service name
     *
     * @return request service name
     */
    String requester();

    /**
     * Event client
     *
     * @return event client
     */
    @NonNull EventbusClient eventClient();

    /**
     * Defines remote service label that is used in case making  an intuitive error message
     *
     * @return remote service label. Default: {@code Remote service}
     */
    default String serviceLabel() {
        return "Remote service";
    }

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
     * Destination service pattern
     *
     * @return service pattern. Default: {@link EventPattern#REQUEST_RESPONSE}
     */
    @NonNull
    default EventPattern pattern() {
        return EventPattern.REQUEST_RESPONSE;
    }

    /**
     * Destination name
     *
     * @return destination address
     */
    @NonNull String destination();

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
        reqData.headers().put(Headers.X_REQUEST_BY, requester());
        return execute(EventMessage.initial(action, reqData));
    }

    /**
     * Do invoke remote service with plain json data
     *
     * @param dataMsg given data message
     * @return single result from remote service
     * @apiNote result can be single error if remote service not found or not compatible with given event action
     * @see EventMessage
     */
    default Single<JsonObject> execute(@NonNull EventMessage dataMsg) {
        final RequestData findReqData = RequestData.builder()
                                                   .body(new JsonObject().put(Params.IDENTIFIER, destination()))
                                                   .filter(new JsonObject().put(Params.BY, "name")
                                                                           .put(Params.STATUS, Status.UP)
                                                                           .put(Params.SCOPE, scope())
                                                                           .put(Params.KIND, kind())
                                                                           .put(Params.VIEW, RecordView.TECHNICAL))
                                                   .build();
        final EventbusClient client = eventClient();
        final EventAction action = dataMsg.getAction();
        return client.request(gatewayAddress(), EventMessage.initial(EventAction.GET_ONE, findReqData))
                     .onErrorReturn(t -> {
                         throw new ServiceNotFoundException(
                             serviceLabel() + " is not found or out of service. Try again later", t);
                     })
                     .map(msg -> {
                         if (msg.isError()) {
                             final ErrorMessage errorMsg = msg.getError();
                             final String error = Strings.format(
                                 "{0} is not found or out of service. Try again later | Cause: {1}", serviceLabel(),
                                 errorMsg.getCode());
                             throw new ServiceNotFoundException(error);
                         }
                         return msg.getData();
                     })
                     .filter(json -> json.getJsonArray("endpoints")
                                         .stream()
                                         .map(JsonObject.class::cast)
                                         .anyMatch(o -> action.name().equalsIgnoreCase(o.getString("action"))))
                     .switchIfEmpty(Single.error(new ServiceException(action + " is unsupported in " + serviceLabel())))
                     .flatMap(json -> client.request(json.getString("location"), dataMsg))
                     .map(msg -> msg.isError() ? msg.getError().toJson() : msg.getData());
    }

}
