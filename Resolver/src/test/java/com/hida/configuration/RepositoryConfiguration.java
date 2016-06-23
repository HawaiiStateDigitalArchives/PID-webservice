package com.hida.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring-boot equivalent to Hibernate Configuration and JPA transaction
 * manager. Spring-boot bunches up all of these configuration classes into the
 * annotations.
 *
 * @author lruffin
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.hida.model"})
@EnableJpaRepositories(basePackages = {"com.hida.repositories"})
@EnableTransactionManagement
public class RepositoryConfiguration {
}
