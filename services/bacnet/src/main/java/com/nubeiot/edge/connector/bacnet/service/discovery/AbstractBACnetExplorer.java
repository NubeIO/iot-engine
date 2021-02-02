package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.List;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventContractor;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.iotdata.IoTEntities;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

/**
 * Defines public service to expose HTTP API for end-user and/or nube-io service
 */
abstract class AbstractBACnetExplorer<K, P extends BACnetEntity<K>, X extends IoTEntities<K, P>>
    extends AbstractBACnetService implements BACnetExplorer<K, P, X> {

    AbstractBACnetExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    @EventContractor(action = "GET_ONE", returnType = Single.class)
    public abstract Single<P> discover(RequestData reqData);

    @Override
    @EventContractor(action = "GET_LIST", returnType = Single.class)
    public abstract Single<X> discoverMany(RequestData reqData);

    protected final Single<PropertyValuesMixin> parseRemoteObject(@NonNull BACnetDevice device,
                                                                  @NonNull RemoteDevice remoteDevice,
                                                                  @NonNull ObjectIdentifier objId, boolean detail,
                                                                  boolean includeError) {
        return Observable.fromIterable(getObjectTypes(detail, objId.getObjectType()))
                         .map(definition -> new ObjectPropertyReference(objId, definition.getPropertyTypeDefinition()
                                                                                         .getPropertyIdentifier()))
                         .collect(PropertyReferences::new,
                                  (refs, opr) -> refs.addIndex(objId, opr.getPropertyIdentifier(),
                                                               opr.getPropertyArrayIndex()))
                         .map(propertyRefs -> RequestUtils.readProperties(device.localDevice(), remoteDevice,
                                                                          propertyRefs, true, null))
                         .map(pvs -> PropertyValuesMixin.create(objId, pvs, includeError));
    }

    protected List<ObjectPropertyTypeDefinition> getObjectTypes(boolean detail, @NonNull ObjectType objectType) {
        return detail
               ? ObjectProperties.getObjectPropertyTypeDefinitions(objectType)
               : ObjectProperties.getRequiredObjectPropertyTypeDefinitions(objectType);
    }

}
