package com.hida.controller;

import com.hida.model.Citation;
import com.hida.service.ResolverService;
import java.util.Map;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

/**
 * Tests the functionality of ResolverController to see if it is capable of
 * performing REST calls.
 *
 * @author lruffin
 */
public class ResolverControllerTest {

    @Mock
    private ResolverService Service;

    @InjectMocks
    private ResolverController Controller;

    @BeforeClass
    public void setUpClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test the retrieve REST call
     *
     * @throws Exception
     */
    @Test
    public void testRetrieve() throws Exception {
        Citation entity = getSampleCitation();
        when(Service.retrieveCitation(any(String.class))).thenReturn(entity);       

        // test to see that Service at least makes a call to get a Citation object
        verify(Service, atLeastOnce()).retrieveCitation(any(String.class));

        // test to see that Json is formated properly
        String jsonObject = Controller.retrieve(entity.getPurl());
        testJsonObject(jsonObject, entity);
    }

    /**
     * Test the edit REST call
     *
     * @throws Exception
     */
    @Test
    public void testEdit() throws Exception {
        // create citation entity to reflect the change that happens after calling edit
        Citation entity = getSampleCitation();

        // pretend the entity was edited and return new entity
        doNothing().when(Service).editUrl(any(String.class), any(String.class));
        when(Service.retrieveCitation(any(String.class))).thenReturn(entity);       

        // test to see that Json is formated properly
        String jsonObject = Controller.edit(entity.getPurl(), entity.getUrl());
        testJsonObject(jsonObject, entity);
    }

    /**
     * Tests the insert REST call
     *
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {
        // create citation entites to reflect the change that happens after calling edit
        Citation entity = getSampleCitation();

        // pretend the entity was inserted 
        doNothing().when(Service).insertCitation(any(Citation.class));       

        // test to see that Json is formated properly
        String jsonObject = Controller.insert(entity.getPurl(),
                entity.getUrl(),
                entity.getErc(),
                entity.getWho(),
                entity.getWhat(),
                entity.getDate());
        testJsonObject(jsonObject, entity);
    }

    /**
     * Tests the delete REST call
     *
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {
        // pretend the entity was deleted
        doNothing().when(Service).deleteCitation(any(String.class));

        // call delete
        ModelAndView mav = Controller.delete("");

        // test to see that the correct view is returned
        Assert.assertEquals("result", mav.getViewName());

        // test to see that Json is formated properly
        Map<String, Object> map = mav.getModel();
        String result = (String) map.get("message");

        Assert.assertEquals("{\"result\":\"deleted\"}", result);
    }   

    /**
     * Tests the given jsonObject to ensure that it matches the given entity
     *
     * @param jsonString Json object to test
     * @param entity Entity to check against
     */
    private void testJsonObject(String jsonString, Citation entity) {
        JSONObject testObject = new JSONObject(jsonString);

        String pid = testObject.getString("pid");
        String url = testObject.getString("url");
        String erc = testObject.getString("erc");
        String who = testObject.getString("who");
        String what = testObject.getString("what");
        String time = testObject.getString("date");

        Assert.assertEquals(entity.getPurl(), pid);
        Assert.assertEquals(entity.getUrl(), url);
        Assert.assertEquals(entity.getErc(), erc);
        Assert.assertEquals(entity.getWho(), who);
        Assert.assertEquals(entity.getWhat(), what);
        Assert.assertEquals(entity.getDate(), time);
    }

    /**
     * Gets a sample Citation object for testing
     *
     * @return sample Citation object
     */
    private Citation getSampleCitation() {
        Citation citation = new Citation("samplePid",
                "sampleURL",
                "sampleERC",
                "sampleWho",
                "sampleWhat",
                "sampleTime");

        return citation;
    }
}
