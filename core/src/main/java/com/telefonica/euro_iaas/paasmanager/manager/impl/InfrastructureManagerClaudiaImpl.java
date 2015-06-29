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

// import static com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider.VM_DEPLOYMENT_DELAY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import com.telefonica.euro_iaas.paasmanager.claudia.ClaudiaClient;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentInstanceDao;
import com.telefonica.euro_iaas.paasmanager.exception.ClaudiaRetrieveInfoException;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.Template;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.util.ClaudiaResponseAnalyser;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

public class InfrastructureManagerClaudiaImpl implements InfrastructureManager {

    private static final long POLLING_INTERVAL = 10000;

    private SystemPropertiesProvider systemPropertiesProvider;
    private ClaudiaClient claudiaClient;
    private ClaudiaResponseAnalyser claudiaResponseAnalyser;

    private TierInstanceManager tierInstanceManager;
    private TierManager tierManager;
    private EnvironmentInstanceDao environmentInstanceDao;
    private NetworkInstanceManager networkInstanceManager;
    private NetworkManager networkManager;

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(InfrastructureManagerClaudiaImpl.class);
    /** Max lenght of an OVF */
    private static final Integer tam_max = 90000;

    /*
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager# cloneTemplate(java.lang.String)
     */
    @Async
    public TierInstance cloneTemplate(String templateName) throws InfrastructureException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<VM> createEnvironment(EnvironmentInstance envInstance, String ovf, ClaudiaData claudiaData)
            throws InfrastructureException {
        // TODO Auto-generated method stub
        return null;
    }

    public EnvironmentInstance createInfrasctuctureEnvironmentInstance(EnvironmentInstance environmentInstance,
            Set<Tier> tiers, ClaudiaData claudiaData) throws InfrastructureException, InvalidEntityException,
            EntityNotFoundException, AlreadyExistsEntityException {

        // Deploy MVs
        log.info("Creating infrastructure for environment instance " + environmentInstance.getBlueprintName());

        for (Tier tier : tiers) {
            for (int numReplica = 1; numReplica <= tier.getInitialNumberInstances(); numReplica++) {
                // claudiaData.setVm(tier.getName());
                log.info("Deploying tier instance for tier " + tier.getName() + " " + tier.getRegion());
                Tier tierDB = tierManager.loadTierWithProductReleaseAndMetadata(tier.getName(),
                        tier.getEnviromentName(), tier.getVdc());

                TierInstance tierInstance = new TierInstance();
                String name = generateVMName(environmentInstance.getBlueprintName(), tier.getName(), numReplica,
                        claudiaData.getVdc());

                tierInstance.setName(name);
                tierInstance.setNumberReplica(numReplica);
                tierInstance.setVdc(claudiaData.getVdc());
                tierInstance.setStatus(Status.DEPLOYING);
                tierInstance.setTier(tierDB);
                VM vm = new VM();
                String fqn = claudiaData.getOrg().replace("_", ".") + ".customers." + claudiaData.getVdc()
                        + ".services." + claudiaData.getService() + ".vees." + tier.getName() + ".replicas."
                        + numReplica;

                String hostname = generateVMName(claudiaData.getService(), tier.getName(), numReplica,
                        claudiaData.getVdc()).toLowerCase();
                log.info("fqn " + fqn + " hostname " + hostname);
                vm.setFqn(fqn);
                vm.setHostname(hostname);
                tierInstance.setVM(vm);

                log.info("Deploy networks if required");

                log.info("Deploying tier instance for tier " + tier.getName() + " " + tier.getRegion());
                this.deployNetworks(claudiaData, tierInstance);
                log.info("Number of networks " + tierInstance.getNetworkInstances().size() + " floatin ip "
                        + tierInstance.getTier().getFloatingip());

                log.info("Inserting in database ");
                tierInstance = insertTierInstanceBD(claudiaData, environmentInstance.getEnvironment().getName(),
                        tierInstance);
                log.info("Return: Number of networks " + tierInstance.getNetworkInstances().size() + " floating ip "
                        + tierInstance.getTier().getFloatingip());
                environmentInstance.addTierInstance(tierInstance);
                environmentInstanceDao.update(environmentInstance);

                try {
                    tierInstanceManager.update(claudiaData, environmentInstance.getEnvironment().getName(),
                            tierInstance);
                } catch (Exception e) {
                    log.error("Error deploying a VM: " + e.getMessage());
                    environmentInstance.setStatus(Status.ERROR);
                    throw new InfrastructureException(e.getMessage());
                }

                log.info("Tier instance name " + environmentInstance.getBlueprintName() + "-" + tier.getName() + "-"
                        + numReplica);
                deployVM(claudiaData, tierInstance, numReplica, vm);

                tierInstance.setVM(vm);

                try {
                    log.info("Inserting in database ");
                    // tierInstance = insertTierInstanceBD(tierInstance);
                    tierInstance.setStatus(Status.DEPLOYED);
                    tierInstanceManager.update(claudiaData, environmentInstance.getEnvironment().getName(),
                            tierInstance);
                } catch (EntityNotFoundException e) {
                    log.info("Entitiy NOt found: Tier " + tierInstance.getTier().getName() + " " + e.getMessage());
                    throw new InfrastructureException(e);
                } catch (InvalidEntityException e) {
                    throw new InfrastructureException(e);
                } catch (AlreadyExistsEntityException e) {
                    throw new InfrastructureException(e);
                } catch (Exception e) {
                    throw new InfrastructureException(e);
                }

            }
        }
        return environmentInstance;
    }

