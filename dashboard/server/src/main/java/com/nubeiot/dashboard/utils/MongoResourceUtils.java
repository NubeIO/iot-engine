package com.nubeiot.dashboard.utils;

import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;
import static com.nubeiot.dashboard.utils.MongoUtils.idQuery;

import com.nubeiot.core.http.RegisterScheme;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class MongoResourceUtils {

    public static String ABSOLUTE_PATH_SUFFIX = "_absolute_path";

    /**
     * @param mongoClient MongoClient's object for doing query
     * @param _id         _id of the MONGO_RESOURCE collection
     * @return returns the actual resource name, which is save on the system (saved on the format:
     *         {hash_id}{image_name}.{extension})
     */
    public static Single<String> convertIdToName(MongoClient mongoClient, String _id) {
        return mongoClient.rxFindOne(MEDIA_FILES, idQuery(_id), null).map(jsonObject -> {
            if (jsonObject != null) {
                return jsonObject.getString("name");
            } else {
                return "";
            }
        });
    }

    /**
     * @param mongoClient MongoClient's object for doing query
     * @param host        server host for building absolute path
     * @param jsonObject  JsonObject value where we want to store the absolute path
     * @param field       field name, whose value is going to be used for querying resource name from MongoDB and
     *                    finally the absolute value will be placed on {field}_absolute_path
     * @param mediaRoot   location where media files are stored
     * @return the input JsonObject with additional field {field}_absolute_path
     */
    public static Single<JsonObject> putAbsPath(MongoClient mongoClient, String host, JsonObject jsonObject,
                                                String field, String mediaRoot) {
        return MongoResourceUtils.convertIdToName(mongoClient, jsonObject.getString(field)).map(name -> {
            if (Strings.isNotBlank(name)) {
                putAbsPath(host, jsonObject, field, Urls.combinePath(mediaRoot, name));
            }
            return jsonObject;
        });
    }

    static void putAbsPath(String host, JsonObject jsonObject, String field, String relativePath) {
        if (Strings.isNotBlank(relativePath)) {
            jsonObject.put(field + ABSOLUTE_PATH_SUFFIX,
                           Urls.optimizeURL(RegisterScheme.getInstance().getHttpScheme().getScheme() + "://" + host,
                                            Urls.combinePath(relativePath)));
        }
    }

}
