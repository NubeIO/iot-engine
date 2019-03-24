package com.nubeiot.dashboard.connector.postgresql;

import com.nubeiot.core.utils.Strings;

class PostgreSqlUrl {

    private String host;
    private int port = 5432;
    private String database = "test";

    PostgreSqlUrl(String url) {
        if (Strings.isNotBlank(url)) {
            String[] values = url.split(":");
            host = values[0];
            if (values.length > 1) {
                String[] values$ = values[1].split("/");
                try {
                    port = Integer.parseInt(values$[0]);
                } catch (Exception ignored) {
                }

                if (values$.length > 1) {
                    database = values$[1];
                }
            }
        }
    }

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    String getDatabase() {
        return database;
    }

}
