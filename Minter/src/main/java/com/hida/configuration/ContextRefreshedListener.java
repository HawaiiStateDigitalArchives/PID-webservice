package com.hida.configuration;

import com.hida.model.DefaultSetting;
import com.hida.service.MinterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * This class initializes the services whenever the context is refreshed so they
 * can be called upon by the constructor.
 *
 * @author lruffin
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    MinterService Service;

    /* 
     * Logger; logfile to be stored in resource folder    
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextRefreshedListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            Service.initializeStoredSetting();

            Service.generateCache();
        }
        catch (Exception exception) {
            LOGGER.error("Exception caught during context refresh", exception);
        }
    }
}