    /**
     * Generates a instance tier name or hostname.
     */
    public String generateVMName(String service, String tierName, int numReplica, String vdc) {

        int lengthVdc = vdc.length();
        String name = service + "-" + tierName + "-" + numReplica + "-";
        if (lengthVdc > 6) {

            name += vdc.substring(lengthVdc - 6);
        } else {

            name += vdc;
        }
        return name;
    }

    @Async
    public Template createTemplate(TierInstance tierInstance) throws InfrastructureException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager# deleteEnvironment
     * (com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance)
     */
    public void deleteEnvironment(ClaudiaData claudiaData, EnvironmentInstance envInstance)
            throws InfrastructureException, InvalidEntityException, EntityNotFoundException {
        log.info("Delete environment " + envInstance.getBlueprintName());
        List<TierInstance> tierInstances = envInstance.getTierInstances();

        if (tierInstances == null)
            return;
        for (int i = 0; i < tierInstances.size(); i++) {
            TierInstance tierInstance = tierInstances.get(i);
            tierInstance = tierInstanceManager.loadNetworkInstnace(tierInstance.getName());
            boolean exists = claudiaClient.existsVMReplica(claudiaData, tierInstance.getName(), tierInstance.getVM(),
                    tierInstance.getTier().getRegion());
            if (exists) {
                claudiaClient.undeployVMReplica(claudiaData, tierInstance);
            }
        }

    }

    private List<NetworkInstance> getNetworkInstInEnv(TierInstance tierInstance) throws InvalidEntityException,
            EntityNotFoundException {
        List<NetworkInstance> netInst = new ArrayList<NetworkInstance>();
        // for (TierInstance tierInstance : envInstance.getTierInstances()) {
        Set<NetworkInstance> netInts = tierInstance.cloneNetworkInt();
        tierInstance.getNetworkInstances().clear();
        tierInstanceManager.update(tierInstance);
        for (NetworkInstance net : netInts) {
            if (!netInst.contains(net)) {
                netInst.add(net);
            }
        }
        // }
        return netInst;
    }

    /**
     * it deletes the networks from the environment if they are not used.
     * 
     * @param claudiaData
     * @param tierInstance
     * @throws InvalidEntityException
     * @throws InfrastructureException
     * @throws EntityNotFoundException
     */
    @Override
    public void deleteNetworksInTierInstance(ClaudiaData claudiaData, TierInstance tierInstance)
            throws InvalidEntityException, InfrastructureException, EntityNotFoundException {
        log.info("Delete the networks in env if there are not being used");

        List<NetworkInstance> netInsts = null;
        try {
            netInsts = getNetworkInstInEnv(tierInstance);
        } catch (EntityNotFoundException e) {
            throw new InfrastructureException("It is not possible to find the network " + e.getMessage());
        }

        tierInstance.getNetworkInstances().clear();
        tierInstanceManager.update(tierInstance);
        for (NetworkInstance network : netInsts) {
            log.info("Is network default? " + network.isDefaultNet());

            if (!network.isDefaultNet()) {
                try {
                    networkInstanceManager.delete(claudiaData, network, network.getRegionName());
                } catch (Exception e) {
                    log.warn("It is not possible to delete  the network " + network.getNetworkName() + " "
                            + network.getRegionName() + " " + e.getMessage());
                }
            }
        }
    }

