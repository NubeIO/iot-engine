package com.nubeiot.edge.module.monitor.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.NetworkParams;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@NoArgsConstructor
public class MonitorNetworkStatusEventHandler implements EventHandler {

    private static JsonArray getNetworkInterfaces(NetworkIF[] networkIFs) {
        JsonArray networkInterfaces = new JsonArray();
        for (NetworkIF net : networkIFs) {
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0 ||
                              net.getPacketsSent() > 0;

            String traffic = String.format("received %s/%s%s; transmitted %s/%s%s",
                                           hasData ? net.getPacketsRecv() + " packets" : "?",
                                           hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                                           hasData ? " (" + net.getInErrors() + " err)" : "",
                                           hasData ? net.getPacketsSent() + " packets" : "?",
                                           hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
                                           hasData ? " (" + net.getOutErrors() + " err)" : "");
            networkInterfaces.add(new JsonObject().put("name", net.getName())
                                                  .put("display_name", net.getDisplayName())
                                                  .put("mac_address", net.getMacaddr())
                                                  .put("mtu", net.getMTU())
                                                  .put("speed", FormatUtil.formatValue(net.getSpeed(), "bps"))
                                                  .put("ipv4", Arrays.toString(net.getIPv4addr()))
                                                  .put("ipv6", Arrays.toString(net.getIPv6addr()))
                                                  .put("traffic", traffic));
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
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
