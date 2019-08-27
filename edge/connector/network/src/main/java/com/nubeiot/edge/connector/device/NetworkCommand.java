package com.nubeiot.edge.connector.device;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = ConnmanctlNetwork.class, name = "CONNMANCTL"),
})
public abstract class NetworkCommand {

    @Getter
    private final NetworkCommandType type;

    /**
     * @param networkInfo NetworkInfo to set IP
     * @return Set IP Address
     */
    public abstract String configIp(NetworkInfo networkInfo);

    /**
     * Release the IP and will configure with DHCP
     */
    public abstract void configDhcp();

    enum NetworkCommandType {
        CONNMANCTL
    }

}
