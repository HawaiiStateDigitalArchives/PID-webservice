package com.hida.model;

/**
 * A group of objects that uniquely identifies a sequence of possible and valid
 * characters that can be found in the name of a Pid.
 *
 * @author lruffin
 */
public enum Token {

    DIGIT("0123456789"),
    LOWER_ALPHABET("abcdefghijklmnopqrstuvwxyz"),
    UPPER_ALPHABET("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    MIXED_ALPHABET("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LOWER_ALPHABET_EXTENDED("0123456789abcdefghijklmnopqrstuvwxyz"),
    UPPER_ALPHABET_EXTENDED("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    MIXED_ALPHABET_EXTENDED("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LOWER_CONSONANTS("bcdfghjklmnpqrstvwxz"),
    UPPER_CONSONANTS("BCDFGHJKLMNPQRSTVWXZ"),
    MIXED_CONSONANTS("bcdfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ"),
    LOWER_CONSONANTS_EXTENDED("0123456789bcdfghjklmnpqrstvwxz"),
    UPPER_CONSONANTS_EXTENDED("0123456789BCDFGHJKLMNPQRSTVWXZ"),
    MIXED_CONSONANTS_EXTENDED("0123456789bcdfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ");

    private final String characters;

    Token(final String characters) {
        this.characters = characters;
    }

    public String getCharacters() {
        return characters;
    }
}
