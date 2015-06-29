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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentInstanceDao;
import com.telefonica.euro_iaas.paasmanager.dao.SecurityGroupDao;
import com.telefonica.euro_iaas.paasmanager.dao.TierDao;
import com.telefonica.euro_iaas.paasmanager.dao.TierInstanceDao;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidOVFException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidProductInstanceRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidVappException;
import com.telefonica.euro_iaas.paasmanager.exception.NotUniqueResultException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.installator.ProductInstallator;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentManager;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.SecurityGroupManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.SecurityGroup;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.EnvironmentInstanceSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

public class EnvironmentInstanceManagerImpl implements EnvironmentInstanceManager {

    private EnvironmentInstanceDao environmentInstanceDao;
    private TierInstanceDao tierInstanceDao;
    private SystemPropertiesProvider systemPropertiesProvider;

    private ProductInstanceManager productInstanceManager;
    private EnvironmentManager environmentManager;
    private InfrastructureManager infrastructureManager;
    private TierInstanceManager tierInstanceManager;
    private NetworkManager networkManager;
    private TierManager tierManager;
    private ProductReleaseManager productReleaseManager;
    private ProductInstallator productInstallator;
    private SecurityGroupManager securityGroupManager;
    private TierDao tierDao;
    private SecurityGroupDao securityGroupDao;

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(EnvironmentInstanceManagerImpl.class);

    /** Max lenght of an OVF */
    private static final Integer tam_max = 90000;

    /**
     * It filter some environment instances.
     * 
     * @param criteria
     *            the search criteria
     * @return
     */
    public List<EnvironmentInstance> findByCriteria(EnvironmentInstanceSearchCriteria criteria) {

        return environmentInstanceDao.findByCriteria(criteria);
    }

    /**
     * It returns all environment instances.
     * 
     * @return
     */
    public List<EnvironmentInstance> findAll() {
        return environmentInstanceDao.findAll();
    }

    /**
     * It creastes the environment instance including hardware and software.
     * 
     * @param claudiaData
     * @param environmentInstance
     * @return
     * @throws AlreadyExistsEntityException
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     * @throws InvalidVappException
     * @throws InvalidOVFException
     * @throws InfrastructureException
     * @throws ProductInstallatorException
     */
    public EnvironmentInstance create(ClaudiaData claudiaData, EnvironmentInstance environmentInstance)
            throws AlreadyExistsEntityException, InvalidEntityException, EntityNotFoundException,
            InfrastructureException, ProductInstallatorException {

        Environment environment = insertEnvironmentInDatabase(claudiaData, environmentInstance.getEnvironment());

        if (environmentInstance.getEnvironment().getOvf() != null)
            environment.setOvf(environmentInstance.getEnvironment().getOvf());

        environmentInstance.setName(environmentInstance.getVdc() + "-" + environment.getName());

        // with this set we loose the productRelease Attributes
        environmentInstance.setEnvironment(environment);
        environmentInstance.setStatus(Status.INIT);

        environmentInstance = insertEnvironmentInstanceInDatabase(environmentInstance);

        environmentManager.loadNetworks(environment);
        if (environment.isNetworkFederated()) {
            try {
                updateFederatedNetworks(claudiaData, environment);
            } catch (Exception e) {
                log.warn("It is not possible to update the federated networks");
            }
        }

        log.info("Creating the infrastructure");
        environmentInstance.setStatus(Status.DEPLOYING);
        environmentInstanceDao.update(environmentInstance);

        try {
            environmentInstance = infrastructureManager.createInfrasctuctureEnvironmentInstance(environmentInstance,
                    environment.getTiers(), claudiaData);

        } catch (Exception e) {
            environmentInstance.setStatus(Status.ERROR);
            environmentInstanceDao.update(environmentInstance);
            throw new InfrastructureException(e.getMessage());
        }

        environmentInstance.setStatus(Status.INSTALLING);
        environmentInstanceDao.update(environmentInstance);

        log.info("Installing software");
        boolean bScalableEnvironment;
        try {
            bScalableEnvironment = installSoftwareInEnvironmentInstance(claudiaData, environmentInstance);
        } catch (ProductInstallatorException e) {
            environmentInstance.setStatus(Status.ERROR);
            environmentInstanceDao.update(environmentInstance);
            throw new ProductInstallatorException(e);
        } catch (InvalidProductInstanceRequestException e) {
            environmentInstance.setStatus(Status.ERROR);
            environmentInstanceDao.update(environmentInstance);
            throw new ProductInstallatorException(e);
        }

        log.info("Is the environment federated? ");
        if (environment.isNetworkFederated()) {
            try {
                log.info("Federating networks");
                infrastructureManager.federatedNetworks(claudiaData, environmentInstance);
            } catch (Exception e) {
                environmentInstance.setStatus(Status.ERROR);
                environmentInstanceDao.update(environmentInstance);
                log.error("Error federating the networks " + e.getMessage());
                throw new InfrastructureException(e);
            }
        }

        environmentInstance.setStatus(Status.INSTALLED);
        environmentInstanceDao.update(environmentInstance);

        infrastructureManager.StartStopScalability(claudiaData, bScalableEnvironment);

        log.info("Environment Instance installed correctly");

        return environmentInstance;
    }

