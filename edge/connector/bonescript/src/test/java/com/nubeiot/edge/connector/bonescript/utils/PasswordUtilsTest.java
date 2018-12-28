package com.nubeiot.edge.connector.bonescript.utils;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.Strings;

public class PasswordUtilsTest extends TestBase {

    @Test
    public void generatePasswordTest() {
        Assert.assertEquals(PasswordUtils.generatePassword().length(), 8);
    }

    @Test
    public void generatePasswordHashTest() {
        Assert.assertTrue(Strings.isNotBlank(PasswordUtils.generatePasswordHash(PasswordUtils.generatePassword())));
    }

    @Test
    public void validatePasswordTest() {
        String password = PasswordUtils.generatePassword();
        String passwordHash = PasswordUtils.generatePasswordHash(password);
        Assert.assertTrue(PasswordUtils.validatePassword(password, passwordHash));
    }

}
