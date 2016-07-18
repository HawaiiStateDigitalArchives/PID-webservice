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

import com.hida.repositories.DefaultSettingRepository;
import com.hida.repositories.PidRepository;
import com.hida.repositories.UsedSettingRepository;
import com.hida.model.DefaultSetting;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.Pid;
import com.hida.model.Token;
import com.hida.model.UsedSetting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the functionality of MinterService using Mockito.
 *
 * @author lruffin
 */
public class MinterServiceTest {

    /**
     * Default setting values stored in resources folder
     */
    private final String TEST_FILE = "testDefaultSetting.properties";

    @Mock
    private DefaultSettingRepository defaultSettingRepo_;

    @Mock
    private PidRepository pidRepo_;

    @Mock
    private UsedSettingRepository usedSettingRepo_;

    @Spy
    private PropertiesLoaderService propertiesService_;

    @InjectMocks
    private MinterService minterService_;

    /**
     * Sets up Mockito
     *
     * @throws Exception
     */
    @BeforeClass
    public void setUpClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        minterService_.setDefaultSettingPath(TEST_FILE);
        minterService_.initializeStoredSetting();
    }

    /**
     * Test the various mint settings (auto/random and random/sequential)
     *
     * @return An array of values
     */
    @DataProvider(name = "mintSettings")
    public Object[][] mintSettings() {
        return new Object[][]{
            {true, true},
            {true, false},
            {false, true},
            {false, false}
        };
    }

    /**
     * Tests the MinterService by assuming that the settings aren't currently
     * stored in the database
     *
     * @param isRandom Determines if the PIDs are created randomly or
     * sequentially
     * @param isAuto Determines which generator, either Auto or Custom, will be
     * used
     */
    @Test(dataProvider = "mintSettings")
    public void testMintWithNewUsedSetting(boolean isRandom, boolean isAuto) throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();
        testSetting.setAuto(isAuto);
        testSetting.setRandom(isRandom);

        // assume that any Pids created aren't already persisted and pretend to persist them
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // assume the UsedSetting isn't persisted and pretend to persist it
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(null);

        when(usedSettingRepo_.save(any(UsedSetting.class))).thenReturn(null);

        // retrieve a sample DefaultSetting entity
        int actualAmount = 5;
        Set<Pid> testSet = minterService_.mint(actualAmount, testSetting);

        // test behavior
        Assert.assertEquals(actualAmount, testSet.size());
        verify(pidRepo_, atLeast(actualAmount)).findOne(any(String.class));
        verify(pidRepo_, atLeast(actualAmount)).save(any(Pid.class));
        verify(usedSettingRepo_, atLeastOnce()).save(any(UsedSetting.class));
        verify(usedSettingRepo_, atLeastOnce()).findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean());
    }

    /**
     * Tests the MinterService under the scenario where UsedSetting entity with
     * matching parameters already exist.
     *
     * @param isRandom Determines if the PIDs are created randomly or
     * sequentially
     * @param isAuto Determines which generator, either Auto or Custom, will be
     * used
     */
    @Test(dataProvider = "mintSettings")
    public void testMintWithOldUsedSetting(boolean isAuto, boolean isRandom) throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();
        testSetting.setAuto(isAuto);
        testSetting.setRandom(isRandom);

        // get a sample UsedSetting entity
        UsedSetting usedSetting = getSampleUsedSetting();

        // assume that any Pids created aren't already persisted and pretend to persist them
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // assume the UsedSetting isn't persisted and pretend to persist it        
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(usedSetting);
        when(usedSettingRepo_.save(usedSetting)).thenReturn(null);

        int preTestAmount = (int) usedSetting.getAmount();
        int actualAmount = 5;
        int postTestAmount = actualAmount + preTestAmount;
        Set<Pid> testSet = minterService_.mint(actualAmount, testSetting);

        // test behavior
        Assert.assertEquals(actualAmount, testSet.size());
        Assert.assertEquals(postTestAmount, usedSetting.getAmount());
        verify(pidRepo_, atLeast(actualAmount)).findOne(any(String.class));
        verify(pidRepo_, atLeast(actualAmount)).save(any(Pid.class));
        verify(usedSettingRepo_, never()).save(usedSetting);
        verify(usedSettingRepo_, atLeastOnce()).findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean());
    }

    /**
     * Tests to ensure that the whenever the stored default setting is used then
     * the requested amount is saved and used as a starting value.
     */
    @Test
    public void testMintWithStartingValue() throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();

        // get a sample UsedSetting entity
        UsedSetting usedSetting = getSampleUsedSetting();

        // assume that any Pids created aren't already persisted and pretend to persist them
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // assume the UsedSetting isn't persisted and pretend to persist it        
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(usedSetting);
        when(usedSettingRepo_.save(any(UsedSetting.class))).thenReturn(null);

        // check to see if all the Pids were created
        long amount = 5;
        long lastAmount = minterService_.getLastSequentialAmount();
        Set<Pid> testSet = minterService_.mint(amount, testSetting);

        Assert.assertEquals(minterService_.getLastSequentialAmount(), (lastAmount + amount) % 10);
    }

    /**
     * Tests the MinterService to ensure that a NotEnoughPermutationsException
     * is thrown whenever the amount retrieved from FindUsedSetting is less than
     * the requested amount.
     *
     * @param isRandom Determines if the PIDs are created randomly or
     * sequentially
     * @param isAuto Determines which generator, either Auto or Custom, will be
     * used
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class, dataProvider = "mintSettings")
    public void testMintNotEnoughPermutationsExceptionInFindUsedSetting(
            boolean isAuto, boolean isRandom) throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();
        testSetting.setAuto(isAuto);
        testSetting.setRandom(isRandom);

        // get a sample UsedSetting entity
        UsedSetting usedSetting = getSampleUsedSetting();

        // assume that any Pids created aren't already persisted and pretend to persist them
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // pretend to find and retrieve variable usedSetting
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(usedSetting);
        when(usedSettingRepo_.findOne(anyInt())).thenReturn(usedSetting);

        // try to mint an amount greater than what is available
        Set<Pid> testSet = minterService_.mint(6, testSetting);
    }

    /**
     * Tests the MinterService to ensure that a NotEnoughPermutationsException
     * is thrown whenever the requested amount of Pids to mint exceeds the
     * possible number of permutations.
     *
     * @param isRandom Determines if the PIDs are created randomly or
     * sequentially
     * @param isAuto Determines which generator, either Auto or Custom, will be
     * used
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class, dataProvider = "mintSettings")
    public void testMintNotEnoughPermutationsExceptionInCalculatePermutations(
            boolean isAuto, boolean isRandom) throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();
        testSetting.setAuto(isAuto);
        testSetting.setRandom(isRandom);

        // assume that any Pids created aren't already persisted and pretend to persist them
        when(pidRepo_.findOne(any(String.class))).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // assume that UsedSetting entity with the relevant parameters does not exist
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(null);
        when(pidRepo_.save(any(Pid.class))).thenReturn(null);

        // try to mint an amount greater than what is possible
        Set<Pid> testSet = minterService_.mint(11, testSetting);
    }

    /**
     * Tests the MinterService to ensure that a NotEnoughPermutationsException
     * is thrown whenever it is no longer possible to 'roll' Pids. This is
     * important because there may be different settings that may have created
     * Pids that could match the fields of the currently used setting.
     *
     * @param isRandom Determines if the PIDs are created randomly or
     * sequentially
     * @param isAuto Determines which generator, either Auto or Custom, will be
     * used
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class, dataProvider = "mintSettings")
    public void testMintNotEnoughPermutationExceptionInRollId(boolean isAuto, boolean isRandom)
            throws Exception {
        // retrieve a sample DefaultSetting entity
        DefaultSetting testSetting = this.sampleDefaultSetting();
        testSetting.setAuto(isAuto);
        testSetting.setRandom(isRandom);

        // pretend any Pid with the name "0" is the only Pid that exists
        Pid sentinelPid = new Pid("1");
        when(pidRepo_.findOne(any(String.class))).thenReturn(sentinelPid);
        when(pidRepo_.findOne("0")).thenReturn(null);

        // assume that UsedSetting entity with the relevant parameters does not exist
        when(usedSettingRepo_.findUsedSetting(any(String.class),
                any(Token.class),
                any(String.class),
                anyInt(),
                anyBoolean())).thenReturn(null);
        when(usedSettingRepo_.save(any(UsedSetting.class))).thenReturn(null);

        // try to mint an amount greater than what is possible
        Set<Pid> testSet = minterService_.mint(10, testSetting);
    }

    /**
     * Test in MinterService that ensures that the CurrentSetting is sought
     * after.
     */
    @Test
    public void testInitializeStoredSetting() throws Exception {
        DefaultSetting testSetting = sampleDefaultSetting();
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
        DefaultSetting testSetting = sampleDefaultSetting();
        when(defaultSettingRepo_.findCurrentDefaultSetting()).thenReturn(null);
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
        DefaultSetting testSetting = sampleDefaultSetting();
        when(defaultSettingRepo_.findCurrentDefaultSetting()).thenReturn(testSetting);

        minterService_.updateCurrentSetting(testSetting);
        verify(defaultSettingRepo_, atLeastOnce()).findCurrentDefaultSetting();
    }

    /**
     * Create a test Default Setting object
     */
    private DefaultSetting sampleDefaultSetting() {
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

    /**
     * Set all the keys' values in testDefaultSettings to default values to
     * ensure that the values are being changed during the updatedChangedSetting
     * tests.
     *
     * @throws Exception
     */
    @AfterTest
    private void resetTestReadDefaultProperties() throws Exception {
        DefaultSetting setting = propertiesService_.readPropertiesFile(TEST_FILE);

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
}
