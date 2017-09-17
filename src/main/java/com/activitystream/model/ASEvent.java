package com.activitystream.model;

import com.activitystream.model.aspects.AspectManager;
import com.activitystream.model.aspects.ItemsManager;
import com.activitystream.model.config.JacksonMapper;
import com.activitystream.model.core.AbstractMapElement;
import com.activitystream.model.entities.EntityReference;
import com.activitystream.model.interfaces.AspectInterface;
import com.activitystream.model.interfaces.BaseStreamElement;
import com.activitystream.model.relations.Relation;
import com.activitystream.model.relations.RelationsManager;
import com.activitystream.model.stream.CustomerEvent;
import com.activitystream.model.stream.ImportanceLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;

public class ASEvent extends CustomerEvent {

    /**
     * Predefined AS Event Types (PAST)
     * Popular AS event types put here for convenience
     *
     * Custom event types are created just by adding them to the event as type.
     *
     * Please note. Some of these events are generic events but they can be made more specific by adding the classification aspect on them.
     * That way, for example, a "as.crm.message.sent" event could have the classification.type = 'email' and the classification.variant = 'marketing'
     * or the "as.crm.visit.started" have the classification.type = 'virtual' and the classification.variant = 'web'.
     *
     */
    public static enum PAST {

        AS_COMMERCE_PRODUCT_VIEWED,
        AS_COMMERCE_PRODUCT_SEARCHED,
        AS_COMMERCE_PRODUCT_CARTED,
        AS_COMMERCE_PRODUCT_UNCARTED,
        AS_COMMERCE_PRODUCT_UNAVAILABLE,

        AS_COMMERCE_ORDER_UPDATED,
        AS_COMMERCE_ORDER_ABANDONED,
        AS_COMMERCE_ORDER_DELIVERY_SELECTED,
        AS_COMMERCE_ORDER_RESERVATION_STARTED,
        AS_COMMERCE_ORDER_RESERVATION_TIMEOUT,
        AS_COMMERCE_ORDER_REVIEWED,

        AS_COMMERCE_PAYMENT_COMPLETED,
        AS_COMMERCE_PAYMENT_STARTED,
        AS_COMMERCE_PAYMENT_FAILED,
        AS_COMMERCE_PAYMENT_TIMEDOUT,

        AS_COMMERCE_TRANSACTION_COMPLETED,

        AS_COMMERCE_SHIPMENT_CREATED,
        AS_COMMERCE_SHIPMENT_PREPARED,
        AS_COMMERCE_SHIPMENT_PICKUP,
        AS_COMMERCE_SHIPMENT_HOP,
        AS_COMMERCE_SHIPMENT_DELIVERED,
        AS_COMMERCE_SHIPMENT_DELIVERY_ATTEMPTED,
        AS_COMMERCE_SHIPMENT_DELIVERY_FAILED,

        AS_PM_ISSUE_CREATED,
        AS_PM_ISSUE_ASSIGNED,
        AS_PM_ISSUE_PROMOTED,
        AS_PM_ISSUE_DEMOTED,
        AS_PM_ISSUE_SOLVED,
        AS_PM_ISSUE_CLOSED,
        AS_PM_ISSUE_REOPENED,
        AS_PM_ISSUE_UPDATED,
        AS_PM_ISSUE_RATED,
        AS_PM_ISSUE_COMMENT_CREATED,

        AS_CRM_VISIT_SCHEDULED,
        AS_CRM_VISIT_UNSCHEDULED,
        AS_CRM_VISIT_STARTED,
        AS_CRM_VISIT_ENDED,
        AS_CRM_VISIT_CANCELLED,

        AS_CRM_MESSAGE_SENT,
        AS_CRM_MESSAGE_BOUNCED,
        AS_CRM_MESSAGE_OPENED,
        AS_CRM_MESSAGE_CLICKED,
        AS_CRM_MESSAGE_SUBSCRIBED,
        AS_CRM_MESSAGE_UNSUBSCRIBED,

        AS_CRM_CONVERSATION_ATTEMPTED,
        AS_CRM_CONVERSATION_STARTED,
        AS_CRM_CONVERSATION_ENDED,

        AS_MARKETING_CONTENT_SHOWN,
        AS_MARKETING_CONTENT_CLICKED,

        AS_EVENT_TICKET_ISSUED,
        AS_EVENT_TICKET_INVALIDATED,
        AS_EVENT_TICKET_TRANSFERRED,
        AS_EVENT_TICKET_USED,
        AS_EVENT_TICKET_REUSED,
        AS_EVENT_SEAT_ASSIGNED,
        AS_EVENT_SEAT_UNASSIGNED,
        AS_EVENT_STARTS,
        AS_EVENT_ENDS,
        AS_EVENT_ANNOUNCED,
        AS_EVENT_CANCELLED,
        AS_EVENT_PRESALE_STARTS,
        AS_EVENT_ONSALE_STARTS,
        AS_EVENT_DOORS_OPEN,
        AS_EVENT_DOORS_CLOSE,