    /**
     * It updates the networks federated.
     * 
     * @param claudiaData
     * @param environment
     * @throws InfrastructureException
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     */
    public void updateFederatedNetworks(ClaudiaData claudiaData, Environment environment)
            throws InfrastructureException, EntityNotFoundException, InvalidEntityException {
        log.info(" Update the federated network ");
        Set<String> fedeNetwork = environment.getFederatedNetworks();
        String range = null;

        Map<String, Set<String>> map = environment.getNetworksRegion();

        for (String net : fedeNetwork) {
            log.info("Updating tier for net " + net);
            Set<String> regions = map.get(net);
            for (String region : regions) {
                log.info("Updating tier for net " + net + " a region " + region);
                Network network = networkManager.load(net, claudiaData.getVdc(), region);
                network.setFederatedNetwork(true);
                if (range == null) {
                    range = infrastructureManager.getFederatedRange(claudiaData, region);
                    log.info("Updating tier for net " + net + " a region " + region + " " + range);
                    network.setFederatedRange(range + ".0/26");
                    log.info(" Federate range " + range + ".0/26");
                } else {
                    network.setFederatedRange(range + ".64/26");
                    log.info(" Federate range " + range + ".64/26");
                }
                networkManager.update(network);
            }

        }
    }

    /**
     * It installs the software in the environment.
     * 
     * @param claudiaData
     * @param environmentInstance
     * @return
     * @throws ProductInstallatorException
     * @throws InvalidProductInstanceRequestException
     * @throws NotUniqueResultException
     * @throws InfrastructureException
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     */
    public boolean installSoftwareInEnvironmentInstance(ClaudiaData claudiaData, EnvironmentInstance environmentInstance)
            throws ProductInstallatorException, InvalidProductInstanceRequestException, InfrastructureException,
            InvalidEntityException, EntityNotFoundException {
        // TierInstance by TierInstance let's check if we have to install
        // software
        boolean bScalableEnvironment = false;

        for (TierInstance tierInstance : environmentInstance.getTierInstances()) {
            log.info("Install software in tierInstance " + tierInstance.getName() + " from tier "
                    + tierInstance.getTier().getName());
            // check if the tier is scalable
            boolean state = checkScalability(tierInstance.getTier());
            if (!bScalableEnvironment) {
                bScalableEnvironment = (state) ? true : false;
            }
            tierInstance.setStatus(Status.INSTALLING);
            tierInstanceDao.update(tierInstance);
            String newOVF = " ";
            Tier tier = tierManager.loadTierWithProductReleaseAndMetadata(tierInstance.getTier().getName(),
                    tierInstance.getTier().getEnviromentName(), tierInstance.getTier().getVdc());
            log.info("The tier " + tier.getName() + " is in bd " + tier.getRegion());

            if ((tier.getProductReleases() != null) && !(tier.getProductReleases().isEmpty())) {

                for (ProductRelease productRelease : tier.getProductReleases()) {

                    log.info("Install software " + productRelease.getProduct() + " " + productRelease.getVersion()
                            + " " + productRelease.getName());

                    productRelease = productReleaseManager.load(productRelease.getName(), claudiaData);

                    log.info("Install software " + productRelease.getProduct() + " " + productRelease.getVersion());

                    try {
                        ProductInstance productInstance = productInstanceManager.install(tierInstance, claudiaData,
                                environmentInstance, productRelease);
                        log.info("Adding product instance " + productInstance.getName());
                        tierInstance.setStatus(Status.INSTALLED);
                        tierInstance.addProductInstance(productInstance);
                    } catch (ProductInstallatorException pie) {
                        String message = " Error installing product " + productRelease.getName() + " "
                                + pie.getMessage();
                        tierInstance.setStatus(Status.ERROR);
                        tierInstanceDao.update(tierInstance);
                        log.error(message);
                        throw new ProductInstallatorException(message, pie);
                    }
                }

                if (state) {

                    if (tierInstance.getNumberReplica() == 1) {
                        log.info("Setup scalability ");
                        String image_Name = infrastructureManager.ImageScalability(claudiaData, tierInstance);
                    }

                    if (tierInstance.getNumberReplica() > 1) {
                        log.info("Updating OVF replica more than 1 ");
                        if (!newOVF.equals(" ")) {
                            tierInstance.setOvf(newOVF);
                        }
                    }
                }

                tierInstance.setStatus(Status.INSTALLED);
                tierInstanceDao.update(tierInstance);
            }
        }
        return bScalableEnvironment;
    }

