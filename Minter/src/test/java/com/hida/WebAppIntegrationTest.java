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

import com.hida.model.DefaultSetting;
import com.hida.model.PidTest;
import com.hida.model.Token;
import com.hida.service.PropertiesLoaderService;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private PropertiesLoaderService propertiesService_;

    private PidTest pidTest_ = new PidTest();

    @Value("${defaultSetting.path}")
    private String defaultSettingPath_;

    private MockMvc mockedContext_;

    private DefaultSetting defaultSetting_;

    private final int AMOUNT = 10;

    @BeforeClass
    public void setup() throws IOException {
        mockedContext_ = MockMvcBuilders.webAppContextSetup(webAppContext_).build();
        initDefaultSetting();
    }

    @Test
    public void testMint() throws Exception {
        MvcResult result = mockedContext_.perform(get("/Minter/mint/" + AMOUNT)
                .accept("application/json"))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JSONArray testJsonArray = new JSONArray(content);

        // test the pid and ensure that they match the used setting
        for (int i = 0; i < AMOUNT; i++) {
            JSONObject object = testJsonArray.getJSONObject(i);
            String name = object.getString("name");
            pidTest_.testAll(name, defaultSetting_);
        }
    }

    @Test
    public void testMintWithDifferentParameters() throws Exception {
        // create different values and set up a path
        String prepend = "http://";
        String prefix = "abc";
        String charMap = "ul";
        Token token = Token.LOWER_ALPHABET;
        int rootLength = 2;
        boolean isAuto = false;
        boolean sansVowel = false;
        String path = String.format("/Minter/mint/%d?prepend=%s"
                + "&prefix=%s"
                + "&charMap=%s"
                + "&tokenType=%s"
                + "&rootLength=%d"
                + "&sansVowel=%b"
                + "&auto=%b", AMOUNT, prepend, prefix, charMap, token, rootLength, isAuto, sansVowel);

        // check /mint path
        MvcResult result = mockedContext_.perform(get(path)
                .accept("application/json"))
                .andExpect(status().isCreated())
                .andReturn();

        DefaultSetting setting = new DefaultSetting(prepend, prefix, defaultSetting_.getCacheSize(),
                token, charMap, rootLength, sansVowel, isAuto, defaultSetting_.isRandom());

        String content = result.getResponse().getContentAsString();
        JSONArray testJsonArray = new JSONArray(content);

        // test the pid and ensure that they match the used setting
        for (int i = 0; i < AMOUNT; i++) {
            JSONObject object = testJsonArray.getJSONObject(i);
            String name = object.getString("name");
            pidTest_.testAll(name, setting);
        }
    }

    @Test
    public void testMintWithNegativeAmount() throws Exception {
        mockedContext_.perform(get("/Minter/mint/" + -1)
                .accept("application/json"))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testMintWithTooManyPermutations() throws Exception {
        mockedContext_.perform(get("/Minter/mint/" + 11)
                .accept("application/json"))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test(dependsOnMethods = {"testMintWithDifferentParameters"})
    public void testMintWithDuplicatePids() throws Exception {
        mockedContext_.perform(get("/Minter/mint/" + 5)
                .accept("application/json"))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
    
    @Test
    public void testAdministration() throws Exception {
        mockedContext_.perform(get("/Minter/administration"))
                .andExpect(status().isOk())
                .andReturn();
    }
    
    @Test 
    public void testIndex() throws Exception {
        mockedContext_.perform(get("/Minter/"))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void initDefaultSetting() throws IOException {
        defaultSetting_ = propertiesService_.readPropertiesFile(defaultSettingPath_);
    }

}
