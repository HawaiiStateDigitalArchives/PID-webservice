package com.hida.service;

import com.hida.model.BadParameterException;
import com.hida.model.TokenType;
import com.hida.dao.PidDaoImpl;
import com.hida.model.AutoIdGenerator;
import com.hida.model.CustomIdGenerator;
import com.hida.model.Pid;
import com.hida.model.IdGenerator;
import com.hida.model.NotEnoughPermutationsException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import java.util.Iterator;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

/**
 * A class used to manage http requests so that data integrity can be maintained
 *
 * @author lruffin
 */
@Repository("pidDao")
@Transactional
public class MinterServiceImpl implements MinterService{

// Logger; logfile to be stored in resource folder
    private static final Logger Logger = LoggerFactory.getLogger(MinterServiceImpl.class);

// names of the columns used in the tables
    private final String ID_COLUMN = "ID";
    private final String PREFIX_COLUMN = "PREFIX";
    private final String AMOUNT_CREATED_COLUMN = "AMOUNT_CREATED";
    private final String SANS_VOWEL_COLUMN = "SANS_VOWEL";
    private final String TOKEN_TYPE_COLUMN = "TOKEN_TYPE";
    private final String ROOT_LENGTH_COLUMN = "ROOT_LENGTH";
    private final String PREPEND_COLUMN = "PREPEND";
    private final String AUTO_COLUMN = "IS_AUTO";
    private final String RANDOM_COLUMN = "IS_RANDOM";
    private final String CHAR_MAP_COLUMN = "CHAR_MAP";

    // names of the tables in the database
    private final String ID_TABLE = "MINTED_IDS";
    private final String FORMAT_TABLE = "FORMAT";
    private final String SETTINGS_TABLE = "SETTINGS";

    /**
     * A connection to a database
     */
    private Connection DatabaseConnection;

    /**
     * The JDBC driver which also specifies the location of the database. If the
     * database does not exist then it is created at the specified location
     * <p />
     * ex: "jdbc:sqlite:[path].db"
     */
    private final String DRIVER;

    /**
     * Set to false by default, the flag is raised (set to true) when the table
     * was created. This is used to prevent from querying for the tables
     * existence after each request.
     */
    private boolean isTableCreatedFlag = false;

    // holds information about the database. 
    private final String DatabasePath;
    private final String DatabaseName;

    /**
     * missing javadoc
     */
    @Autowired
    private PidDaoImpl PidDao;

    /**
     * Declares a Generator object to manage
     */
    private IdGenerator Generator;

    /**
     * Used to explicitly define the name and path of database
     *
     * @param DatabasePath path of the database
     * @param DatabaseName name of the database
     */
    public MinterServiceImpl(String DatabasePath, String DatabaseName) {
        this.DatabasePath = DatabasePath;
        this.DatabaseName = DatabaseName;

        this.DRIVER = "jdbc:sqlite:" + DatabasePath + DatabaseName;
    }

    /**
     * Used by default, this method will create a database named PID.db wherever
     * the default location server is located.
     */
    public MinterServiceImpl() {
        this.DatabasePath = "";
        this.DatabaseName = "PID.db";
        this.DRIVER = "jdbc:sqlite:" + DatabaseName;
    }

