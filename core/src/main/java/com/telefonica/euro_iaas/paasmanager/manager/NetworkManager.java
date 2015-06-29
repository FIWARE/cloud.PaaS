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

package com.telefonica.euro_iaas.paasmanager.manager;

import java.util.List;

import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * @author henar
 */
public interface NetworkManager {
    /**
     * Create a network.
     * 
     * @param network
     * @return the tierInstance created
     * @throws InfrastructureException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     */
    Network create(Network network) throws EntityNotFoundException, InvalidEntityException,
            AlreadyExistsEntityException;

    /**
     * Delete a Network.
     * 
     * @param network
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     * @throws InfrastructureException
     */
    void delete(Network network) throws EntityNotFoundException, InvalidEntityException;

    /**
     * Retrieve all Network created in the system.
     * 
     * @return the existent networks.
     */
    List<Network> findAll();

    /**
     * Load the network.
     * 
     * @return the network.
     */
    Network load(String networkName, String vdc, String region) throws EntityNotFoundException;

    /**
     * If the network exists in DB.
     * 
     * @return .
     */
    boolean exists(String networkName, String vdc, String region);

    /**
     * Update a network.
     * 
     * @param network
     * @return the tierInstance created
     */
    Network update(Network network) throws InvalidEntityException;

    /**
     * Create subNetwork from network.
     * 
     * @param network
     * @param subNetwork
     * @throws InvalidEntityException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     */
    void createSubNetwork(Network network, SubNetwork subNetwork) throws InvalidEntityException,
            AlreadyExistsEntityException, EntityNotFoundException;

}
