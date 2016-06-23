package com.hida.model;

import junit.framework.Assert;

/**
 * A class created to test the validity of a Pid
 *
 * @author lruffin
 */
public class PidTest {

    /**
     * Tests the name to see if it matches the provided prepend
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPrepend(String name, DefaultSetting setting) {
        String prepend = setting.getPrepend();

        Assert.assertTrue(name + ", testing prepend: " + prepend, name.startsWith(prepend));
    }

    /**
     * Tests the name to see if it matches the provided prefix
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPrefix(String name, Setting setting) {
        String prefix = setting.getPrefix();

        Assert.assertTrue(name + ", testing prefix: " + prefix, name.startsWith(prefix));
    }

    /**
     * Tests the name to see if it matches the provided token type
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testTokenType(String name, Setting setting) {
        String prefix = setting.getPrefix();
        Token tokenType = setting.getTokenType();
        String rootName = name.replace(prefix, "");

        // check to see if each character in the rootName is contained by the Token
        boolean contains = true;
        for (int i = 0; i < rootName.length() && contains; i++) {
            char c = rootName.charAt(i);

            // if the name doesn't contain the character, then contains is false
            if (!tokenType.getCharacters().contains(c + "")) {
                contains = false;
            }
        }

        Assert.assertEquals(name + ", testing tokenType: " + tokenType, true, contains);
    }

    /**
     * Tests the name to see if it matches the provided root length
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testRootLength(String name, Setting setting) {
        String prefix = setting.getPrefix();

        name = name.replace(prefix, "");
        int rootLength = name.length();
        int expRootLength = setting.getRootLength();
        Assert.assertEquals(name + ", testing rootLength: " + rootLength,
                rootLength, expRootLength);
    }

    /**
     * Tests the name to see if it matches the provided char map
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testCharMap(String name, Setting setting) {
        String prefix = setting.getPrefix();
        String charMap = setting.getCharMap();
        String rootName = name.replace(prefix, "");

        boolean sansVowel = setting.isSansVowels();
        boolean contains = true;

        for (int i = 0; i < charMap.length() && contains; i++) {
            char mappedCharacter = charMap.charAt(i);
            String c = rootName.charAt(i) + "";
            switch (mappedCharacter) {
                case 'd':
                    contains = Token.DIGIT.getCharacters().contains(c);
                    break;
                case 'l':
                    contains = (sansVowel)
                            ? Token.LOWER_CONSONANTS.getCharacters().contains(c)
                            : Token.LOWER_ALPHABET.getCharacters().contains(c);
                    break;
                case 'u':
                    contains = (sansVowel)
                            ? Token.UPPER_CONSONANTS.getCharacters().contains(c)
                            : Token.UPPER_ALPHABET.getCharacters().contains(c);
                    break;
                case 'm':
                    contains = (sansVowel)
                            ? Token.MIXED_CONSONANTS.getCharacters().contains(c)
                            : Token.MIXED_ALPHABET.getCharacters().contains(c);
                    break;
                case 'e':
                    contains = (sansVowel)
                            ? Token.MIXED_CONSONANTS_EXTENDED.getCharacters().contains(c)
                            : Token.MIXED_ALPHABET_EXTENDED.getCharacters().contains(c);
                    break;
                default:
                    contains = false;
                    break;
            }
        }

        Assert.assertEquals(name + ", testing charMap: " + charMap, true, contains);
    }

    /**
     * Tests the order of two names. The previous name must have a lesser value
     * than the next name to pass the test.
     *
     * @param previous The previous name
     * @param next The next name
     */
    public void testOrder(Pid previous, Pid next) {
        Assert.assertEquals(-1, previous.compareTo(next));
    }      
}
