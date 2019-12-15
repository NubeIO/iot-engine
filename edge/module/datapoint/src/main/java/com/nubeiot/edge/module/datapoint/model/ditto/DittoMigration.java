package com.nubeiot.edge.module.datapoint.model.ditto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.NonNull;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.exception.JsonQueryException;

public final class DittoMigration {

    private static final String THING_FEATURES_JQ_EXPR = ".thing.features";
    private static final String POINT_JQ_EXPR = ".thing.features.points.properties";
    private static final Map<Class<? extends IDittoModel>, String> MAP = new LinkedHashMap<>();

    static {
        MAP.put(DittoDevice.class, ".thing.attributes");
        MAP.put(DittoPoint.class, POINT_JQ_EXPR);
        MAP.put(DittoHistorySetting.class,
                POINT_JQ_EXPR + " | to_entries | map({code: .key, historySettings: .value.historySettings})");
        MAP.put(DittoHistoryData.class, THING_FEATURES_JQ_EXPR + ".histories.properties");
        MAP.put(DittoEquipment.class, THING_FEATURES_JQ_EXPR + ".sensorList.properties.list");
    }

    private final Scope rootScope;

    public DittoMigration() {
        this.rootScope = Scope.newEmptyScope();
        rootScope.loadFunctions(Scope.class.getClassLoader());
    }

    public void migrate(String file) {
        migrate(new File(file));
    }

    public void migrate(@NonNull File file) {
        try {
            migrate(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new NubeException(ErrorCode.NOT_FOUND, "File not found", e);
        }
    }

    public void migrate(@NonNull InputStream is) {
        try {
            JsonNode inn = JsonData.MAPPER.readTree(is);
            MAP.forEach((clazz, jqExpr) -> extract(inn, clazz, jqExpr));
        } catch (IOException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Cannot parse ", e);
        }
    }

    private void extract(JsonNode inn, Class<? extends IDittoModel> clazz, String jqExpr) {
        try {
            JsonQuery qq = JsonQuery.compile(jqExpr);
            List<JsonNode> apply = qq.apply(rootScope, inn);
            if (apply.isEmpty() || Objects.isNull(apply.get(0))) {
                throw new NotFoundException("Not found");
            }
            JsonNode node = apply.get(0);
            if (node.isArray()) {
                List data = JsonData.MAPPER.readValue(JsonData.MAPPER.treeAsTokens(node),
                                                      JsonData.MAPPER.getTypeFactory()
                                                                     .constructCollectionType(List.class, clazz));
                System.out.println(data);
            } else if (node.isObject()) {
                IDittoModel<VertxPojo> data = JsonData.MAPPER.readValue(JsonData.MAPPER.treeAsTokens(node), clazz);
                System.out.println(data.get().toJson());
            } else {
                return;
            }
            System.out.println("------------------------");
        } catch (JsonQueryException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Cannot parse", e);
        } catch (IOException e) {
            throw new NubeException(ErrorCode.UNKNOWN_ERROR, "Cannot parse", e);
        }
    }

}
