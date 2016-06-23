package com.hida.model;

import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the functionality of CustomIdGeneratorTest
 *
 * @author lruffin
 */
public class CustomIdGeneratorTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CustomIdGeneratorTest.class);
    private final PidTest PidTest = new PidTest();

    /**
     * Data set with varying charMap values
     *
     * @return A data set
     */
    @DataProvider(name = "sansVowel")
    public Object[][] sansVowelParameters() {
        return new Object[][]{
            {"", true, "d", 10},
            {"", true, "l", 20},
            {"", true, "u", 20},
            {"", true, "m", 40},
            {"", true, "e", 50},
            {"", true, "dl", 200},
            {"", true, "du", 200},
            {"", false, "d", 10},
            {"", false, "l", 26},
            {"", false, "u", 26},
            {"", false, "m", 52},
            {"", false, "e", 62},
            {"", false, "dl", 260},
            {"", false, "du", 260},};
    }

    /**
     * Data set with varying prefix values
     *
     * @return A data set
     */
    @DataProvider(name = "prefix")
    public Object[][] prefixParameters() {
        return new Object[][]{
            {"", false, "d", 10},
            {"!@*(", false, "d", 10},
            {"www", false, "d", 10},
            {"123", false, "d", 10},
            {"123abc", false, "d", 10},
            {"!a1", false, "d", 10},
            {" ", false, "d", 10}
        };
    }

    /**
     * Data set with varying root length values
     *
     * @return A data set
     */
    @DataProvider(name = "rootLength")
    public Object[][] rootLengthParameters() {
        Object[][] parameter = new Object[2][];
        String charMap = "d";
        for (int i = 1; i <= 2; i++) {
            Object[] array = {"", false, charMap, (int) Math.pow(10, i)};
            parameter[i - 1] = array;
            charMap += "d";
        }
        return parameter;
    }

    /**
     * Tests CustomIdGenerator for the presence of vowels through the
     * sequentialMint method.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testSequentialMintSansVowels(String prefix, boolean sansVowel,
            String charMap, int amount) {
        LOGGER.trace("inside testSequentialMintSansVowels");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> sequentialSet = minter.sequentialMint(amount);

        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testCharMap(current.getName(), setting);

            if (prev != null) {
                PidTest.testOrder(prev, current);
            }

            prev = current;
        }

        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests CustomIdGenerator for the presence of vowels through the
     * sequentialMint method at an arbitrary starting value.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testSequentialMintSansVowelsWithStartingValue(String prefix, boolean sansVowel,
            String charMap, int amount) {
        LOGGER.trace("inside testSequentialMintSansVowels");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator generator = new CustomIdGenerator(prefix, sansVowel, charMap);
        int startingValue = amount / 2;
        Set<Pid> sequentialSet = generator.sequentialMint(amount, startingValue);

        int counter = 0;
        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testCharMap(current.getName(), setting);

            if (prev != null && counter != startingValue) {
                PidTest.testOrder(prev, current);
            }

            counter++;

            prev = current;
        }

        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests CustomIdGenerator for the presence of vowels through the randomMint
     * method.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testRandomMintSansVowels(String prefix, boolean sansVowel,
            String charMap, int amount) {
        LOGGER.debug("inside testRandomMintSansVowels");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> randomSet = minter.randomMint(amount);

        for (Pid id : randomSet) {
            // fail if the id does not match the token 
            PidTest.testCharMap(id.getName(), setting);
        }
        // test to see if the amount matches the size of the generated set        
        Assert.assertEquals(randomSet.size(), amount);
    }

    /**
     * Tests to see if the sequentialMint method will print the desired prefix
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testSequentialMintPrefix(String prefix, boolean sansVowel, String charMap,
            int amount) {
        LOGGER.debug("inside testSequentialMintPrefix");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> sequentialSet = minter.sequentialMint(amount);

        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testPrefix(current.getName(), setting);

            if (prev != null) {
                PidTest.testOrder(prev, current);
            }

            prev = current;
        }

        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests to see if the sequentialMint method will print the desired prefix
     * at an arbitrary starting value.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testSequentialMintPrefixWithStartingValue(String prefix, boolean sansVowel, 
            String charMap, int amount) {
        LOGGER.debug("inside testSequentialMintPrefix");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator generator = new CustomIdGenerator(prefix, sansVowel, charMap);
        int startingValue = amount / 2;
        Set<Pid> sequentialSet = generator.sequentialMint(amount, startingValue);

        int counter = 0;
        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testPrefix(current.getName(), setting);

            if (prev != null && counter != startingValue) {
                PidTest.testOrder(prev, current);
            }

            counter++;
            prev = current;
        }

        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests to see if the randomMint method will print the desired prefix
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testRandomMintPrefix(String prefix, boolean sansVowel,
            String charMap, int amount) {
        LOGGER.debug("inside testRandomMintPrefix");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, 0, sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> randomSet = minter.randomMint(amount);

        for (Pid id : randomSet) {
            PidTest.testPrefix(id.getName(), setting);
        }

        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(randomSet.size(), amount);
    }

    /**
     * Tests sequentialMint to see if it will produce the correct length of Pids
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testSequentialLength(String prefix, boolean sansVowel, String charMap,
            int amount) {
        LOGGER.debug("inside testSequentialLength");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, charMap.length(), sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> sequentialSet = minter.sequentialMint(amount);

        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testRootLength(current.getName(), setting);

            if (prev != null) {
                PidTest.testOrder(prev, current);
            }

            prev = current;
        }

        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests sequentialMint to see if it will produce the correct length of Pids
     * at an arbitrary starting value.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testSequentialLengthWithStartingValue(String prefix, boolean sansVowel,
            String charMap, int amount) {
        LOGGER.debug("inside testSequentialLength");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, charMap.length(), sansVowel);

        // create a generator object
        CustomIdGenerator generator = new CustomIdGenerator(prefix, sansVowel, charMap);
        int startingValue = amount / 2;
        Set<Pid> sequentialSet = generator.sequentialMint(amount, startingValue);

        int counter = 0;
        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testRootLength(current.getName(), setting);

            if (prev != null && counter < startingValue) {
                PidTest.testOrder(prev, current);
            }

            counter++;

            prev = current;
        }

        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests randomMint to see if it will produce the correct length of Pids
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure PIDs
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testRandomLength(String prefix, boolean sansVowel, String charMap, int amount) {
        LOGGER.debug("inside testRandomLength");

        // store parameters in a setting object
        Setting setting = new Setting(prefix, null, charMap, charMap.length(), sansVowel);

        // create a generator object
        CustomIdGenerator minter = new CustomIdGenerator(prefix, sansVowel, charMap);
        Set<Pid> randomSet = minter.randomMint(amount);

        for (Pid id : randomSet) {
            PidTest.testRootLength(id.getName(), setting);
        }
        // test to see if the amount matches the size of the generated set
        Assert.assertEquals(randomSet.size(), amount);
    }

    /**
     * Tests to see if sequentialMint will through NotEnoughPermutation
     * exception when the amount exceeds the total permutations
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class)
    public void testSequentialNotEnoughPermutationException() {
        LOGGER.debug("inside testSequentialNotEnoughPermutationException");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");
        long total = minter.getMaxPermutation();

        Set<Pid> sequentialSet = minter.randomMint(total + 1);
    }

    /**
     * Tests to see if randomMint will through NotEnoughPermutation exception
     * when the amount exceeds the total permutations
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class)
    public void testRandomNotEnoughPermutationException() {
        LOGGER.debug("inside testRandomNotEnoughPermutationException");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");
        long total = minter.getMaxPermutation();

        Set<Pid> randomSet = minter.randomMint(total + 1);
    }

    /**
     * Tests to see if randomMint will through NotEnoughPermutation exception
     * when the amount is negative
     */
    @Test
    public void testRandomMintNegativeAmount() {
        LOGGER.debug("inside testRandomMintNegativeAmount");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");

        Set<Pid> randomSet = minter.randomMint(-1);
        Assert.assertEquals(randomSet.isEmpty(), true);
    }

    /**
     * Tests to see if sequentialMint will through NotEnoughPermutation
     * exception when the amount is negative
     */
    @Test
    public void testSequentialMintNegativeAmount() {
        LOGGER.debug("inside testSequentialMintNegativeAmount");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");
        long total = minter.getMaxPermutation();

        Set<Pid> sequentialSet = minter.sequentialMint(-1);
        Assert.assertEquals(sequentialSet.isEmpty(), true);
    }

    /**
     * Tests to see if the the randomMint method returns an empty set
     */
    @Test
    public void testRandomMintZeroAmount() {
        LOGGER.debug("inside testRandomMintZeroAmount");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");

        Set<Pid> randomSet = minter.randomMint(0);
        Assert.assertEquals(randomSet.isEmpty(), true);
    }

    /**
     * Tests to see if the the sequentialMint method returns an empty set
     */
    @Test
    public void testSequentialMintZeroAmount() {
        LOGGER.debug("inside testSequentialMintZeroAmount");

        IdGenerator minter = new CustomIdGenerator("", true, "ddddd");

        Set<Pid> sequentialSet = minter.sequentialMint(0);
        Assert.assertEquals(sequentialSet.isEmpty(), true);
    }
}
