package com.activitystream.core.model.aspects;

import com.activitystream.sdk.ASConstants;
import com.activitystream.core.model.entities.BusinessEntity;
import com.activitystream.core.model.entities.EntityReference;
import com.activitystream.core.model.interfaces.*;
import com.activitystream.core.model.utils.StreamIdUtils;
import com.activitystream.core.model.validation.AdjustedPropertyWarning;
import com.activitystream.core.model.validation.IgnoredPropertyError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class ClientDeviceAspect extends AbstractMapAspect implements CompactableElement, LinkedElement, EnrichableElement {

    public static final AspectType ASPECT_TYPE = new AspectType(ASConstants.ASPECTS_CLIENT_DEVICE, ClientDeviceAspect::new) {
    };

    protected static final Logger logger = LoggerFactory.getLogger(ClientDeviceAspect.class);

    public ClientDeviceAspect() {
    }

    public ClientDeviceAspect(String userAgent, BaseStreamElement root) {
        setRoot(root);
        put(ASConstants.FIELD_USER_AGENT, userAgent);
    }

    @Override
    public void loadFromValue(Object m) {
        if (m instanceof String) put(ASConstants.FIELD_USER_AGENT, m);
        else super.loadFromValue(m);
    }

    /************
     * Utility functions
     ************/

    @Override
    public AspectType getAspectType() {
        return ASPECT_TYPE;
    }

    //todo create enrichment getters

    /************
     * CEP Utility Functions and Getters
     ************/


    public String getUserAgent() {
        return (String) get(ASConstants.FIELD_USER_AGENT);
    }

    public ClientDeviceAspect withUserAgent(String userAgent) {
        if (userAgent != null && !userAgent.isEmpty()) put(ASConstants.FIELD_USER_AGENT,userAgent);
        else remove(ASConstants.FIELD_USER_AGENT);
        return this;
    }

    @Override
    public void compact() {
    }

    private String getUserAgentSignature() {
        return getUserAgent().replaceAll("/", "--").replaceAll(",", "").replaceAll(";", "").replaceAll("\\.", "_").replaceAll("\\(", "[").replaceAll("\\)", "]")
                .replaceAll("  ", " ").replaceAll(" ", "_");
    }

    private EntityReference getUserAgentEntityRef() {
        return new EntityReference(ASConstants.AS_CLIENT_DEVICE, StreamIdUtils.calculateStreamId(getUserAgentSignature()).toString());
    }

    @Override
    public void onEachEntityReference(Consumer<EntityReference> action) {
        if (containsKey(ASConstants.FIELD_USER_AGENT)) {
            EntityReference agentReference = getUserAgentEntityRef();
            agentReference.setDefaults(new HashMap<String, Object>() {{
                put("signature", getUserAgent());
            }});
            action.accept(agentReference);
        }
    }

    @Override
    public void onEachRelationType(StreamItemRelationTypeConsumer action) {
        if (getRoot() instanceof BusinessEntity)
            action.accept(ASConstants.REL_USES_DEVICE, ((BusinessEntity) getRoot()).getElementType(), ASConstants.AS_CLIENT_DEVICE);
    }

    /************ Assignment & Validation ************/

    @Override
    public Object put(Object key, Object value) {

        String theKey = key.toString();
        String theLCKey = theKey.toLowerCase();
        if (!theKey.equals(theLCKey)) {
            this.addProblem(new AdjustedPropertyWarning("The property name: '" + theKey + "' was converted to lower case"));
            theKey = theLCKey;
        }

        switch (theKey) {
            case ASConstants.FIELD_USER_AGENT:
            case "bind_to":
            case ASConstants.FIELD_TRACK_FOR:
                break;
            default:
                if (!theKey.startsWith("_")) {
                    this.addProblem(new IgnoredPropertyError("The " + theKey + " property is not supported for the Client Device Aspect"));
                    return null;
                }
        }
        return super.put(theKey, value);
    }

    @Override
    public void verify() {

    }

    public static ClientDeviceAspect clientDevice() {
        return new ClientDeviceAspect();
    }

    public static ClientDeviceAspect clientDevice(String clientDevice) {
        return new ClientDeviceAspect(clientDevice, null);
    }
}
