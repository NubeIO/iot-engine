package com.nubeiot.core.sql;

import java.nio.file.Paths;

import org.jooq.SQLDialect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.utils.Strings;
import com.zaxxer.hikari.HikariConfig;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class SqlConfig implements IConfig {

    public static final String NAME = "__sql__";

    private SQLDialect dialect = SQLDialect.POSTGRES;
    private String dbName;
    private boolean dbLocalFile;

    @JsonProperty(value = Hikari.NAME)
    private Hikari hikariConfig = new Hikari();

    public String computeJdbcUrl(String dataDir) {
        if (Strings.isNotBlank(dbName) && isDbLocalFile() && Strings.isBlank(getHikariConfig().getJdbcUrl())) {
            if (SQLDialect.H2.equals(dialect)) {
                return "jdbc:h2:file:" + Paths.get(dataDir, dbName);
            }
            if (SQLDialect.SQLITE.equals(dialect)) {
                return "jdbc:sqlite:" + Paths.get(dataDir, dbName);
            }
        }
        return getHikariConfig().getJdbcUrl();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

    public static class Hikari extends HikariConfig implements IConfig {

        public static final String NAME = "__hikari__";

        @JsonCreator
        public Hikari() {
            this.setMinimumIdle(2);
            this.setMaximumPoolSize(10);
            this.setConnectionTimeout(60000);
            this.setIdleTimeout(600000);
            this.setMaxLifetime(1800000);
            this.setConnectionTestQuery("SELECT 1");
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return SqlConfig.class; }

    }

}
