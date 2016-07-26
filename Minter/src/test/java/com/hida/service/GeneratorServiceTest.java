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
import com.hida.model.DefaultSetting;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.Pid;
import com.hida.model.PidTest;
import com.hida.model.Token;
import com.hida.repositories.PidRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeast;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A test class designed to test the functionality of GeneratorService
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
public class GeneratorServiceTest extends AbstractTestNGSpringContextTests {

    private final String TEST_FILE = "testDefaultSetting.properties";

    @Mock
    private PidRepository pidRepo_;

    @InjectMocks
    private GeneratorService service_;

    private DefaultSetting defaultSetting_;

    private PidTest pidTest_ = new PidTest();

    /**
     * Get default setting values from test properties file
     *
     * @throws Exception
     */
    @BeforeClass
    public void setUpClass() throws Exception {
        defaultSetting_ = this.readPropertiesFile(TEST_FILE);
    }

    /**
     * Refreshes the mocks after each test
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
        // set up Mockito
        MockitoAnnotations.initMocks(this);

        // return null when any repository attempts to save
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);
    }

    /**
     * Test to ensure that pids can be saved
     */
    @Test
    public void testSavePid() {
        service_.savePid(new Pid());
        verify(pidRepo_, times(1)).save(any(Pid.class));
    }

    /**
     * Test to ensure that generatePid works
     */
    @Test
    public void testGeneratePids() {
        // pretend that no newly generated Pids were persisted
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);

        // generate the set of Pids
        int actualAmount = 5;
        Set<Pid> testSet = service_.generatePids(defaultSetting_, actualAmount);

        // test behavior
        Assert.assertEquals(actualAmount, testSet.size());
        verify(pidRepo_, atLeast(actualAmount)).findOne(any(String.class));
    }

    /**
     * Test to ensure that the starting value is saved and is used in Pid
     * generation when appropriate.
     */
    @Test
    public void testGeneratePidsWithStartingValue() {
        // pretend that no newly generated Pids were persisted
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);

        // set the starting value
        int actualAmount = 5;
        service_.setStartingValue(actualAmount);
        service_.setCacheSetting(defaultSetting_);

        // generate the set of Pids
        Set<Pid> testSet = service_.generatePids(defaultSetting_, actualAmount);

        // test behavior
        int value = actualAmount;
        Iterator<Pid> iter = testSet.iterator();
        while (iter.hasNext()) {
            Pid pid = iter.next();
            String name = pid.getName();
            boolean isRightName = Integer.parseInt(name) == value;
            Assert.assertEquals(isRightName, true, String.format("name = %s, value = %d", name, value));
            value++;
        }

        Assert.assertEquals(actualAmount, testSet.size());
        Assert.assertEquals(service_.getStartingValue(), value);
        verify(pidRepo_, atLeast(actualAmount)).findOne(any(String.class));
    }

    /**
     * Test to ensure that Pids are incremented when generating a set of Pids. A
     * Pid should only be incremented when it already exists in either the
     * database or in the set it creates.
     */
    @Test
    public void testGeneratePidsWithRollOver() {
        // pretend to persist all odd Pids
        int amount = 5;
        for (int i = 0; i < amount; i++) {
            when(pidRepo_.findOne((2 * i + 1) + "")).thenReturn(new Pid());
        }

        // generate the set of Pids
        Set<Pid> testSet = service_.generatePids(defaultSetting_, amount);

        // test behavior
        Assert.assertEquals(amount, testSet.size());
        verify(pidRepo_, atLeast(amount)).findOne(any(String.class));

        // check to see that only Pids with even names are contained
        Iterator<Pid> iter = testSet.iterator();
        while (iter.hasNext()) {
            Pid pid = iter.next();

            boolean isEven = Integer.parseInt(pid.getName()) % 2 == 0;
            Assert.assertEquals(isEven, true, String.format("%s did not rollover", pid));

        }
    }

    /**
     * Ensures that Pids are wrapping around to the 0th name.
     */
    @Test
    public void testGeneratePidsWithWrapAround() {
        // pretend that no newly generated Pids were persisted
        int amount = 10;
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);

        // generate the set of Pids
        Set<Pid> testSet = service_.generatePids(defaultSetting_, amount);

        // test behavior and ensure that the set contains the desired amonut
        Assert.assertEquals(amount, testSet.size());
        verify(pidRepo_, atLeast(amount)).findOne(any(String.class));
    }

    /**
     * Test to ensure that the cache can be generated normally and will contain
     * the desired amount when possible.
     */
    @Test
    public void testGenerateCacheWithinMaxPermutation() {
        DefaultSetting setting = new DefaultSetting(defaultSetting_);
        long size = 5;
        setting.setCacheSize(size);

        service_.generateCache(setting);
        Assert.assertEquals(service_.getCacheSize(), size);
    }

    /**
     * Test to ensure that the cache will try to meet the desired cache size by
     * filling the cache with all possible permutations.
     */
    @Test
    public void testGenerateCacheBeyondMaxPermutation() {
        service_.generateCache(defaultSetting_);
        long max = service_.getMaxPermutation(defaultSetting_);
        Assert.assertEquals(service_.getCacheSize(), max);
    }

    /**
     * Test to ensure that the cache can be regenerated.
     */
    @Test
    public void testGenerateCacheNonEmptyCache() {
        DefaultSetting setting = new DefaultSetting(defaultSetting_);
        long size = 5;
        setting.setCacheSize(size);

        // generate intitial content for cache
        service_.generateCache(setting);

        // reduce the cache
        service_.collectCache(3);

        // regenerate cache
        service_.generateCache(setting);
        Assert.assertEquals(service_.getCacheSize(), size);
    }

    /**
     * Test to ensure that the generatePid method uses the cache.
     */
    @Test
    public void testGeneratePidWithCache() {
        DefaultSetting setting = new DefaultSetting(defaultSetting_);
        long size = service_.getMaxPermutation(setting) / 2;
        setting.setCacheSize(size);

        // generate intitial content for cache
        service_.generateCache(setting);

        Set<Pid> set = service_.generatePids(setting, size * 2);

        Assert.assertEquals(set.size(), size * 2);
    }

    /**
     * Test to ensure that the cache is replaced when the cache setting changes
     */
    @Test
    public void testCacheReplacement() {
        DefaultSetting setting1 = new DefaultSetting(defaultSetting_);
        DefaultSetting setting2 = new DefaultSetting(defaultSetting_);

        long amount = defaultSetting_.getCacheSize();

        setting1.setTokenType(Token.DIGIT);
        setting2.setTokenType(Token.LOWER_ALPHABET);

        // test the first pid in the first cache to ensure it matches Token.DIGIT
        service_.generateCache(setting1);
        Iterator<Pid> iter1 = service_.peekCache(amount).iterator();
        pidTest_.testTokenType(iter1.next().getName(), setting1);

        // test the first pid in the second cache to ensure it matches Token.LOWER_ALPHABET
        service_.generateCache(setting2);
        Iterator<Pid> iter2 = service_.peekCache(amount).iterator();
        pidTest_.testTokenType(iter2.next().getName(), setting2);
    }

    @Test(expectedExceptions = {NotEnoughPermutationsException.class})
    public void testNEPExceptionInGeneratePid() throws Exception {
        when(pidRepo_.findOne(any(String.class))).thenReturn(new Pid());

        long amount = 5;
        service_.generatePids(defaultSetting_, amount);
    }

    /**
     * Empties the cache after each test case.
     */
    @AfterMethod
    private void emptyCache() {
        service_.clearCache();
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