    /**
     * Attempts to connect to the database.
     *
     * Upon connecting to the database, the method will try to detect whether or
     * not a table exists. A table is created if it does not already exists.
     *
     * @return true if connection and table creation was successful, false
     * otherwise
     * @throws ClassNotFoundException thrown whenever the JDBC driver is not
     * found
     * @throws SQLException thrown whenever there is an error with the database
     */
    @Override
    public boolean createConnection() throws ClassNotFoundException, SQLException {
        System.out.println("connection created: " + DRIVER);

        // connect to database
        Class.forName("org.sqlite.JDBC");
        DatabaseConnection = DriverManager.getConnection(DRIVER);
        //Logger.info("Database Connection Created Using: org.sqlite.JDBC");
        // allow the database connection to use regular expressions
        
        if (this.isTableCreatedFlag) {
            return true;
        } else {

            System.out.println("creating table");
            //Logger.info("Database Created created using Regular Expressions from \"REGEX\"");
            if (!tableExists(ID_TABLE)) {
                Logger.warn("Database Not Setup, Creating Tables");

                // string to hold a query that sets up table in SQLite3 syntax
                String createIdTable = String.format("CREATE TABLE %s "
                        + "(%s PRIMARY KEY NOT NULL);", ID_TABLE, ID_COLUMN);

                Logger.info("Table Created with Query: " + createIdTable);

                // a database statement that allows database/webservice communication 
                //Logger.info("Created BUS from Database to WebService");
                Statement databaseStatement = DatabaseConnection.createStatement();
                databaseStatement.executeUpdate(createIdTable);
                databaseStatement.close();

                Logger.info("Creating Table: " + ID_TABLE + " with Column Name: " + ID_COLUMN);
            }
            if (!tableExists(FORMAT_TABLE)) {

                Logger.info("Creating Table: " + FORMAT_TABLE);
                String createFormatTable = String.format("CREATE TABLE %s "
                        + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "%s TEXT NOT NULL, "
                        + "%s BOOLEAN NOT NULL, "
                        + "%s TEXT NOT NULL, "
                        + "%s INT NOT NULL, "
                        + "%s UNSIGNED BIG INT NOT NULL);",
                        FORMAT_TABLE, ID_COLUMN, PREFIX_COLUMN, SANS_VOWEL_COLUMN,
                        TOKEN_TYPE_COLUMN, ROOT_LENGTH_COLUMN, AMOUNT_CREATED_COLUMN);
                Logger.info("Format Table created with: " + createFormatTable);

                //Logger.info("Cleaning up Connection and Table");
                Statement databaseStatement = DatabaseConnection.createStatement();
                databaseStatement.executeUpdate(createFormatTable);
                databaseStatement.close();
            }

            if (!tableExists(SETTINGS_TABLE)) {
                Logger.info("Creating Table: " + SETTINGS_TABLE + 
                        " with Column Name: " + ID_COLUMN);
                String createSettingsTable = String.format("CREATE TABLE %s "
                        + "(%s TEXT, "
                        + "%s TEXT, "
                        + "%s TEXT, "
                        + "%s INT, "
                        + "%s TEXT, "
                        + "%s BOOLEAN NOT NULL, "
                        + "%s BOOLEAN NOT NULL, "
                        + "%s BOOLEAN NOT NULL);",
                        SETTINGS_TABLE, PREPEND_COLUMN, PREFIX_COLUMN, TOKEN_TYPE_COLUMN,
                        ROOT_LENGTH_COLUMN, CHAR_MAP_COLUMN, AUTO_COLUMN, RANDOM_COLUMN,
                        SANS_VOWEL_COLUMN);
                Logger.info("Format Table created with: " + createSettingsTable);

                //Logger.info("Cleaning up Connection and Table");
                Statement databaseStatement = DatabaseConnection.createStatement();
                databaseStatement.executeUpdate(createSettingsTable);
                databaseStatement.close();

                // populate the settings table with default settings
                assignDefaultSettings();
            }

            this.isTableCreatedFlag = true;

            return true;
        }
    }

    /**
     * missing javadoc
     *          
     * @param prefix
     * @param sansVowel
     * @param tokenType
     * @param rootLength
     * @throws BadParameterException
     */
    @Override
    public void createAutoMinter(String prefix, boolean sansVowel,
            TokenType tokenType, int rootLength) throws BadParameterException {
        Generator = new AutoIdGenerator(prefix, sansVowel, tokenType, rootLength);
    }

    /**
     * missing javadoc
     *     
     * @param prefix
     * @param sansVowel
     * @param charMap
     * @throws BadParameterException
     */
    @Override
    public void createCustomMinter(String prefix, boolean sansVowel,
            String charMap) throws BadParameterException {
        Generator = new CustomIdGenerator(prefix, sansVowel, charMap);
    }

    /**
     * Used by AutoMinters to determine the number of permutations are
     * available.
     *
     * Each id will be matched against a CachedFormat, specified by this
     * method's parameter. If a matching CachedFormat does not exist, then a
     * CachedFormat will be created for it and the database will be searched to
     * see how many other ids exist in the similar formats.
     *
     * Once a format exists the database will calculate the remaining number of
     * permutations that can be created using the given parameters
     * @param minter
     * @return
     * @throws SQLException
     * @throws BadParameterException
     */
    private long getRemainingPermutations(AutoIdGenerator minter) throws SQLException,
            BadParameterException {
        //Logger.info("in Permutations 1");
        // calculate the total number of possible permuations        
        long totalPermutations = minter.calculatePermutations();
        long amountCreated = getAmountCreated(minter.getPrefix(), minter.getTokenType(),
                minter.isSansVowel(), minter.getRootLength());

        // format wasn't found
        if (amountCreated == -1) {
            Logger.warn("Format Doesn't Exist");
            this.createFormat(minter.getPrefix(), minter.getTokenType(), minter.isSansVowel(),
                    minter.getRootLength());
            return totalPermutations;
        } else {
            // format was found
            Logger.info("format Exists");
            return totalPermutations - amountCreated;
        }
    }

