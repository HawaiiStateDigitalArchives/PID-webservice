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

import com.hida.repositories.DefaultSettingRepository;
import com.hida.repositories.PidRepository;
import com.hida.repositories.UsedSettingRepository;
import com.hida.model.AutoIdGenerator;
import com.hida.model.CustomIdGenerator;
import com.hida.model.DefaultSetting;
import com.hida.model.Pid;
import com.hida.model.IdGenerator;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.UsedSetting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service class that is used as a medium between the requests received by the
 * controller and the transactions done by Hibernate.
 *
 * @author lruffin
 */
@Service("minterService")
@Transactional
public class MinterService {

    /**
     * Logger; logfile to be stored in resource folder
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MinterService.class);

    /**
     * Default setting values stored in resources folder
     */
    @Value("${defaultSetting.path}")
    private String defaultSettingPath_ = "DefaultSetting.properties";

    @Autowired
    private PidRepository pidRepo_;

    @Autowired
    private UsedSettingRepository usedSettingRepo_;

    @Autowired
    private DefaultSettingRepository defaultSettingRepo_;

    @Autowired
    private PropertiesLoaderService propertiesService_;

    private ArrayList<Pid> cachedPid_;

    private long lastSequentialAmount_;

    /**
     * Declares a Generator object to manage
     */
    private IdGenerator generator_;

    /**
     * The setting used to store the values of the current request
     */
    private DefaultSetting currentSetting_;

    /**
     * The default values that are currently stored in the properties file and
     * in the database.
     */
    private DefaultSetting storedSetting_;

    /**
     * No-arg constructor
     */
    public MinterService() {

    }

    /**
     * Returns the difference between the total permutations and the amount of
     * Pids that were already created using the requested settings.
     *
     * @return The amount of permutations remaining
     */
    private long getRemainingPermutations() {
        LOGGER.info("in getRemainingPerumtations");
        long totalPermutations = generator_.getMaxPermutation();
        long amountCreated = getAmountCreated();

        return totalPermutations - amountCreated;
    }

    /**
     * Returns the amount of Pids that were created using the requested settings
     *
     * @return amount of Pids
     */
    private long getAmountCreated() {
        UsedSetting entity = findUsedSetting();
        if (entity == null) {
            return 0;
        }
        else {
            return entity.getAmount();
        }
    }

    /**
     * Creates a generator to be used in accordance to the setting
     */
    private void createGenerator() {
        LOGGER.info("in createGenerator");
        if (currentSetting_.isAuto()) {
            generator_ = new AutoIdGenerator(
                    currentSetting_.getPrefix(),
                    currentSetting_.getTokenType(),
                    currentSetting_.getRootLength());

            LOGGER.info("AutoGenerator created");
        }
        else {
            generator_ = new CustomIdGenerator(
                    currentSetting_.getPrefix(),
                    currentSetting_.isSansVowels(),
                    currentSetting_.getCharMap());
            LOGGER.info("CustomIdGenerator created");
        }
    }

    /**
     * Attempts to create a number of Pids and store them in database
     *
     * @param amount The number of PIDs to be created
     * @param setting The desired setting used to create a Pid
     * @return
     * @throws IOException Thrown when the file cannot be found
     */
    public Set<Pid> mint(long amount, DefaultSetting setting) throws IOException {
        LOGGER.info("in mint");

        // store the desired setting values 
        this.currentSetting_ = setting;

        // create appropriate generator
        createGenerator();

        // calculate total number of permutations
        long total = generator_.getMaxPermutation();

        // determine remaining amount of permutations
        long remaining = getRemainingPermutations();

        // determine if its possible to create the requested amount of ids
        if (remaining < amount) {
            NotEnoughPermutationsException exception
                    = new NotEnoughPermutationsException(remaining, amount);
            LOGGER.error("Not enough remaining permutations", exception);
            throw exception;
        }
        LOGGER.info("request is valid");

        /* 
         if the current setting is random, have the generator return a random set,
         otherwise, have the generator return a sequential set
         */
        Set<Pid> set;
        if (currentSetting_.isRandom()) {
            set = generator_.randomMint(amount);
        }
        else if (currentSetting_.equals(storedSetting_)) {
            set = generator_.sequentialMint(amount, lastSequentialAmount_);
            lastSequentialAmount_ = (lastSequentialAmount_ + amount) % total;
        }
        else {
            set = generator_.sequentialMint(amount);
        }

        // check ids and increment them appropriately
        set = rollPidSet(set, total, amount);

        // add the set of ids to the id table in the database and their formats
        addPidSet(set, amount);

        // return the set of ids
        return set;
    }

