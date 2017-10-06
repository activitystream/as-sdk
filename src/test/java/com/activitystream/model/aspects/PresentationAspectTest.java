package com.activitystream.model.aspects;

import com.activitystream.model.ASEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.junit.Test;

import static com.activitystream.model.aspects.PresentationAspect.presentation;


public class PresentationAspectTest {

    private static final Logger logger = LoggerFactory.getLogger(PresentationAspectTest.class);

    @Test
    public void testSimplePresentation() throws Exception {

        ASEntity venue = new ASEntity("Venue", "983983");
        venue.withAspect(presentation()
                .withLabel("Royal Albert Hall")
                .withDescription("The Royal Albert Hall is a concert hall on the northern edge of South Kensington, London, which holds the Proms concerts annually each summer since 1941. It has a capacity of up to 5,272 seats.")
                .withDetailsUrl("https://www.royalalberthall.com/")
                .withThumbnail("https://cdn.royalalberthall.com/file/1396975600/32830992449")
                .withIcon("ol-venue"));

        Assert.assertEquals(venue.toJSON().equals("{\"entity_ref\":\"Venue/983983\",\"aspects\":{\"presentation\":{\"label\":\"Royal Albert Hall\",\"description\":\"The Royal Albert Hall is a concert hall on the northern edge of South Kensington, London, which holds the Proms concerts annually each summer since 1941. It has a capacity of up to 5,272 seats.\",\"details_url\":\"https://www.royalalberthall.com/\",\"thumbnail\":\"https://cdn.royalalberthall.com/file/1396975600/32830992449\",\"icon\":\"ol-venue\"}}}"),true);

        ASEntity parsedVenue = ASEntity.fromJSON(venue.toJSON());
        //Round-trip test
        Assert.assertEquals(venue.toJSON().equals(parsedVenue.toJSON()),true);
        Assert.assertEquals(venue.getStreamId().equals(parsedVenue.getStreamId()),true);

        //Stream IDs are always calculated the same way so they are deterministic.
        Assert.assertEquals(venue.getStreamId().toString().equals("e769e03d-0393-37ce-a40f-7d70b2036906"),true);

    }

}
