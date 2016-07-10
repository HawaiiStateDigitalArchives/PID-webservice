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
import java.util.Set;
import java.util.LinkedHashSet;
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
        // generate ids        
        Set<Pid> pidSet = new LinkedHashSet<>();

        // randomly generate pids using a random number generator
        for (int i = 0; i < amount; i++) {
            long value = Math.abs(Rng.nextLong()) % MaxPermutation;
            Pid pid = new Pid(this.longToName(value));

            // create pid and add it to the set
            while (!pidSet.add(pid)) {
                this.incrementPid(pid);
            }

            LOGGER.trace("Generated Auto Random ID: {}", pid);
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
        return sequentialMint(amount, 0);
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
        if (MaxPermutation < startingValue) {
            throw new NotEnoughPermutationsException(MaxPermutation, startingValue);
        }
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        if(startingValue < 0){
            throw new IllegalArgumentException("starting value cannot be negative");
        }

        // create a set to contain Pids
        Set<Pid> pidSet = new LinkedHashSet<>();
        for (int i = 0; i < amount; i++) {
            Pid newPid = new Pid(longToName(startingValue));
            pidSet.add(newPid);
            startingValue = (startingValue + 1) % MaxPermutation;

            LOGGER.trace("Generated Custom Sequential ID: {}", newPid);
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