    /**
     * Used by CustomMinters to determine the number of permutations are
     * available.
     *
     * Each id will be matched against a CachedFormat, specified by this
     * method's parameter. If a matching CachedFormat does not exist, then a
     * CachedFormat will be created for it and the database will be searched to
     * see how many other ids exist in the similar formats.
     *
     * Once a format exists the database will calculate the remaining number of
     * permutations that can be created using the given parameters
     * 
     * @param minter
     * @return
     * @throws SQLException
     * @throws BadParameterException
     */
    private long getRemainingPermutations(CustomIdGenerator minter) throws SQLException,
            BadParameterException {
        // calculate the total number of possible permuations
        int charMapLength = minter.getCharMap().length();
        TokenType token = convertToToken(minter.getCharMap());
        long totalPermutations = minter.calculatePermutations();
        long amountCreated = getAmountCreated(minter.getPrefix(), token,
                minter.isSansVowel(), charMapLength);

        // format wasn't found
        if (amountCreated == -1) {
            Logger.warn("Format Doesn;t Exist");
            this.createFormat(minter.getPrefix(), token, minter.isSansVowel(), charMapLength);
            return totalPermutations;
        } else {
            // format was found
            Logger.info("format exists");
            return totalPermutations - amountCreated;
        }
    }

    /**
     * missing javadoc
     *
     * @param total
     * @param amountCreated
     * @return
     */
    private boolean isValidRequest(long remaining, long amount) {
        // throw an error if its impossible to generate ids 
        if (remaining < amount) {
            Logger.error("Not enough remaining Permutations, "
                    + "Requested Amount=" + amount + " --> "
                    + "Amount Remaining=" + remaining);
            throw new NotEnoughPermutationsException(remaining, amount);

        }
        return remaining < amount;
    }

    /**
     * missing javadoc
     *
     * @param amount
     * @param isRandom
     * @return
     * @throws SQLException
     * @throws BadParameterException
     */
    @Override
    public Set<Pid> mint(long amount, boolean isRandom) throws SQLException, BadParameterException {
        // calculate total number of permutations
        long total = Generator.calculatePermutations();

        // determine remaining amount of permutations
        long remaining;
        TokenType token;
        int rootLength;
        if (Generator instanceof AutoIdGenerator) {
            remaining = getRemainingPermutations((AutoIdGenerator) Generator);
            token = ((AutoIdGenerator) Generator).getTokenType();
            rootLength = ((AutoIdGenerator) Generator).getRootLength();
        } else {
            remaining = getRemainingPermutations((CustomIdGenerator) Generator);
            token = convertToToken(((CustomIdGenerator) Generator).getCharMap());
            rootLength = ((CustomIdGenerator) Generator).getCharMap().length();
        }

        // determine if its possible to create the requested amount of ids
        if (isValidRequest(remaining, amount)) {
            Logger.error("Not enough remaining Permutations, "
                    + "Requested Amount=" + amount + " --> "
                    + "Amount Remaining=" + remaining);
            throw new NotEnoughPermutationsException(remaining, amount);
        }

        // have Generator return a set of ids and check them   
        Set<Pid> set;
        if (isRandom) {
            set = Generator.randomMint(amount);
        } else {
            set = Generator.sequentialMint(amount);
        }

        // check ids and increment them appropriately
        set = rollIdSet(set, total, amount, token, rootLength);

        // add the set of ids to the id table in the database and their formats
        addIdList(set, amount, Generator.getPrefix(), token, Generator.isSansVowel(), rootLength);

        // return the set of ids
        return set;
    }

