package com.nubeiot.edge.installer.loader;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.edge.installer.model.tables.pojos.Application;

public class ApplicationRuleTest {

    @Test(expected = InvalidModuleType.class)
    public void test_rule_invalid() {
        final RuleRepository ruleRepository = RuleRepository.createJVMRule("");
        final Application application = new Application().setAppId(null).setServiceType(VertxModuleType.JAVA);
        ruleRepository.get(VertxModuleType.JAVA).validate(application);
    }

    @Test
    public void test_jvm_rule_one_artifact_group() {
        final RuleRepository repo = RuleRepository.createJVMRule("com.nubeio.test");
        final Application application = new Application().setAppId("com.nubeio.test.app");
        Assert.assertNotNull(repo.get(VertxModuleType.JAVA).validate(application));
        Assert.assertNotNull(repo.get(VertxModuleType.GROOVY).validate(application));
        Assert.assertNotNull(repo.get(VertxModuleType.KOTLIN).validate(application));
        Assert.assertNotNull(repo.get(VertxModuleType.SCALA).validate(application));
    }

    @Test
    public void test_jvm_rule_many_artifact_groups() {
        final RuleRepository repo = RuleRepository.createJVMRule("com.nubeio.test", "com.nubeio.hub");
        Assert.assertNotNull(
            repo.get(VertxModuleType.JAVA).validate(new Application().setAppId("com.nubeio.test.app")));
        Assert.assertNotNull(
            repo.get(VertxModuleType.KOTLIN).validate(new Application().setAppId("com.nubeio.hub.app")));
    }

}
