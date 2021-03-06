package com.activitystream.core.model.aspects;

import com.activitystream.sdk.ASConstants;
import com.activitystream.core.model.interfaces.AspectInterface;
import com.activitystream.core.model.validation.AdjustedPropertyWarning;
import com.activitystream.core.model.validation.MissingPropertyError;
import com.activitystream.core.model.validation.UnsupportedAspectError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ABTestingAspect extends AbstractMapAspect implements AspectInterface {

    public static final AspectType ASPECT_TYPE = new AspectType(ASConstants.ASPECTS_AB_TEST, ABTestingAspect::new);

    protected static final Logger logger = LoggerFactory.getLogger(ABTestingAspect.class);

    public ABTestingAspect() {

    }

    @Override
    public void loadFromValue(Object value) {
        super.loadFromValue(value);
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

    public String getId() {
        return (String) get(ASConstants.FIELD_ID);
    }

    public void setId(String id) {
        if (id != null && !id.isEmpty()) put(ASConstants.FIELD_ID, id);
        else remove(ASConstants.FIELD_ID);
    }

    public ABTestingAspect withTestId(String id) {
        setId(id);
        return this;
    }

    public String getVariant() {
        return (String) get(ASConstants.FIELD_VARIANT);
    }

    public void setVariant(String variant) {
        if (variant != null && !variant.isEmpty()) put(ASConstants.FIELD_VARIANT, variant);
        else remove(ASConstants.FIELD_VARIANT);
    }

    public ABTestingAspect withVariant(String variant) {
        setVariant(variant);
        return this;
    }

    public String getOutcome() {
        return (String) get(ASConstants.FIELD_OUTCOME);
    }

    public void setOutcome(String outcome) {
        if (outcome != null && !outcome.isEmpty()) put(ASConstants.FIELD_OUTCOME, outcome);
        else remove(ASConstants.FIELD_OUTCOME);
    }

    public ABTestingAspect withOutcome(String outcome) {
        setOutcome(outcome);
        return this;
    }

    public Double getMetric() {
        return (Double) get(ASConstants.FIELD_METRIC);
    }

    public void setMetric(Number metric) {
        if (metric != null) put(ASConstants.FIELD_METRIC, metric.doubleValue());
        else remove(ASConstants.FIELD_METRIC);
    }

    public ABTestingAspect withMetric(Number metric) {
        setMetric(metric);
        return this;
    }

    public Double getAmount() {
        return (Double) get(ASConstants.FIELD_AMOUNT);
    }

    public void setAmount(Number amount) {
        if (amount != null) put(ASConstants.FIELD_AMOUNT, amount.doubleValue());
        else remove(ASConstants.FIELD_AMOUNT);
    }

    public ABTestingAspect withAmount(Number amount) {
        setAmount(amount);
        return this;
    }

    //todo - create Esper friendly property getters

    /************ Assignment & Validation ************/

    @Override
    public Object put(Object key, Object value) {

        String theKey = key.toString();
        String theLCKey = theKey.toLowerCase();
        if (!theKey.equals(theLCKey)) {
            this.addProblem(new AdjustedPropertyWarning("The property name: '" + theKey + "' was converted to lower case"));
            theKey = theLCKey;
        }

        //Todo implement aspects for dimensions and metrics
        switch (theKey) {
            case ASConstants.FIELD_ID:
                value = validator().processIdString(theKey, value, true);
                break;
            case ASConstants.FIELD_VARIANT:
            case ASConstants.FIELD_OUTCOME:
                value = validator().processString(theKey, value, false);
                break;
            case ASConstants.FIELD_METRIC:
            case ASConstants.FIELD_AMOUNT:
                value = validator().processDouble(theKey, value, false, null, null);
                break;
            case ASConstants.FIELD_PROPERTIES:
                value = validator().processMap(theKey, value, false);
                break;
            default:
                this.addProblem(new UnsupportedAspectError("The " + theKey + " property is not supported for the Client Device Aspect"));
        }
        return super.put(theKey, value);
    }

    @Override
    public void verify() {
        if (!containsKey(ASConstants.FIELD_ID)) {
            addProblem(new MissingPropertyError("No AB test ID was provides."));
        }
        super.verify();
    }

    public static ABTestingAspect abTest() {
        return new ABTestingAspect();
    }

}
