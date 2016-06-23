package com.hida.repositories;

import com.hida.model.Citation;
import org.springframework.data.repository.CrudRepository;

/**
 * Allows the use of CRUD operations on Citation objects
 *
 * @author lruffin
 */
public interface CitationRepository extends CrudRepository<Citation, String> {

}