    /**
     * Generates a list of Pids based on default settings.
     *
     * @throws IOException Thrown when the DEFAULT_SETTING_PATH cannot be found
     */
    public void generateCache() throws IOException {
        LOGGER.trace("in generateCache");

        // get default settings
        currentSetting_ = this.getStoredSetting();

        // create the generator
        createGenerator();

        // get the maximum number of permutations 
        long maxPermutation = generator_.getMaxPermutation();

        // create all possible permutations
        Set<Pid> cache = generator_.sequentialMint(500);

        // add each mmember of the set to CachedPid
        ArrayList<Pid> list = new ArrayList<>();

        Iterator<Pid> iter = cache.iterator();
        while (iter.hasNext()) {
            Pid pid = iter.next();
            list.add(pid);
            LOGGER.info("adding {}", pid);
        }

        cachedPid_ = list;
        LOGGER.trace("cache generated");
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
    private Set<Pid> rollPidSet(Set<Pid> set, long totalPermutations, long amount) {
        LOGGER.info("in rollIdSet");
        // Used to count the number of unique ids. Size methods aren't used because int is returned
        long uniqueIdCounter = 0;

        // Declares and initializes a list that holds unique values.          
        Set<Pid> uniqueList = new LinkedHashSet<>();

        // iterate through every id 
        for (Pid currentId : set) {
            // counts the number of times an id has been rejected
            long counter = 0;

            // continuously increments invalid or non-unique ids
            while (!isValidPid(currentId) || uniqueList.contains(currentId)) {
                /* 
                 if counter exceeds totalPermutations, then id has iterated through every 
                 possible permutation. Related format is updated as a quick look-up reference
                 with the number of ids that were inadvertedly been created using other formats.
                 NotEnoughPermutationsException is thrown stating remaining number of ids.
                 */
                if (counter > totalPermutations) {
                    NotEnoughPermutationsException exception
                            = new NotEnoughPermutationsException(uniqueIdCounter, amount);
                    LOGGER.error("Not enough remaining Permutations {} ", exception);
                    throw exception;
                }
                generator_.incrementPid(currentId);
                counter++;
            }
            // unique ids are added to list and uniqueIdCounter is incremented.
            // Size methods aren't used because int is returned
            uniqueIdCounter++;
            uniqueList.add(currentId);
        }
        return uniqueList;
    }

    /**
     * Adds a requested amount of formatted ids to the database.
     *
     * @param list list of ids to check.
     * @param amountCreated Holds the true size of the list as list.size method
     * can only return the maximum possible value of an integer.
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     */
    private void addPidSet(Set<Pid> list, long amountCreated) {
        LOGGER.info("in addIdlIst");

        for (Pid pid : list) {
            pidRepo_.save(pid);
            pid.setName(currentSetting_.getPrepend() + pid.getName());
        }

        LOGGER.info("DatabaseUpdated with new pids");
        // update table format
        recordSettings(amountCreated);

    }

    /**
     * Attempts to find a UsedSetting based on the currently used DefaultSetting
     *
     * @return Returns a UsedSetting entity if found, null otherwise
     */
    private UsedSetting findUsedSetting() {
        LOGGER.info("in findUsedSetting");

        return usedSettingRepo_.findUsedSetting(currentSetting_.getPrefix(),
                currentSetting_.getTokenType(),
                currentSetting_.getCharMap(),
                currentSetting_.getRootLength(),
                currentSetting_.isSansVowels());
    }

    /**
     * Attempts to record the setting that were used to create the current set
     * of Pids
     *
     * @param amount The number of PIDs that were created
     */
    private void recordSettings(long amount) {
        LOGGER.info("in recordSettings");

        UsedSetting entity = findUsedSetting();

        if (entity == null) {
            entity = new UsedSetting(currentSetting_.getPrefix(),
                    currentSetting_.getTokenType(),
                    currentSetting_.getCharMap(),
                    currentSetting_.getRootLength(),
                    currentSetting_.isSansVowels(),
                    amount);

            usedSettingRepo_.save(entity);
        }
        else {
            long previousAmount = entity.getAmount();
            entity.setAmount(previousAmount + amount);
        }
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
     * Updates the CurrentDefaultSetting to match the values in the given
     * DefaultSetting.
     *
     * @param newSetting A DefaultSetting object that contains newly requested
     * values
     * @throws IOException
     */
    public void updateCurrentSetting(DefaultSetting newSetting) throws Exception {
        LOGGER.info("in updateCurrentSetting");

        currentSetting_ = defaultSettingRepo_.findCurrentDefaultSetting();
        currentSetting_.setPrepend(newSetting.getPrepend());
        currentSetting_.setPrefix(newSetting.getPrefix());
        currentSetting_.setCharMap(newSetting.getCharMap());
        currentSetting_.setRootLength(newSetting.getRootLength());
        currentSetting_.setTokenType(newSetting.getTokenType());
        currentSetting_.setAuto(newSetting.isAuto());
        currentSetting_.setRandom(newSetting.isRandom());
        currentSetting_.setSansVowels(newSetting.isSansVowels());

        // record Default Setting values into properties file
        propertiesService_.writeToPropertiesFile(defaultSettingPath_, newSetting);
    }

    /**
     * Initializes the storedSetting_ field by reading its value in the
     * database. If its null, then it is given initial values.
     *
     * @throws IOException Thrown when the file cannot be found
     */
    public void initializeStoredSetting() throws IOException {
        storedSetting_ = defaultSettingRepo_.findCurrentDefaultSetting();
        if (storedSetting_ == null) {
            // read default values stored in properties file and save it
            storedSetting_ = propertiesService_.readPropertiesFile(defaultSettingPath_);
            defaultSettingRepo_.save(storedSetting_);
        }
    }

    public DefaultSetting getStoredSetting() {

        return storedSetting_;
    }

    public long getLastSequentialAmount() {
        return lastSequentialAmount_;
    }

    public String getDefaultSettingPath() {
        return defaultSettingPath_;
    }

    public void setDefaultSettingPath(String DefaultSettingPath) {
        this.defaultSettingPath_ = DefaultSettingPath;
    }
}
