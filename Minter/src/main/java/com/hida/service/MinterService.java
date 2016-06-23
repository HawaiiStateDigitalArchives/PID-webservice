package com.hida.service;

import com.hida.repositories.DefaultSettingRepository;
import com.hida.repositories.PidRepository;
import com.hida.model.Token;
import com.hida.repositories.UsedSettingRepository;
import com.hida.model.AutoIdGenerator;
import com.hida.model.CustomIdGenerator;
import com.hida.model.DefaultSetting;
import com.hida.model.Pid;
import com.hida.model.IdGenerator;
import com.hida.model.NotEnoughPermutationsException;
import com.hida.model.UsedSetting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.LinkedHashSet;
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
    private String DefaultSettingPath = "DefaultSetting.properties";

    @Autowired
    private PidRepository PidRepo;

    @Autowired
    private UsedSettingRepository UsedSettingRepo;

    @Autowired
    private DefaultSettingRepository DefaultSettingRepo;

    private ArrayList<Pid> CachedPid;

    private long LastSequentialAmount;

    /**
     * Declares a Generator object to manage
     */
    private IdGenerator Generator;

    /**
     * The setting used to store the values of the current request
     */
    private DefaultSetting CurrentSetting;

    /**
     * The default values that are currently stored in the properties file and
     * in the database.
     */
    private DefaultSetting StoredSetting;

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
        long totalPermutations = Generator.getMaxPermutation();
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
        if (CurrentSetting.isAuto()) {
            Generator = new AutoIdGenerator(
                    CurrentSetting.getPrefix(),
                    CurrentSetting.getTokenType(),
                    CurrentSetting.getRootLength());

            LOGGER.info("AutoGenerator created");
        }
        else {
            Generator = new CustomIdGenerator(
                    CurrentSetting.getPrefix(),
                    CurrentSetting.isSansVowels(),
                    CurrentSetting.getCharMap());
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
        this.CurrentSetting = setting;        

        // create appropriate generator
        createGenerator();

        // calculate total number of permutations
        long total = Generator.getMaxPermutation();

        // determine remaining amount of permutations
        long remaining = getRemainingPermutations();

        // determine if its possible to create the requested amount of ids
        if (remaining < amount) {
            LOGGER.error("Not enough remaining Permutations, "
                    + "Requested Amount=" + amount + " --> "
                    + "Amount Remaining=" + remaining);
            throw new NotEnoughPermutationsException(remaining, amount);
        }
        LOGGER.info("request is valid");

        /* 
         if the current setting is random, have the generator return a random set,
         otherwise, have the generator return a sequential set
         */
        Set<Pid> set;
        if (CurrentSetting.isRandom()) {
            set = Generator.randomMint(amount);
        }
        else if (CurrentSetting.equals(StoredSetting)) {
            set = Generator.sequentialMint(amount, LastSequentialAmount);
            LastSequentialAmount = (LastSequentialAmount + amount) % total;
        }
        else {
            set = Generator.sequentialMint(amount);
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
        CurrentSetting = this.getStoredSetting();

        // create the generator
        createGenerator();

        // get the maximum number of permutations 
        long maxPermutation = Generator.getMaxPermutation();

        // create all possible permutations
        Set<Pid> cache = Generator.sequentialMint(500);

        // add each mmember of the set to CachedPid
        ArrayList<Pid> list = new ArrayList<>();

        Iterator<Pid> iter = cache.iterator();
        while (iter.hasNext()) {
            Pid pid = iter.next();
            list.add(pid);
            LOGGER.info("adding " + pid);
        }

        CachedPid = list;
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
                    LOGGER.error("Total number of Permutations Exceeded: Total Permutation Count="
                            + totalPermutations);
                    throw new NotEnoughPermutationsException(uniqueIdCounter, amount);
                }
                Generator.incrementPid(currentId);
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
            PidRepo.save(pid);
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

        return UsedSettingRepo.findUsedSetting(CurrentSetting.getPrefix(),
                CurrentSetting.getTokenType(),
                CurrentSetting.getCharMap(),
                CurrentSetting.getRootLength(),
                CurrentSetting.isSansVowels());
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
            entity = new UsedSetting(CurrentSetting.getPrefix(),
                    CurrentSetting.getTokenType(),
                    CurrentSetting.getCharMap(),
                    CurrentSetting.getRootLength(),
                    CurrentSetting.isSansVowels(),
                    amount);

            UsedSettingRepo.save(entity);
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
        Pid entity = this.PidRepo.findOne(pid.getName());
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

        CurrentSetting = DefaultSettingRepo.findCurrentDefaultSetting();
        CurrentSetting.setPrepend(newSetting.getPrepend());
        CurrentSetting.setPrefix(newSetting.getPrefix());
        CurrentSetting.setCharMap(newSetting.getCharMap());
        CurrentSetting.setRootLength(newSetting.getRootLength());
        CurrentSetting.setTokenType(newSetting.getTokenType());
        CurrentSetting.setAuto(newSetting.isAuto());
        CurrentSetting.setRandom(newSetting.isRandom());
        CurrentSetting.setSansVowels(newSetting.isSansVowels());

        // record Default Setting values into properties file
        writeToPropertiesFile(DefaultSettingPath, newSetting);
    }
        
    /**
     * Initializes the StoredSetting field by reading its value in the database.
     * If its null, then it is given initial values.
     *
     * @throws IOException Thrown when the file cannot be found
     */
    public void initializeStoredSetting() throws IOException {
        StoredSetting = DefaultSettingRepo.findCurrentDefaultSetting();
        if (StoredSetting == null) {
            // read default values stored in properties file and save it
            StoredSetting = readPropertiesFile(DefaultSettingPath);
            DefaultSettingRepo.save(StoredSetting);
        }
    }
    
    public DefaultSetting getStoredSetting() {        
        return StoredSetting;
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
     * Writes to a given properties file, updating its key-value pairs using
     * the values stored in the setting parameter. If the file does not exist it
     * is created.
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

    public long getLastSequentialAmount() {
        return LastSequentialAmount;
    }

    public String getDefaultSettingPath() {
        return DefaultSettingPath;
    }

    public void setDefaultSettingPath(String DefaultSettingPath) {
        this.DefaultSettingPath = DefaultSettingPath;
    }        
}
