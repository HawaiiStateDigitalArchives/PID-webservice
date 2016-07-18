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
package com.hida.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.hida.model.Citation;
import com.hida.model.CitationDoesNotExistException;
import com.hida.model.DuplicateCitationException;
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
    private static final org.slf4j.Logger LOGGER
            = LoggerFactory.getLogger(ResolverController.class);

    @Autowired
    private ResolverService resolverService_;

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
    public Citation retrieve(@RequestParam(value = "purl", required = true) String purl) 
            throws Exception {
        LOGGER.info("Retrieve was Called");
        // retrieve citation jsonString
        Citation citation = resolverService_.retrieveCitation(purl);
        
        if(citation == null){
            throw new Exception("");
        }
        
        LOGGER.info("Retrieve returned: {}", citation);
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
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    @ResponseStatus(code = HttpStatus.OK)
    @RequestMapping("/edit")
    public String edit(@RequestParam(value = "purl", required = true) String purl,
            @RequestParam(value = "url", required = true) String url) throws IOException, 
            CitationDoesNotExistException {
        LOGGER.info("Edit was Called");
        // edit the purl and then retrieve its entire contents
        resolverService_.editUrl(purl, url);
        Citation citation = resolverService_.retrieveCitation(purl);

        LOGGER.info("Edit returned: {}", citation);
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
     * @throws DuplicateCitationException Thrown when another citation with the
     * given purl already exists in the database
     */
    @ResponseStatus(code = HttpStatus.CREATED)
    @RequestMapping("/insert")
    public String insert(@RequestParam(value = "purl", required = true) String purl,
            @RequestParam(value = "url", required = true) String url,
            @RequestParam(value = "erc", required = true) String erc,
            @RequestParam(value = "who", required = true) String who,
            @RequestParam(value = "what", required = true) String what,
            @RequestParam(value = "when", required = true) String when
    ) throws IOException, DuplicateCitationException {
        LOGGER.info("Insert was Called");
        // create purl jsonString to store information
        Citation citation = new Citation(purl, url, erc, who, what, when);

        // insert purl
        resolverService_.insertCitation(citation);

        LOGGER.info("insert returned: {}", citation);
        return "";
    }

    /**
     * matches url: /PURL/delete deletes row of table with corresponding purl
     * returns view : deleted if successful returns model : null if not
     *
     * @param purl purl of desired deleted row
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @RequestMapping("/delete")
    public String delete(@RequestParam(value = "purl", required = true) String purl)
            throws IOException, CitationDoesNotExistException {
        LOGGER.info("Insert was Called");

        // delete Citation
        resolverService_.deleteCitation(purl);

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
        LOGGER.error("Exception caught ", exception);

        mav.setViewName("error");
        return mav;
    }
}
