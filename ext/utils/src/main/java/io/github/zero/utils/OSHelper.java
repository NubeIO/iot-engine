package io.github.zero.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface OSHelper {

    String OS = System.getProperty("os.name").toLowerCase();

    static boolean isWin() {
        return OS.contains("win");
    }

    static boolean isMac() {
        return OS.contains("mac");
    }

    static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    static boolean isSolaris() {
        return OS.contains("sunos");
    }

    static Path getAbsolutePathByOs(String path) {
        if (isWin()) {
            return Paths.get("C:", path);
        }
        return Paths.get(path);
    }

}
