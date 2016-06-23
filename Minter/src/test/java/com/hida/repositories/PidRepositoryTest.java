package com.hida.repositories;

import com.hida.configuration.RepositoryConfiguration;
import com.hida.model.Pid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Tests the functionality of PidRepository
 *
 * @author lruffin
 */
@WebAppConfiguration
@IntegrationTest
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
@TestPropertySource(locations = "classpath:testConfig.properties")
@TestExecutionListeners(inheritListeners = false, listeners = {
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
public class PidRepositoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private PidRepository PidRepo;

    /**
     * A test that ensures that Pids with the same name cannot be added to the
     * database
     */
    @Test
    public void testUniqeness() {
        Pid pid1 = getSamplePid();
        Pid pid2 = getSamplePid();

        PidRepo.save(pid1);
        PidRepo.save(pid2);

        long sizeBefore = PidRepo.count();
        Assert.assertEquals(sizeBefore, 1);
    }

    /**
     * Tests the save and delete function
     */
    @Test
    public void testSaveAndDelete() {
        Pid pid = getSamplePid();

        PidRepo.save(pid);

        long sizeBefore = PidRepo.count();
        Assert.assertEquals(sizeBefore, 1);

        PidRepo.delete(pid);

        long sizeAfter = PidRepo.count();
        Assert.assertEquals(sizeAfter, 0);
    }

    /**
     * Test to see if a Pid can be located by its Name
     */
    @Test
    public void testFindByName() {
        Pid pid = getSamplePid();

        PidRepo.save(pid);

        Pid entity = PidRepo.findOne(pid.getName());
        Assert.assertNotNull(entity);
    }

    private Pid getSamplePid() {
        Pid sample = new Pid("a");

        return sample;
    }

    /**
     * Deletes all entries in the in-memory database after each test
     */
    @AfterMethod
    public void tearDown() {
        PidRepo.deleteAll();
    }
}
