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
import com.hida.model.Token;
import com.hida.repositories.UsedSettingRepository;
import com.hida.model.DefaultSetting;
import com.hida.model.Pid;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.UsedSetting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private String defaultSettingPath_ = "DefaultSetting.properties";
    
    @Autowired
    private UsedSettingRepository usedSettingRepo_;

    @Autowired
    private DefaultSettingRepository defaultSettingRepo_;

    @Autowired
    private GeneratorService repoService_;

    private long lastSequentialAmount_;    

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
     * Attempts to create a number of Pids and store them in database
     *
     * @param amount The number of PIDs to be created
     * @param setting The desired setting used to create a Pid
     * @return
     * @throws IOException Thrown when the file cannot be found
     */
    public Set<Pid> mint(long amount, DefaultSetting setting) throws IOException {
        LOGGER.info("in mint");
        
        // generate pids at a recorded starting value whenever possible
        Set<Pid> set;
        if(setting.equals(storedSetting_) && !setting.isRandom()){
            set = repoService_.generatePids(setting, amount, lastSequentialAmount_);
        }else{
            set = repoService_.generatePids(setting, amount);
        }

        if (set.size() != amount) {
            LOGGER.error("Not enough remaining Permutations, "
                    + "Requested Amount=" + amount + " --> "
                    + "Amount Remaining=" + set.size());
            throw new NotEnoughPermutationsException(set.size(), amount);
        }
        else {
            // persist the set of Pids
            persistPids(setting, set, amount);

            // return the set of ids
            return set;
        }

    }

    /**
     * Generates a list of Pids based on default settings.
     *
     * @throws IOException Thrown when the DEFAULT_SETTING_PATH cannot be found
     */
    public void generateCache() throws IOException {
        LOGGER.trace("in generateCache");
        repoService_.generateCache(storedSetting_);
        LOGGER.trace("cache generated");
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
        writeToPropertiesFile(defaultSettingPath_, newSetting);
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
            storedSetting_ = readPropertiesFile(defaultSettingPath_);
            defaultSettingRepo_.save(storedSetting_);
        }
    }
    
    /**
     * Persists a set of Pids in the database. Also adds the Prepend to the Pids
     *
     * @param setting The settings the Pids are based off of
     * @param set A set of Pids
     * @param amountCreated Amount of pids that were created
     */
    public void persistPids(DefaultSetting setting, Set<Pid> set, long amountCreated) {
        LOGGER.info("in persistPids");

        for (Pid pid : set) {
            repoService_.savePid(pid);
            pid.setName(setting.getPrepend() + pid.getName());
        }

        LOGGER.info("DatabaseUpdated with new pids");

        // update table format
        recordSettings(setting, amountCreated);
    }

    public DefaultSetting getStoredSetting() {
        return storedSetting_;
    }
    
    /**
     * Returns the amount of Pids that were created using the settings
     *
     * @param setting The settings the Pids are based off of
     * @return The amount of permutations remaining
     */
    public long getCurrentAmount(DefaultSetting setting) {
        UsedSetting entity = this.findUsedSetting(setting);

        return entity.getAmount();
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
    
    /**
     * Attempts to record the setting that were used to create the current set
     * of Pids
     *
     * @param amount The number of PIDs that were created
     */
    private void recordSettings(DefaultSetting setting, long amount) {
        LOGGER.info("in recordSettings");

        UsedSetting entity = findUsedSetting(setting);

        if (entity == null) {
            entity = new UsedSetting(setting.getPrefix(),
                    setting.getTokenType(),
                    setting.getCharMap(),
                    setting.getRootLength(),
                    setting.isSansVowels(),
                    amount);

            usedSettingRepo_.save(entity);
        }
        else {
            long previousAmount = entity.getAmount();
            entity.setAmount(previousAmount + amount);
        }
    }
    
    /**
     * Attempts to find a UsedSetting based on the currently used DefaultSetting
     *
     * @return Returns a UsedSetting entity if found, null otherwise
     */
    private UsedSetting findUsedSetting(DefaultSetting setting) {
        LOGGER.info("in findUsedSetting");

        return usedSettingRepo_.findUsedSetting(setting.getPrefix(),
                setting.getTokenType(),
                setting.getCharMap(),
                setting.getRootLength(),
                setting.isSansVowels());
    }

    /**
     * Read a given properties file and return its values in the form of a
     * DefaultSetting object
     *
     * @return DefaultSetting object with read values
     * @throws IOException Thrown when the file cannot be found
     */
    private DefaultSetting readPropertiesFile(String filename) throws IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream(filename);

        DefaultSetting setting = new DefaultSetting();

        // load a properties file
        prop.load(input);

        // get the property value, store it, and return it
        setting.setPrepend(prop.getProperty("prepend"));
        setting.setPrefix(prop.getProperty("prefix"));
        setting.setCacheSize(Long.parseLong(prop.getProperty("cacheSize")));
        setting.setCharMap(prop.getProperty("charMap"));
        setting.setTokenType(Token.valueOf(prop.getProperty("tokenType")));
        setting.setRootLength(Integer.parseInt(prop.getProperty("rootLength")));
        setting.setSansVowels(Boolean.parseBoolean(prop.getProperty("sansVowel")));
        setting.setAuto(Boolean.parseBoolean(prop.getProperty("auto")));
        setting.setRandom(Boolean.parseBoolean(prop.getProperty("random")));

        // close and return
        input.close();
        return setting;
    }

    /**
     * Writes to a given properties file, updating its key-value pairs using the
     * values stored in the setting parameter. If the file does not exist it is
     * created.
     *
     * @param setting The setting whose value needs to be stored
     * @throws Exception
     */
    private void writeToPropertiesFile(String filename, DefaultSetting setting)
            throws Exception {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(filename);
        File file = new File(url.toURI());
        OutputStream output = new FileOutputStream(file);

        // set the properties value
        prop.setProperty("prepend", setting.getPrepend());
        prop.setProperty("prefix", setting.getPrefix());
        prop.setProperty("cacheSize", setting.getCacheSize() + "");
        prop.setProperty("charMap", setting.getCharMap());
        prop.setProperty("rootLength", setting.getRootLength() + "");
        prop.setProperty("tokenType", setting.getTokenType() + "");
        prop.setProperty("sansVowel", setting.isSansVowels() + "");
        prop.setProperty("auto", setting.isAuto() + "");
        prop.setProperty("random", setting.isRandom() + "");

        // save and close
        prop.store(output, "");
        output.close();
    }   
}
