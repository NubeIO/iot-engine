package com.nubeiot.edge.installer;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero88.utils.OSHelper;
import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.BasicCredential;
import com.nubeiot.auth.Credential;
import com.nubeiot.auth.CredentialType;
import com.nubeiot.auth.ExternalServer;
import com.nubeiot.core.IConfig;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;

public class InstallerConfigTest {

    @Test
    public void test_override_local_dir() {
        InstallerConfig installerConfig = new InstallerConfig();
        installerConfig.getRepoConfig().recomputeLocal(OSHelper.getAbsolutePathByOs("/data"));
        Assert.assertEquals(OSHelper.getAbsolutePathByOs("/data/repositories").toString(),
                            installerConfig.getRepoConfig().getLocal());
    }

    @Test
    public void test_serialize_default() {
        InstallerConfig installerConfig = new InstallerConfig();
        RemoteRepositoryConfig remoteConfig = installerConfig.getRepoConfig().getRemoteConfig();
        remoteConfig.setCredential(new BasicCredential(CredentialType.BASIC, "user", "password"));
        remoteConfig.addUrl(ModuleType.JAVA, new ExternalServer("abc"));
        Assert.assertTrue(installerConfig.getBuiltinApps().isEmpty());
        JsonObject jsonObject = installerConfig.toJson();
        System.out.println(jsonObject.encodePrettily());
    }

    @Test
    public void test_parse_default() {
        String localPath = OSHelper.getAbsolutePathByOs("/abc").toString();
        InstallerConfig installerConfig = IConfig.from(
            "{\"auto_install\":true,\"repository\":{\"local\":\"" + localPath.replaceAll("\\\\", "\\\\\\\\") + "\"," +
            "\"remote\":{\"credential\":{\"type\":\"BASIC\",\"user\":\"user\",\"password\":\"password\"}," +
            "\"urls\":{\"java\":[{\"url\":\"abc\"}, {\"credential\":{\"type\":\"BASIC\",\"user\":\"u1\"," +
            "\"password\":\"p1\"}," +
            "\"url\":\"xyz\"}]}}},\"builtin_app\":[{\"metadata\":{\"group_id\":\"com.nubeiot.edge.module\"," +
            "\"artifact_id\":\"bios-installer\",\"version\":\"1.0.0-SNAPSHOT\",\"service_name\":\"bios-installer\"}," +
            "\"appConfig\":{\"__sql__\":{\"dialect\":\"H2\",\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:" +
            "./bios-installer\",\"minimumIdle\":1,\"maximumPoolSize\":2,\"connectionTimeout\":30000," +
            "\"idleTimeout\":180000,\"maxLifetime\":300000}}}}]}", InstallerConfig.class);
        Assert.assertTrue(installerConfig.isAutoInstall());
        Assert.assertNotNull(installerConfig.getRepoConfig());

        Assert.assertEquals(localPath, installerConfig.getRepoConfig().getLocal());
        Assert.assertEquals(localPath,
                            installerConfig.getRepoConfig().recomputeLocal(OSHelper.getAbsolutePathByOs("/data")));

        Assert.assertNotNull(installerConfig.getRepoConfig().getRemoteConfig());
        Credential credential = installerConfig.getRepoConfig().getRemoteConfig().getCredential();
        Assert.assertNotNull(credential);
        Assert.assertEquals(CredentialType.BASIC, credential.getType());
        Assert.assertEquals("user", credential.getUser());
        Assert.assertEquals("password", ((BasicCredential) credential).getPassword());

        Map<ModuleType, List<ExternalServer>> urls = installerConfig.getRepoConfig().getRemoteConfig().getUrls();
        List<ExternalServer> externalServers = urls.get(ModuleType.JAVA);
        Assert.assertEquals(2, externalServers.size());
        Assert.assertEquals("abc", externalServers.get(0).getUrl());
        Assert.assertNull(externalServers.get(0).getCredential());
        Assert.assertEquals("xyz", externalServers.get(1).getUrl());
        Assert.assertEquals(CredentialType.BASIC, externalServers.get(1).getCredential().getType());
        Assert.assertEquals("u1", externalServers.get(1).getCredential().getUser());
        Assert.assertEquals("p1", ((BasicCredential) externalServers.get(1).getCredential()).getPassword());

        Assert.assertEquals(1, installerConfig.getBuiltinApps().size());
        final RequestedServiceData serviceData = installerConfig.getBuiltinApps().get(0);
        Assert.assertEquals("com.nubeiot.edge.module", serviceData.getMetadata().getString("group_id"));
        Assert.assertNotNull(serviceData.getAppConfig());
    }

}
