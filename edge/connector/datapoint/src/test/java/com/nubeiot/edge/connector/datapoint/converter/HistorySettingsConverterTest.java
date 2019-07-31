package com.nubeiot.edge.connector.datapoint.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.edge.connector.datapoint.model.ditto.DittoHistorySetting;

import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;

public class HistorySettingsConverterTest {

    @Test
    public void test() throws IOException {
        Scope rootScope = Scope.newEmptyScope();
        rootScope.loadFunctions(Scope.class.getClassLoader());
        Scope childScope = Scope.newChildScope(rootScope);
        final InputStream is = Reflections.contextClassLoader().getResourceAsStream("ditto-test.json");
        JsonNode inn = JsonData.MAPPER.readTree(is);
        JsonQuery qq = JsonQuery.compile(
            ".thing.features.points.properties | to_entries | map({code: .key, historySettings: .value" +
            ".historySettings})");
        final List<JsonNode> apply = qq.apply(childScope, inn);
        System.out.println(apply);
        Assert.assertEquals(1, apply.size());
        List<DittoHistorySetting> data = JsonData.MAPPER.readValue(JsonData.MAPPER.treeAsTokens(apply.get(0)),
                                                                   JsonData.MAPPER.getTypeFactory()
                                                                                  .constructCollectionType(List.class,
                                                                                                           DittoHistorySetting.class));
        data.forEach(d -> System.out.println(d.get().toJson()));
    }

    @Test
    public void test1() {
        //        new DittoMigration().migrate(Reflections.contextClassLoader().getResourceAsStream("ditto-test.json"));
    }

}
