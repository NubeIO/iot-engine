package com.nubeiot.buildscript.jooq;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.ForcedType;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Logging;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;

import io.github.jklingsporn.vertx.jooq.generate.VertxGenerator;
import io.github.jklingsporn.vertx.jooq.shared.JsonArrayConverter;
import io.github.jklingsporn.vertx.jooq.shared.JsonObjectConverter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

abstract class NubeJooqProvider {

    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    @SuppressWarnings("unchecked")
    static <T extends VertxGenerator> Class<T> getVertxGenerator(JooqGenerateTask.APIType apiType,
                                                                 JooqGenerateTask.JDBCType jdbcType) {
        if (apiType != JooqGenerateTask.APIType.RX) {
            throw new RuntimeException("Unsupported API type: " + apiType);
        }
        if (jdbcType == JooqGenerateTask.JDBCType.PLAIN_JDBC) {
            return (Class<T>) NubeJdbcGenerator.class;
        }
        return (Class<T>) ReactivePgJdbcGenerator.class;
    }

    static <T extends VertxGenerator> Configuration createConfiguration(Database databaseCfg, Target target,
                                                                        Class<T> generatorClass) {
        Generator generator = new Generator().withDatabase(databaseCfg)
                                             .withGenerate(createGenerate())
                                             .withStrategy(createStrategy())
                                             .withTarget(target)
                                             .withName(generatorClass.getName());
        return new Configuration().withGenerator(generator).withLogging(Logging.FATAL);
    }

    static Database createDatabase(String ddlDir, List<ForcedType> forcedTypes) {
        ForcedType jsonObjectType = new ForcedType();
        jsonObjectType.setUserType(JsonObject.class.getName());
        jsonObjectType.setConverter(JsonObjectConverter.class.getName());
        jsonObjectType.setExpression(".+_json");
        jsonObjectType.setTypes("TEXT");

        ForcedType jsonArrayType = new ForcedType();
        jsonArrayType.setUserType(JsonArray.class.getName());
        jsonArrayType.setConverter(JsonArrayConverter.class.getName());
        jsonArrayType.setExpression(".+_json_array");
        jsonArrayType.setTypes("TEXT");

        forcedTypes.addAll(Arrays.asList(jsonArrayType, jsonObjectType));

        return new Database().withName("org.jooq.meta.extensions.ddl.DDLDatabase")
                             .withProperties(new Property().withKey("scripts").withValue(ddlDir))
                             .withUnsignedTypes(false)
                             .withIncludes(".*")
                             .withForcedTypes(forcedTypes);
    }

    static Target createTarget(@NonNull String targetDir, @NonNull String packageName) {
        return new Target().withPackageName(packageName)
                           .withDirectory(targetDir)
                           .withClean(true)
                           .withEncoding(StandardCharsets.UTF_8.name());
    }

    static Generate createGenerate() {
        return new Generate().withDeprecated(false)
                             .withInterfaces(true)
                             .withPojos(true)
                             .withDaos(true)
                             .withFluentSetters(true)
                             .withJavaTimeTypes(true)
                             .withJpaAnnotations(true);
    }

    static Strategy createStrategy() {
        return new Strategy().withName(NubeGeneratorStrategy.class.getName());
    }

}
