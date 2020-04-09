package com.nubeiot.edge.installer.mock;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ApplicationRule;
import com.nubeiot.edge.installer.loader.RuleRepository;
import com.nubeiot.edge.installer.loader.VertxModuleType;
import com.nubeiot.edge.installer.mock.MockInstallerService.MockApplicationService;
import com.nubeiot.edge.installer.mock.MockInstallerService.MockTransactionService;
import com.nubeiot.edge.installer.service.AppDeployerDefinition;
import com.nubeiot.edge.installer.service.InstallerService;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class MockInstallerVerticle extends InstallerVerticle {

    private final AppDeployerDefinition definition;
    private String configFile = "mock-installer.json";

    @Override
    protected @NonNull Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return MockInstallerEntityHandler.class;
    }

    @Override
    protected @NonNull AppDeployerDefinition appDeployerDefinition() {
        return definition;
    }

    protected @NonNull RuleRepository ruleRepository() {
        return new RuleRepository().add(VertxModuleType.JAVA, ApplicationRule.jvmRule("com.nubeiot.edge.mock"));
    }

    @Override
    protected @NonNull Supplier<Set<? extends InstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> Stream.of(new MockApplicationService(handler), new MockTransactionService(handler))
                           .collect(Collectors.toSet());
    }

    @Override
    public String configFile() {
        return configFile;
    }

}
