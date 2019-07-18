package com.nubeiot.edge.connector.datapoint.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.edge.connector.datapoint.model.DittoHistorySetting;
import com.nubeiot.edge.connector.datapoint.model.IDittoModel;

import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;

public class HistorySettingsConverterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void test() throws IOException {
        Scope rootScope = Scope.newEmptyScope();
        rootScope.loadFunctions(Scope.class.getClassLoader());
        Scope childScope = Scope.newChildScope(rootScope);
        final InputStream is = Reflections.contextClassLoader().getResourceAsStream("ditto-test.json");
        JsonNode inn = MAPPER.readTree(is);
        JsonQuery qq = JsonQuery.compile(IDittoModel.mock(DittoHistorySetting.class).jqExpr());
        final List<JsonNode> apply = qq.apply(childScope, inn);
        System.out.println(apply);
        Assert.assertEquals(1, apply.size());
        List<DittoHistorySetting> myObjects = Arrays.asList(
            MAPPER.readValue(MAPPER.treeAsTokens(apply.get(0)), DittoHistorySetting[].class));
        System.out.println(myObjects);
    }

}