    /**
     * It deletes the VM.
     * 
     * @param claudiaData
     * @param tierInstance
     * @throws InfrastructureException
     */
    public void deleteVMReplica(ClaudiaData claudiaData, TierInstance tierInstance) throws InfrastructureException {
        claudiaClient.undeployVMReplica(claudiaData, tierInstance);

    }

    /**
     * It desploys the VM.
     * 
     * @param claudiaData
     * @param tierInstance
     * @param replica
     * @param vm
     * @throws InfrastructureException
     */
    public void deployVM(ClaudiaData claudiaData, TierInstance tierInstance, int replica, VM vm)
            throws InfrastructureException {

        log.info("Deploy VM for tier " + tierInstance.getTier().getName() + " with networks "
                + tierInstance.getNetworkInstances().size() + " and public ip "
                + tierInstance.getTier().getFloatingip());

        claudiaClient.deployVM(claudiaData, tierInstance, replica, vm);

        List<String> ips = claudiaClient.getIP(claudiaData, tierInstance.getTier().getName(), replica, vm, tierInstance
                .getTier().getRegion());
        if (ips != null) {
            for (String ip : ips) {
                log.info("Ip " + ip);
            }
        } else {
            log.warn("ips null");
        }
        log.info("VM ");

        vm.setIp(ips.get(0));
    }

    /**
     * @param claudiaData
     * @param tierInstance
     * @return
     * @throws InfrastructureException
     */
    public String ImageScalability(ClaudiaData claudiaData, TierInstance tierInstance) throws InfrastructureException {
        log.info("Image scalability ");

        String scaleResponse;
        try {
            scaleResponse = claudiaClient.createImage(claudiaData, tierInstance);
        } catch (ClaudiaRetrieveInfoException e) {
            String errorMessage = "Error creating teh image of the VM with the " + "fqn: "
                    + tierInstance.getVM().getFqn() + ". Descrption. " + e.getMessage();
            log.error(errorMessage);
            throw new InfrastructureException(errorMessage);
        }
        return scaleResponse;
    }

    /**
     * It deploys the required networks.
     * 
     * @param data
     * @param tierInstance
     * @throws InvalidEntityException
     * @throws InfrastructureException
     * @throws EntityNotFoundException
     */
    public void deployNetworks(ClaudiaData data, TierInstance tierInstance) throws InvalidEntityException,
            InfrastructureException, EntityNotFoundException, AlreadyExistsEntityException {
        Tier tier = tierInstance.getTier();
        tier = tierManager.loadTierWithNetworks(tier.getName(), data.getVdc(), tier.getEnviromentName());
        // Creating networks...
        log.info("Deploying network for tier instance " + tierInstance.getName() + " " + tier.getNetworks().size()
                + " region " + tier.getRegion());
        List<Network> networkToBeDeployed = new ArrayList<Network>();
        for (Network network : tier.getNetworks()) {
            log.info("Network to be added " + network.getNetworkName());
            if (network.getNetworkName().equals("Internet")) {

                tier.setFloatingip("true");
                tier = tierManager.update(tier);
                tierInstance.update(tier);
            } else {
                networkToBeDeployed.add(network);
            }

        }

        for (Network network : networkToBeDeployed) {
            log.info("Network instance to be deployed: " + network.getNetworkName() + " vdc " + data.getVdc() + " "
                    + network.getRegion() + " and federated " + network.getfederatedNetwork());
            network = networkManager.load(network.getNetworkName(), data.getVdc(), network.getRegion());
            NetworkInstance networkInst = network.toNetworkInstance();
            log.info("Network instance to be deployed: " + network.getNetworkName() + " vdc " + data.getVdc()
                    + " region " + networkInst.getRegionName() + " and networkInst " + network.getfederatedNetwork());

            if (networkInstanceManager.exists(data, networkInst, tier.getRegion())) {
                log.info("the network inst " + networkInst.getNetworkName() + " already exists");
                networkInst = networkInstanceManager.load(networkInst.getNetworkName(), data, tier.getRegion());
            } else {
                try {
                    log.info("the network inst " + networkInst.getNetworkName() + " do not exists");
                    networkInst = networkInstanceManager.create(data, networkInst, tierInstance.getTier().getRegion());
                } catch (AlreadyExistsEntityException e2) {
                    throw new InvalidEntityException(network);
                } catch (InfrastructureException e) {
                    String mens = "Error to deploy a network " + network.getNetworkName() + " :" + e.getMessage();
                    throw new InfrastructureException(mens);
                }
            }
            log.info("Adding network to tier isntance " + networkInst.getNetworkName());
            tierInstance.addNetworkInstance(networkInst);
        }
    }

