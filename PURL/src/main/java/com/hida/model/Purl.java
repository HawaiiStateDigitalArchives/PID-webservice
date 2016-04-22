package com.hida.model;

/**
 * POJO for PURLs, logical units of transaction that can be resolved into
 * various records and locations.
 *
 * @author lruffin
 * @author: leland lopez
 */
public class Purl {

    //variables
    private String URL;
    private String PURL;
    private String ERC;
    private String Who;
    private String What;
    private String When;

    /**
     * Purl Constructor
     *
     * @param PURL purlid of model_purl
     */
    public Purl(String PURL) {
        this.PURL = PURL;
    }

    /**
     * No-arg constructor used by Hibernate
     */
    public Purl() {

    }

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public String getPURL() {
        return PURL;
    }

    public void setPURL(String pURL) {
        PURL = pURL;
    }

    public String getERC() {
        return ERC;
    }

    public void setERC(String eRC) {
        ERC = eRC;
    }

    public String getWho() {
        return Who;
    }

    public void setWho(String who) {
        Who = who;
    }

    public String getWhat() {
        return What;
    }

    public void setWhat(String what) {
        What = what;
    }

    public String getWhen() {
        return When;
    }

    public void setWhen(String when) {
        When = when;
    }

    /**
     *
     * @return Json String
     */
    public String toJSON() {
        String json = "";
        json += "{";
        json += "\"PURL\":\"" + PURL + "\",";
        json += "\"URL\":\"" + URL + "\",";
        json += "\"ERC\":\"" + ERC + "\",";
        json += "\"Who\":\"" + Who + "\",";
        json += "\"What\":\"" + What + "\",";
        json += "\"When\":\"" + When + "\"";
        json += "}";
        return json;
    }

}