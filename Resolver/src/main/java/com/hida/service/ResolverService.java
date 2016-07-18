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

import org.apache.log4j.Logger;
import com.hida.model.Citation;
import com.hida.model.CitationDoesNotExistException;
import com.hida.model.DuplicateCitationException;
import com.hida.repositories.CitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Allows the performance of CRUD operations on citation objects by by serving
 * as a middle ground between the database and the controller
 *
 * @author lruffin
 * @author: leland lopez
 */
@Service("resolverService")
@Transactional
public class ResolverService {

    @Autowired
    private CitationRepository citationRepo_;

    private final static Logger LOGGER = Logger.getLogger(ResolverService.class);

    /**
     * Retrieves url of provided purlid
     *
     * @param purl purlid of desired row
     * @return String
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    public String retrieveUrl(String purl) throws CitationDoesNotExistException {
        Citation entity = retrieveCitation(purl);
        String url = entity.getUrl();

        return url;
    }

    /**
     * Inserts PURL into database
     *
     * @param citation Citation to insert into database
     * @throws DuplicateCitationException Thrown when another citation with the
     * given purl already exists in the database
     */
    public void insertCitation(Citation citation) throws DuplicateCitationException {
        // check to see if the citation already exists
        Citation entity = citationRepo_.findOne(citation.getPurl()); 
        if(entity != null){
            throw new DuplicateCitationException("Cannot insert citation with purl");
        }
        citationRepo_.save(citation);
    }

    /**
     * Edits url of db row with corresponding purlid
     *
     * @param purl purlid of desired edited row
     * @param url url that desired row url will be changed to
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    public void editUrl(String purl, String url) throws CitationDoesNotExistException {
        Citation entity = retrieveCitation(purl);
        entity.setUrl(url);
    }

    /**
     * Deletes db row with corresponding purlid
     *
     * @param purl purlid of desired deleted row
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    public void deleteCitation(String purl) throws CitationDoesNotExistException {
        Citation entity = retrieveCitation(purl);

        if (entity == null) {
            throw new CitationDoesNotExistException(entity);
        }

        citationRepo_.delete(entity);
    }

    /**
     * Retrieves citation corresponding to a particular purl
     *
     * @param purl purlid of desired row that will become Citation
     * @return Citation
     * @throws CitationDoesNotExistException Thrown when the citation with the
     * given purl does not exist
     */
    public Citation retrieveCitation(String purl) throws CitationDoesNotExistException {
        Citation entity = citationRepo_.findOne(purl);

        if (entity == null) {
            throw new CitationDoesNotExistException(entity);
        }

        return entity;
    }
}
