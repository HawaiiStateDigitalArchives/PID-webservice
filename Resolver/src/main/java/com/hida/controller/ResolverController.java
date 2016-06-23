package com.hida.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.hida.model.Citation;
import com.hida.service.ResolverService;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller that handles the interactions between a client and the Resolver.
 *
 * @author leland lopez
 * @author lruffin
 */
@RestController
@RequestMapping("/Resolver")
public class ResolverController {

    /* 
     * Logger; logfile to be stored in resource folder    
     */
    private static final org.slf4j.Logger Logger
            = LoggerFactory.getLogger(ResolverController.class);

    @Autowired
    private ResolverService ResolverService;

    /**
     * Maps to the home page.
     *
     * @return view to the home page
     */
    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping(value = {""}, method = {RequestMethod.GET})
    public String displayIndex() {
        return "";
    }

    /**
     * matches url: /PURL/retrieve retrieves corresponding citation row of
     * provided citation returns model - purl and view : retrieve if successful
     * returns model - null if not
     *
     * @param purl purl of desired retrieved row
     * @return ModelAndView Holds resulting Model and view information
     */
    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping("/retrieve")
    public Citation retrieve(@RequestParam(value = "purl", required = true) String purl) {
        Logger.info("Retrieve was Called");
        // retrieve citation jsonString
        Citation citation = ResolverService.retrieveCitation(purl);

        Logger.info("Retrieve returned: {}", citation);
        return citation;
    }

    /**
     * matches url: /PURL/edit edit Citation row url, with provided url returns
     * model : Citation and view : edit if successful returns model : null if
     * not
     *
     * @param purl purl of desired edited row
     * @param url The url that the desired Citation will have
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping("/edit")
    public String edit(@RequestParam(value = "purl", required = true) String purl,
            @RequestParam(value = "url", required = true) String url) throws IOException {
        Logger.info("Edit was Called");
        // edit the purl and then retrieve its entire contents
        ResolverService.editUrl(purl, url);
        Citation citation = ResolverService.retrieveCitation(purl);

        Logger.info("Edit returned: {}", citation);
        return "";
    }

    /**
     * matches url: /PURL/insert inserts Citation url, erc, who, what, when to
     * new row of table returns model : Citation and view : insert if successful
     * returns model : null if not
     *
     * @param purl Citation to be inserted
     * @param url url to be inserted
     * @param erc erc to be inserted
     * @param who who to be inserted
     * @param what what to be inserted
     * @param when when to be insertd
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @ResponseStatus(code = HttpStatus.CREATED)
    @RequestMapping("/insert")
    public String insert(@RequestParam(value = "purl", required = true) String purl,
            @RequestParam(value = "url", required = true) String url,
            @RequestParam(value = "erc", required = true) String erc,
            @RequestParam(value = "who", required = true) String who,
            @RequestParam(value = "what", required = true) String what,
            @RequestParam(value = "when", required = true) String when
    ) throws IOException {
        Logger.info("Insert was Called");
        // create purl jsonString to store information
        Citation citation = new Citation(purl, url, erc, who, what, when);

        // insert purl
        ResolverService.insertCitation(citation);

        Logger.info("insert returned: {}", citation);
        return "";
    }

    /**
     * matches url: /PURL/delete deletes row of table with corresponding purl
     * returns view : deleted if successful returns model : null if not
     *
     * @param purl purl of desired deleted row
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping("/delete")
    public String delete(@RequestParam(value = "purl", required = true) String purl)
            throws IOException {
        Logger.info("Insert was Called");

        // delete Citation
        ResolverService.deleteCitation(purl);

        return "";
    }

    /**
     * Throws any exception that may be caught within the program
     *
     * @param req the HTTP request
     * @param exception the caught exception
     * @return The view of the error message
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralError(HttpServletRequest req, Exception exception) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("status", 500);
        mav.addObject("exception", exception.getClass().getSimpleName());
        mav.addObject("message", exception.getMessage());
        Logger.error("Exception caught ", exception);

        mav.setViewName("error");
        return mav;
    }
}
