package com.hida.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * An object that references a list of values that are used to create a Pid.
 *
 * @author lruffin
 */
@MappedSuperclass
public class Setting {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @NotNull
    @Column(name = "PID_PREFIX", nullable = false)
    private String Prefix;

    @Column(name = "TOKENTYPE")
    @Enumerated(EnumType.STRING)
    private Token TokenType;

    @Column(name = "CHARMAP")
    private String CharMap;

    @Column(name = "ROOTLENGTH")
    private int RootLength;

    @Column(name = "SANSVOWELS")
    private boolean SansVowels;

    /**
     * Copy constructor.
     *
     * @param s the CachedSetting to copy
     */
    public Setting(Setting s) {
        Prefix = s.getPrefix();
        TokenType = s.getTokenType();
        CharMap = s.getCharMap();
        RootLength = s.getRootLength();
        SansVowels = s.isSansVowels();
    }

    /**
     * Constructor that represents the necessary values all settings should contain
     *
     * @param Prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param TokenType An enum used to configure PIDS
     * @param CharMap A sequence of characters used to configure PIDs
     * @param RootLength Designates the length of the id's root
     * @param SansVowels Dictates whether or not vowels are allowed
     */
    public Setting(String Prefix, Token TokenType, String CharMap, int RootLength,
            boolean SansVowels) {
        this.Prefix = Prefix;
        this.TokenType = TokenType;
        this.CharMap = CharMap;
        this.RootLength = RootLength;
        this.SansVowels = SansVowels;
    }

    /**
     * Default constructor.
     */
    public Setting() {
    }

    // Typical getters and setters    
    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getPrefix() {
        return Prefix;
    }

    public void setPrefix(String Prefix) {
        this.Prefix = Prefix;
    }

    public Token getTokenType() {
        return TokenType;
    }

    public void setTokenType(Token TokenType) {
        this.TokenType = TokenType;
    }

    public String getCharMap() {
        return CharMap;
    }

    public void setCharMap(String CharMap) {
        this.CharMap = CharMap;
    }

    public int getRootLength() {
        return RootLength;
    }

    public void setRootLength(int RootLength) {
        this.RootLength = RootLength;
    }

    public boolean isSansVowels() {
        return SansVowels;
    }

    public void setSansVowels(boolean SansVowels) {
        this.SansVowels = SansVowels;
    }

}
