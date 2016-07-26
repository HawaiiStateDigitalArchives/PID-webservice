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
package com.hida.service;

import com.hida.model.AutoIdGenerator;
import com.hida.model.Cache;
import com.hida.model.CustomIdGenerator;
import com.hida.model.DefaultSetting;
import com.hida.model.IdGenerator;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.Pid;
import com.hida.repositories.PidRepository;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service that handles the generation and persistence of Pids. All generated
 * sets of Pids that are guaranteed to be disjoint from the database.
 *
 * @author lruffin
 */
@Service("repositoryService")
@Transactional
public class GeneratorService {

    @Autowired
    private PidRepository pidRepo_;

    private IdGenerator generator_;

    private DefaultSetting cacheSetting_;

    private long startingValue_;

    private Cache<Pid> cache_ = new Cache<>();

    /**
     * Logger; logfile to be stored in resource folder
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorService.class);

    /**
     * Creates a set of Pids that are guaranteed to be disjoint from the set of
     * Pids persisted in the database. If the current setting is random, have
     * the generator return a random set, otherwise, have the generator return a
     * sequential set
     *
     * @param setting The settings the Pids are based off of
     * @param amount The requested amount of Pids
     * @return A set of Pids
     */
    public Set<Pid> generatePids(DefaultSetting setting, long amount)
            throws NotEnoughPermutationsException {
        // create a generator
        generator_ = getGenerator(setting);
        long max = generator_.getMaxPermutation();

        // collect the cache when possible and ensure that the cache is still unique
        Set<Pid> set1 = new LinkedHashSet<>();
        if (setting.equals(cacheSetting_)) {
            set1 = cache_.peek(amount);
            rollPidSet(set1, max);
        }
        
        // if the amount from the cache isn't enough then generate Pids to meet request
        if (amount > set1.size()) {            
            // create a second set of Pids to add to the original
            Set<Pid> set2 = createSet(setting, amount - set1.size());
            // combine the original and new set
            this.combinePidSet(set1, set2, max);
        }

        if (set1.size() != amount) {
            throw new NotEnoughPermutationsException(set1.size(), amount);
        }
        else {
            cache_.collect(amount);
            return set1;
        }
    }

    /**
     * Retrieves the maximum number of possible permutation based on the passed
     * setting.
     *
     * @param setting The setting values to base the Pids off of
     * @return The maximum number of permutations
     */
    public long getMaxPermutation(DefaultSetting setting) {
        generator_ = this.getGenerator(setting);
        return generator_.getMaxPermutation();
    }

    /**
     * Generates the cache. The method will check to see if the values passed in
     * setting matches the setting of the cache. If it does it'll attempt to
     * regenerate the cache, otherwise it'll create an entirely new cache.
     *
     * @param setting The setting values to base the Pids off of
     */
    public void generateCache(DefaultSetting setting) {

        // try to fulfill the requested cache size        
        long amount;
        long max = getMaxPermutation(setting);

        // regenerate the cache
        if (setting.equals(cacheSetting_)) {
            if (max > setting.getCacheSize()) {
                amount = setting.getCacheSize() - cache_.getSize();
            }
            else {
                amount = max - cache_.getSize();
            }
            Set<Pid> set = createSet(setting, amount);
            Set<Pid> cachedSet = cache_.collect(amount);
            this.combinePidSet(cachedSet, set, max);
            cache_ = new Cache(cachedSet);
        }
        // store the setting and create a new cache
        else {
            this.cacheSetting_ = setting;
            cache_.removeAll();
            if (max > setting.getCacheSize()) {
                amount = setting.getCacheSize();
            }
            else {
                amount = max;
            }
            Set<Pid> set = createSet(setting, amount);
            this.rollPidSet(set, max);

            cache_ = new Cache(set);
            // reset startingValue_
            startingValue_ = 0;
        }
    }

    /**
     * Retrieves the requested amount of Pids from the cache.
     *
     * @param amount The amount of Pids
     * @return A set containing the requested amount of Pids
     */
    public Set<Pid> peekCache(long amount) {
        return cache_.peek(amount);
    }

    /**
     * Retrieves the requested amount of Pids from the cache. Also removes the
     * selected Pids from the cache.
     *
     * @param amount The amount of Pids
     * @return A set containing the requested amount of Pids
     */
    public Set<Pid> collectCache(long amount) {
        return cache_.collect(amount);
    }

    /**
     * Removes all elements in the cache
     */
    public void clearCache() {
        cache_.removeAll();
    }

    /**
     * Saves a Pid to the database
     *
     * @param pid Pid to be saved
     */
    public void savePid(Pid pid) {
        pidRepo_.save(pid);
    }

