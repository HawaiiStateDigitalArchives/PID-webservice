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
    private String prepend_;

    @Column(name = "ISAUTO")
    private boolean auto_;

    @Column(name = "ISRANDOM")
    private boolean random_;

    @Column(name = "CACHESIZE")
    private long cacheSize_;

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
        this.prepend_ = Prepend;
        this.cacheSize_ = CacheSize;
        this.auto_ = Auto;
        this.random_ = Random;

    }

    /**
     * Copy constructor
     *
     * @param setting the values to copy
     */
    public DefaultSetting(DefaultSetting setting) {
        this.setPrepend(setting.getPrepend());
        this.setPrefix(setting.getPrefix());
        this.setCacheSize(setting.getCacheSize());
        this.setTokenType(setting.getTokenType());
        this.setCharMap(setting.getCharMap());
        this.setRootLength(setting.getRootLength());
        this.setSansVowels(setting.isSansVowels());
        this.setAuto(setting.isAuto());
        this.setRandom(setting.isRandom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPrepend(),
                this.getCacheSize(),
                this.getCharMap(),
                this.getPrefix(),
                this.getTokenType(),
                this.getRootLength(),
                this.isAuto(),
                this.isRandom(),
                this.isSansVowels());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultSetting)) {
            return false;
        }

        final DefaultSetting other = (DefaultSetting) obj;

        return Objects.equals(this.getPrepend(), other.getPrepend())
                && Objects.equals(this.getPrefix(), other.getPrefix())
                && Objects.equals(this.getRootLength(), other.getRootLength())
                && Objects.equals(this.getCharMap(), other.getCharMap())
                && Objects.equals(this.getTokenType(), other.getTokenType())
                && this.isSansVowels() == other.isSansVowels()
                && this.isAuto() == other.isAuto()
                && this.isRandom() == other.isRandom();
    }

    /**
     * No-arg constructor used by Hibernate
     */
    public DefaultSetting() {

    }

    public String getPrepend() {
        return this.prepend_;
    }

    public void setPrepend(String prepend) {
        this.prepend_ = prepend;
    }

    public boolean isAuto() {
        return auto_;
    }

    public void setAuto(boolean Auto) {
        this.auto_ = Auto;
    }

    public boolean isRandom() {
        return random_;
    }

    public void setRandom(boolean Random) {
        this.random_ = Random;
    }

    public long getCacheSize() {
        return cacheSize_;
    }

    public void setCacheSize(long CacheSize) {
        this.cacheSize_ = CacheSize;
    }
}
