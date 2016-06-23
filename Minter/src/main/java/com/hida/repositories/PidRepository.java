package com.hida.repositories;

import com.hida.model.Pid;
import org.springframework.data.repository.CrudRepository;

/**
 * Allows the use of CRUD operations on Pid objects
 *
 * @author lruffin
 */
public interface PidRepository extends CrudRepository<Pid, String> {

}
