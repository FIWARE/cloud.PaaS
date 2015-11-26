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
import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;

/**
 * Utilities for manage OpenStack regions.
 */
public interface OpenStackRegion {

    /**
     * How to get an endpoint by region.
     * 
     * @param name
     *            e.g. nova, quantum, glance, etc...
     * @param regionName
     * @return the http url with de endpoint.
     * @throws OpenStackException
     */
    String getEndPointByNameAndRegionName(String name, String regionName) throws OpenStackException;

    /**
     * Get endpoint for nova services.
     * 
     * @param regionName
     * @return
     * @throws OpenStackException
     */
    String getNovaEndPoint(String regionName) throws OpenStackException;

    /**
     * Get the endpoint for networks services.
     * 
     * @param regionName
     * @return
     * @throws OpenStackException
     */
    String getQuantumEndPoint(String regionName) throws OpenStackException;

    /**
     * @param regionName
     * @return
     * @throws OpenStackException
     */
    String getSdcEndPoint(String regionName) throws OpenStackException;

    /**
     * It Obtains the support server endpoint.
     * @return the endpoint
     * @throws OpenStackException
     */
    String getSupportEndPoint() throws OpenStackException;

    /**
     * @return
     * @throws OpenStackException
     */
    String getDefaultRegion() throws OpenStackException;

    /**
     * @return
     * @throws OpenStackException
     */
    String getFederatedQuantumEndPoint() throws OpenStackException;

    /**
     * @param region
     * @return
     * @throws OpenStackException
     */
    String getChefServerEndPoint(String region) throws OpenStackException;

    /**
     * @param regionName
     * @return
     * @throws OpenStackException
     */
    String getPuppetMasterEndPoint(String regionName) throws OpenStackException;

    /**
     * @return
     * @throws OpenStackException
     */
    OpenStackAccess getTokenAdmin() throws OpenStackException;
}
