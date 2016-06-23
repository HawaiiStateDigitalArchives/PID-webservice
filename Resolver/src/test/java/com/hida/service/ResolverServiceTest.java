package com.hida.service;

import com.hida.model.Citation;
import com.hida.repositories.CitationRepository;
import org.junit.Assert;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

/**
 * This class tests the functionality of MinterServiceImpl using Mockito.
 *
 * @author lruffin
 */
public class ResolverServiceTest {

    @Mock
    private CitationRepository CitationRepo;

    @InjectMocks
    private ResolverService Service;

    /**
     * Sets up Mockito
     *
     * @throws Exception
     */
    @BeforeClass
    public void setUpClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests to see if the URL of a given Citation entity is properly retrieved
     */
    @Test
    public void testRetrieveUrl() {
        String purl = "purl";
        String url = "url";
        Citation entity = new Citation(purl);
        entity.setUrl(url);
        when(CitationRepo.findOne(purl)).thenReturn(entity);

        String entityUrl = Service.retrieveUrl(purl);

        verify(CitationRepo, atLeastOnce()).findOne(purl);
        Assert.assertEquals(url, entityUrl);
        
    }

    /**
     * Tests to see if a Citation entity can be edited
     */
    @Test
    public void testEditUrl() {
        Citation entity = new Citation();
        when(CitationRepo.findOne(any(String.class))).thenReturn(entity);

        String newUrl = "newUrl";
        Service.editUrl("purl", newUrl);
        verify(CitationRepo, atLeastOnce()).findOne(any(String.class));
        Assert.assertEquals(newUrl, entity.getUrl());
    }

    /**
     * Tests to see if a Citation entity can be deleted
     */
    @Test
    public void testDeleteCitation() {
        Citation entity = new Citation();
        when(CitationRepo.findOne(any(String.class))).thenReturn(entity);
        doNothing().when(CitationRepo).delete(entity);

        Service.deleteCitation("");
        verify(CitationRepo, atLeastOnce()).delete(any(Citation.class));
    }

    /**
     * Tests to see if a Citation entity is retrievable
     */
    @Test
    public void testRetrieveCitation() {
        Citation citation = new Citation();
        String purl = "purl";
        citation.setPurl(purl);
        
        when(CitationRepo.findOne(any(String.class))).thenReturn(citation);

        Citation entity = Service.retrieveCitation(purl);
        verify(CitationRepo, atLeastOnce()).findOne(any(String.class));
        Assert.assertEquals(citation, entity);
    }

    /**
     * Tests to see if a Citation object can be persisted
     */
    @Test
    public void testInsertCitation() {
        Citation purl = new Citation();
        when(CitationRepo.save(purl)).thenReturn(null);

        Service.insertCitation(purl);
        verify(CitationRepo, atLeastOnce()).save(any(Citation.class));
    }

}
