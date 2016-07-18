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
    private CitationRepository citationRepo_;

    @InjectMocks
    private ResolverService service_;

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
    public void testRetrieveUrl() throws Exception {
        String purl = "purl";
        String url = "url";
        Citation entity = new Citation(purl);
        entity.setUrl(url);
        when(citationRepo_.findOne(purl)).thenReturn(entity);

        String entityUrl = service_.retrieveUrl(purl);

        verify(citationRepo_, atLeastOnce()).findOne(purl);
        Assert.assertEquals(url, entityUrl);

    }

    /**
     * Tests to see if a Citation entity can be edited
     */
    @Test
    public void testEditUrl() throws Exception {
        Citation entity = new Citation();
        when(citationRepo_.findOne(any(String.class))).thenReturn(entity);

        String newUrl = "newUrl";
        service_.editUrl("purl", newUrl);
        verify(citationRepo_, atLeastOnce()).findOne(any(String.class));
        Assert.assertEquals(newUrl, entity.getUrl());
    }

    /**
     * Tests to see if a Citation entity can be deleted
     */
    @Test
    public void testDeleteCitation() throws Exception {
        Citation entity = new Citation();
        when(citationRepo_.findOne(any(String.class))).thenReturn(entity);
        doNothing().when(citationRepo_).delete(entity);

        service_.deleteCitation("");
        verify(citationRepo_, atLeastOnce()).delete(any(Citation.class));
    }

    /**
     * Tests to see if a Citation entity is retrievable
     */
    @Test
    public void testRetrieveCitation() throws Exception {
        Citation citation = new Citation();
        String purl = "purl";
        citation.setPurl(purl);

        when(citationRepo_.findOne(any(String.class))).thenReturn(citation);

        Citation entity = service_.retrieveCitation(purl);
        verify(citationRepo_, atLeastOnce()).findOne(any(String.class));
        Assert.assertEquals(citation, entity);
    }

    /**
     * Tests to see if a Citation object can be persisted
     */
    @Test
    public void testInsertCitation() throws Exception {
        Citation purl = new Citation();
        purl.setPurl("testPurl");
        when(citationRepo_.save(purl)).thenReturn(null);

        service_.insertCitation(purl);
        verify(citationRepo_, atLeastOnce()).save(any(Citation.class));
    }

}
