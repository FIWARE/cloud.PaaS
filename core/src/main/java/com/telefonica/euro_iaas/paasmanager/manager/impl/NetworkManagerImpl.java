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

import com.telefonica.euro_iaas.paasmanager.dao.NetworkDao;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkManager;
import com.telefonica.euro_iaas.paasmanager.manager.SubNetworkManager;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * Manager from networks
 */
public class NetworkManagerImpl implements NetworkManager {

    private NetworkDao networkDao = null;
    private SubNetworkManager subNetworkManager = null;
    private NetworkInstanceManager networkInstanceManager = null;

    private static Logger log = LoggerFactory.getLogger(NetworkManagerImpl.class);

    /**
     * To create a network.
     * 
     * @throws EntityNotFoundException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     * @throws AlreadyExistsEntityException
     * @throws InvalidEntityException
     * @throws InfrastructureException
     * @params claudiaData
     * @params network
     */
    public Network create(Network network) throws EntityNotFoundException, InvalidEntityException,
            AlreadyExistsEntityException {
        log.info("Create network " + network.getNetworkName());
        Network networkDB = null;

        if (network.getVdc() == null) {
            network.setVdc("");
        }
        if (exists(network.getNetworkName(), network.getVdc(), network.getRegion())) {

            networkDB = networkDao.load(network.getNetworkName(), network.getVdc(), network.getRegion());
            log.info("The network " + network.getNetworkName() + " already exists with subnets  "
                    + networkDB.getSubNets());

            if (networkDB.getSubNets().isEmpty()) {
                log.info("There is not a associated subnet");
                defineDefaultSubNetwork(networkDB);
            }

            for (SubNetwork subnet : network.getSubNets()) {
                if (!networkDB.contains(subnet)) {
                    log.info("The subnet " + subnet.getName() + " is not in the net " + networkDB.getNetworkName());
                    createSubNetwork(networkDB, subnet);
                }
            }

            networkDao.update(networkDB);

        } else {

            if (network.getSubNets().isEmpty()) {
                defineDefaultSubNetwork(network);
            }
            networkDB = networkDao.create(network);

        }

        return networkDB;
    }

    /**
     * It creates a subnet in the network.
     * 
     * @param network
     * @param subNetwork
     * @throws InvalidEntityException
     * @throws InfrastructureException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     * @throws InfrastructureException
     */
    public void createSubNetwork(Network network, SubNetwork subNetwork) throws InvalidEntityException,
            AlreadyExistsEntityException, EntityNotFoundException {
        log.info("Creating subnect " + subNetwork.getName());
        try {
            subNetwork = subNetworkManager.create(subNetwork);
        } catch (AlreadyExistsEntityException e) {
            subNetwork = subNetworkManager.load(subNetwork.getName(), subNetwork.getVdc(), subNetwork.getRegion());
        }
        network.updateSubNet(subNetwork);
        log.info("SubNetwork " + subNetwork.getName() + " in network  " + network.getNetworkName() + " deployed");
    }

    /**
     * It creates a subnet object in the network.
     * 
     * @param network
     * @throws AlreadyExistsEntityException
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     */
    private void defineDefaultSubNetwork(Network network) throws InvalidEntityException, AlreadyExistsEntityException,
            EntityNotFoundException {
        SubNetwork subNet = new SubNetwork("sub-net-" + network.getNetworkName(), network.getVdc(), network.getRegion());
        network.addSubNet(subNet);
    }

    /**
     * To remove a network.
     * 
     * @params network
     */
    public void delete(Network network) throws EntityNotFoundException, InvalidEntityException {
        log.info("Destroying network " + network.getNetworkName());

        try {
            networkDao.remove(network);
        } catch (Exception e) {
            log.error("Error to remove the network/subNetworks from BD " + e.getMessage());
            throw new InvalidEntityException(network);
        }

    }

    /**
     * To obtain the list of networks.
     * 
     * @return the network list
     */
    public List<Network> findAll() {
        return networkDao.findAll();
    }

    /**
     * To obtain the network.
     * 
     * @param networkName
     * @return the network
     */
    public Network load(String networkName, String vdc, String region) throws EntityNotFoundException {
        return networkDao.load(networkName, vdc, region);
    }

    public void setNetworkDao(NetworkDao networkDao) {
        this.networkDao = networkDao;
    }

    public void setSubNetworkManager(SubNetworkManager subNetworkManager) {
        this.subNetworkManager = subNetworkManager;
    }

    public void setNetworkInstanceManager(NetworkInstanceManager networkInstanceManager) {
        this.networkInstanceManager = networkInstanceManager;
    }

    /**
     * To update the network.
     * 
     * @param network
     * @return the network
     */
    public Network update(Network network) throws InvalidEntityException {
        return networkDao.update(network);
    }

    public Network update(Network network, Network network2) throws InvalidEntityException {
        network.setNetworkName(network2.getNetworkName());
        network.setVdc(network2.getVdc());
        return networkDao.update(network);
    }

    /**
     * It checks if the network already exists.
     */
    public boolean exists(String networkName, String vdc, String region) {
        try {
            networkDao.load(networkName, vdc, region);
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

}
