package com.activitystream.core.model.aspects;

import com.activitystream.sdk.ASConstants;
import com.activitystream.core.model.validation.AdjustedPropertyWarning;
import com.activitystream.core.model.validation.IgnoredPropertyError;

public class ResolvableAspect extends AbstractMapAspect {

    public static final AspectType ASPECT_TYPE = new AspectType(ASConstants.ASPECTS_RESOLVABLE, ResolvableAspect::new);

    public ResolvableAspect() {
    }

    public ResolvableAspect(String externalId) {
        setExternalId(externalId);
    }

    @Override
    public void loadFromValue(Object value) {
        if (value instanceof String) {
            put("external_id", value);
        } else {
            super.loadFromValue(value);
        }
    }

    /************
     * Utility functions
     ************/

    @Override
    public AspectType getAspectType() {
        return ASPECT_TYPE;
    }

    /************
     * CEP Utility Functions and Getters
     ************/

    public String getExternalId() {
        return (String) get(ASConstants.FIELD_EXTERNAL_ID);
    }

    public void setExternalId(String externalId) {
        if (externalId != null && !externalId.isEmpty()) put(ASConstants.FIELD_EXTERNAL_ID, externalId);
        else remove(ASConstants.FIELD_EXTERNAL_ID);
    }

    public ResolvableAspect withExternalId(String externalId) {
        setExternalId(externalId);
        return this;
    }

    public String getBatchId() {
        return (String) get(ASConstants.FIELD_BATCH_ID);
    }

    public void setBatchId(String batchId) {
        if (batchId != null && !batchId.isEmpty()) put(ASConstants.FIELD_BATCH_ID, batchId);
        else remove(ASConstants.FIELD_BATCH_ID);
    }

    public ResolvableAspect withBatchId(String batchId) {
        setBatchId(batchId);
        return this;
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
            case ASConstants.FIELD_EXTERNAL_ID:
                value = validator().processString(theKey, value, true);
                break;
            case ASConstants.FIELD_BATCH_ID:
                value = validator().processString(theKey, value, false);
                break;
            default:
                this.addProblem(new IgnoredPropertyError("The " + theKey + " property is not supported for the Resolvable Aspect"));
        }
        return super.put(key, value);
    }

    @Override
    public void verify() {

    }

    public static ResolvableAspect resolvable() {
        return new ResolvableAspect();
    }

    public static ResolvableAspect resolvable(String externalId) {
        return new ResolvableAspect(externalId);
    }

}
