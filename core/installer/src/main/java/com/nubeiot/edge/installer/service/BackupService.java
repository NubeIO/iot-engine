package com.nubeiot.edge.installer.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.archiver.AsyncZipFolder;
import com.nubeiot.core.archiver.ZipArgument;
import com.nubeiot.core.archiver.ZipNotificationHandler;
import com.nubeiot.core.archiver.ZipOutput;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorData;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;
import com.nubeiot.edge.installer.service.InstallerApiIndex.ApplicationMetadata;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents Backup service.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BackupService implements InstallerService, ZipNotificationHandler {

    @NonNull
    private final InstallerEntityHandler entityHandler;

    protected abstract @NonNull Path backupFolder();

    @Override
    public final String servicePath() {
        return "/:app_id/backup";
    }

    @Override
    public final String paramPath() {
        return null;
    }

    @Override
    public final ActionMethodMapping methodMapping() {
        return ActionMethodMapping.create(Collections.singletonMap(EventAction.BACKUP, HttpMethod.POST));
    }

    @Override
    public final @NonNull Collection<EventAction> getAvailableEvents() {
        return Stream.of(ZipNotificationHandler.super.getAvailableEvents(), Collections.singleton(EventAction.BACKUP))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @EventContractor(action = EventAction.BACKUP, returnType = Single.class)
    public final Single<JsonObject> backup(@NonNull RequestData data) {
        final String appId = Strings.requireNotBlank(data.body().getString("app_id"), "Missing application id");
        return entityHandler.dao(TblModuleDao.class)
                            .findOneById(appId)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .switchIfEmpty(Single.error(ApplicationMetadata.INSTANCE.notFound(appId)))
                            .map(this::createBackupRecord)
                            .doOnSuccess(this::doBackup);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public final boolean success(@NonNull ZipOutput result) {
        return true;
    }

    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public final boolean error(@NonNull ErrorData error) {
        return true;
    }

    protected @NonNull JsonObject createBackupRecord(@NonNull TblModule app) {
        return new JsonObject().put("backup_id", UUID64.random())
                               .put("deploy_location", app.getDeployLocation())
                               .put("data_dir", app.getSystemConfig().getString(NubeConfig.DATA_DIR));
    }

    protected void doBackup(@NonNull JsonObject object) {
        final JsonObject trackingInfo = new JsonObject().put("backup_id", object.getString("backup_id"));
        final AsyncZipFolder zipper = AsyncZipFolder.builder()
                                                    .notifiedAddress(address())
                                                    .transporter(entityHandler.eventClient())
                                                    .build();
        zipper.run(ZipArgument.createDefault(trackingInfo.put("type", "deploy_location")), backupFolder(),
                   Paths.get(object.getString("deploy_location")));
        zipper.run(ZipArgument.createDefault(trackingInfo.put("type", "data_dir")), backupFolder(),
                   Paths.get(object.getString(NubeConfig.DATA_DIR)));
    }

}
