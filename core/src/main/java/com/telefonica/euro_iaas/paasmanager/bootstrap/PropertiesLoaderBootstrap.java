/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U <br>
 * This file is part of FI-WARE project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * </p>
 * <p>
 * You may obtain a copy of the License at:<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * <p>
 * See the License for the specific language governing permissions and limitations under the License.
 * </p>
 * <p>
 * For those usages not covered by the Apache version 2.0 License please contact with opensource@tid.es
 * </p>
 */

package com.telefonica.euro_iaas.paasmanager.bootstrap;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.telefonica.euro_iaas.paasmanager.exception.PaasManagerServerRuntimeException;
import com.telefonica.fiware.commons.properties.PropertiesProvider;
import com.telefonica.fiware.commons.properties.impl.PropertiesProviderFactoryImpl;

/**
 * Class in charge to persist the system properties in data base on bootstrap time.
 * 
 * @author Jesus M. Movilla
 */
public class PropertiesLoaderBootstrap implements ServletContextListener {

    private static Logger log = LoggerFactory.getLogger(PropertiesLoaderBootstrap.class);

    private static final String NAMESPACE = "/SystemConfiguration.properties";

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent event) {
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
        EntityManagerFactory emf = (EntityManagerFactory) ctx.getBean("entityManagerFactory");

        PropertiesProvider propertiesProvider = new PropertiesProviderFactoryImpl().createPropertiesProvider(emf);
        Properties properties = propertiesProvider.load(NAMESPACE);
        try {
            log.info("store namespace: " + NAMESPACE);
            propertiesProvider.store(properties, NAMESPACE);
        } catch (Exception e) {
            throw new PaasManagerServerRuntimeException(e);
        }
    }

}
