package com.hida.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A type of Setting that references the default values that could be used to
 * create a set of Pids. These default values can be overridden and only one
 * DefaultSetting object is persisted in the database at any given time.
 *
 * @author lruffin
 */
@Entity
@Table(name = "DEFAULT_SETTING")
public class DefaultSetting extends Setting {

    @Column(name = "PREPEND", nullable = false)
    private String Prepend;

    @Column(name = "ISAUTO")
    private boolean Auto;

    @Column(name = "ISRANDOM")
    private boolean Random;
    
    @Column(name = "CACHESIZE")
    private long CacheSize;

    /**
     * Constructor used to create a DefaultSetting entity
     *
     * @param Prepend Primarily used to turn a Pid into a PURL
     * @param Prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param CacheSize The size of the cache that'll be generated whenever the
     * context is refreshed
     * @param TokenType An enum used to configure PIDS
     * @param CharMap A sequence of characters used to configure PIDs
     * @param RootLength Designates the length of the id's root
     * @param SansVowels Dictates whether or not vowels are allowed
     * @param Auto Determines which generator, either Auto or Custom, will be
     * used
     * @param Random Determines if the PIDs are created randomly or sequentially
     */
    public DefaultSetting(String Prepend, String Prefix, long CacheSize, Token TokenType, 
            String CharMap, int RootLength, boolean SansVowels, boolean Auto, boolean Random) {
        super(Prefix, TokenType, CharMap, RootLength, SansVowels);
        this.Prepend = Prepend;
        this.CacheSize = CacheSize;
        this.Auto = Auto;
        this.Random = Random;

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.Prepend);
        hash = 43 * hash + Objects.hashCode(this.getCharMap());
        hash = 43 * hash + Objects.hashCode(this.getPrefix());
        hash = 43 * hash + Objects.hashCode(this.getTokenType());
        hash = 43 * hash + Objects.hashCode(this.getRootLength());
        hash = 43 * hash + Objects.hashCode(this.isSansVowels());
        hash = 43 * hash + (this.Auto ? 1 : 0);
        hash = 43 * hash + (this.Random ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultSetting other = (DefaultSetting) obj;
        if (!Objects.equals(this.Prepend, other.Prepend)) {
            return false;
        }
        if (!Objects.equals(this.getPrefix(), other.getPrefix())) {
            return false;
        }
        if (!Objects.equals(this.getRootLength(), other.getRootLength())) {
            return false;
        }
        if (!Objects.equals(this.getCharMap(), other.getCharMap())) {
            return false;
        }
        if (!Objects.equals(this.getTokenType(), other.getTokenType())) {
            return false;
        }
        if (!Objects.equals(this.isSansVowels(), other.isSansVowels())) {
            return false;
        }
        if (this.Auto != other.Auto) {
            return false;
        }
        if (this.Random != other.Random) {
            return false;
        }
        return true;
    }

    /**
     * No-arg constructor used by Hibernate
     */
    public DefaultSetting() {

    }

    public String getPrepend() {
        return this.Prepend;
    }

    public void setPrepend(String prepend) {
        this.Prepend = prepend;
    }

    public boolean isAuto() {
        return Auto;
    }

    public void setAuto(boolean Auto) {
        this.Auto = Auto;
    }

    public boolean isRandom() {
        return Random;
    }

    public void setRandom(boolean Random) {
        this.Random = Random;
    }

    public long getCacheSize() {
        return CacheSize;
    }

    public void setCacheSize(long CacheSize) {
        this.CacheSize = CacheSize;
    }        
}
