package com.nubeiot.edge.connector.bonescript.handlers;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.ID;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY_ARRAY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.edge.connector.bonescript.DittoDBOperation;
import com.nubeiot.edge.connector.bonescript.operations.Ditto;
import com.nubeiot.edge.connector.bonescript.operations.Historian;
import com.nubeiot.edge.connector.bonescript.utils.PointUtils;
import com.nubeiot.edge.connector.bonescript.validations.PointsUpdateValidation;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.Getter;
import lombok.NonNull;

public class PointsEventHandler implements EventHandler {

    private final Vertx vertx;
    @Getter
    private final List<EventAction> availableEvents;

    public PointsEventHandler(@NonNull Vertx vertx, @NonNull EventModel eventModel) {
        this.vertx = vertx;
        this.availableEvents = Collections.unmodifiableList(new ArrayList<>(eventModel.getEvents()));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> getList(RequestData data) {
        return DittoDBOperation.getDittoData()
                               .map(value -> (JsonObject) JsonUtils.getObject(value,
                                                                              "thing.features.points.properties"));
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patchPoints(RequestData data) {

        Validation<Object, ?> validation = new PointsUpdateValidation<>();

        return validation.validate(data.getBody()).flatMap(v -> DittoDBOperation.getDittoData().flatMap(db -> {
            JsonArray newPoints = (JsonArray) v.getData();
            return patchPoints(db, newPoints);
        }));
    }

    private Single<JsonObject> patchPoints(JsonObject db, JsonArray newPoints) {
        JsonObject points = (JsonObject) JsonUtils.getObject(db, "thing.features.points.properties");
        return patchablePoints(newPoints, db).flatMap(
            ignored -> Observable.fromIterable(newPoints).flatMapSingle(newPoint$ -> {
                AtomicBoolean updateDitto = new AtomicBoolean(false);
                JsonObject newPoint = (JsonObject) newPoint$;
                final String id = newPoint.getString("id");

                patchPoint(points, updateDitto, newPoint, id);
                patchAction(points, updateDitto, id);

                return Single.just(true);
            }).toList()).map(ignored -> new JsonObject());
    }

    // Returns NotFoundException on when user tries to update non-existing points
    private Single<List<Boolean>> patchablePoints(JsonArray newPoints, JsonObject db) {
        return Single.just(db).map(db$ -> {
            JsonObject points = (JsonObject) JsonUtils.getObject(db$, "thing.features.points.properties");
            if (points == null) {
                throw new NotFoundException("No any operation since DB doesn't contain any points");
            }
            return points;
        }).flatMap(points -> Observable.fromIterable(newPoints).flatMapSingle(newPoint$ -> {
            JsonObject newPoint = (JsonObject) newPoint$;
            if (!points.containsKey(newPoint.getString("id"))) {
                throw new NotFoundException(
                    "The request was not processed as point " + newPoint.getString("id") + " was not found");
            }
            return Single.just(true);
        }).toList());
    }

    // Updates the points values and updateDitto flag according to the newPoint
    protected void patchPoint(JsonObject points, AtomicBoolean updateDitto, JsonObject newPoint, String id) {
        newPoint.forEach(prop -> {
            Object value = prop.getValue();
            if (value == null) {
                value = "null";
            }

            if (prop.getKey().equals(VALUE)) {
                JsonObject oldPriorityArray = points.getJsonObject(id).getJsonObject(PRIORITY_ARRAY);

                points.getJsonObject(id)
                      .getJsonObject(PRIORITY_ARRAY)
                      .put(newPoint.getValue(PRIORITY).toString(), value);

                PointUtils.setValueAndPriority(id, points.getJsonObject(id));

                if (!updateDitto.get()) {
                    updateDitto.set(Ditto.getInstance()
                                         .shouldDittoUpdate(points.getJsonObject(id), prop.getKey(), value,
                                                            oldPriorityArray));
                }
            } else if (!SQLUtils.in(prop.getKey(), ID, PRIORITY)) {
                if (!updateDitto.get()) {
                    updateDitto.set(
                        Ditto.getInstance().shouldDittoUpdate(points.getJsonObject(id), prop.getKey(), value));
                }
                points.getJsonObject(id).put(prop.getKey(), value);
            }
        });
    }

    // Actions performed under patch requests
    private void patchAction(JsonObject points, AtomicBoolean updateDitto, String id) {
        long timestamp = new Date().getTime();
        Historian.recordCov(vertx, id, points.getJsonObject(id).getValue(VALUE), timestamp);

        if (updateDitto.get()) {
            Ditto.pointToDitto(vertx, points.getJsonObject(id), id, timestamp);
        }
    }

}