        AS_AUTHENTICATION_USER_CREATED,
        AS_AUTHENTICATION_USER_REMOVED,
        AS_AUTHENTICATION_USER_BLOCKED,
        AS_AUTHENTICATION_PASSWORD_SET,
        AS_AUTHENTICATION_PASSWORD_CHANGED,
        AS_AUTHENTICATION_PASSWORD_CONFIRMED,
        AS_AUTHENTICATION_EMAIL_CONFIRMED,

        AS_ACCESS_LOGIN_SUCCEED,
        AS_ACCESS_LOGIN_FAILED,
        AS_ACCESS_LOGOUT_SUCCEED,
        AS_ACCESS_BLOCKED,
    }

    public ASEvent() {
    }

    public ASEvent(PAST type, String origin, String description, ImportanceLevel importance, Object... involves) {
        this(type.toString().replaceAll("_",".").toLowerCase(), origin, description, importance, Arrays.asList(involves));
    }

    public ASEvent(PAST type, String origin) {
        this(type.toString().replaceAll("_",".").toLowerCase(), origin, null, ImportanceLevel.IMPORTANT, (Object) null);
    }

    public ASEvent(String type, String origin, String description, ImportanceLevel importance, Object... involves) {
        this(type, origin, description, importance, Arrays.asList(involves));
    }

    public ASEvent(String type, String origin, String description, ImportanceLevel importance, Object involves) {
        this(new DateTime(), type, origin, description, importance, involves);
    }

    public ASEvent(String occurred_at, PAST type, String origin, String description, ImportanceLevel importance, Object... involves) {
        this(occurred_at, type.toString().replaceAll("_",".").toLowerCase(), origin, description, importance, Arrays.asList(involves));
    }

    public ASEvent(String occurred_at, String type, String origin, String description, ImportanceLevel importance, Object involves) {
        this(DateTime.parse(occurred_at), type, origin, description, importance, involves);
    }

    public ASEvent(DateTime occurred_at, String type, String origin, String description, ImportanceLevel importance, Object involves) {
        super();
        put(ASConstants.FIELD_OCCURRED_AT, occurred_at);
        put(ASConstants.FIELD_TYPE, type);
        put(ASConstants.FIELD_ORIGIN, origin);

        if (description != null) put(ASConstants.FIELD_DESCRIPTION, description);

        List<Relation> allRelations = new LinkedList<>();
        if (involves != null) {
            if (involves instanceof String || involves instanceof ASEntity) {
                allRelations.add(new Relation("ACTOR", involves, this));
            } else if (involves instanceof List) {
                for (Object relation : (List<Relation>) involves) {
                    if (relation instanceof Relation) {
                        ((Relation)relation).setRoot(this);
                        allRelations.add((Relation) relation);
                    } else if (relation instanceof String) {
                        allRelations.add(new Relation("ACTOR", relation, this));
                    }
                }
            } else if (involves instanceof Relation) {
                Relation relation = (Relation) involves;
                relation.setRoot(this);
                allRelations.add(relation);
            }
        }
        RelationsManager relationsManager = getRelationsManager(true);
        relationsManager.addAll(allRelations);

        if (importance != null) put(ASConstants.FIELD_IMPORTANCE, importance.ordinal());
        else put(ASConstants.FIELD_IMPORTANCE, ImportanceLevel.NOT_IMPORTANT);

        HashMap<String, Object> aspects = new LinkedHashMap<>();
        if (!aspects.isEmpty()) put(ASConstants.FIELD_ASPECTS, aspects);
    }

    public ASEvent(Map map, BaseStreamElement root) {
        super(map, root);
    }

    public ASEvent(Map map) {
        super(map);
    }

    /************  UtilityFunctions ************/

    public ASEvent addType(PAST type) {
        put(ASConstants.FIELD_TYPE, type.toString().replaceAll("_",".").toLowerCase());
        return this;
    }

    public ASEvent addType(String type) {
        put(ASConstants.FIELD_TYPE, type);
        return this;
    }

    public ASEvent addOccurredAt(DateTime occurredAt) {
        put(ASConstants.FIELD_OCCURRED_AT, occurredAt);
        return this;
    }

    public ASEvent addOccurredAt(String occurredAt) {
        put(ASConstants.FIELD_OCCURRED_AT, occurredAt);
        return this;
    }

    public ASEvent addOrigin(String origin) {
        put(ASConstants.FIELD_ORIGIN, origin);
        return this;
    }

    public ASEvent addPartition(String partition) {
        put(ASConstants.FIELD_PARTITION, partition);
        return this;
    }

