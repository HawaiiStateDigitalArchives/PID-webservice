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
package com.hida.model;

/**
 * An Id Generator that creates Pids. For each Pid, every digit in their names
 * will be created using the possible characters provided by a character map.
 * Each digit in a character map references a different Token object.
 *
 *
 * @author lruffin
 */
public class CustomIdGenerator extends IdGenerator {

    /**
     * The mapping used to describe range of possible characters at each of the
     * id's root's digits. There are total of 5 different ranges that a charMap
     * can contain:
     *
     * <pre>
     * d: digits only
     * l: lower case letters only
     * u: upper case letters only
     * m: letters only
     * e: any valid character specified by d, l, u, and m.
     * </pre>
     *
     */
    private String charMap_;

    /**
     * An array of Strings that contains the possible characters at each index
     * of Pid's root name. This field is generated by charMap_.
     */
    private final String[] tokenMap_;

    /**
     * A variable that will affect whether or not vowels have the possibility of
     * being included in each id.
     */
    private boolean sansVowel_;

    /**
     * Instantiates an Id Generator that creates Pids primarily based on a
     * charMap. The only valid charMap characters are regex("[dlume]+"). No
     * restrictions are placed on the other parameters.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param sansVowel Dictates whether or not vowels are allowed
     * @param charMap A sequence of characters used to configure Pids
     */
    public CustomIdGenerator(String prefix, boolean sansVowel, String charMap) {
        super(prefix);
        this.charMap_ = charMap;
        this.tokenMap_ = new String[charMap.length()];
        this.sansVowel_ = sansVowel;
        this.maxPermutation_ = getMaxPermutation();

        initializeTokenMap();
    }

    /**
     * This method calculates and returns the total possible number of
     * permutations using the values given in the constructor.
     *
     * @return number of permutations
     */
    @Override
    final public long getMaxPermutation() {
        long totalPermutations = 1;
        for (int i = 0; i < charMap_.length(); i++) {
            if (charMap_.charAt(i) == 'd') {
                totalPermutations *= 10;
            }
            else if (charMap_.charAt(i) == 'l' || charMap_.charAt(i) == 'u') {
                totalPermutations *= (sansVowel_) ? 20 : 26;
            }
            else if (charMap_.charAt(i) == 'm') {
                totalPermutations *= (sansVowel_) ? 40 : 52;
            }
            else if (charMap_.charAt(i) == 'e') {
                totalPermutations *= (sansVowel_) ? 50 : 62;
            }
        }
        return totalPermutations;
    }

    /**
     * Initializes TokenMap to contain a String of characters at each index to
     * designate the possible values that can be assigned to each index of a
     * Pid's BaseMap.
     */
    private void initializeTokenMap() {
        for (int i = 0; i < tokenMap_.length; i++) {
            // get char
            char c = charMap_.charAt(i);

            // assign each index a string of characters
            if (c == 'd') {
                tokenMap_[i] = Token.DIGIT.getCharacters();
            }
            else if (c == 'l') {
                tokenMap_[i] = (sansVowel_) ? Token.LOWER_CONSONANTS.getCharacters()
                        : Token.LOWER_ALPHABET.getCharacters();
            }
            else if (c == 'u') {
                tokenMap_[i] = (sansVowel_) ? Token.UPPER_CONSONANTS.getCharacters()
                        : Token.UPPER_ALPHABET.getCharacters();
            }
            else if (c == 'm') {
                tokenMap_[i] = (sansVowel_) ? Token.MIXED_CONSONANTS.getCharacters()
                        : Token.MIXED_ALPHABET.getCharacters();
            }
            else {
                tokenMap_[i] = (sansVowel_) ? Token.MIXED_CONSONANTS_EXTENDED.getCharacters()
                        : Token.MIXED_ALPHABET_EXTENDED.getCharacters();
            }
        }
    }

    /**
     * Increments a value of a PID. If the maximum limit is reached the values
     * will wrap around.
     *
     * @param pid The pid to increment
     */
    @Override
    public void incrementPid(Pid pid) {
        long next = (this.PidToLong(pid) + 1) % this.maxPermutation_;
        pid.setName(this.longToName(next));
    }

    /**
     * Translates an ordinal number into a sequence of characters with a radix
     * based on TokenType.
     *
     * @param ordinal The nth position of a permutation
     * @return The sequence at the nth position
     */
    @Override
    protected String longToName(long ordinal) {
        StringBuilder name = new StringBuilder("");
        int fullNameLength = charMap_.length() + prefix_.length();

        long remainder = ordinal;
        for (int i = fullNameLength - 1; i >= prefix_.length(); i--) {
            String map = tokenMap_[i - prefix_.length()];
            int radix = map.length();
            name.insert(0, map.charAt((int) (remainder % radix)));

            remainder /= radix;
        }

        return prefix_ + name.toString();
    }

    /**
     * Translates a Pid into an ordinal number.
     *
     * @param pid A persistent identifier
     * @return The ordinal number of the Pid
     */
    @Override
    protected long PidToLong(Pid pid) {
        String name = pid.getName();
        int fullNameLength = charMap_.length() + prefix_.length();
        int totalRadix = 1;

        long ordinal = 0;
        for (int i = fullNameLength - 1; i >= prefix_.length(); i--) {
            String map = tokenMap_[i - prefix_.length()];
            int mapIndex = map.indexOf(name.charAt(i));
            int currentRadix = map.length();

            ordinal += totalRadix * mapIndex;
            totalRadix *= currentRadix;
        }

        return ordinal;
    }

    /* getters and setters */
    public String getCharMap() {
        return charMap_;
    }

    public void setCharMap(String CharMap) {
        this.charMap_ = CharMap;
    }

    public boolean isSansVowel() {
        return sansVowel_;
    }

    public void setSansVowel(boolean SansVowel) {
        this.sansVowel_ = SansVowel;
    }
}
