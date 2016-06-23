package com.hida.repositories;

import com.hida.configuration.RepositoryConfiguration;
import com.hida.model.DefaultSetting;
import com.hida.model.Token;
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
import org.testng.annotations.Test;

/**
 * Tests the functionality of DefaultSettingRepository.
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
public class DefaultSettingRepositoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DefaultSettingRepository DefaultSettingRepo;

    /**
     * Tests to see if DefaultSettingDao can find an entity with an id of 1.
     */
    @Test
    public void testSaveAndFind() {
        DefaultSetting sample = getSampleDefaultSetting();
        DefaultSettingRepo.save(sample);

        DefaultSetting entity = DefaultSettingRepo.findCurrentDefaultSetting();
        Assert.assertNotNull(entity);
    }

    /**
     * Returns a sample DefaultSetting entity.
     *
     * @return
     */
    private DefaultSetting getSampleDefaultSetting() {
        DefaultSetting setting = new DefaultSetting("",
                "",
                5,
                Token.DIGIT,
                "d",
                1,
                true,
                true,
                true);

        return setting;
    }
}
