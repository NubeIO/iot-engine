package com.nubeiot.edge.installer.mock;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.mock.MockInstallerService.MockModuleService;
import com.nubeiot.edge.installer.mock.MockInstallerService.MockTransactionService;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.edge.installer.service.InstallerService;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class MockInstallerVerticle extends InstallerVerticle {

    private final AppDeployer appDeployer;
    private String configFile = "mock-installer.json";

    @Override
    protected @NonNull Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return MockInstallerEntityHandler.class;
    }

    @Override
    protected @NonNull Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return () -> new ModuleTypeRule().registerRule(ModuleType.JAVA,
                                                       Collections.singletonList("com.nubeiot.edge.module"));
    }

    @Override
    protected @NonNull AppDeployer appDeployer() {
        return appDeployer;
    }

    @Override
    protected @NonNull Supplier<Set<? extends InstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> Stream.of(new MockModuleService(handler), new MockTransactionService(handler))
                           .collect(Collectors.toSet());
    }

    @Override
    public String configFile() {
        return configFile;
    }

}
