/*
 * Copyright 2016 Lawrence Ruffin, Leland Lopez, Brittany Cruz, Stephen Anspach
 *
 * Developed in collaboration with the Hawaii State Digital Archives.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.hida;

import com.hida.model.Citation;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * An integration test of the entire application to ensure functionality
 *
 * @author lruffin
 */
@WebIntegrationTest("server.port:9000")
@SpringApplicationConfiguration(classes = {Application.class})
@TestPropertySource(locations = "classpath:testConfig.properties")
public class WebAppIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext webAppContext_;

    private Citation citation_;

    private MockMvc mockedContext_;

    @BeforeClass
    public void setup() throws IOException {
        mockedContext_ = MockMvcBuilders.webAppContextSetup(webAppContext_).build();
        initCitation();
    }

    @Test
    public void testIndex() throws Exception {
        mockedContext_.perform(get("/Resolver/"))
                .andExpect(status().isOk());
    }

    @Test
    public void testInsert() throws Exception {
        // initialize path
        String path = String.format("/Resolver/insert?"
                + "purl=%s&url=%s&erc=%s&who=%s&what=%s&when=%s", citation_.getPurl(),
                citation_.getUrl(), citation_.getErc(), citation_.getWho(), citation_.getWhat(),
                citation_.getDate());              

        mockedContext_.perform(get(path))
                .andExpect(status().isCreated());        
    }

    @Test(dependsOnMethods = {"testInsert"}, priority = 2)
    public void testRetrieve() throws Exception {
        // get the persited citation
        String path = "/Resolver/retrieve?purl=" + citation_.getPurl();
        MvcResult result = mockedContext_.perform(get(path))
                .andExpect(status().isOk())
                .andReturn();

        // assert equality
        String content = result.getResponse().getContentAsString();
        JSONObject object = new JSONObject(content);

        Assert.assertEquals(citation_.getPurl(), object.get("purl") + "");
        Assert.assertEquals(citation_.getUrl(), object.get("url") + "");
        Assert.assertEquals(citation_.getErc(), object.get("erc") + "");
        Assert.assertEquals(citation_.getWho(), object.get("who") + "");
        Assert.assertEquals(citation_.getDate(), object.get("date") + "");
    }

    @Test(dependsOnMethods = {"testRetrieve"})
    public void testEdit() throws Exception {
        // create a new url value
        String newUrl = "newTestUrl";

        // edit the persisted citation's url
        String editPath = String.format("/Resolver/edit?purl=%s&url=%s",
                citation_.getPurl(), newUrl);
        mockedContext_.perform(get(editPath))
                .andExpect(status().isOk())
                .andReturn();

        // get the persisted citation
        String retrievePath = "/Resolver/retrieve?purl=" + citation_.getPurl();
        MvcResult result = mockedContext_.perform(get(retrievePath))
                .andExpect(status().isOk())
                .andReturn();

        // assert equality
        String content = result.getResponse().getContentAsString();
        JSONObject object = new JSONObject(content);

        Assert.assertEquals(citation_.getPurl(), object.get("purl") + "");
        Assert.assertEquals(newUrl, object.get("url") + "");
        Assert.assertEquals(citation_.getErc(), object.get("erc") + "");
        Assert.assertEquals(citation_.getWho(), object.get("who") + "");
        Assert.assertEquals(citation_.getDate(), object.get("date") + "");
    }

    @Test(dependsOnMethods = {"testEdit"})
    public void testDelete() throws Exception {
        // delete the persisted citation
        String deletePath = "/Resolver/delete?purl=" + citation_.getPurl();
        mockedContext_.perform(get(deletePath))
                .andExpect(status().isNoContent());
        
        // get the persisted citation
        String retrievePath = "/Resolver/retrieve?purl=" + citation_.getPurl();
        mockedContext_.perform(get(retrievePath))
                .andExpect(status().is4xxClientError());        
    }
    
    @Test(dependsOnMethods = {"testInsert"}, priority = 1)
    public void testDuplicateCitation() throws Exception{
        // initialize path
        String path = String.format("/Resolver/insert?"
                + "purl=%s&url=%s&erc=%s&who=%s&what=%s&when=%s", citation_.getPurl(),
                citation_.getUrl(), citation_.getErc(), citation_.getWho(), citation_.getWhat(),
                citation_.getDate());              

        mockedContext_.perform(get(path))
                .andExpect(status().is4xxClientError()); 
    }

    private void initCitation() {
        citation_ = new Citation("testPurl",
                "testUrl",
                "testErc",
                "testWho",
                "testWhat",
                "testDate");
    }
}