    private boolean checkScalability(Tier tier) {
        boolean state;
        if (tier.getMaximumNumberInstances() > tier.getMinimumNumberInstances()) {
            state = true;
        } else {
            state = false;
        }
        return state;
    }

    public EnvironmentInstance load(String vdc, String name) throws EntityNotFoundException {
        try {
            return environmentInstanceDao.load(name, vdc);
        } catch (Exception e) {
            log.info("error to find environment instance " + e.getMessage());
            throw new EntityNotFoundException(EnvironmentInstance.class, "vdc", vdc);
        }
    }

    public EnvironmentInstance update(EnvironmentInstance envInst) throws InvalidEntityException {
        try {
            return environmentInstanceDao.update(envInst);
        } catch (Exception e) {
            log.error("It is not possible to update the environment " + envInst.getName() + " : " + e.getMessage());
            throw new InvalidEntityException(EnvironmentInstance.class, e);

        }
    }

    /**
     * It destroy the environment.
     * 
     * @param claudiaData
     * @param envInstance
     * @throws Exception
     */
    public void destroy(ClaudiaData claudiaData, EnvironmentInstance envInstance) throws Exception {
        log.info("Destroying environment instance " + envInstance.getBlueprintName() + " with environment "
                + envInstance.getEnvironment().getName() + " vdc " + envInstance.getVdc());
        boolean error = false;
        try {
            envInstance.setStatus(Status.UNINSTALLING);
            envInstance = environmentInstanceDao.update(envInstance);
            for (int i = 0; i < envInstance.getTierInstances().size(); i++) {
                deleteVM(claudiaData, envInstance);
            }

        } catch (NullPointerException ne) {
            log.info("Environment Instance " + envInstance.getBlueprintName()
                    + " does not have any TierInstance associated");
        }

        List<TierInstance> tierInstancesSDC = envInstance.getTierInstances();
        envInstance.setTierInstances(null);

        for (int i = 0; i < tierInstancesSDC.size(); i++) {
            // delete data on SDC
            TierInstance tierInstance = tierInstancesSDC.get(i);
            try {
                deleteTierOnSDC(claudiaData, tierInstance, error);
            } catch (Exception e) {
                String errorMsg = "Error deleting node from Node Manager : " + tierInstance.getVM().getFqn() + "    "
                        + e.getMessage();
                log.error(errorMsg);
                error = true;
            }

            envInstance.setStatus(Status.UNINSTALLED);
            envInstance = environmentInstanceDao.update(envInstance);

            deletePaaSDB(claudiaData, envInstance, tierInstance, error);

        }
        log.info("Environment Instance " + envInstance.getBlueprintName() + " DESTROYED");
        envInstance.setStatus(Status.UNDEPLOYED);
        environmentInstanceDao.remove(envInstance);

        if (error) {
            String errorMsg = "Unexpected error destroying  environmentInstance";
            throw new Exception(errorMsg);
        }

    }

    private void deletePaaSDB(ClaudiaData claudiaData, EnvironmentInstance envInstance, TierInstance tierInstance,
            boolean error) throws InvalidEntityException, InfrastructureException {
        // Borrado del registro en BBDD paasmanager
        log.info("Deleting the environment instance " + envInstance.getBlueprintName() + " in the database ");

        envInstance = environmentInstanceDao.update(envInstance);
        try {
            infrastructureManager.deleteNetworksInTierInstance(claudiaData, tierInstance);
        } catch (Exception e) {
            log.error(e.getMessage());
            error = true;
        } finally {
            // Deleting SG
            log.info("Deleting security group from tierInstance " + tierInstance.getName() + " in TierInstance");
            SecurityGroup secGroup = tierInstance.getSecurityGroup();
            if (secGroup != null) {
                SecurityGroup securityGroup = null;
                try {
                    securityGroup = securityGroupDao.loadWithRules(secGroup.getName());
                } catch (EntityNotFoundException e1) {
                    String msg = "SecurityGroup is not present in database " + securityGroup.getName();
                    log.error(msg);
                    e1.printStackTrace();
                    throw new InvalidEntityException(msg);
                }
                log.info("Deleting security group " + securityGroup.getName() + " associated to tierInstance "
                        + tierInstance.getName());
                tierInstance.setSecurityGroup(null);
                tierInstanceDao.update(tierInstance);
                securityGroupManager.destroy(tierInstance.getTier().getRegion(), claudiaData.getUser().getToken(),
                        tierInstance.getVdc(), securityGroup);
            }
            tierInstanceManager.remove(tierInstance);
        }
    }