    /**
     * Continuously increments a set of ids until the set is completely filled
     * with unique ids.
     *
     * @param set the set of ids
     * @param order determines whether or not the ids will be ordered
     * @param isAuto determines whether or not the ids are AutoId or CustomId
     * @param amount the amount of ids to be created.
     * @return A set of unique ids
     * @throws SQLException - thrown whenever there is an error with the
     * database.
     */
    private Set<Pid> rollIdSet(Set<Pid> set, long totalPermutations, long amount,
            TokenType token, int rootLength)
            throws SQLException, NotEnoughPermutationsException, BadParameterException {
        // Used to count the number of unique ids. Size methods aren't used because int is returned
        long uniqueIdCounter = 0;

        // Declares and initializes a list that holds unique values.          
        Set<Pid> uniqueList = new TreeSet<>();

        // iterate through every id 
        for (Pid currentId : set) {
            // counts the number of times an id has been rejected
            long counter = 0;

            // continuously increments invalid or non-unique ids
            while (!isValidId(currentId) || uniqueList.contains(currentId)) {
                /* 
                 if counter exceeds totalPermutations, then id has iterated through every 
                 possible permutation. Related format is updated as a quick look-up reference
                 with the number of ids that were inadvertedly been created using other formats.
                 NotEnoughPermutationsException is thrown stating remaining number of ids.
                 */
                if (counter > totalPermutations) {
                    long amountTaken = totalPermutations - uniqueIdCounter;

                    Logger.error("Total number of Permutations Exceeded: Total Permutation Count=" 
                            + totalPermutations);
                    setAmountCreated(Generator.getPrefix(), token, Generator.isSansVowel(), 
                            rootLength, amountTaken);
                    throw new NotEnoughPermutationsException(uniqueIdCounter, amount);
                }
                currentId.incrementId();
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
     * This method returns an equivalent token for any given charMap
     *
     * @param charMap The mapping used to describe range of possible characters
     * at each of the id's root's digits
     * @return the token equivalent to the charMap
     * @throws BadParameterException thrown whenever a malformed or invalid
     * parameter is passed
     */
    private TokenType convertToToken(String charMap) throws BadParameterException {

        // true if charMap only contains character 'd'
        if (charMap.matches("^[d]+$")) {
            return TokenType.DIGIT;
        } // true if charMap only contains character 'l'.
        else if (charMap.matches("^[l]+$")) {
            return TokenType.LOWERCASE;
        } // true if charMap only contains character 'u'
        else if (charMap.matches("^[u]+$")) {
            return TokenType.UPPERCASE;
        } // true if charMap only contains character groups 'lu' or 'm'
        else if (charMap.matches("(^(?=[lum]*l)(?=[lum]*u)[lum]*$)" + "|"
                + "(^(?=[lum]*m)[lum]*$)")) {
            return TokenType.MIXEDCASE;
        } // true if charMap only contains characters 'dl'
        else if (charMap.matches("(^(?=[dl]*l)(?=[ld]*d)[dl]*$)")) {
            return TokenType.LOWER_EXTENDED;
        } // true if charMap only contains characters 'du'
        else if (charMap.matches("(^(?=[du]*u)(?=[du]*d)[du]*$)")) {
            return TokenType.UPPER_EXTENDED;
        } // true if charMap at least contains character groups 'dlu' or 'md' or 'e' respectively
        else if (charMap.matches("(^(?=[dlume]*d)(?=[dlume]*l)(?=[dlume]*u)[dlume]*$)" + "|"
                + "(^(?=[dlume]*m)(?=[dlume]*d)[dlume]*$)" + "|"
                + "(^(?=[dlume]*e)[dlume]*$)")) {
            return TokenType.MIXED_EXTENDED;
        } else {
            throw new BadParameterException(charMap, "detected in getToken method");
        }
    }

    /**
     * Assigns default values to the settings table. Default values are as
     * follows:
     * <pre>
     * prepend = ""
     * prefix = "xyz"
     * tokenType = "DIGIT"
     * rootLength = 5
     * charMap = "ddlume"
     * auto = true
     * random = true
     * sansVowels = true
     * </pre>
     *
     * @throws SQLException thrown whenever there is an error with the database
     */
    private void assignDefaultSettings() throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        String sqlQuery = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) "
                + "VALUES ('%s', '%s', %d, '%s', %d, %d, %d);",
                SETTINGS_TABLE, PREFIX_COLUMN, TOKEN_TYPE_COLUMN,
                ROOT_LENGTH_COLUMN, CHAR_MAP_COLUMN, AUTO_COLUMN, RANDOM_COLUMN,
                SANS_VOWEL_COLUMN, "xyz", "MIXED_EXTENDED", 5, "ddlume", 1, 1, 1);

        databaseStatement.executeUpdate(sqlQuery);

        databaseStatement.close();
    }

    /**
     * Updates values given by the parameter to the settings table
     *
     * @param prepend the prepended String to be attached to each id
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param rootLength Designates the length of the id's root.
     * @param isAuto Determines which minter will be used
     * @param isRandom Determines whether the ids will be generated at random or
     * sequentially
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @throws SQLException
     */
    public void assignSettings(String prepend, String prefix, TokenType tokenType, int rootLength,
            boolean isAuto, boolean isRandom, boolean sansVowel) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        int autoFlag = (isAuto) ? 1 : 0;
        int randomFlag = (isRandom) ? 1 : 0;
        int vowelFlag = (sansVowel) ? 1 : 0;
        String sqlQuery = String.format("UPDATE %s SET %2$s = '%9$s', %3$s = '%10$s', "
                + "%4$s = '%11$s', %5$s = %12$s, %6$s = %13$s, %7$s = %14$s, %8$s = %15$s",
                SETTINGS_TABLE, PREPEND_COLUMN, PREFIX_COLUMN, TOKEN_TYPE_COLUMN,
                ROOT_LENGTH_COLUMN, AUTO_COLUMN, RANDOM_COLUMN,
                SANS_VOWEL_COLUMN, prepend, prefix, tokenType, rootLength,
                autoFlag, randomFlag, vowelFlag);