    /**
     * Gets the starting value of the generator
     *
     * @return starting value
     */
    public long getStartingValue() {
        return startingValue_;
    }

    /**
     * Gets the size of the cache
     *
     * @return size of the cache
     */
    public long getCacheSize() {
        return cache_.getSize();
    }

    /**
     * Sets the starting value of the generator
     *
     * @param startingValue Starting value of the generator
     */
    public void setStartingValue(long startingValue) {
        this.startingValue_ = startingValue;
    }

    /**
     * Stores the setting fo the cache
     *
     * @param setting
     */
    public void setCacheSetting(DefaultSetting setting) {
        this.cacheSetting_ = setting;
    }

    /**
     * Creates a set of Pids based on desired values.
     *
     * @param setting The settings the Pids are based off of
     * @param amount The amount of Pids to create
     * @return The set of Pids created based on the passed parameters
     */
    private Set<Pid> createSet(DefaultSetting setting, long amount) {
        Set<Pid> set2;
        generator_ = getGenerator(setting);

        if (!setting.isRandom() && setting.equals(cacheSetting_)) {
            set2 = generator_.sequentialMint(amount, startingValue_);
            startingValue_ += amount;
        }
        else if (setting.isRandom()) {
            set2 = generator_.randomMint(amount);
        }
        else {
            set2 = generator_.sequentialMint(amount);
        }
        return set2;
    }

    /**
     * Checks to see if a Pid already exists in the database.
     *
     * @param pid Pid to be checked
     * @return Returns true if a Pid with the same doesn't exist, false
     * otherwise
     */
    private boolean isValidPid(Pid pid) {
        LOGGER.info("in isValidId");
        Pid entity = this.pidRepo_.findOne(pid.getName());
        return entity == null;
    }

    /**
     * Continuously increments a set of ids until the set is completely filled
     * with unique ids.
     *
     * @param set the set of ids
     * @param order determines whether or not the ids will be ordered
     * @param isAuto determines whether or not the ids are AutoId or CustomId
     * @param amount the amount of ids to be created.
     * @return A set of unique ids database.
     */
    private Set<Pid> rollPidSet(Set<Pid> set, long totalPermutations) {
        LOGGER.info("in rollIdSet");
        // Declares and initializes a list that holds unique values.          
        Set<Pid> uniqueList = new LinkedHashSet<>();

        // iterate through every id 
        for (Pid currentId : set) {
            // counts the number of times an id has been rejected
            long counter = 0;

            // continuously increments invalid or non-unique ids
            while (!isValidPid(currentId) || uniqueList.contains(currentId)) {
                // return a partially complete unique set if needed
                if (counter > totalPermutations) {
                    return uniqueList;
                }
                generator_.incrementPid(currentId);
                counter++;
            }

            // a unique Pid has been found, add it to the list
            uniqueList.add(currentId);
        }
        return uniqueList;
    }

    /**
     * Combines two sets by continuously incrementing non-unique pids in set2
     * until set1 is completely filled with unique ids. If the all possile Pids
     * have been iterated through then the method returns.
     *
     * @param set1 the set of pids to add into
     * @param set2 the set of pids to add from
     * @param order determines whether or not the ids will be ordered
     * @param isAuto determines whether or not the ids are AutoId or CustomId
     * @param amount the amount of ids to be created.
     * @return A set of unique ids database.
     */
    private void combinePidSet(Set<Pid> set1, Set<Pid> set2, long totalPermutations) {
        boolean flag = true;
        Iterator<Pid> iter = set2.iterator();
        // iterate through every id 
        while (iter.hasNext() && flag) {
            Pid pid = iter.next();

            // counts the number of times an id has been rejected
            long counter = 0;

            // continuously increments invalid or non-unique ids
            while (!isValidPid(pid) || !set1.add(pid)) {

                if (counter > totalPermutations) {
                    return;
                }
                else {
                    // all possible permutations have not been checked, increment
                    generator_.incrementPid(pid);
                    counter++;
                }

            }
        }
    }

    /**
     * Creates and returns a generator based on the settings passed to it.
     *
     * @param setting The settings the Pids are based off of
     * @return The appropriate generator type based on the settings
     */
    private IdGenerator getGenerator(DefaultSetting setting) {
        LOGGER.info("in createGenerator");
        if (setting.isAuto()) {
            LOGGER.info("AutoGenerator created");
            return new AutoIdGenerator(
                    setting.getPrefix(),
                    setting.getTokenType(),
                    setting.getRootLength());
        }
        else {
            LOGGER.info("CustomIdGenerator created");
            return new CustomIdGenerator(
                    setting.getPrefix(),
                    setting.isSansVowels(),
                    setting.getCharMap());
        }
    }
}
