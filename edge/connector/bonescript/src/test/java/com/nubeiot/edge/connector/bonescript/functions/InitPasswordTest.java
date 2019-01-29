package com.nubeiot.edge.connector.bonescript.functions;

import java.nio.file.Path;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bonescript.BoneScriptInit;
import com.nubeiot.edge.connector.bonescript.utils.PasswordUtils;

import io.vertx.core.json.JsonObject;

public class InitPasswordTest extends TestBase {

    @Test
    public void testInitPasswordFunction() {
        Function<JsonObject, JsonObject> initPasswordFunction = new InitPassword();
        JsonObject jsonObject = initPasswordFunction.apply(BoneScriptInit.initDittoTemplate());
        String passwordHash = jsonObject.getJsonObject("thing")
                                        .getJsonObject("features")
                                        .getJsonObject("users")
                                        .getJsonObject("admin")
                                        .getString("password");

        Path path = FileUtils.resolveDataFolder(InitPassword.PASSWORD_FILE_NAME_RESOURCE);
        String password = FileUtils.readFileToString(path.toString());

        Assert.assertTrue(PasswordUtils.validatePassword(password, passwordHash));
    }

}