    private TierInstance insertTierInstanceBD(ClaudiaData claudiaData, String envName, TierInstance tierInstance)
            throws EntityNotFoundException, InvalidEntityException, AlreadyExistsEntityException,
            InfrastructureException {

        log.info("Inserting in database for tier instance " + tierInstance.getName() + " "
                + tierInstance.getNetworkInstances().size() + " " + tierInstance.getTier().getName() + " "
                + tierInstance.getTier().getFloatingip());
        TierInstance tierInstanceDB = null;

        try {
            tierInstanceDB = tierInstanceManager.load(tierInstance.getName());
            log.warn("the tier already exists");
        } catch (EntityNotFoundException e) {
            tierInstanceDB = tierInstanceManager.create(claudiaData, envName, tierInstance);

        }
        return tierInstanceDB;
    }

    /**
     * Introducing some delay after vm deployment (only for old claudia)
     * 
     * @param delay
     * @throws InfrastructureException
     */
    private void introduceDelay(Long delay) throws InfrastructureException {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            String errorThread = "Thread Interrupted Exception " + "during delay after vm deployment";
            log.warn(errorThread);
            throw new InfrastructureException(errorThread);
        }
    }

    /**
     * @param claudiaClient
     *            the claudiaClient to set
     */

    public void setClaudiaClient(ClaudiaClient claudiaClient) {
        this.claudiaClient = claudiaClient;
    }

    public void setClaudiaResponseAnalyser(ClaudiaResponseAnalyser claudiaResponseAnalyser) {
        this.claudiaResponseAnalyser = claudiaResponseAnalyser;
    }

    public void setEnvironmentInstanceDao(EnvironmentInstanceDao environmentInstanceDao) {
        this.environmentInstanceDao = environmentInstanceDao;
    }

    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    public void setTierInstanceManager(TierInstanceManager tierInstanceManager) {
        this.tierInstanceManager = tierInstanceManager;
    }

    public String StartStopScalability(ClaudiaData claudiaData, boolean b) throws InfrastructureException {
        String scalalility = claudiaClient.onOffScalability(claudiaData, claudiaData.getService(), b);
        return scalalility;

    }

    public void setNetworkInstanceManager(NetworkInstanceManager networkInstanceManager) {
        this.networkInstanceManager = networkInstanceManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setTierManager(TierManager tierManager) {
        this.tierManager = tierManager;
    }

    public String getFederatedRange(ClaudiaData data, String region) throws InfrastructureException {

        int cidrdb = networkInstanceManager.findAll().size();
        int cidrOpenstack = networkInstanceManager.getNumberDeployedNetwork(data, region);
        int cidrCount = cidrdb + cidrOpenstack;
        return "155.0." + cidrCount;
    }

    public void federatedNetworks(ClaudiaData data, EnvironmentInstance environmentInstance)
            throws InfrastructureException {
        log.info("Federate networks in the enviornment");
        // Get the networks to be federated

        Set<String> federatedNetworks = environmentInstance.getEnvironment().getFederatedNetworks();
        Map<String, Set<String>> relation = environmentInstance.getEnvironment().getNetworksRegion();
        List<NetworkInstance> networkInstances = new ArrayList<NetworkInstance>();

        for (String net : federatedNetworks) {
            log.info("Network in the federated network " + net);
            Set<String> regions = relation.get(net);
            log.info("regions " + regions);

            for (String region : regions) {
                log.info("region " + region);
                NetworkInstance netInstance = environmentInstance.getNetworkInstanceFromNetwork(net, region);
                log.info("net  " + netInstance.getNetworkName() + " " + netInstance.getIdNetwork() + " for region "
                        + region);
                if (netInstance != null) {
                    networkInstances.add(netInstance);
                }
            }

        }
        try {
            networkInstanceManager.joinNetwork(data, networkInstances.get(0), networkInstances.get(1));
        }

        catch (Exception e) {
            String mens = "Error federating networks  :" + e.getMessage();
            throw new InfrastructureException(mens);
        }

    }

}
