package com.nubeiot.dashboard.helpers;

import static com.nubeiot.dashboard.constants.Collection.COMPANY;

import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.dashboard.Role;

public class MultiTenantQueryBuilderHelper {

    public static Single<JsonObject> byAdminCompanyGetAdminWithManagerSelectionListQuery(MongoClient mongoClient,
                                                                                         String companyId) {
        return mongoClient
            .rxFind(COMPANY,
                    new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
            .map(response -> {
                JsonObject associatedCompanyIds =
                    new JsonObject().put("$in", MongoUtils.getIdsOnJsonArray(response).add(companyId));
                return new JsonObject().put("associated_company_id", associatedCompanyIds);
            });
    }

    public static Single<List<String>> byAdminCompanyGetAdminWithManagerSelectionList(MongoClient mongoClient,
                                                                                      String companyId) {
        return mongoClient
            .rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
                .put("role", Role.MANAGER.toString()))
            .map(response -> {
                List<String> companies = MongoUtils.getIdsOnList(response);
                companies.add(companyId);
                return companies;
            });
    }

    public static Single<JsonObject> byAdminCompanyGetManagerSelectionListQuery(MongoClient mongoClient,
                                                                                String companyId) {
        return mongoClient.rxFind(COMPANY, new JsonObject().put("associated_company_id", companyId)
            .put("role", Role.MANAGER.toString()))
            .map(response ->
                     new JsonObject()
                         .put("associated_company_id", new JsonObject()
                             .put("$in", MongoUtils.getIdsOnJsonArray(response))));
    }

    public static Single<List<String>> byAdminCompanyGetManagerSelectionList(MongoClient mongoClient,
                                                                             String companyId) {
        return mongoClient
            .rxFind(COMPANY,
                    new JsonObject().put("associated_company_id", companyId).put("role", Role.MANAGER.toString()))
            .map(MongoUtils::getIdsOnList);
    }

}
