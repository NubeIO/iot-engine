package com.nubeiot.edge.connector.bonescript.functions;

import java.util.function.Function;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bonescript.utils.PasswordUtils;

import io.vertx.core.json.JsonObject;

public class InitPassword implements Function<JsonObject, JsonObject> {

    public static String PASSWORD_FILE_NAME_RESOURCE = "ditto_password.txt";

    @Override
    public JsonObject apply(JsonObject jsonObject) {
        String password = PasswordUtils.generatePassword();
        String passwordHash = PasswordUtils.generatePasswordHash(password);
        FileUtils.createFile(PASSWORD_FILE_NAME_RESOURCE, password);

        jsonObject.getJsonObject("thing")
                  .getJsonObject("features")
                  .getJsonObject("users")
                  .getJsonObject("admin")
                  .put("password", passwordHash);

        return jsonObject;
    }

}
