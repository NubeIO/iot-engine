package com.nubeiot.edge.connector.device.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.edge.connector.device.utils.Command;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class DeviceStatusEventHandler implements EventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int MB_TO_BYTES = 1048576;

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        JsonObject jsonObject = new JsonObject();

        // Device OS and build
        String osBuild = Command.execute("lsb_release -a");
        logger.info("OS build: " + osBuild);
        // total memory
        long mbTotal = (Runtime.getRuntime().totalMemory() / MB_TO_BYTES);
        long mbFree = (Runtime.getRuntime().freeMemory() / MB_TO_BYTES);
        // topActiveProcess Running
        String topProcess = Command.execute("ps -eo pid,comm,%cpu,%mem --sort=-%cpu | head -n 30");
        List<List<String>> topProcessGrouped = new ArrayList<>();
        if (topProcess != null) {
            String[] topProcessConverted = topProcess.replaceAll("\n", " ").split("\\s+");
            topProcessGrouped = breakArrayIntoGroups(topProcessConverted);
        }
        // Device uptime
        List<String> uptime = Command.executeWithSplit("uptime -p");
        List<String> upSince = Command.executeWithSplit(("uptime -s"));
        // Disc Size
        List<String> fileUsage = Command.executeWithSplit((" df -h /"));

        jsonObject.put("osBuild", osBuild);
        jsonObject.put("freeMem_MB", mbFree);
        jsonObject.put("totalMem_MB", mbTotal);
        jsonObject.put("topProcess", topProcessGrouped);
        jsonObject.put("upTime", uptime);
        jsonObject.put("upSince", upSince);
        jsonObject.put("fileUsage", fileUsage);

        return jsonObject;
    }

    /**
     * @param data example: [PID, COMMAND, %CPU, %MEM, PID, COMMAND, %CPU, %MEM]
     * @return [[PID, COMMAND, %CPU, %MEM], [PID, COMMAND, %CPU, %MEM]]
     */
    private List<List<String>> breakArrayIntoGroups(String[] data) {
        List<List<String>> groups = new ArrayList<>();
        for (int index = 0; index < data.length; index += 4) {
            String[] group = Arrays.copyOfRange(data, index, index + 4);
            groups.add(Arrays.asList(group));
        }
        return groups;
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
