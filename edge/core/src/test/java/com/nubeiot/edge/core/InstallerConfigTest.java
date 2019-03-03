package com.nubeiot.edge.core;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.core.InstallerConfig.Credential;
import com.nubeiot.edge.core.InstallerConfig.RemoteUrl;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.core.loader.ModuleType;

public class InstallerConfigTest {

    @Test
    public void test_override_local_dir() {
        InstallerConfig installerConfig = new InstallerConfig();
        installerConfig.getRepoConfig().recomputeLocal(Paths.get("/data"));
        Assert.assertEquals("/data/repositories", installerConfig.getRepoConfig().getLocal());
    }

    @Test
    public void test_serialize_default() {
        InstallerConfig installerConfig = new InstallerConfig();
        RemoteRepositoryConfig remoteConfig = installerConfig.getRepoConfig().getRemoteConfig();
        remoteConfig.setCredential(new Credential("user", "password"));
        remoteConfig.addUrl(ModuleType.JAVA, new RemoteUrl("abc"));
        Assert.assertTrue(installerConfig.getBuiltinApps().isEmpty());
        JsonObject jsonObject = installerConfig.toJson();
        System.out.println(jsonObject.encodePrettily());
    }

    @Test
    public void test_parse_default() {
        InstallerConfig installerConfig = IConfig.from(
            "{\"auto_install\":true,\"repository\":{\"local\":\"file:///repository\"," +
            "\"remote\":{\"credential\":{\"user\":\"user\",\"password\":\"password\"}," +
            "\"urls\":{\"java\":[{\"url\":\"abc\"}, {\"credential\":{\"user\":\"u1\",\"password\":\"p1\"}," +
            "\"url\":\"xyz\"}]}}},\"builtin_app\":[{\"metadata\":{\"group_id\":\"com.nubeiot.edge.module\"," +
            "\"artifact_id\":\"bios-installer\",\"version\":\"1.0.0-SNAPSHOT\",\"service_name\":\"bios-installer\"}," +
            "\"appConfig\":{\"__sql__\":{\"dialect\":\"H2\",\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:" +
            "./bios-installer\",\"minimumIdle\":1,\"maximumPoolSize\":2,\"connectionTimeout\":30000," +
            "\"idleTimeout\":180000,\"maxLifetime\":300000}}}}]}", InstallerConfig.class);
        System.out.println(installerConfig.toJson().encodePrettily());
        Assert.assertTrue(installerConfig.isAutoInstall());
        Assert.assertNotNull(installerConfig.getRepoConfig());

        Assert.assertEquals("file:///repository", installerConfig.getRepoConfig().getLocal());
        Assert.assertEquals(FileUtils.toPath("file:///repository").toString(),
                            installerConfig.getRepoConfig().recomputeLocal(Paths.get("/data")));

        Assert.assertNotNull(installerConfig.getRepoConfig().getRemoteConfig());
        Credential credential = installerConfig.getRepoConfig().getRemoteConfig().getCredential();
        Assert.assertNotNull(credential);
        Assert.assertEquals("user", credential.getUser());
        Assert.assertEquals("password", credential.getPassword());

        Map<ModuleType, List<RemoteUrl>> urls = installerConfig.getRepoConfig().getRemoteConfig().getUrls();
        List<RemoteUrl> remoteUrls = urls.get(ModuleType.JAVA);
        Assert.assertEquals(2, remoteUrls.size());
        Assert.assertEquals("abc", remoteUrls.get(0).getUrl());
        Assert.assertNull(remoteUrls.get(0).getCredential());
        Assert.assertEquals("xyz", remoteUrls.get(1).getUrl());
        Assert.assertEquals("u1", remoteUrls.get(1).getCredential().getUser());
        Assert.assertEquals("p1", remoteUrls.get(1).getCredential().getPassword());

        Assert.assertEquals(1, installerConfig.getBuiltinApps().size());
        final RequestedServiceData serviceData = installerConfig.getBuiltinApps().get(0);
        Assert.assertEquals("com.nubeiot.edge.module", serviceData.getMetadata().getString("group_id"));
        Assert.assertNotNull(serviceData.getAppConfig());
    }

    @Test
    public void test_missing_repository_credential() {
        InstallerConfig installerConfig = IConfig.from("{\"__sql__\":{\"dialect\":\"H2\"," +
                                                       "\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:/data/db/bios\"}}," +
                                                       "\"__installer__\":{\"auto_install\":true," +
                                                       "\"repository\":{\"remote\":{\"urls\":{\"java\":[{\"url" +
                                                       "\":\"http://127.0.0.1:8081/repository/maven-releases/\"}," +
                                                       "{\"url\":\"http://127.0.0" +
                                                       ".1:8081/repository/maven-snapshots/\"},{\"url\":\"http://127" +
                                                       ".0.0.1:8081/repository/maven-central/\"}]}}}," +
                                                       "\"builtin_app\":[{\"metadata\":{\"group_id\":\"com.nubeiot" +
                                                       ".edge.module\",\"artifact_id\":\"bios-installer\"," +
                                                       "\"version\":\"1.0.0-SNAPSHOT\"," +
                                                       "\"service_name\":\"bios-installer\"}," +
                                                       "\"appConfig\":{\"__sql__\":{\"dialect\":\"H2\"," +
                                                       "\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:/data/db/bios" +
                                                       "-installer\"}}}}]}}",
                                                       InstallerConfig.class);
    }

}
