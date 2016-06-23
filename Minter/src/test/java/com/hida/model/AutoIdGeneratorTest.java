package com.hida.model;

import java.util.Iterator;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the functionality of AutoIdGenerator
 *
 * @author lruffin
 */
public class AutoIdGeneratorTest {

    private final PidTest PidTest = new PidTest();

    /**
     * Data set with varying tokenType values
     *
     * @return A data set
     */
    @DataProvider(name = "sansVowel")
    public Object[][] sansVowelParameters() {
        return new Object[][]{
            {"", true, Token.DIGIT, 1, 10},
            {"", true, Token.LOWER_CONSONANTS, 1, 20},
            {"", true, Token.UPPER_CONSONANTS, 1, 20},
            {"", true, Token.MIXED_CONSONANTS, 1, 40},
            {"", true, Token.LOWER_CONSONANTS_EXTENDED, 1, 30},
            {"", true, Token.UPPER_CONSONANTS_EXTENDED, 1, 30},
            {"", true, Token.MIXED_CONSONANTS_EXTENDED, 1, 50},
            {"", false, Token.LOWER_ALPHABET, 1, 26},
            {"", false, Token.UPPER_ALPHABET, 1, 26},
            {"", false, Token.MIXED_ALPHABET, 1, 52},
            {"", false, Token.LOWER_ALPHABET_EXTENDED, 1, 36},
            {"", false, Token.UPPER_ALPHABET_EXTENDED, 1, 36},
            {"", false, Token.MIXED_ALPHABET_EXTENDED, 1, 62}
        };
    }

    /**
     * Data set with varying prefix values
     *
     * @return A data set
     */
    @DataProvider(name = "prefix")
    public Object[][] prefixParameters() {
        return new Object[][]{
            {"", false, Token.DIGIT, 1, 10},
            {"!@*(", false, Token.DIGIT, 1, 10},
            {"www", false, Token.DIGIT, 1, 10},
            {"123", false, Token.DIGIT, 1, 10},
            {"123abc", false, Token.DIGIT, 1, 10},
            {"!a1", false, Token.DIGIT, 1, 10},
            {" ", false, Token.DIGIT, 1, 10}
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
        for (int i = 1; i <= 2; i++) {
            Object[] array = {"", false, Token.DIGIT, i, (int) Math.pow(10, i)};
            parameter[i - 1] = array;
        }
        return parameter;
    }

    /**
     * Tests the AutoIdGenerator for the presence of vowels through the
     * sequentialMint method.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testSequentialMintSansVowels(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        Set<Pid> sequentialSet = generator.sequentialMint(amount);

        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testTokenType(current.getName(), setting);

            if (prev != null) {
                PidTest.testOrder(prev, current);
            }

            prev = current;
        }

        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests the AutoIdGenerator for the presence of vowels through the
     * sequentialMint method at an arbitrary starting value.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testSequentialMintSansVowelsWithStartingValue(String prefix, boolean sansVowel,
            Token tokenType, int rootLength, int amount) {

        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        int startingValue = amount / 2;
        Set<Pid> sequentialSet = generator.sequentialMint(amount, startingValue);

        int counter = 0;
        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testTokenType(current.getName(), setting);

            if (prev != null && counter != startingValue) {
                PidTest.testOrder(prev, current);
            }

            counter++;
            prev = current;
        }

        Assert.assertEquals(sequentialSet.size(), amount);
    }

    /**
     * Tests AutoIdGenerator for the presence of vowels through the randomMint
     * method
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "sansVowel")
    public void testRandomMintSansVowels(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        Set<Pid> randomSet = generator.randomMint(amount);

        for (Pid id : randomSet) {
            PidTest.testTokenType(id.getName(), setting);
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
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testSequentialMintPrefix(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        Set<Pid> sequentialSet = generator.sequentialMint(amount);

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
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testSequentialMintPrefixWithStartingValue(String prefix, boolean sansVowel,
            Token tokenType, int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
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
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "prefix")
    public void testRandomMintPrefix(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {

        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);
        IdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        Set<Pid> randomSet = generator.randomMint(amount);

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
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testSequentialRootLength(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);

        // create a new minter and create a set
        AutoIdGenerator minter = new AutoIdGenerator(prefix, tokenType, rootLength);
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
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testSequentialRootLengthWithStartingValue(String prefix, boolean sansVowel,
            Token tokenType, int rootLength, int amount) {

        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);

        // create a new minter and create a set
        AutoIdGenerator generator = new AutoIdGenerator(prefix, tokenType, rootLength);
        int startingValue = amount / 2;
        Set<Pid> sequentialSet = generator.sequentialMint(amount, startingValue);

        int counter = 0;
        Pid prev = null;
        Iterator<Pid> iter = sequentialSet.iterator();
        while (iter.hasNext()) {
            // fail if the length does not match
            Pid current = iter.next();
            PidTest.testRootLength(current.getName(), setting);

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
     * Tests randomMint to see if it will produce the correct length of Pids
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     * @param amount The number of PIDs to be created
     */
    @Test(dataProvider = "rootLength")
    public void testRandomRootLength(String prefix, boolean sansVowel, Token tokenType,
            int rootLength, int amount) {
        // store parameters in a setting object
        Setting setting = new Setting(prefix, tokenType, null, rootLength, sansVowel);

        // create a new minter and create a set
        AutoIdGenerator minter = new AutoIdGenerator(prefix, tokenType, rootLength);
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
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);
        long total = minter.getMaxPermutation();

        Set<Pid> sequentialSet = minter.randomMint(total + 1);
    }

    /**
     * Tests to see if randomMint will through NotEnoughPermutation exception
     * when the amount exceeds the total permutations
     */
    @Test(expectedExceptions = NotEnoughPermutationsException.class)
    public void testRandomNotEnoughPermutationException() {
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);
        long total = minter.getMaxPermutation();

        Set<Pid> randomSet = minter.randomMint(total + 1);
    }

    /**
     * Tests to see if randomMint will through NotEnoughPermutation exception
     * when the amount is negative
     */
    @Test
    public void testRandomMintNegativeAmount() {
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);

        Set<Pid> randomSet = minter.randomMint(-1);
        Assert.assertEquals(randomSet.isEmpty(), true);
    }

    /**
     * Tests to see if sequentialMint will through NotEnoughPermutation
     * exception when the amount is negative
     */
    @Test
    public void testSequentialMintNegativeAmount() {
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);
        long total = minter.getMaxPermutation();

        Set<Pid> sequentialSet = minter.sequentialMint(-1);
        Assert.assertEquals(sequentialSet.isEmpty(), true);
    }

    /**
     * Tests to see if the the randomMint method returns an empty set
     */
    @Test
    public void testRandomMintZeroAmount() {
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);

        Set<Pid> randomSet = minter.randomMint(0);
        Assert.assertEquals(randomSet.isEmpty(), true);
    }

    /**
     * Tests to see if the the sequentialMint method returns an empty set
     */
    @Test
    public void testSequentialMintZeroAmount() {
        IdGenerator minter = new AutoIdGenerator("", Token.DIGIT, 5);

        Set<Pid> sequentialSet = minter.sequentialMint(0);
        Assert.assertEquals(sequentialSet.isEmpty(), true);
    }
}
