package com.nubeiot.core.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

@Deprecated
public class SecurityUtils {
    private static String SALT = "NubeIoTSalt";

    public static String getBase64EncodedHash(String toEncode) {
        return (Base64.encodeBase64URLSafeString(DigestUtils.md5(toEncode + SALT)));
    }
}