    // PRVATE METHODS

    private void deleteVM(ClaudiaData claudiaData, EnvironmentInstance envInstance) throws InvalidEntityException {
        // Borrado de VMs
        try {
            log.info("Deleting Virtual Machines for environment instance " + envInstance.getBlueprintName());
            envInstance.setStatus(Status.UNDEPLOYING);
            envInstance = environmentInstanceDao.update(envInstance);

            infrastructureManager.deleteEnvironment(claudiaData, envInstance);

        } catch (Exception e) {
            log.error("It is not possible to delete the environment " + envInstance.getName() + " : " + e.getMessage());
            throw new InvalidEntityException(EnvironmentInstance.class, e);
        }

    }

    private void deleteTierOnSDC(ClaudiaData claudiaData, TierInstance tierInstance, boolean error) throws Exception {
        log.info("Deleting node " + tierInstance.getVM().getHostname());
        tierInstance.setStatus(Status.UNINSTALLING);
        tierInstanceDao.update(tierInstance);
        try {

            productInstallator.deleteNode(claudiaData, tierInstance.getVdc(), tierInstance.getVM().getHostname());

            tierInstance.setStatus(Status.UNINSTALLED);
            tierInstanceDao.update(tierInstance);
        } catch (Exception e) {
            throw e;
        }

    }

