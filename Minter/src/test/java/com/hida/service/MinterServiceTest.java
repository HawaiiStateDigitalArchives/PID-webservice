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
package com.hida.service;

import com.hida.configuration.RepositoryConfiguration;
import com.hida.repositories.DefaultSettingRepository;
import com.hida.repositories.UsedSettingRepository;
import com.hida.model.DefaultSetting;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.Pid;
import com.hida.model.Token;
import com.hida.model.UsedSetting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests the functionality of MinterService using Mockito.
 *
 * @author lruffin
 */
@WebAppConfiguration
@IntegrationTest
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
@TestPropertySource(locations = "classpath:testConfig.properties")
@TestExecutionListeners(inheritListeners = false, listeners = {
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
public class MinterServiceTest extends AbstractTestNGSpringContextTests {

    /**
     * Default setting values stored in resources folder
     */
    private final String TEST_FILE = "testDefaultSetting.properties";

    @Mock
    private DefaultSettingRepository defaultSettingRepo_;

    @Mock
    private GeneratorService genService_;

    @Mock
    private UsedSettingRepository usedSettingRepo_;

    @InjectMocks
    private MinterService minterService_;   
    
    
    
    @BeforeMethod
    public void setUpMethod(){
        MockitoAnnotations.initMocks(this);
        when(genService_.getMaxPermutation(getSampleDefaultSetting())).thenCallRealMethod();
    }   

    /**
     * Tests the MinterService by assuming that the settings aren't currently
     * stored in the database
     */
    @Test
    public void testMintWithNewUsedSetting() throws Exception {
        Set<Pid> sampleSet = getSampleSet();
        when(genService_.generatePids(any(DefaultSetting.class), anyLong()))
                .thenReturn(sampleSet);

        // don't do anything when a save attempt has been made
        setSaveBehavior();
        setFindUsedSettingBehavior(null);

        // start behavior
        minterService_.mint(sampleSet.size(), getSampleDefaultSetting());

        // verify that save attempts have been made
        verify(genService_, atLeast(sampleSet.size())).savePid(any(Pid.class));
        verify(usedSettingRepo_, times(1)).save(any(UsedSetting.class));
        verify(usedSettingRepo_, times(1)).findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean());
    }

    /**
     * Tests the MinterService under the scenario where UsedSetting entity with
     * matching parameters already exist.     
     */
    @Test
    public void testMintWithOldUsedSetting() throws Exception {
        Set<Pid> sampleSet = getSampleSet();
        UsedSetting sampleUsedSetting = getSampleUsedSetting();
        DefaultSetting sampleDefaultSetting = getSampleDefaultSetting();
        
        // set up behavior
        when(genService_.generatePids(any(DefaultSetting.class), anyLong())).thenReturn(sampleSet);
        setSaveBehavior();
        setFindUsedSettingBehavior(sampleUsedSetting);
        
        // record previous amount
        long oldAmount = sampleUsedSetting.getAmount();      

        // start behavior
        long max = genService_.getMaxPermutation(sampleDefaultSetting);
        long mintAmount = max - oldAmount;
        minterService_.mint(mintAmount, getSampleDefaultSetting());

        // verify that save attempts have been made
        verify(genService_, atLeast(sampleSet.size())).savePid(any(Pid.class));
        verify(usedSettingRepo_, times(1)).findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean());
        
        // the new amount should be the previous amount + the requested amount
        Assert.assertEquals(sampleUsedSetting.getAmount(), mintAmount + oldAmount);
    }
    
    @Test(expectedExceptions = NotEnoughPermutationsException.class)
    public void testExceptionThroughUsedSetting() throws Exception {
        UsedSetting sampleUsedSetting = getSampleUsedSetting();
        Set<Pid> sampleSet = getSampleSet();
        when(genService_.generatePids(any(DefaultSetting.class), anyLong()))
                .thenReturn(sampleSet);
        
        // pretend a usedSetting was found
        setFindUsedSettingBehavior(sampleUsedSetting);
        
        // try to mint an impossible amount
        minterService_.mint(sampleSet.size(), getSampleDefaultSetting());        
    }
    
    @Test(expectedExceptions = NotEnoughPermutationsException.class)
    public void testExceptionForImpossibleRequest() throws Exception {
        Set<Pid> sampleSet = getSampleSet();
        when(genService_.generatePids(any(DefaultSetting.class), anyLong()))
                .thenReturn(sampleSet);
        long max = genService_.getMaxPermutation(getSampleDefaultSetting());
        
        // try to mint an impossible amount
        minterService_.mint(max + 1, getSampleDefaultSetting());        
    }
        

    /**
     * Test in MinterService that ensures that the CurrentSetting is sought
     * after.
     */
    @Test
    public void testInitializeStoredSetting() throws Exception {
        DefaultSetting testSetting = getSampleDefaultSetting();
        when(defaultSettingRepo_.findCurrentDefaultSetting()).thenReturn(testSetting);

        minterService_.initializeStoredSetting();
        verify(defaultSettingRepo_, atLeastOnce()).findCurrentDefaultSetting();
    }

    /**
     * Test in MinterService that ensures that the CurrentSetting is sought
     * after and if it does not exist, a new DefaultSetting is created and
     * saved.
     */
    @Test
    public void testGetCurrentSettingWithoutExistingDefaultSetting() throws Exception {
        DefaultSetting testSetting = getSampleDefaultSetting();
        when(defaultSettingRepo_.findCurrentDefaultSetting()).thenReturn(null);  
        
        minterService_.initializeStoredSetting();
        DefaultSetting actualSetting = minterService_.getStoredSetting();
                
        Assert.assertEquals(actualSetting.getCharMap(), testSetting.getCharMap());
        Assert.assertEquals(actualSetting.getPrefix(), testSetting.getPrefix());
        Assert.assertEquals(actualSetting.getPrepend(), testSetting.getPrepend());
        Assert.assertEquals(actualSetting.getRootLength(), testSetting.getRootLength());
        Assert.assertEquals(actualSetting.getTokenType(), testSetting.getTokenType());
        Assert.assertEquals(actualSetting.isAuto(), testSetting.isAuto());
        Assert.assertEquals(actualSetting.isRandom(), testSetting.isRandom());
        Assert.assertEquals(actualSetting.isSansVowels(), testSetting.isSansVowels());
    }

    /**
     * Test in MinterService that checks if CurrentSetting in MinterService is
     * being properly updated.
     */
    @Test
    public void testUpdateCurrentSetting() throws Exception {
        DefaultSetting testSetting = getSampleDefaultSetting();
        when(defaultSettingRepo_.findCurrentDefaultSetting()).thenReturn(testSetting);

        minterService_.updateStoredSetting(testSetting);
        verify(defaultSettingRepo_, atLeastOnce()).findCurrentDefaultSetting();
    }
   
    @Test
    public void testGenerateCache() throws Exception {
        doNothing().when(genService_).generateCache(any(DefaultSetting.class));
        
        minterService_.generateCache();
        
        verify(genService_, times(1)).generateCache(any(DefaultSetting.class));        
    }

    /**
     * Create a test Default Setting object
     */
    private DefaultSetting getSampleDefaultSetting() {
        return new DefaultSetting("", // prepend
                "", // prefix
                500, // cacheSize
                Token.DIGIT, // token type
                "d", // charmap
                1, // rootlength
                true, // sans vowel
                true, // is auto
                false); // is random
    }

    /**
     * Return a sample UsedSetting
     *
     * @return
     */
    private UsedSetting getSampleUsedSetting() {
        return new UsedSetting("", // prefix
                Token.DIGIT, // tokentype
                "d", // charmap
                1, // rootlength
                true, //sans vowels
                5); // amount
    }

    private Set<Pid> getSampleSet() {
        Set<Pid> set = new LinkedHashSet<>();

        for (int i = 0; i < 10; i++) {
            set.add(new Pid(i + ""));
        }

        return set;
    }
        
    private void setSaveBehavior(){
        doNothing().when(genService_).savePid(any(Pid.class));        
        when(usedSettingRepo_.save(any(UsedSetting.class))).thenReturn(null);
        when(defaultSettingRepo_.save(any(DefaultSetting.class))).thenReturn(null);
    }
    
    private void setFindUsedSettingBehavior(UsedSetting setting){
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(setting);
    }

    /**
     * Set all the keys' values in testDefaultSettings to default values to
     * ensure that the values are being changed during the updatedChangedSetting
     * tests.
     *
     * @throws Exception
     */
    @AfterTest
    private void resetTestReadDefaultProperties() throws Exception {
        DefaultSetting setting = readPropertiesFile(TEST_FILE);

        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(TEST_FILE);
        File file = new File(url.toURI());
        OutputStream output = new FileOutputStream(file);

        // set the properties value
        prop.setProperty("prepend", setting.getPrepend());
        prop.setProperty("prefix", setting.getPrefix());
        prop.setProperty("cacheSize", setting.getCacheSize() + "");
        prop.setProperty("charMap", setting.getCharMap());
        prop.setProperty("rootLength", setting.getRootLength() + "");
        prop.setProperty("tokenType", setting.getTokenType() + "");
        prop.setProperty("sansVowel", setting.isSansVowels() + "");
        prop.setProperty("auto", setting.isAuto() + "");
        prop.setProperty("random", setting.isRandom() + "");

        // save and close
        prop.store(output, "");
        output.close();

    }

    /**
     * Read a given properties file and return its values in the form of a
     * DefaultSetting object
     *
     * @return DefaultSetting object with read values
     * @throws IOException Thrown when the file cannot be found
     */
    private DefaultSetting readPropertiesFile(String filename) throws IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream(filename);

        DefaultSetting setting = new DefaultSetting();

        // load a properties file
        prop.load(input);

        // get the property value, store it, and return it
        setting.setPrepend(prop.getProperty("prepend"));
        setting.setPrefix(prop.getProperty("prefix"));
        setting.setCacheSize(Long.parseLong(prop.getProperty("cacheSize")));
        setting.setCharMap(prop.getProperty("charMap"));
        setting.setTokenType(Token.valueOf(prop.getProperty("tokenType")));
        setting.setRootLength(Integer.parseInt(prop.getProperty("rootLength")));
        setting.setSansVowels(Boolean.parseBoolean(prop.getProperty("sansVowel")));
        setting.setAuto(Boolean.parseBoolean(prop.getProperty("auto")));
        setting.setRandom(Boolean.parseBoolean(prop.getProperty("random")));

        // close and return
        input.close();
        return setting;
    }

}
