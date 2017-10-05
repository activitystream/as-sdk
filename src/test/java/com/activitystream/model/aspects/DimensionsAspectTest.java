package com.activitystream.model.aspects;

import com.activitystream.model.ASEntity;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;


public class DimensionsAspectTest {

    private static final Logger logger = LoggerFactory.getLogger(DimensionsAspectTest.class);

    @Test
    public void simpleDimensionsTest() throws Exception {

        ASEntity venue = new ASEntity("Venue", "983983");
        venue.withDimension("house_color","white");
        venue.addDimensions("door_faces","north", "door_color","brown");

        Assert.assertEquals(venue.toJSON().equals("{\"entity_ref\":\"Venue/983983\",\"aspects\":{\"dimensions\":{\"house_color\":\"white\",\"door_faces\":\"north\",\"door_color\":\"brown\"}}}"),true);
    }

}