    private Environment insertEnvironmentInDatabase(ClaudiaData claudiaData, Environment env)
            throws InvalidEntityException, EntityNotFoundException {
        log.info("Insert Environment from User into the database");
        Environment environment = null;
        if (env.getVdc() == null) {
            env.setVdc(claudiaData.getVdc());
        }
        if (env.getOrg() == null) {
            env.setOrg(claudiaData.getOrg());
        }

        if (systemPropertiesProvider.getProperty(SystemPropertiesProvider.CLOUD_SYSTEM).equals("FIWARE")) {
            try {
                environment = environmentManager.load(env.getName(), env.getVdc());
                log.info("after obtain environment");

                Set<Tier> tiers = new HashSet();
                for (Tier tier : env.getTiers()) {
                    tier.setVdc(env.getVdc());
                    Tier tierDB = tierManager.loadComplete(tier);
                    log.info("tier " + tier.getName() + " " + env.getVdc() + " " + tier.getRegion());
                    log.info("tierDB " + tierDB.getName() + " " + env.getVdc() + " " + tierDB.getRegion());
                    tierDB = updateTierDB(tierDB, tier);
                    tierDB = tierManager.update(tierDB);

                    List<ProductRelease> pReleases = new ArrayList<ProductRelease>();
                    List<ProductRelease> productReleases = tier.getProductReleases();

                    for (ProductRelease pRelease : productReleases) {
                        if (pRelease != null) {
                            ProductRelease pReleaseDB = productReleaseManager.load(pRelease.getProduct() + "-"
                                    + pRelease.getVersion(), claudiaData);
                            pReleaseDB = updateProductReleaseDB(pReleaseDB, pRelease);
                            pReleaseDB = productReleaseManager.update(pReleaseDB);
                            pReleases.add(pReleaseDB);
                        }
                    }
                    tierDB.setProductReleases(null);
                    tierDB.setProductReleases(pReleases);
                    tiers.add(tierDB);
                }
                environment.setTiers(null);
                environment.setTiers(tiers);

                return environment;
            } catch (Exception e1) {
                log.warn("Error loading environment " + e1.getMessage());
                throw new EntityNotFoundException(Environment.class,
                        "The environment should have been already created", e1);
            }
        }

        try {
            environment = environmentManager.load(env.getName(), env.getVdc());
            if (environment.getOvf() == null && env.getOvf() != null) {
                environment.setOvf(env.getOvf());
                environment = environmentManager.update(environment);
            }
            return environment;
        } catch (EntityNotFoundException e1) {
            try {
                environment = environmentManager.create(claudiaData, env);
            } catch (InvalidEnvironmentRequestException e) {
                // TODO Auto-generated catch block
                String errorMessage = " Error creating the environment. " + environment.getName() + ". Desc: "
                        + e.getMessage();
                log.error(errorMessage);
                throw new InvalidEntityException(Environment.class, e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                String errorMessage = " Error to creating the environment. " + environment.getName() + ". Desc: "
                        + e.getMessage();
                log.error(errorMessage);
                throw new InvalidEntityException(Environment.class, e);
            }
        }
        return environment;
    }

    private EnvironmentInstance insertEnvironmentInstanceInDatabase(EnvironmentInstance environmentInstance)
            throws InvalidEntityException {
        try {
            environmentInstance = environmentInstanceDao.create(environmentInstance);
        } catch (Exception e) {
            String errorMessage = " Invalid environmentInstance object. Desc: " + e.getMessage();
            log.error(errorMessage);
            throw new InvalidEntityException(EnvironmentInstance.class, e);

        }

        return environmentInstance;
    }

    private Tier updateTierDB(Tier tierDB, Tier tier) {

        if (tier.getName() != null)
            tierDB.setName(tier.getName());
        if (tier.getRegion() != null)
            tierDB.setRegion(tier.getRegion());
        if (tier.getFlavour() != null)
            tierDB.setFlavour(tier.getFlavour());
        if (tier.getImage() != null)
            tierDB.setImage(tier.getImage());
        if (tier.getIcono() != null)
            tierDB.setIcono(tier.getIcono());
        if (tier.getKeypair() != null)
            tierDB.setKeypair(tier.getKeypair());

        return tierDB;
    }

    private ProductRelease updateProductReleaseDB(ProductRelease productReleaseDB, ProductRelease productRelease) {

        if (productRelease.getDescription() != null) {
            productReleaseDB.setDescription(productRelease.getDescription());
        }
        if (productRelease.getName() != null) {
            productReleaseDB.setName(productRelease.getName());
        }
        if (productRelease.getTierName() != null) {
            productReleaseDB.setTierName(productRelease.getTierName());
        }
        if (productRelease.getAttributes() != null && !productRelease.getAttributes().isEmpty()) {
            List<ProductRelease> productReleases = new ArrayList<ProductRelease>();
            productReleaseDB.setAttributes(null);
            for (Attribute attr : productRelease.getAttributes()) {
                productReleaseDB.addAttribute(attr);
            }
            productReleases.add(productReleaseDB);
        }
        return productReleaseDB;
    }

    /**
     * @param tierInstanceDao
     *            the tierInstanceDao to set
     */
    public void setTierInstanceDao(TierInstanceDao tierInstanceDao) {
        this.tierInstanceDao = tierInstanceDao;
    }

    /**
     * @param environmentInstanceDao
     *            the environmentInstanceDao to set
     */
    public void setEnvironmentInstanceDao(EnvironmentInstanceDao environmentInstanceDao) {
        this.environmentInstanceDao = environmentInstanceDao;
    }

    /**
     * @param productInstanceManager
     *            the productInstanceManager to set
     */
    public void setProductInstanceManager(ProductInstanceManager productInstanceManager) {
        this.productInstanceManager = productInstanceManager;
    }

    /**
     * @param tierInstanceManager
     *            the tierInstanceManager to set
     */
    public void setTierInstanceManager(TierInstanceManager tierInstanceManager) {
        this.tierInstanceManager = tierInstanceManager;
    }

    /**
     * @param environmentManager
     *            the environmentManager to set
     */
    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    /**
     * @param infrastructureManager
     *            the infrastructureManager to set
     */
    public void setInfrastructureManager(InfrastructureManager infrastructureManager) {
        this.infrastructureManager = infrastructureManager;
    }

    /**
     * @param productInstallator
     *            the productInstallator to set
     */
    public void setProductInstallator(ProductInstallator productInstallator) {
        this.productInstallator = productInstallator;
    }

    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    public void setTierManager(TierManager tierManager) {
        this.tierManager = tierManager;
    }

    public void setProductReleaseManager(ProductReleaseManager productReleaseManager) {
        this.productReleaseManager = productReleaseManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    /**
     * @param securityGroupManager
     *            the securityGroupManager to set
     */
    public void setSecurityGroupManager(SecurityGroupManager securityGroupManager) {
        this.securityGroupManager = securityGroupManager;
    }

    /**
     * @param tierDao
     *            the tierDao to set
     */
    public void setTierDao(TierDao tierDao) {
        this.tierDao = tierDao;
    }

    /**
     * @param securityGroupDao
     *            the securityGroupDao to set
     */
    public void setSecurityGroupDao(SecurityGroupDao securityGroupDao) {
        this.securityGroupDao = securityGroupDao;
    }
}
