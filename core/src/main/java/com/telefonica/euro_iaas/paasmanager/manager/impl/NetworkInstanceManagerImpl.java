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
import java.util.Set;

import org.apache.log4j.Logger;

import com.telefonica.euro_iaas.commons.dao.AlreadyExistsEntityException;
import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.claudia.NetworkClient;
import com.telefonica.euro_iaas.paasmanager.dao.NetworkInstanceDao;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.RouterManager;
import com.telefonica.euro_iaas.paasmanager.manager.SubNetworkInstanceManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.Port;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.euro_iaas.paasmanager.model.SubNetworkInstance;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

/**
 * @author henar
 */
public class NetworkInstanceManagerImpl implements NetworkInstanceManager {

    private NetworkInstanceDao networkInstanceDao = null;
    private NetworkClient networkClient = null;
    private SubNetworkInstanceManager subNetworkInstanceManager = null;
    private RouterManager routerManager = null;
    private SystemPropertiesProvider systemPropertiesProvider;

    private static Logger log = Logger.getLogger(NetworkInstanceManagerImpl.class);

    /**
     * To create a network.
     * 
     * @throws AlreadyExistsEntityException
     * @params claudiaData
     * @params network
     * @return NetworkInstance.class
     * @throws InvalidEntityException
     * @throws InfrastructureException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     * @throws InfrastructureException
     */
    public NetworkInstance create(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws InvalidEntityException, AlreadyExistsEntityException, EntityNotFoundException,
            InfrastructureException {
        log.debug("Create network instance " + networkInstance.getNetworkName() + " vdc " +  claudiaData.getVdc());
        

        if (exists(networkInstance.getNetworkName(), claudiaData.getVdc())) {
            networkInstance = networkInstanceDao.load(networkInstance.getNetworkName(), claudiaData.getVdc());
            log.debug("The network already exists");
        } else {
            networkClient.deployNetwork(claudiaData, networkInstance, region);
            log.debug("Network isntance " + networkInstance.getNetworkName() + " with vdc " + claudiaData.getVdc()+ ": " + networkInstance.getIdNetwork()
                    + " deployed");
            try {
                createSubNetworksInstance(claudiaData, networkInstance, region);
                networkClient.addNetworkToPublicRouter(claudiaData, networkInstance, region);
                log.debug("Storing network instance " + networkInstance.getNetworkName() + " vdc " +  claudiaData.getVdc());
                networkInstance = networkInstanceDao.create(networkInstance);
            } catch (InfrastructureException e) {
                log.warn("There is an error to deploy an subNet " + e.getMessage());
                restoreNetwork(claudiaData, networkInstance, region);
                throw new InfrastructureException("There is an error to deploy a subNet " + e.getMessage());
            }

        }
        return networkInstance;
    }
    
    /**
     * 
     * @param claudiaData
     * @param networkInstance
     * @throws InvalidEntityException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     * @throws InfrastructureException
     */
    public void createFederatedNetwork(ClaudiaData claudiaData, List<NetworkInstance> networkInstance)
        throws InvalidEntityException, AlreadyExistsEntityException, EntityNotFoundException,
        InfrastructureException {
        log.debug("Create federated network  for vdc " +  claudiaData.getVdc() + " with netwrks " + networkInstance.get(0).getNetworkName() + " and " +
        		networkInstance.get(1).getNetworkName());

        networkClient.joinNetworks(claudiaData, networkInstance.get(0), networkInstance.get(1));
    }
    
    public NetworkInstance createInDB (NetworkInstance networkInstance) throws InvalidEntityException, AlreadyExistsEntityException {
        return networkInstanceDao.create(networkInstance);
        
    }

    /**
     * It restore the situation if there is a failure.
     * 
     * @param claudiaData
     * @param networkInstance
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     * @throws InfrastructureException
     */
    private void restoreNetwork(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws EntityNotFoundException, InvalidEntityException, InfrastructureException {
        for (SubNetworkInstance subNet : networkInstance.getSubNets()) {
            if (subNetworkInstanceManager.isSubNetworkDeployed(claudiaData, subNet, region)) {
                subNetworkInstanceManager.delete(claudiaData, subNet, region);
            }
        }
        log.debug("Deleting the network");
        networkClient.destroyNetwork(claudiaData, networkInstance, region);

        networkInstance.setSubNets(null);
        networkInstanceDao.update(networkInstance);
        networkInstanceDao.remove(networkInstance);

    }

    /**
     * It creates a subnet in the network.
     */
    private void createSubNetworksInstance(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws InvalidEntityException, AlreadyExistsEntityException, InfrastructureException

    {
        Set<SubNetworkInstance> subNetAxu = networkInstance.cloneSubNets();
        for (SubNetworkInstance subNet : subNetAxu) {

            log.debug("SubNetwork " + subNet.getName() + " id net " + subNet.getIdNetwork());
            String cidr = getDefaultCidr(claudiaData,region);
            subNet.setIdNetwork(networkInstance.getIdNetwork());
            subNet.setCidr(cidr);
            networkInstance.updateSubNet(subNet);
            subNet = subNetworkInstanceManager.create(claudiaData, subNet, region);
            
            log.debug("SubNetwork " + subNet.getName() + " id net " + subNet.getIdNetwork() + " in network  "
                    + networkInstance.getNetworkName() + " deployed");
        }
    }

    /**
     * To remove a network.
     * 
     * @params claudiaData
     * @params network
     */
    public void delete(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws EntityNotFoundException, InvalidEntityException, InfrastructureException {
        log.debug("Destroying network " + networkInstance.getNetworkName());
        
        if (!canBeDeleted (claudiaData, networkInstance, region )) {
            return;
        }

        log.debug("Deleting the public interface interfaces");
        networkInstance = networkInstanceDao.load(networkInstance.getNetworkName(), claudiaData.getVdc());
        networkClient.deleteNetworkToPublicRouter(claudiaData, networkInstance, region);
        log.debug("Deleting the subnets");
        Set<SubNetworkInstance> subNetAux = networkInstance.cloneSubNets();
        networkInstance.getSubNets().clear();
        networkInstanceDao.update(networkInstance);
        for (SubNetworkInstance subNet : subNetAux) {
            subNetworkInstanceManager.delete(claudiaData, subNet, region);
        }
        log.debug("Deleting the network");
        networkClient.destroyNetwork(claudiaData, networkInstance, region);
        try {
            networkInstanceDao.remove(networkInstance);
        } catch (Exception e) {
            log.error("Error to remove the network in BD " + e.getMessage());
            throw new InvalidEntityException(networkInstance);
        }

    }
    
    /**
     * To remove a network.
     * 
     * @params claudiaData
     * @params network
     */
    public boolean canBeDeleted(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws EntityNotFoundException, InvalidEntityException, InfrastructureException {
        log.debug("Obtaining ports from network" + networkInstance.getNetworkName());

        List<Port> ports = networkClient.listPortsFromNetwork(claudiaData, region, networkInstance.getIdNetwork());
        if (ports.size()==0) {
            return true;
        }
        else {
            log.debug ("It is not possible to undeply the network since there are VMs associated to it");
            return false;
            
        }
    }

    /**
     * To obtain the list of networks.
     * 
     * @return the network list
     */
    public List<NetworkInstance> findAll() {
        return networkInstanceDao.findAll();
    }

    /**
     * To obtain the list of networks.
     * 
     * @return the network list
     */
    public boolean exists(String networkInstance, String vdc) {
        try {
            networkInstanceDao.load(networkInstance, vdc);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
    
    public List<NetworkInstance> listNetworks (ClaudiaData claudiaData, String region) throws InfrastructureException {
        List<NetworkInstance> networkInstances = networkClient.loadAllNetwork(claudiaData, region);
        return networkInstances;
    }

    /**
     * To obtain the network.
     * 
     * @param networkName
     * @return the network
     */
    public NetworkInstance load(String networkName, String vdc) throws EntityNotFoundException {
        return networkInstanceDao.load(networkName, vdc);
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public void setNetworkInstanceDao(NetworkInstanceDao networkInstanceDao) {
        this.networkInstanceDao = networkInstanceDao;
    }

    public void setRouterManager(RouterManager routerManager) {
        this.routerManager = routerManager;
    }

    public void setSubNetworkInstanceManager(SubNetworkInstanceManager subNetworkInstanceManager) {
        this.subNetworkInstanceManager = subNetworkInstanceManager;
    }

    /**
     * @param systemPropertiesProvider
     *            the systemPropertiesProvider to set
     */
    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    public NetworkInstance update(NetworkInstance networkInstance) throws InvalidEntityException {
        return networkInstanceDao.update(networkInstance);
    }

    public int getNumberDeployedNetwork(ClaudiaData claudiaData, String region) throws InfrastructureException {
        return networkClient.loadAllNetwork(claudiaData, region).size();
    }
    
    private String getDefaultCidr(ClaudiaData claudiaData,String region)
    throws InvalidEntityException, AlreadyExistsEntityException, InfrastructureException {
        int cidrOpenstack =1;
        if (!(claudiaData.getVdc() == null || claudiaData.getVdc().isEmpty())) {
        	cidrOpenstack = getNumberDeployedNetwork(claudiaData, region) + 1;
        }

        int cidrdb = this.findAll().size();
        int cidrCount = cidrdb+cidrOpenstack;

        return "10.0."+cidrCount+".0/24";
    }

}
