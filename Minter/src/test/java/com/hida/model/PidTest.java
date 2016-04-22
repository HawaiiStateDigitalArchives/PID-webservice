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
    public void testPidPrepend(String name, DefaultSetting setting) {
        String prepend = setting.getPrepend();

        Assert.assertTrue(name + " testing prepend", name.startsWith(prepend));
    }

    /**
     * Tests the name to see if it matches the provided prefix
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPidPrefix(String name, Setting setting) {
        String prefix = setting.getPrefix();

        Assert.assertTrue(name + " testing prefix", name.startsWith(prefix));
    }

    /**
     * Tests the name to see if it matches the provided token type
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPidTokenType(String name, Setting setting) {
        String prefix = setting.getPrefix();
        TokenType tokenType = setting.getTokenType();
        boolean sansVowel = setting.isSansVowels();

        boolean matchesToken = containsCorrectCharacters(prefix, name, tokenType, sansVowel);
        Assert.assertEquals(name + " testing tokenType", true, matchesToken);
    }

    /**
     * Tests the name to see if it matches the provided root length
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPidRootLength(String name, Setting setting) {
        String prefix = setting.getPrefix();

        name = name.replace(prefix, "");
        Assert.assertEquals(name + " testing rootLength", name.length(), setting.getRootLength());
    }

    /**
     * Tests the name to see if it matches the provided char map
     *
     * @param name Unique identifier of a Pid
     * @param setting The desired setting used to create a Pid
     */
    public void testPidCharMap(String name, Setting setting) {
        String prefix = setting.getPrefix();
        String charMap = setting.getCharMap();
        boolean sansVowel = setting.isSansVowels();

        boolean matchesToken = containsCorrectCharacters(prefix, name, sansVowel, charMap);
        Assert.assertEquals(name + " testing charMap", true, matchesToken);
    }   

    /**
     * Checks to see if the Pid matches the given parameters
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param name Unique identifier of a Pid
     * @param tokenType An enum used to configure PIDS
     * @param sansVowel Dictates whether or not vowels are allowed
     * @return True if the Pid matches the parameters, false otherwise
     */
    private boolean containsCorrectCharacters(String prefix, String name, TokenType tokenType,
            boolean sansVowel) {
        String regex = retrieveRegex(tokenType, sansVowel);
        return name.matches(String.format("^(%s)%s$", prefix, regex));
    }

    /**
     * Checks to see if the Pid matches the given parameters
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param name Unique identifier of a Pid
     * @param tokenType An enum used to configure PIDS
     * @param sansVowel Dictates whether or not vowels are allowed
     * @return True if the Pid matches the parameters, false otherwise
     */
    private boolean containsCorrectCharacters(String prefix, String name, boolean sansVowel,
            String charMap) {
        String regex = retrieveRegex(charMap, sansVowel);
        return name.matches(String.format("^(%s)%s$", prefix, regex));
    }

    /**
     * Returns an equivalent regular expression that'll map that maps to a
     * specific TokenType
     *
     * @param tokenType Designates what characters are contained in the id's
     * root
     * @param sansVowel Dictates whether or not vowels are allowed
     * @return a regular expression
     */
    private String retrieveRegex(TokenType tokenType, boolean sansVowel) {

        switch (tokenType) {
            case DIGIT:
                return "([\\d]*)";
            case LOWERCASE:
                return (sansVowel) ? "([^aeiouyA-Z\\W\\d]*)" : "([a-z]*)";
            case UPPERCASE:
                return (sansVowel) ? "([^a-zAEIOUY\\W\\d]*)" : "([A-Z]*)";
            case MIXEDCASE:
                return (sansVowel) ? "([^aeiouyAEIOUY\\W\\d]*)" : "([a-zA-Z]*)";
            case LOWER_EXTENDED:
                return (sansVowel) ? "([^aeiouyA-Z\\W]*)" : "([a-z\\d]*)";
            case UPPER_EXTENDED:
                return (sansVowel) ? "([^a-zAEIOUY\\W]*)" : "([A-Z\\d]*)";
            default:
                return (sansVowel) ? "([^aeiouyAEIOUY\\W]*)" : "(^[a-zA-z\\d]*)";
        }
    }

    /**
     * Returns an equivalent regular expression that'll map that maps to a
     * specific TokenType
     *
     * @param charMap Designates what characters are contained in the id's root
     * @param sansVowel Dictates whether or not vowels are allowed
     * @return a regular expression
     */
    private String retrieveRegex(String charMap, boolean sansVowel) {
        String regex = "";
        for (int i = 0; i < charMap.length(); i++) {
            char key = charMap.charAt(i);
            if (key == 'd') {
                regex += "[\\d]";
            }
            else if (key == 'l') {
                regex += (sansVowel) ? "[^aeiouyA-Z\\W\\d]" : "[a-z]";
            }
            else if (key == 'u') {
                regex += (sansVowel) ? "[^a-zAEIOUY\\W\\d]" : "[A-Z]";
            }
            else if (key == 'm') {
                regex += (sansVowel) ? "[^aeiouyAEIOUY\\W\\d]" : "[a-zA-Z]";
            }
            else if (key == 'e') {
                regex += (sansVowel) ? "[^aeiouyAEIOUY\\W]" : "[a-zA-z\\d]";
            }
        }
        return regex;
    }

}
