package com.hida.model;

/**
 * An Id Generator that creates Pids. For each Pid, their entire name will be
 * uniformly determined by the possible characters provided by a single Token.
 *
 *
 * @author lruffin
 */
public class AutoIdGenerator extends IdGenerator {

    /**
     * Designates what characters are contained in the id's root name.
     */
    private Token TokenType;

    /**
     * Designates the length of the id's root.
     */
    private int RootLength;

    /**
     * Default constructor. Aside from Token, there are no restrictions placed
     * on the parameters and can be used however one sees fit.
     *
     * @param prefix A sequence of characters that appear in the beginning of
     * PIDs
     * @param tokenType An enum used to configure PIDS
     * @param rootLength Designates the length of the id's root
     */
    public AutoIdGenerator(String prefix, Token tokenType, int rootLength) {
        super(prefix);
        this.TokenType = tokenType;
        this.RootLength = rootLength;
        this.MaxPermutation = getMaxPermutation();
    }
    
    /**
     * This method calculates and returns the total possible number of
     * permutations using the values given in the constructor.
     *
     * @return number of permutations
     */
    @Override
    final public long getMaxPermutation() {
        // get the base of each character
        int base = TokenType.getCharacters().length();

        // raise it to the power of how ever long the rootLength is
        return ((long) Math.pow(base, RootLength));
    }

    /**
     * Increments a value of a PID. If the maximum limit is reached the values
     * will wrap around.
     *
     * @param pid The pid to increment
     */
    @Override
    public void incrementPid(Pid pid) {
        long next = (this.PidToLong(pid) + 1) % this.MaxPermutation;       
        pid.setName(this.longToName(next));       
    }

    /**
     * Translates an ordinal number into a sequence of characters with a radix
     * based on TokenType.
     *
     * @param ordinal The nth position of a permutation
     * @return The sequence at the nth position
     */
    @Override
    protected String longToName(long ordinal) {
        StringBuilder name = new StringBuilder("");
        String map = TokenType.getCharacters();
        int radix = map.length();
        int fullNameLength = RootLength + Prefix.length();

        long remainder = ordinal;
        for (int i = fullNameLength - 1; i >= Prefix.length(); i--) {
            name.insert(0, map.charAt( (int)(remainder % radix)));

            remainder /= radix;
        }

        return Prefix + name.toString();
    }

    /**
     * Translates a Pid into an ordinal number.
     *
     * @param pid A persistent identifier
     * @return The ordinal number of the Pid
     */
    @Override
    protected long PidToLong(Pid pid) {
        String name = pid.getName();
        String map = TokenType.getCharacters();
        int radix = map.length();
        int fullNameLength = RootLength + Prefix.length();

        long ordinal = 0;
        for (int i = Prefix.length(); i < fullNameLength; i++) {
            int mapIndex = map.indexOf(name.charAt(i));

            ordinal += (long) Math.pow(radix, name.length() - i - 1) * mapIndex;
        }

        return ordinal;
    }

    /* getters and setters */
    public Token getTokenType() {
        return TokenType;
    }

    public void setTokenType(Token TokenType) {
        this.TokenType = TokenType;
    }

    public int getRootLength() {
        return RootLength;
    }

    public void setRootLength(int RootLength) {
        this.RootLength = RootLength;
    }
    
}
