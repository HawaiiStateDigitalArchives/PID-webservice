package com.hida.service;

import org.apache.log4j.Logger;
import com.hida.model.Citation;
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
    private CitationRepository CitationRepo;

    private final static Logger logger = Logger.getLogger(ResolverService.class);

    /**
     * Retrieves url of provided purlid
     *
     * @param purl purlid of desired row
     * @return String
     */
    public String retrieveUrl(String purl) {
        Citation entity = CitationRepo.findOne(purl);
        String url = entity.getUrl();

        return url;
    }

    /**
     * Inserts PURL into database
     *
     * @param citation Citation to insert into database
     */
    public void insertCitation(Citation citation) {
        CitationRepo.save(citation);
    }

    /**
     * Edits url of db row with corresponding purlid
     *
     * @param purl purlid of desired edited row
     * @param url url that desired row url will be changed to
     */
    public void editUrl(String purl, String url) {
        Citation entity = CitationRepo.findOne(purl);
        entity.setUrl(url);
    }

    /**
     * Deletes db row with corresponding purlid
     *
     * @param purl purlid of desired deleted row
     */
    public void deleteCitation(String purl) {
        Citation entity = CitationRepo.findOne(purl);
        CitationRepo.delete(entity);
    }

    /**
     * Retrieves citation corresponding to a particular purl
     *
     * @param purl purlid of desired row that will become Citation
     * @return Citation
     */
    public Citation retrieveCitation(String purl) {
        Citation entity = CitationRepo.findOne(purl);

        return entity;
    }
}
