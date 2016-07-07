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

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.LongStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract Id generator used to create Pids
 *
 * @author Brittany Cruz
 * @author lruffin
 */
public abstract class IdGenerator {

    protected long MaxPermutation;

    /**
     * Creates and new random number generator to aid in the production of
     * non-deterministic ids.
     */
    private static final SecureRandom Rng = new SecureRandom();

    /**
     * LOGGER; logfile to be stored in resource folder
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerator.class);

    /**
     * The string that will be at the front of every id
     */
    protected String Prefix;

    public IdGenerator(String prefix) {
        this.Prefix = prefix;
    }

    public abstract long getMaxPermutation();

    public abstract void incrementPid(Pid pid);

    protected abstract String longToName(long value);

    protected abstract long PidToLong(Pid pid);

    /**
     * Creates Pids without regard to a natural order.
     *
     * @param amount The number of Pids to be created
     * @return A set of Pids
     */
    public Set<Pid> randomMint(long amount) {
        // checks to see if its possible to produce or add requested amount of
        if (MaxPermutation < amount) {
            throw new NotEnoughPermutationsException(MaxPermutation, amount);
        }
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }

        // create sets to guarantee unique membership           
        Set<Long> longSet = new LinkedHashSet<>();
        Set<Pid> pidSet = new LinkedHashSet<>();

        // create a LongStream of size amount, bound by [0, maxPermutation_)                
        LongStream longStream = Rng.longs(amount, 0, MaxPermutation);

        // iterate through every member of the stream
        Iterator<Long> longIter = longStream.iterator();
        while (longIter.hasNext()) {

            // try to add the value to the set, if it can't be added increment it
            long value = longIter.next();
            while (!longSet.add(value)) {
                value = (value + 1) % MaxPermutation;
            }

            // if a value can be added to longSet then it can be added to PidSet
            Pid pid = new Pid(longToName(value));
            pidSet.add(pid);
        }

        return pidSet;
    }

    /**
     * Creates Pids in ascending order
     *
     * @param amount The number of PIDs to be created
     * @return A set of Pids
     */
    public Set<Pid> sequentialMint(long amount) {
        // checks to see if its possible to produce or add requested amount of
        if (MaxPermutation < amount) {
            throw new NotEnoughPermutationsException(MaxPermutation, amount);
        }

        // create a set to contain Pids
        Set<Pid> pidSet = new LinkedHashSet<>();

        long ordinal = 0;
        Pid basePid = new Pid(this.longToName(ordinal));
        for (int i = 0; i < amount; i++) {

            // copy the Name of basePid into a new Pid instance
            Pid pid = new Pid(basePid.getName());

            // add the pid to the set
            pidSet.add(pid);

            // increment the base Pid
            this.incrementPid(basePid);

            LOGGER.trace("Generated Custom Sequential ID: {}", pid);
        }
        return pidSet;
    }

    /**
     * Creates Pids in ascending order starting at an arbitrary value.
     *
     * @param amount The number of Pids to be created
     * @param startingValue The value to start sequentially generating Pids
     * @return A set of Pids
     */
    public Set<Pid> sequentialMint(long amount, long startingValue) {
        if (MaxPermutation < amount) {
            throw new NotEnoughPermutationsException(MaxPermutation, amount);
        }

        // create a set to contain Pids
        Set<Pid> pidSet = new LinkedHashSet<>();

        long ordinal = startingValue;
        Pid basePid = new Pid(this.longToName(ordinal));
        for (int i = 0; i < amount; i++) {

            // copy the Name of basePid into a new Pid instance
            Pid pid = new Pid(basePid.getName());

            // add the pid to the set
            pidSet.add(pid);

            // increment the base Pid
            this.incrementPid(basePid);

            LOGGER.trace("Generated Custom Sequential ID: {}", pid);
        }

        return pidSet;
    }

    /**
     * Checks whether or not the prefix is valid.
     *
     * @param prefix The string that will be at the front of every id.
     * @return true if it contains numbers and letters and does not exceed 20
     * characters.
     */
    public static final boolean isValidPrefix(String prefix) {
        return prefix.matches("^[0-9a-zA-Z]*$") && prefix.length() <= 20;
    }

    /**
     * Checks whether or not the requested amount is valid.
     *
     * @param amount The amount of ids requested.
     * @return True if amount is non-negative.
     */
    public static final boolean isValidAmount(long amount) {
        return amount >= 0;
    }

    /**
     * Checks whether or not the requested root length is valid
     *
     * @param rootLength Designates the length of the id's root.
     * @return True if rootLength is non-negative and less than or equal to 10.
     */
    public static final boolean isValidRootLength(long rootLength) {
        return rootLength >= 0 && rootLength <= 10;
    }

    /**
     * Checks whether or not the given charMap is valid for this minter.
     *
     * @param charMap The mapping used to describe range of possible characters
     * at each of the id's root's digits.
     * @return True if charMap only contains the characters: 'd', 'l', 'u', 'm',
     * or 'e'.
     */
    public static final boolean isValidCharMap(String charMap) {
        return charMap.matches("^[dlume]+$");
    }

    /* typical getter and setter methods */
    public String getPrefix() {
        return Prefix;
    }

    public void setPrefix(String Prefix) {
        this.Prefix = Prefix;
    }
}
