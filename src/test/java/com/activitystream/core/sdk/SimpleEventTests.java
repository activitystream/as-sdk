package com.activitystream.core.sdk;

import com.activitystream.core.model.relations.ASEntityRelationTypes;
import com.activitystream.core.model.relations.ASEventRelationTypes;
import com.activitystream.core.model.relations.Relation;
import com.activitystream.core.model.stream.ImportanceLevel;
import com.activitystream.sdk.ASConstants;
import com.activitystream.sdk.ASEntity;
import com.activitystream.sdk.ASEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.IOException;

import static com.activitystream.core.model.aspects.ClassificationAspect.classification;
import static com.activitystream.core.model.aspects.ClientDeviceAspect.clientDevice;
import static com.activitystream.core.model.aspects.ClientIpAspect.clientIP;
import static com.activitystream.core.model.aspects.PresentationAspect.presentation;

public class SimpleEventTests {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventTests.class);

    @Test
    public void basicASEventTests() throws IOException {

        //Minimum valid Message
        ASEvent webVisitStarts = new ASEvent()
                .withType(ASEvent.PRE.AS_CRM_VISIT_STARTED)
                .withOrigin("wwww.mysite.domain")
                .withOccurredAt("2017-01-01T00:00:00.000Z")
                .withRelationIfValid(ASEventRelationTypes.ACTOR,"Customer","007")
                .withAspect(classification()
                        .withType("virtual")
                        .withVariant("web"))
                .withAspect(clientIP("127.0.0.1"))
                .withAspect(clientDevice("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36"));

        Assert.assertEquals(webVisitStarts.toJSON().equals("{\"type\":\"as.crm.visit.started\",\"origin\":\"wwww.mysite.domain\",\"occurred_at\":\"2017-01-01T00:00:00.000Z\",\"involves\":[{\"ACTOR\":{\"entity_ref\":\"Customer/007\"}}],\"aspects\":{\"classification\":{\"type\":\"virtual\",\"variant\":\"web\"},\"client_ip\":{\"ip\":\"127.0.0.1\"},\"client_device\":{\"user_agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36\"}}}"),true);

        ASEntity customer = new ASEntity("Customer","30893928")
                .withAspect(presentation()
                        .withLabel("John Doe"))
                .withRelationIfValid(ASEntityRelationTypes.AKA,"Email", "john.doe@gmail.com")
                .withRelationIfValid(ASEntityRelationTypes.AKA,"Phone", "+150012348765");

        webVisitStarts = new ASEvent()
                .withType(ASEvent.PRE.AS_CRM_VISIT_STARTED)
                .withOrigin("wwww.mysite.domain")
                .withOccurredAt("2017-01-01T00:00:00.000Z")
                .withRelation(ASEventRelationTypes.ACTOR,customer)
                .withAspect(classification()
                        .withType("virtual")
                        .withVariant("web"))
                .withAspect(clientIP("127.0.0.1"))
                .withAspect(clientDevice("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36"));

        Assert.assertEquals(webVisitStarts.toJSON().equals("{\"type\":\"as.crm.visit.started\",\"origin\":\"wwww.mysite.domain\",\"occurred_at\":\"2017-01-01T00:00:00.000Z\",\"involves\":[{\"ACTOR\":{\"entity_ref\":\"Customer/30893928\",\"aspects\":{\"presentation\":{\"label\":\"John Doe\"}},\"relations\":[{\"AKA\":{\"entity_ref\":\"Email/john.doe@gmail.com\"}},{\"AKA\":{\"entity_ref\":\"Phone/+150012348765\"}}]}}],\"aspects\":{\"classification\":{\"type\":\"virtual\",\"variant\":\"web\"},\"client_ip\":{\"ip\":\"127.0.0.1\"},\"client_device\":{\"user_agent\":\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36\"}}}"),true);
    }

    @Test
    public void testBasicMessageValidation() throws IOException {

        //Minimum valid Message
        ASEvent asEvent = new ASEvent()
                .withType("as.application.authentication.login")
                .withOrigin("your.web.application")
                .withOccurredAt("2017-01-01T00:00:00.000Z")
                .withImportance(ImportanceLevel.NOT_IMPORTANT)
                .withRelationIfValid(ASEventRelationTypes.ACTOR, "Customer","314");

        Assert.assertEquals(asEvent.isValid(), true);
        Assert.assertEquals(asEvent.validator().hasErrors(), 0);
        Assert.assertEquals(asEvent.toJSON().equals("{\"type\":\"as.application.authentication.login\",\"origin\":\"your.web.application\",\"occurred_at\":\"2017-01-01T00:00:00.000Z\",\"importance\":2,\"involves\":[{\"ACTOR\":{\"entity_ref\":\"Customer/314\"}}]}"), true);

        //Valid Message
        asEvent = new ASEvent("as.commerce.purchase.complete", "as.sdk.test", null, ImportanceLevel.NOT_IMPORTANT, "Customer/314");
        Assert.assertEquals(asEvent.isValid(), true);
        Assert.assertEquals(asEvent.validator().hasErrors(), 0);

        //Invalid Type string
        asEvent = new ASEvent("as.commerce. invalid purchase.complete", "as.sdk.test", null, ImportanceLevel.NOT_IMPORTANT, "Customer/314");
        Assert.assertEquals(asEvent.isValid(), false);
        Assert.assertEquals(asEvent.validator().hasErrors(), 1);
        logger.debug("Error: " + asEvent.validator().getErrors());

        //Invalid Origin
        asEvent = new ASEvent("as.commerce.purchase.complete", "as.sdk invalid .test", null, ImportanceLevel.NOT_IMPORTANT, "Customer/314");
        Assert.assertEquals(asEvent.isValid(), false);
        Assert.assertEquals(asEvent.validator().hasErrors(), 1);

        //Invalid type string
        asEvent = new ASEvent("2017-01-01T00:00:00.000","as.commerce.purchase.complete$", "as.sdk.test", null, ImportanceLevel.NOT_IMPORTANT, "Customer/314");
        Assert.assertEquals(asEvent.isValid(), false);
        Assert.assertEquals(asEvent.validator().hasErrors(), 1);

    }

    @Test
    public void testJSONSerialisationWithStreamIds() throws IOException {

        ASEvent asEvent = new ASEvent("2017-01-01T00:00:00.000","as.COMMERCE.purchase.completed", "as.sdk.test", null, ImportanceLevel.NOT_IMPORTANT,
                "Customer/314");
        Assert.assertEquals(asEvent.isValid(), true);

        Assert.assertEquals(asEvent.getStreamId().toString(), "12d88deb-801f-3f80-9cf4-6b9ed0afaecc");
        Assert.assertEquals(asEvent.validator().getWarnings().isEmpty(), false); //Should contain warning
        Assert.assertEquals(asEvent.toJSON(),"{\"occurred_at\":\"2017-01-01T00:00:00.000Z\",\"type\":\"as.commerce.purchase.completed\",\"origin\":\"as.sdk" +
                ".test\",\"involves\":[{\"ACTOR\":{\"entity_ref\":\"Customer/314\"}}],\"importance\":2," +
                "\"stream_id\":\"12d88deb-801f-3f80-9cf4-6b9ed0afaecc\"}");

        //Test a full round-trip to and from JSON

        ASEvent roundTripEvent = asEvent.fromJSON(asEvent.toJSON());
        roundTripEvent.remove(ASConstants.FIELD_STREAM_ID); // remove to recalculate
        Assert.assertEquals(roundTripEvent.getStreamId().toString(), "12d88deb-801f-3f80-9cf4-6b9ed0afaecc"); //Recalculated stream id should match
        Assert.assertEquals(roundTripEvent.toJSON().equals(asEvent.toJSON()), true);
        Assert.assertEquals(roundTripEvent.getType().equals(asEvent.getType()), true);
        Assert.assertEquals(roundTripEvent.getOrigin().equals(asEvent.getOrigin()), true);
        Assert.assertEquals(roundTripEvent.getImportance().equals(asEvent.getImportance()), true);
        Assert.assertEquals(roundTripEvent.getStreamId().equals(asEvent.getStreamId()), true);
        Assert.assertEquals((roundTripEvent.getOccurredAt().getMillis() ==  asEvent.getOccurredAt().getMillis()), true);
        Assert.assertEquals(roundTripEvent.getRelationsManager().getFirstRelationsOfType("ACTOR").equals(asEvent.getRelationsManager().getFirstRelationsOfType("ACTOR")), true);

        //Create the smallest possible event the long way
        ASEvent asNewEvent = new ASEvent();
        Assert.assertEquals(asNewEvent.isValid(),false);
        asNewEvent.withRelation(new Relation(ASEventRelationTypes.ACTOR,"Customer/314"));
        Assert.assertEquals(asNewEvent.isValid(),false);
        asNewEvent.setType("as.commerce.purchase.completed");
        Assert.assertEquals(asNewEvent.isValid(true),true);

    }

}