    public ASEvent addImportance(ImportanceLevel importance) {
        put(ASConstants.FIELD_IMPORTANCE, importance.ordinal());
        return this;
    }

    public ASEvent addImportance(Integer importance) {
        put(ASConstants.FIELD_IMPORTANCE, importance);
        return this;
    }

    public ASEvent addAspect(AspectInterface aspect) {
        if (!aspect.isEmpty()) super.addAspect(aspect, this);
        return this;
    }

    public ASEvent addRelation(String type, Object value) {
        this.getRelationsManager(true).addRelation(new Relation(type, value));
        return this;
    }

    public ASEvent addRelation(Relation relation) {
        this.getRelationsManager(true).addRelation(relation);
        return this;
    }

    public ASEvent addRelation(Relation relation, ASEntity entity) {
        if (entity != null) this.getRelationsManager(true).addRelation(relation);
        return this;
    }

    public ASEvent addDimensions(Map dimensionsMap) {
        super.addDimensions(dimensionsMap, this);
        return this;
    }

    public ASEvent addDimensions(String... dimensions) {
        super.addDimensions(this, dimensions);
        return this;
    }

    public ASEvent addDimension(String dimension, String value) {
        if (value != null && !value.isEmpty()) super.addDimension(dimension, value, this);
        return this;
    }

    @Override
    public ASEvent addMetrics(Map<String, Double> metricsMap, AbstractMapElement root) {
        super.addMetrics(metricsMap, root);
        return this;
    }

    @Override
    public ASEvent addMetric(AbstractMapElement root, Object... metrics) {
        super.addMetric(root, metrics);
        return this;
    }

    public ASEvent addMetric(String metric, double value) {
        super.addMetric(metric, value, this);
        return this;
    }

    @Override
    public ASEvent addMetric(String metric, double value, AbstractMapElement root) {
        super.addMetric(metric, value, root);
        return this;
    }

    public ASEvent addRelationIfValid(String type, String entityType, String entityId) {
        return addRelationIfValid(type, entityType, entityId, (Map) null, "Some");
    }

    public ASEvent addRelationIfValid(String type, String entityReference) {
        if (entityReference != null) {
            EntityReference entReference = new EntityReference(entityReference);
            if (entReference.isValid()) addRelationIfValid(type, entReference);
        }
        return this;
    }

    public ASEvent addRelationIfValid(String type, String entityType, String entityId, Map<String,Object> relationsProperties) {
        return addRelationIfValid(type, entityType, entityId, relationsProperties, "Some");
    }

    public ASEvent addRelationIfValid(String type, String entityType, String entityId, String mustBeValue) {
        return addRelationIfValid(type, entityType, entityId, (Map) null, "Some");
    }

    public ASEvent addRelationIfValid(String type, String entityType, String entityId, Map<String,Object> relationsProperties, String mustBeValue) {
        //todo - mustBeValue should be better implemented
        if (!entityType.isEmpty() && entityId != null && !entityId.isEmpty() && mustBeValue != null && !mustBeValue.isEmpty()) {
            Relation newRelation = new Relation(type, new EntityReference(entityType, entityId, this), this);
            if (relationsProperties != null) {
                newRelation.directPut("properties", relationsProperties);
            }
            this.getRelationsManager(true).addRelation(newRelation);
        }
        return this;
    }

    public ASEvent addRelationIfValid(String type, EntityReference entityRef) {
        if (entityRef != null) {
            this.getRelationsManager(true).addRelation(new Relation(type, entityRef));
        }
        return this;
    }

    @Override
    public ASEvent addProperties(Object... properties) {
        super.addProperties(properties);
        return this;
    }

    @Override
    public ASEvent addProperties(String property, Object value) {
        super.addProperties(property, value);
        return this;
    }

    /*
    Utilities
     */

    public ASEvent addLineItem(ASLineItem itemLine) {
        AspectManager aspectManager = getAspectManager(true);
        ItemsManager itemsManager = (ItemsManager) aspectManager.getOrCreateAspect(ItemsManager.ASPECT_TYPE.getAspectSignature());
        itemsManager.add(itemLine);
        return this;
    }

    public ASEvent mergeLineItem(ASLineItem itemLine) {
        AspectManager aspectManager = getAspectManager(true);
        ItemsManager itemsManager = (ItemsManager) aspectManager.getOrCreateAspect(ItemsManager.ASPECT_TYPE.getAspectSignature());
        itemsManager.mergeItemLine(itemLine);
        return this;
    }

    /*
    Serialization utils
     */

    public String toJSON() throws JsonProcessingException {
        return JacksonMapper.getMapper().writeValueAsString(this);
    }

    public static ASEvent fromJSON(String json) throws IOException {
        return JacksonMapper.getMapper().readValue(json, ASEvent.class);
    }




}
