/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hida;

import static com.hida.TestMinter.Rng;
import static com.hida.TestMinter.Logger;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author lruffin
 */
public class AutoMinter extends TestMinter {

    /**
     * Designates what characters are contained in the id's root. There are 7
     * types of token maps, each describing a range of possible characters in
     * the id. This range is further affected by the variable SansVowel.
     *
     * <pre>
     * DIGIT: Digit values only.
     * LOWERCASE: Lowercase letters only.
     * UPPERCASE: Uppercase letters only.
     * MIXEDCASE: Lowercase and Uppercase letters only.
     * LOWER_EXTENDED: Digit values and Lowercase letters only.
     * UPPER_EXTENDED: Digits and Uppercase letters only
     * MIXED_EXTENDED: All characters specified by previous tokens
     * </pre>
     *
     */
    private TokenType TokenType;

    /**
     * Designates the length of the id's root.
     */
    private int RootLength;

    public AutoMinter(String prepend, String prefix, boolean sansVowel, TokenType tokenType,
            int rootLength) {
        super(prepend, prefix, sansVowel);
        this.TokenType = tokenType;
        this.RootLength = rootLength;
        
        // assign base map the appropriate values
        this.BaseMap.put(TokenType.DIGIT, DIGIT_TOKEN);
        if (sansVowel) {
            this.BaseMap.put(TokenType.LOWERCASE, SANS_VOWEL_TOKEN);
            this.BaseMap.put(TokenType.UPPERCASE, SANS_VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.MIXEDCASE, 
                    SANS_VOWEL_TOKEN + SANS_VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.LOWER_EXTENDED, DIGIT_TOKEN + SANS_VOWEL_TOKEN);
            this.BaseMap.put(TokenType.UPPER_EXTENDED, 
                    DIGIT_TOKEN + SANS_VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.MIXED_EXTENDED,
                    DIGIT_TOKEN + SANS_VOWEL_TOKEN + SANS_VOWEL_TOKEN.toUpperCase());
        } else {
            this.BaseMap.put(TokenType.LOWERCASE, VOWEL_TOKEN);
            this.BaseMap.put(TokenType.UPPERCASE, VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.MIXEDCASE, VOWEL_TOKEN + VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.LOWER_EXTENDED, DIGIT_TOKEN + VOWEL_TOKEN);
            this.BaseMap.put(TokenType.UPPER_EXTENDED, DIGIT_TOKEN + VOWEL_TOKEN.toUpperCase());
            this.BaseMap.put(TokenType.MIXED_EXTENDED,
                    DIGIT_TOKEN + VOWEL_TOKEN + VOWEL_TOKEN.toUpperCase());
        }
        Logger.info("BaseMap values set to: "+BaseMap);
    }

    /**
     * missing javadoc
     *
     * @param amount
     * @return
     */
    @Override
    public Set<Id> randomMint(long amount) {
        String tokenMap = BaseMap.get(TokenType);

        Set<Id> tempIdList = new LinkedHashSet((int) amount);

        for (int i = 0; i < amount; i++) {
            int[] tempIdBaseMap = new int[RootLength];
            for (int j = 0; j < RootLength; j++) {
                tempIdBaseMap[j] = Rng.nextInt(tokenMap.length());
            }
            Id currentId = new AutoId(Prefix, tempIdBaseMap, tokenMap);
            Logger.info("Generated Auto Random ID: " + currentId);

            while (tempIdList.contains(currentId)) {
                currentId.incrementId();
            }
            tempIdList.add(currentId);
        }
        return tempIdList;
    }

    /**
     * missing javadoc
     *
     * @param amount
     * @return
     */
    @Override
    public Set<Id> sequentialMint(long amount) {
        //Logger.info("in genIdAutoSequential: " + amount);        

        // checks to see if its possible to produce or add requested amount of
        // ids to database
        String tokenMap = BaseMap.get(TokenType);

        Set<Id> tempIdList = new TreeSet();

        int[] previousIdBaseMap = new int[RootLength];
        AutoId firstId = new AutoId(Prefix, previousIdBaseMap, tokenMap);
        Logger.info("Generated Auto Sequential ID: " + firstId);
        tempIdList.add(firstId);

        for (int i = 0; i < amount - 1; i++) {
            AutoId currentId = new AutoId(firstId);
            Logger.info("Generated Auto Sequential ID: " + currentId);
            currentId.incrementId();
            tempIdList.add(currentId);
            firstId = new AutoId(currentId);
        }

        return tempIdList;
    }

    /* getters and setters */
    public TokenType getTokenType() {
        return TokenType;
    }

    public void setTokenType(TokenType TokenType) {
        this.TokenType = TokenType;
    }
    
    public int getRootLength() {
        return RootLength;
    }

    public void setRootLength(int RootLength) {
        this.RootLength = RootLength;
    }
    
    /**
     * Created and used by AutoMinter
     */
    private class AutoId extends Id {

        private String TokenMap;

        public AutoId(AutoId id) {
            super(id);
            this.TokenMap = id.getTokenMap();
        }

        public AutoId(String prefix, int[] baseMap, String tokenMap) {
            super(baseMap, prefix);
            this.TokenMap = tokenMap;
        }

        @Override
        public boolean incrementId() {
            int range = this.getBaseMap().length - 1;

            boolean overflow = true;
            for (int k = 0; k < this.getBaseMap().length && overflow; k++) {
                // record value of current index
                int value = this.getBaseMap()[range - k];

                if (value == TokenMap.length() - 1) {
                    this.getBaseMap()[range - k] = 0;
                } else {
                    this.getBaseMap()[range - k]++;
                    overflow = false;
                }
            }

            return !overflow;
        }

        @Override
        public String getRootName() {
            String charId = "";
            for (int i = 0; i < this.getBaseMap().length; i++) {
                charId += TokenMap.charAt(this.getBaseMap()[i]);
            }
            return charId;
        }

        @Override
        public String toString() {
            return Prefix + this.getRootName();
        }

        // getters and setters
        public String getTokenMap() {
            return TokenMap;
        }

        public void setTokenMap(String TokenMap) {
            this.TokenMap = TokenMap;
        }

    }
}
