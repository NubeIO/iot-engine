package com.nubeiot.dashboard.helpers;

import static com.nubeiot.dashboard.helpers.MultiTenantQueryBuilderHelper.byAdminCompanyGetAdminWithManagerSelectionList;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.dashboard.Role;

public class MultiTenantPermissionHelper {

    public static Single<List<Boolean>> checkPermissionAndReturnValue(MongoClient mongoClient,
                                                                      String companyId, Role role,
                                                                      List<JsonObject> objects) {
        return Observable
            .fromIterable(objects)
            .flatMapSingle(object -> objectLevelPermission(mongoClient, companyId, role,
                                                           object.getString("associated_company_id")))
            .toList();
    }

    public static Single<Boolean> objectLevelPermission(MongoClient mongoClient, String companyId, Role role,
                                                        String toCheckCompanyId) {
        if (role == Role.SUPER_ADMIN) {
            return Single.just(true);
        } else if (role == Role.ADMIN) {
            return byAdminCompanyGetAdminWithManagerSelectionList(mongoClient, companyId).map(list -> {
                if (!list.contains(toCheckCompanyId)) {
                    throw HttpException.forbidden();
                } else {
                    return true;
                }
            });
        } else if (role == Role.MANAGER) {
            if (companyId.equals(toCheckCompanyId)) {
                return Single.just(true);
            } else {
                throw HttpException.forbidden();
            }
        }
        return Single.just(false);
    }

    public static JsonObject nullableCheck(JsonObject object, String message) {
        if (object != null) {
            return object;
        } else {
            throw HttpException.badRequest(message);
        }
    }

}
