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
package com.hida.model;

/**
 * This Exception is thrown whenever an attempt has been made to edit or delete
 * a non-persisted Citation in the database
 * @author lruffin
 */
public class CitationDoesNotExistException extends Exception {

    private String purl_;
    
    /**
     * Creates a new instance of <code>CitationDoesNotExistException</code>
     * without detail message.
     */
    public CitationDoesNotExistException() {
    }
    
    
    public CitationDoesNotExistException(Citation citation){
        purl_ = citation.getPurl();
    }

    /**
     * Constructs an instance of <code>CitationDoesNotExistException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public CitationDoesNotExistException(String msg) {
        super(msg);
    }
    
    @Override
    public String getMessage() {
        return String.format("The citation with the given purl \"%s\" does not exist", purl_);
    }
}
