package com.nubeiot.buildscript.jooq

import java.nio.charset.StandardCharsets

import org.gradle.api.logging.Logger
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.Target

import io.github.jklingsporn.vertx.jooq.generate.VertxGenerator
import io.github.jklingsporn.vertx.jooq.shared.JsonArrayConverter
import io.github.jklingsporn.vertx.jooq.shared.JsonObjectConverter
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import com.nubeiot.buildscript.jooq.JooqGenerateTask.APIType
import com.nubeiot.buildscript.jooq.JooqGenerateTask.JDBCType

abstract class JooqProvider {

    private static final Logger logger = org.gradle.api.logging.Logging.getLogger(JooqProvider.class)

    static <T extends VertxGenerator> Class<T> getVertxGenerator(APIType apiType, JDBCType jdbcType) {
        if (apiType != APIType.RX) {
            throw new RuntimeException("Unsupported API type: " + apiType)
        }
        if (jdbcType == JDBCType.PLAIN_JDBC) {
            return NubeJdbcGenerator.class
        }
        return ReactivePgJdbcGenerator.class
    }

    static <T extends VertxGenerator> Configuration createConfiguration(Database databaseCfg, Target target,
                                                                        Class<T> generatorClass) {
        logger.info("-" * 20)
        logger.info("Java Types")
        logger.info("-" * 20)
        CacheDataType.instance().converters.each { logger.info(it.key) }
        logger.info("")
        Generator generator = new Generator().withDatabase(databaseCfg).withGenerate(createGenerate())
                                             .withStrategy(createStrategy()).withTarget(target)
                                             .withName(generatorClass.getName())
        return new Configuration().withGenerator(generator).withLogging(Logging.TRACE)
    }

    static Database createDatabase(String ddlDir, Set<ForcedType> forcedTypes) {
        def jsonType = new ForcedType(userType: JsonObject.class.getName(), converter:
            JsonObjectConverter.class.getName(), expression: DB.COL_REGEX.json, types: DB.TYPES.text)
        def jsonArrayType = new ForcedType(userType: JsonArray.class.getName(), converter:
            JsonArrayConverter.class.getName(), expression: DB.COL_REGEX.jsonArray, types: DB.TYPES.text)

        forcedTypes += [jsonType, jsonArrayType]
        logger.info("-" * 20)
        logger.info("Database Force Types")
        logger.info("-" * 20)
        forcedTypes.each { logger.info(it.toString()) }
        logger.info("")
        return new Database().withName("org.jooq.meta.extensions.ddl.DDLDatabase")
                             .withProperties(new Property().withKey("scripts").withValue(ddlDir))
                             .withUnsignedTypes(false).withIncludes(".*").withForcedTypes(forcedTypes)
    }

    static Target createTarget(String targetDir, String packageName) {
        return new Target().withPackageName(Utils.requireNotBlank(packageName, "packageName cannot be blank"))
                           .withDirectory(Utils.requireNotBlank(targetDir, "targetDir cannot be blank"))
                           .withClean(true).withEncoding(StandardCharsets.UTF_8.name())
    }

    static Generate createGenerate() {
        return new Generate().withDeprecated(false).withInterfaces(true).withPojos(true).withDaos(true)
                             .withFluentSetters(true).withJavaTimeTypes(false).withJpaAnnotations(true)
    }

    static Strategy createStrategy() { return new Strategy().withName(NubeGeneratorStrategy.class.getName()) }
}
