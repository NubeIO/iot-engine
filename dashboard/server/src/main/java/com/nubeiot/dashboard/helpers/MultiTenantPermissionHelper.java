package com.nubeiot.dashboard.helpers;

import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetAdminWithManagerSelectionList;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.dashboard.Role;

public class MultiTenantPermissionHelper {

    public static SingleSource<List<JsonObject>> checkPermissionAndReturnValue(MongoClient mongoClient,
                                                                               String companyId, Role role,
                                                                               List<JsonObject> objects) {
        return Observable.fromIterable(objects)
            .flatMapSingle(
                object -> objectLevelPermission(mongoClient, companyId, role, object.getString("associated_company_id"))
                    .map(permitted -> {
                        if (!permitted) {
                            throw HttpException.forbidden();
                        }
                        return object;
                    })).toList();
    }

    public static Single<Boolean> objectLevelPermission(MongoClient mongoClient, String companyId, Role role,
                                                        String toCheckCompanyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(true);
        } else if (role == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionList(mongoClient, companyId).map(
                list -> list.contains(toCheckCompanyId));
        } else if (role == Role.MANAGER) {
            return Single.just(companyId.equals(toCheckCompanyId));
        }
        return Single.just(false);
    }

}
