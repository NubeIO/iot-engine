package com.nubeiot.edge.connector.bonescript.utils;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static String generatePassword() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
    }

    public static String generatePasswordHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean validatePassword(String password, String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }

}
