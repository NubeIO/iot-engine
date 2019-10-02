package com.nubeiot.edge.module.monitor.service;

import java.util.Arrays;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.module.monitor.info.NetworkInterface;

import lombok.NoArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;

@NoArgsConstructor
public class MonitorNetworkStatusService implements MonitorService {

    private static JsonArray getNetworkInterfaces(NetworkIF[] networkIFs) {
        JsonArray networkInterfaces = new JsonArray();
        for (NetworkIF net : networkIFs) {
            networkInterfaces.add(NetworkInterface.from(net).toJson());
        }
        return networkInterfaces;
    }

    private static JsonObject getNetworkParams(NetworkParams networkParams) {
        return new JsonObject().put("host_name", networkParams.getHostName())
                               .put("domain_name", networkParams.getDomainName())
                               .put("dns_servers", Arrays.toString(networkParams.getDnsServers()))
                               .put("ipv4_gateway", networkParams.getIpv4DefaultGateway())
                               .put("ipv6_gateway", networkParams.getIpv6DefaultGateway());
    }

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("network_interfaces", getNetworkInterfaces(hal.getNetworkIFs()));
        jsonObject.put("network_params", getNetworkParams(os.getNetworkParams()));
        return jsonObject;
    }

    @Override
    public String servicePath() {
        return "/network";
    }

}
