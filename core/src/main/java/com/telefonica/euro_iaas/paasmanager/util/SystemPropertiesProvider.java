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

package com.telefonica.euro_iaas.paasmanager.util;

import java.util.Properties;

/**
 * <p>
 * PropertiesProvider interface.
 * </p>
 * 
 * @author Jesus M. Movilla
 */
public interface SystemPropertiesProvider {

    String PAAS_MANAGER_URL = "paas_manager_url";

    /** The Constant KEYSTONE_URL. */
    String KEYSTONE_URL = "openstack-tcloud.keystone.url";

    /** The Constant CLOUD_SYSTEM. */
    String CLOUD_SYSTEM = "openstack-tcloud.cloudSystem";

    /** The Constant KEYSTONE_USER. */
    String KEYSTONE_USER = "openstack-tcloud.keystone.user";

    String KEYSTONE_PASS = "openstack-tcloud.keystone.pass";

    /** Keystone tenant id **/
    String KEYSTONE_TENANT = "openstack-tcloud.keystone.tenant";

    /** Path for the user data **/
    String USER_DATA_PATH = "user_data_path";

    String SUPPORT_KEY = "support_key";

    String AVAILABLE_ATTRIBUTE_TYPES = "available.attribute.types";

    /**
     * max num of retries in requests to openStack.
     */
    String OPENSTACK_RETRIES = "openstack-tcloud.retries";

    /**
     * Get the property for a given key.
     * 
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Integer getIntProperty(String key);

    /**
     * Get the property for a given key.
     * 
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getProperty(String key);

    /**
     * Find all system configuration properties.
     * 
     * @return
     */
    Properties loadProperties();

    /**
     * Persist the configuration properties in the SystemConfiguration namespace.
     * 
     * @param configuration
     *            the properties to store∫
     */
    void setProperties(Properties configuration);

}
