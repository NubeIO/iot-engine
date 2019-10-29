package com.nubeiot.edge.connector.bacnet.mixin;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.type.constructed.Address;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressWrapper implements JsonData {

    @NonNull
    private final Address address;

    @Override
    public JsonObject toJson() {
        return new JsonObject().put("networkNumber", address.getNetworkNumber().longValue())
                               .put("description", address.getDescription())
                               .put("mac", address.getMacAddress().toString())
                               .put("address", IpNetworkUtils.getInetAddress(address.getMacAddress()).toString());
    }

}
