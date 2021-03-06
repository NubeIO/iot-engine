package com.nubeiot.edge.connector.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.zero88.utils.Strings;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.protocol.network.IpNetwork;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NetworkIPEventHandler implements EventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final NetworkCommand networkCommand;

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        JsonObject address = data.body();
        NetworkInfo networkInfo = NetworkInfo.from(address);

        String invalidIps = Stream.of(networkInfo.getIpAddress(), networkInfo.getSubnetMask(), networkInfo.getGateway())
                                  .filter(ip -> !IpNetwork.isIpv4(ip))
                                  .collect(Collectors.joining(", "));

        if (Strings.isNotBlank(invalidIps)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid " + invalidIps);
        }
        return Single.just(new JsonObject().put("success", true)
                                           .put("message",
                                                "Updated IP Address: " + networkCommand.configIp(networkInfo)));
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData data) {
        networkCommand.configDhcp();
        return Single.just(new JsonObject());
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(NetworkEventModels.NETWORK_IP.getEvents()));
    }

}
