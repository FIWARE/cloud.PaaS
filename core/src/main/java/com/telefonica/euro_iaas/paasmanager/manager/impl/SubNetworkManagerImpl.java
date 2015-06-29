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

package com.telefonica.euro_iaas.paasmanager.manager.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.euro_iaas.paasmanager.dao.SubNetworkDao;
import com.telefonica.euro_iaas.paasmanager.manager.SubNetworkManager;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * Manager from subNetworks
 */
public class SubNetworkManagerImpl implements SubNetworkManager {

    private SubNetworkDao subNetworkDao = null;
    private static Logger log = LoggerFactory.getLogger(SubNetworkManagerImpl.class);

    /**
     * To create a network.
     * 
     * @throws AlreadyExistsEntityException
     *             , InfrastructureException, InvalidEntityException
     * @params claudiaData
     * @params network
     */
    public SubNetwork create(SubNetwork subNetwork) throws InvalidEntityException, AlreadyExistsEntityException {
        log.info("Create subnetwork " + subNetwork.getName());

        try {
            subNetwork = subNetworkDao.load(subNetwork.getName(), subNetwork.getVdc(), subNetwork.getRegion());
            throw new AlreadyExistsEntityException(subNetwork);

        } catch (EntityNotFoundException e1) {
            try {
                subNetwork = subNetworkDao.create(subNetwork);
            } catch (Exception e) {
                log.error("Error to create the subnetwork in BD " + e.getMessage());
                throw new InvalidEntityException(subNetwork);
            }
        }
        return subNetwork;
    }

    /**
     * To remove a subnetwork.
     * 
     * @params claudiaData
     * @params subNetwork
     */
    public void delete(SubNetwork subNetwork) throws EntityNotFoundException, InvalidEntityException {
        log.info("Destroying the subnetwork " + subNetwork.getName());
        try {
            subNetworkDao.remove(subNetwork);
        } catch (Exception e) {
            log.error("Error to remove the subnetwork in BD " + e.getMessage());
            throw new InvalidEntityException(subNetwork);
        }

    }

    /**
     * To obtain the list of subnetworks.
     * 
     * @return the subnetwork list
     */
    public List<SubNetwork> findAll() {
        return subNetworkDao.findAll();
    }

    /**
     * To obtain the subnetwork.
     * 
     * @param name
     * @param vdc
     * @param region
     * @return the network
     */
    public SubNetwork load(String name, String vdc, String region) throws EntityNotFoundException {
        return subNetworkDao.load(name, vdc, region);
    }

    public void setSubNetworkDao(SubNetworkDao subNetworkDao) {
        this.subNetworkDao = subNetworkDao;
    }

    /**
     * To update the subnetwork.
     * 
     * @param subNetwork
     * @return the subNetwork
     */
    public SubNetwork update(SubNetwork subNetwork) throws InvalidEntityException {
        return subNetworkDao.update(subNetwork);
    }

}
