package com.nubeiot.dashboard.helpers;

import static com.nubeiot.core.mongo.MongoUtils.idQuery;
import static com.nubeiot.dashboard.constants.Collection.COMPANY;
import static com.nubeiot.dashboard.constants.Collection.SITE;
import static com.nubeiot.dashboard.constants.Collection.USER;
import static com.nubeiot.dashboard.constants.Collection.USER_GROUP;

import io.github.zero88.utils.Strings;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.handler.ResponseDataWriter;

public class MultiTenantRepresentationHelper {

    public static void userRepresentation(MongoClient mongoClient, JsonObject query, Future<ResponseData> future) {
        mongoClient.rxFind(USER, query)
                   .flatMap(response -> Observable.fromIterable(response)
                                                  .flatMapSingle(object ->
                                   associatedCompanyRepresentation(mongoClient, object)
                                       .flatMap(obj -> companyRepresentation(mongoClient, obj))
                                       .flatMap(obj -> siteRepresentation(mongoClient, obj))
                                       .flatMap(obj -> groupRepresentation(mongoClient, obj))).toList())
                   .subscribe(
                       response -> future.complete(ResponseDataWriter.serializeResponseData(response.toString())),
                       throwable -> future.complete(ResponseDataConverter.convert(throwable)));
    }

    public static Single<JsonObject> associatedCompanyRepresentation(MongoClient mongoClient, JsonObject object) {
        return mongoClient
            .rxFindOne(COMPANY, idQuery(object.getString("associated_company_id")), null)
            .map(associatedCompany -> associatedCompany == null
                                      ? object
                                      : object.put("associated_company", associatedCompany));
    }

    public static Single<JsonObject> companyRepresentation(MongoClient mongoClient, JsonObject object) {
        return mongoClient
            .rxFindOne(COMPANY, idQuery(object.getString("company_id")), null)
            .map(company -> company == null ? object : object.put("company", company));
    }

    public static Single<JsonObject> groupRepresentation(MongoClient mongoClient, JsonObject object) {
        if (Strings.isNotBlank(object.getString("group_id"))) {
            return mongoClient
                .rxFindOne(USER_GROUP, idQuery(object.getString("group_id")), null)
                .map(group -> group == null ? object : object.put("group", group));
        } else {
            return Single.just(object);
        }
    }

    public static Single<JsonObject> siteRepresentation(MongoClient mongoClient, JsonObject object) {
        if (Strings.isNotBlank(object.getString("site_id"))) {
            return mongoClient
                .rxFindOne(SITE, idQuery(object.getString("site_id")), null)
                .map(site -> site == null ? object : object.put("site", site));
        } else {
            return Single.just(object);
        }
    }

}
