package com.nubeiot.edge.installer.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Field;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.archiver.AsyncZipFolder;
import com.nubeiot.core.archiver.ZipArgument;
import com.nubeiot.core.archiver.ZipNotificationHandler;
import com.nubeiot.core.archiver.ZipOutput;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorData;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.service.AbstractReferencingEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationDao;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.ApplicationBackup;
import com.nubeiot.edge.installer.service.InstallerApiIndex.ApplicationMetadata;
import com.nubeiot.edge.installer.service.InstallerApiIndex.BackupMetadata;

import lombok.NonNull;

/**
 * Represents Backup service.
 *
 * @since 1.0.0
 */
public abstract class BackupByAppService extends AbstractReferencingEntityService<ApplicationBackup, BackupMetadata>
    implements InstallerService, ZipNotificationHandler {

    protected BackupByAppService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public BackupMetadata context() {
        return BackupMetadata.INSTANCE;
    }

    @Override
    public final String servicePath() {
        return "/:app_id/backup";
    }

    @Override
    public final String paramPath() {
        return "backup_id";
    }

    @Override
    public final ActionMethodMapping methodMapping() {
        final Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.BACKUP, HttpMethod.POST);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        return ActionMethodMapping.create(map);
    }

    @Override
    public final @NonNull Collection<EventAction> getAvailableEvents() {
        return Stream.of(ZipNotificationHandler.super.getAvailableEvents(),
                         Arrays.asList(EventAction.BACKUP, EventAction.GET_ONE))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    public final EntityReferences referencedEntities() {
        return new EntityReferences().add(ApplicationMetadata.INSTANCE);
    }

    @EventContractor(action = EventAction.BACKUP, returnType = Single.class)
    public final Single<JsonObject> backup(@NonNull RequestData data) {
        final String appId = Strings.requireNotBlank(data.body().getString("app_id"), "Missing application id");
        return entityHandler().dao(ApplicationDao.class)
                              .findOneById(appId)
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .switchIfEmpty(Single.error(ApplicationMetadata.INSTANCE.notFound(appId)))
                              .flatMap(this::createBackupRecord)
                              .doOnSuccess(this::doBackup);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public final boolean success(@NonNull ZipOutput result) {
        final ApplicationBackup backup = context().parseFromRequest(result.getTrackingInfo());
        final String type = result.getTrackingInfo().getString("type");
        patch(RequestData.builder()
                         .body(backup.toJson()
                                     .put("status", Status.SUCCESS)
                                     .put(type, result.toJson(Collections.singleton("trackingInfo"))))
                         .build());
        return true;
    }

    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public final boolean error(@NonNull ErrorData error) {
        final ApplicationBackup backup = context().parseFromRequest(error.getExtraInfo());
        patch(RequestData.builder()
                         .body(backup.toJson().put("status", Status.FAILED).put("error", error.getError().toJson()))
                         .build());
        return true;
    }

    protected @NonNull Path backupFolder() {
        return entityHandler().dataDir().resolve("backup");
    }

    protected @NonNull Single<JsonObject> createBackupRecord(@NonNull Application app) {
        final ApplicationBackup backup = new ApplicationBackup().setAppId(app.getAppId()).setStatus(Status.INITIAL);
        final String idField = jsonField(context().table().ID);
        return create(RequestData.builder().body(backup.toJson()).build()).map(
            json -> new JsonObject().put(context().requestKeyName(), UUID64.uuidToBase64(json.getString(idField)))
                                    .put(jsonField(context().table().APP_ID), app.getAppId())
                                    .put(jsonField(context().table().INSTALLATION_DIR), app.getDeployLocation())
                                    .put(jsonField(context().table().DATA_DIR),
                                         app.getSystemConfig().getString(NubeConfig.DATA_DIR)));
    }

    protected void doBackup(@NonNull JsonObject record) {
        final JsonObject info = new JsonObject().put(context().requestKeyName(),
                                                     record.getString(context().requestKeyName()))
                                                .put(ApplicationMetadata.INSTANCE.requestKeyName(),
                                                     record.getString(ApplicationMetadata.INSTANCE.requestKeyName()));
        final AsyncZipFolder zipper = AsyncZipFolder.builder()
                                                    .notifiedAddress(address())
                                                    .transporter(entityHandler().eventClient())
                                                    .build();
        //        final String installationDirField = jsonField(context().table().INSTALLATION_DIR);
        final String dataDirField = jsonField(context().table().DATA_DIR);
        //        zipper.run(ZipArgument.createDefault(info.put("type", installationDirField)), backupFolder(),
        //                   Paths.get(installationDirField));
        zipper.zip(ZipArgument.createDefault(info.put("type", dataDirField)), backupFolder(), Paths.get(dataDirField));
    }

    private String jsonField(Field installation_dir) {
        return context().table().getJsonField(installation_dir);
    }

}
