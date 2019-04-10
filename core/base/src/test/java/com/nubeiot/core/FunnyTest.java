package com.nubeiot.core;

import java.util.Optional;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.JsonData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FunnyTest {

    @Test
    public void test_override_config() {
        String jsonInput = "{\"__system__\":{\"__eventBus__\":{\"clientAuth\":\"REQUIRED\",\"ssl\":true," +
                           "\"clustered\":true,\"keyStoreOptions\":{\"path\":\"eventBusKeystore.jks\"," +
                           "\"password\":\"nubesparkEventBus\"},\"trustStoreOptions\":{\"path\":\"eventBusKeystore" +
                           ".jks\",\"password\":\"nubesparkEventBus\"}},\"__cluster__\":{\"active\":true,\"ha\":true," +
                           "\"listenerAddress\":\"com.nubeiot.dashboard.connector.edge.cluster\"}}," +
                           "\"__app__\":{\"__http__\":{\"host\":\"0.0.0.0\",\"port\":8086,\"enabled\":true," +
                           "\"rootApi\":\"/api\"},\"api.name\":\"edge-connector\"}}";
        AppConfig input = IConfig.from(jsonInput, AppConfig.class);

        String key = "nube.app.http.port";
        String value = "1111";
        Optional<JsonObject> result = overrideConfig(key, value, input);
    }

    private Optional<JsonObject> overrideConfig(String key, String value, AppConfig input) {
        String[] array = key.split("\\.");
        if (!array[0].equals("nube")) {
            return Optional.empty();
        }
        JsonObject json = new JsonObject();
        for (int i = 2; i < array.length; i++) {
            String item = array[i];
            String jsonStr = getJsonItem(input, item);
        }
        return Optional.of(json);
    }

    private String getJsonItem(AppConfig appConfig, String item) {
        Optional<String> json = getJsonForCustomObject(appConfig, item);
        if (json.isPresent()) {
            return json.get();
        }
        json = getJsonForCustomObject(appConfig, "__" + item + "__");
        if (json.isPresent()) {
            return json.get();
        }
        //json = getJsonFromPrimitiveType(appConfig, item);
        return json.orElse(null);
    }

//    private Optional<String> getJsonFromPrimitiveType(AppConfig appConfig, String item) {
//        Object result = appConfig.get(item);
////        if(result.getClass().isPrimitive()){
////
////        }
//    }

    private Optional<String> getJsonForCustomObject(AppConfig appConfig, String item) {
        try {
            Object result = appConfig.get(item);
            if (IConfig.class.isAssignableFrom(result.getClass())) {
                return Optional.of(IConfig.from(result, HttpConfig.class).toJson().toString());
            }
            if (JsonData.class.isAssignableFrom(result.getClass())) {
                return Optional.of(JsonData.from(result, HttpConfig.class).toJson().toString());
            }
            if (JsonObject.class.isAssignableFrom(result.getClass())) {
                return Optional.of(JsonObject.mapFrom(result).toString());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    final class HttpConfig implements IConfig {

        public static final String NAME = "__http__";
        private String host = "0.0.0.0";
        private int port = 8080;

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return NubeConfig.AppConfig.class; }


    }
}