        databaseStatement.executeUpdate(sqlQuery);
        databaseStatement.close();
    }

    /**
     * Updates values given by the parameter to the settings table
     *
     * @param prepend the prepended String to be attached to each id
     * @param prefix The string that will be at the front of every id.
     * @param charMap The mapping used to describe range of possible characters
     * at each of the id's root's digits.
     * @param isAuto Determines which minter will be used
     * @param isRandom Determines whether the ids will be generated at random or
     * sequentially
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @throws SQLException thrown whenever there is an error with the database.
     */
    public void assignSettings(String prepend, String prefix, String charMap, boolean isAuto,
            boolean isRandom, boolean sansVowel) throws SQLException {
        Statement databaseStatement = DatabaseConnection.createStatement();

        int autoFlag = (isAuto) ? 1 : 0;
        int randomFlag = (isRandom) ? 1 : 0;
        int vowelFlag = (sansVowel) ? 1 : 0;

        String sqlQuery = String.format("UPDATE %s SET %2$s = '%8$s', %3$s = '%9$s', "
                + "%4$s = '%10$s', %5$s = %11$s, %6$s = %12$s, %7$s = %13$s",
                SETTINGS_TABLE, PREPEND_COLUMN, PREFIX_COLUMN, CHAR_MAP_COLUMN, AUTO_COLUMN,
                RANDOM_COLUMN, SANS_VOWEL_COLUMN, prepend, prefix, charMap,
                autoFlag, randomFlag, vowelFlag);

        databaseStatement.executeUpdate(sqlQuery);

        databaseStatement.close();
    }

    /**
     * Retrieves the default setting stored at the column in Settings table
     *
     * @param column name of the parameter to be changed
     * @return setting stored at that location
     * @throws SQLException thrown whenever there is an error with the database.
     */
    public Object retrieveSetting(String column) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        String sqlQuery = String.format("SELECT %s FROM %s;",
                column, SETTINGS_TABLE);

        Object parameter = null;
        ResultSet result = databaseStatement.executeQuery(sqlQuery);

        if (result.next()) {
            parameter = result.getObject(column);
        }

        databaseStatement.close();
        return parameter;
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
     * @throws SQLException thrown whenever there is an error with the database
     * @throws BadParameterException thrown whenever a malformed or invalid
     * parameter is passed
     */
    public void addIdList(Set<Pid> list, long amountCreated, String prefix, TokenType tokenType,
            boolean sansVowel, int rootLength) throws SQLException, BadParameterException {
        Logger.info("Database being updated");
        String valueQuery = "";
        String insertQuery = "INSERT INTO " + ID_TABLE + "('" + ID_COLUMN + "') VALUES ";
        Logger.info("ID inserted into: " + ID_TABLE + ", Column: " + ID_COLUMN);
        int counter = 1;
        Iterator<Pid> listIterator = list.iterator();
        while (listIterator.hasNext()) {
            Pid id = listIterator.next();

            // concatenate valueQuery with the current id as a value to the statement
            if (counter == 1) {
                valueQuery += String.format(" ('%s')", id);
            } else {
                valueQuery += String.format(", ('%s')", id);
            }

            /* 
             500 is used because, to my knowledge, the max number of values that 
             can be inserted at once is 500. The condition is also met whenever the
             id is the last id in the list.            
             */
            if (counter == 500 || !listIterator.hasNext()) {
                // finalize query
                String completeQuery = insertQuery + valueQuery + ";";
                Statement updateDatabase = DatabaseConnection.createStatement();

                // execute statement and cleanup
                updateDatabase.executeUpdate(completeQuery);
                updateDatabase.close();
                Logger.info("Database Update Finished: IDs Added");
                // reset counter and valueQuery
                counter = 0;
                valueQuery = "";
            }

            counter++;
        }

        // update table format
        this.addAmountCreated(prefix, tokenType, sansVowel, rootLength, amountCreated);

        //Logger.info("Finished; IDs printed to Database");
    }

    /**
     * Checks to see if the Id is valid in the database.
     *
     * @param pid Id to check
     * @return true if the pid doesn't exist in the database; false otherwise
     * @throws SQLException thrown whenever there is an error with the database
     */
    public boolean isValidId(Pid pid) throws SQLException {

        boolean isUnique;
        // a string to query a specific pid for existence in database
        String sqlQuery = String.format("SELECT %s FROM %2$s WHERE "
                + "%1$s = '%3$s'", ID_COLUMN, ID_TABLE, pid);

        Statement databaseStatement = DatabaseConnection.createStatement();

        // execute statement and retrieves the result
        ResultSet databaseResponse = databaseStatement.executeQuery(sqlQuery);

        // adds pid to redundantIdList if it already exists in database
        if (databaseResponse.next()) {

            // the pid is not unique and is therefore set to false
            pid.setUnique(false);

            // because one of the ids weren't unique, isIdListUnique returns false
            isUnique = false;
        } else {
            // the pid was unique and is therefore set to true
            pid.setUnique(true);
            isUnique = true;
        }
        // clean-up                
        databaseResponse.close();
        databaseStatement.close();

        return isUnique;
    }

    /**
     * For any valid Generator, a regular expression is returned that'll
 match that Generator's mapping.
     *
     * @param tokenType Designates what characters are contained in the id's
     * root
     * @return a regular expression
     */
    private String retrieveRegex(String tokenType, boolean sansVowel)
            throws BadParameterException {

        if (tokenType.equals("DIGIT")) {
            return String.format("([\\d])");
        } else if (tokenType.equals("LOWERCASE")) {
            return (sansVowel) ? "([^aeiouyA-Z\\d])" : "([a-z])";
        } else if (tokenType.equals("UPPERCASE")) {
            return (sansVowel) ? "([^a-zAEIOUY\\d])" : "([A-Z])";
        } else if (tokenType.equals("MIXEDCASE")) {
            return (sansVowel) ? "([^aeiouyAEIOUY\\d])" : "(([a-z])|([A-Z]))";
        } else if (tokenType.equals("LOWER_EXTENDED")) {
            return (sansVowel) ? "((\\d)|([^aeiouyA-Z]))" : "((\\d)|([a-z]))";
        } else if (tokenType.equals("UPPER_EXTENDED")) {
            return (sansVowel) ? "((\\d)|([^a-zAEIOUY]))" : "((\\d)|([A-Z]))";
        } else if (tokenType.equals("MIXED_EXTENDED")) {
            return (sansVowel) ? "((\\d)|([^aeiouyAEIOUY]))" : "((\\d)|([a-z])|([A-Z]))";
        } else {
            Logger.error("Error found in REGEX used for retrieveRegex");

            throw new BadParameterException(tokenType, "caused an error in retrieveRegex");
        }

    }
    
    /**
     * Creates a format using the given parameters. The format will be stored in
     * the FORMAT table of the database. The amount created column of the format
     * will always be initialized to a value of 0.
     *
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     * @throws SQLException thrown whenever there is an error with the database
     */
    protected void createFormat(String prefix, TokenType tokenType, boolean sansVowel,
            int rootLength) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        int vowelFlag = (sansVowel) ? 1 : 0;
        String sqlQuery = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) "
                + "VALUES ('%s', '%s', %d, %d, 0)",
                FORMAT_TABLE, PREFIX_COLUMN, TOKEN_TYPE_COLUMN, SANS_VOWEL_COLUMN,
                ROOT_LENGTH_COLUMN, AMOUNT_CREATED_COLUMN, prefix, tokenType, vowelFlag,
                rootLength);
        Logger.info("Format Updated: " + sqlQuery);
        databaseStatement.executeUpdate(sqlQuery);

        databaseStatement.close();
    }

    /**
     * Retrieves the value in the amountCreated column of a format and adds onto
     * it. The parameters given describe the format to be affected.
     *
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     * @param amountCreated The number of ids to add to the format.
     * @throws SQLException thrown whenever there is an error with the database.
     */
    public void addAmountCreated(String prefix, TokenType tokenType, boolean sansVowel,
            int rootLength, long amountCreated) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        long currentAmount = getAmountCreated(prefix, tokenType, sansVowel, rootLength);

        int vowelFlag = (sansVowel) ? 1 : 0;

        long newAmount = currentAmount + amountCreated;

        String sqlQuery = String.format("UPDATE %s SET %s = %d WHERE %s = '%s' AND "
                + "%s = '%s' AND %s = %d AND %s = %d;",
                FORMAT_TABLE, AMOUNT_CREATED_COLUMN, newAmount, PREFIX_COLUMN, prefix,
                TOKEN_TYPE_COLUMN, tokenType, SANS_VOWEL_COLUMN, vowelFlag,
                ROOT_LENGTH_COLUMN, rootLength);

        databaseStatement.executeUpdate(sqlQuery);
        databaseStatement.close();
    }

    /**
     * Sets the value of the amountCreated column of a format without regarding
     * the previous value. The parameters given describe the format to be
     * affected.
     *
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     * @param amountCreated The number of ids to insert into the format.
     * @throws SQLException thrown whenever there is an error with the database.
     */
    protected void setAmountCreated(String prefix, TokenType tokenType, boolean sansVowel,
            int rootLength, long amountCreated) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        int vowelFlag = (sansVowel) ? 1 : 0;

        String sqlQuery = String.format("UPDATE %s SET %s = %d WHERE %s = '%s' AND "
                + "%s = '%s' AND %s = %d AND %s = %d;",
                FORMAT_TABLE, AMOUNT_CREATED_COLUMN, amountCreated, PREFIX_COLUMN, prefix,
                TOKEN_TYPE_COLUMN, tokenType, SANS_VOWEL_COLUMN, vowelFlag,
                ROOT_LENGTH_COLUMN, rootLength);

        databaseStatement.executeUpdate(sqlQuery);
        databaseStatement.close();
    }

    /**
     * Retrieves the value in the amountCreated column of a format, identified
     * by the given parameters.
     *
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     * @return The value stored in the amountCreated column. Returns -1 if a
     * format was not found.
     * @throws SQLException thrown whenever there is an error with the database.
     */
    public long getAmountCreated(String prefix, TokenType tokenType, boolean sansVowel,
            int rootLength) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        int vowelFlag = (sansVowel) ? 1 : 0;
        String sqlQuery = String.format("SELECT %s FROM %s WHERE %s = '%s' AND "
                + "%s = '%s' AND %s = %d AND %s = %d;",
                AMOUNT_CREATED_COLUMN, FORMAT_TABLE, PREFIX_COLUMN, prefix,
                TOKEN_TYPE_COLUMN, tokenType, SANS_VOWEL_COLUMN, vowelFlag,
                ROOT_LENGTH_COLUMN, rootLength);

        ResultSet result = databaseStatement.executeQuery(sqlQuery);
        long value;
        if (result.next()) {
            value = result.getLong(AMOUNT_CREATED_COLUMN);
        } else {
            // format wasn't found
            value = -1;
        }

        result.close();
        databaseStatement.close();
        return value;

    }

    /**
     * Checks the database to see if a specific format exists using the given
     * parameters below.
     *
     * @param prefix The string that will be at the front of every id.
     * @param tokenType Designates what characters are contained in the id's
     * root.
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * @param rootLength Designates the length of the id's root.
     * @return true if the format exists, false otherwise.
     * @throws SQLException thrown whenever there is an error with the database.
     */
    public boolean formatExists(String prefix, TokenType tokenType, boolean sansVowel,
            int rootLength) throws SQLException {

        Statement databaseStatement = DatabaseConnection.createStatement();

        int vowelFlag = (sansVowel) ? 1 : 0;
        String sqlQuery = String.format("SELECT %s FROM %s WHERE %s = '%s' AND "
                + "%s = '%s' AND %s = %d AND %s = %d;",
                AMOUNT_CREATED_COLUMN, FORMAT_TABLE, PREFIX_COLUMN, prefix,
                TOKEN_TYPE_COLUMN, tokenType, SANS_VOWEL_COLUMN, vowelFlag,
                ROOT_LENGTH_COLUMN, rootLength);
        Logger.info("Format Created: " + sqlQuery);

        ResultSet result = databaseStatement.executeQuery(sqlQuery);
        boolean value;
        if (result.next()) {
            value = true;
        } else {
            // format wasn't found
            value = false;
        }

        result.close();
        databaseStatement.close();
        return value;
    }       

    /**
     * Refers to retrieveRegex method to build regular expressions to match each
     * character of charMap.
     *
     * The characters correspond to a unique tokenMapping and is dependent on
     * sansVowel. SansVowel acts as a toggle between regular expressions that
     * include or excludes vowels.
     *
     * @param sansVowel Designates whether or not the id's root contains vowels.
     * If the root does not contain vowels, the sansVowel is true; false
     * otherwise.
     * @param charMap The mapping used to describe range of possible characters
     * at each of the id's root's digits
     * @return the regular expression
     */
    private String regexBuilder(boolean sansVowel, String charMap) throws BadParameterException {
        String regex = "";
        for (int i = 0; i < charMap.length(); i++) {
            char c = charMap.charAt(i);
            String token;
            // get the correct token
            if (c == 'd') {
                token = "DIGIT";
            } else if (c == 'l') {
                token = "LOWERCASE";
            } else if (c == 'u') {
                token = "UPPERCASE";
            } else if (c == 'm') {
                token = "MIXEDCASE";
            } else if (c == 'e') {
                token = "MIXED_EXTENDED";
            } else {
                token = "error";
                Logger.error("error found in Regex Builder");
            }

            // get the token map and append int to regex
            String retrievedRegex = retrieveRegex(token, sansVowel);
            regex += String.format("[%s]", retrievedRegex);
        }
        return regex;
    }

    /**
     * Prints a list of ids to the server. Strictly used for error checking.
     *
     * @throws SQLException thrown whenever there is an error with the database
     */
    private void printData() throws SQLException {

        Statement s = DatabaseConnection.createStatement();
        String sql = String.format("SELECT * FROM %s;", ID_TABLE);
        ResultSet r = s.executeQuery(sql);

        for (int i = 0; r.next(); i++) {
            String curr = r.getString("id");
            System.out.println("id " + i + ")" + ") " + curr);
            //Logger.info("Generated ID: "+i+" "+curr);
        }
        //Logger.info("Done with Print");
    }

    /**
     * Prints entire list of formats to console
     *
     * @throws SQLException thrown whenever there is an error with the database
     */
    public void printFormat() throws SQLException {

        Statement s = DatabaseConnection.createStatement();
        String sql = String.format("SELECT * FROM %s;", FORMAT_TABLE);
        ResultSet r = s.executeQuery(sql);

        while (r.next()) {
            int id = r.getInt("id");
            String prefix = r.getString("PREFIX");
            String tokenType = r.getString("TOKEN_TYPE");
            boolean sansVowel = r.getBoolean("SANS_VOWEL");
            int rootLength = r.getInt("ROOT_LENGTH");
            long amountCreated = r.getLong("AMOUNT_CREATED");

            System.out.printf("%10d):%20s%20s%20b%20d%20d\n",
                    id, prefix, tokenType, sansVowel, rootLength, amountCreated);
        }

    }

    /**
     * Prints the current default settings
     *
     * @throws SQLException
     */
    public void printCache() throws SQLException {

        Statement s = DatabaseConnection.createStatement();
        String sql = String.format("SELECT * FROM %s;", SETTINGS_TABLE);
        ResultSet r = s.executeQuery(sql);

        while (r.next()) {
            String prepend = r.getString(this.getPREPEND_COLUMN());
            String prefix = r.getString(this.getPREFIX_COLUMN());
            String tokenType = r.getString(this.getTOKEN_TYPE_COLUMN());
            String charMap = r.getString(this.getCHAR_MAP_COLUMN());
            boolean sansVowel = r.getBoolean(this.getSANS_VOWEL_COLUMN());
            boolean isAuto = r.getBoolean(this.getAUTO_COLUMN());
            boolean isRandom = r.getBoolean(this.getRANDOM_COLUMN());
            int rootLength = r.getInt(this.getROOT_LENGTH_COLUMN());

            System.out.printf("%20s%20s%20s%20s%20b%20b%20b%20d\n",
                    prepend, prefix, tokenType, charMap, sansVowel, isAuto, isRandom, rootLength);
        }

    }

    /**
     * Used by an external method to close this connection.
     *
     * @return 
     * @throws SQLException thrown whenever there is an error with the database
     */
    @Override
    public boolean closeConnection() throws SQLException {
        Logger.info("DB Connection Closed");
        DatabaseConnection.close();
        return true;
    }
    
    /**
     * Checks to see if the database for the existence of a particular table. If
     * the database returns a null value then the given tableName does not exist
     * within the database.
     *
     * @param tableName
     * @return true if table exists in database, false otherwise
     * @throws SQLException thrown whenever there is an error with the database
     */
    public boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData metaData = DatabaseConnection.getMetaData();

        ResultSet databaseResponse = metaData.getTables(null, null, tableName, null);
        boolean flag;
        System.out.println("in tableExists");

        if (!databaseResponse.next()) {
            flag = false;
        } else {
            flag = true;
        }

        // close connection
        databaseResponse.close();
        return flag;
    }
    
    /* typical getters and setters */
    public String getPREFIX_COLUMN() {
        return PREFIX_COLUMN;
    }

    public String getSANS_VOWEL_COLUMN() {
        return SANS_VOWEL_COLUMN;
    }

    public String getTOKEN_TYPE_COLUMN() {
        return TOKEN_TYPE_COLUMN;
    }

    public String getROOT_LENGTH_COLUMN() {
        return ROOT_LENGTH_COLUMN;
    }

    public String getPREPEND_COLUMN() {
        return PREPEND_COLUMN;
    }

    public String getAUTO_COLUMN() {
        return AUTO_COLUMN;
    }

    public String getRANDOM_COLUMN() {
        return RANDOM_COLUMN;
    }

    public String getCHAR_MAP_COLUMN() {
        return CHAR_MAP_COLUMN;
    }

    public String getSETTINGS_TABLE() {
        return SETTINGS_TABLE;
    }

    public String getID_COLUMN() {
        return ID_COLUMN;
    }

    public String getID_TABLE() {
        return ID_TABLE;
    }

    public String getFORMAT_TABLE() {
        return FORMAT_TABLE;
    }

    public String getDRIVER() {
        return DRIVER;
    }

    public String getDatabasePath() {
        return DatabasePath;
    }

    public String getDatabaseName() {
        return DatabaseName;
    }
    
}