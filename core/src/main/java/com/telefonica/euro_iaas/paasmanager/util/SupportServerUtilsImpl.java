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

import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for manage Support Server.
 */
public class SupportServerUtilsImpl implements SupportServerUtils {

    /**
     * The log.
     */

    private static Logger log = LoggerFactory.getLogger(SupportServerUtilsImpl.class);

    public OpenOperationUtil openOperationUtil = null;

       /**
     * It obtains the gpgkey from the Support Server.
     * @param regionName
     * @return
     */
    public String getKey (String regionName, String key) {
        String sshKey = null;
        try {
            HttpUriRequest request = openOperationUtil.createSupportGetRequest("/v1/support/"+ regionName +
                "/" + key, APPLICATION_JSON);
            sshKey = openOperationUtil.executeSupportRequest(request);
        } catch (OpenStackException e) {
            log.debug("Error to obtain the ssh " + e.getMessage());
        }
        return sshKey;

    }

    /**
     * It obtains the sshkey from the Support Server.
     * @param regionName
     * @return
     */
    public String getSshKey (String regionName) {
        return getKey(regionName, "sshkey");
    }

    /**
     * It obtains the gpgkey from the Support Server.
     * @param regionName
     * @return
     */
    public String getGpgKey (String regionName) {
        return getKey(regionName, "gpgkey");
    }

    /**
     * It set the OpenOperationUtil instance.
     * @param openOperationUtil
     */
    public void setOpenOperationUtil (OpenOperationUtil openOperationUtil) {
        this.openOperationUtil = openOperationUtil;
    }








}
