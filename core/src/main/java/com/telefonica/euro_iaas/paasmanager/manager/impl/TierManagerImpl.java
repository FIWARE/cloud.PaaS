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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.euro_iaas.paasmanager.dao.TierDao;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.TierSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * It is the manager for the Tier.
 * 
 * @author henar
 */
public class TierManagerImpl implements TierManager {

    private TierDao tierDao;

    private ProductReleaseManager productReleaseManager;
    // private SecurityGroupManager securityGroupManager;

    private NetworkManager networkManager;

    private SystemPropertiesProvider systemPropertiesProvider;

    private static Logger log = LoggerFactory.getLogger(TierManagerImpl.class);

    /**
     * It add teh security groups related the products.
     */
    /*
     * public void addSecurityGroupToProductRelease(ClaudiaData claudiaData, Tier tier, ProductRelease productRelease)
     * throws InvalidEntityException, AlreadyExistsEntityException, InfrastructureException { Attribute
     * openPortsAttribute = productRelease.getAttribute("openports"); if (openPortsAttribute != null) { StringTokenizer
     * st = new StringTokenizer(openPortsAttribute.getValue()); while (st.hasMoreTokens()) { String token =
     * st.nextToken(); Rule rule = createRulePort(token, "TCP"); securityGroupManager.addRule(tier.getRegion(), token,
     * tier.getVdc(), tier.getSecurityGroup(), rule); } } }
     */

    /**
     * It creates a tier.
     * 
     * @throws EntityNotFoundException
     * @throws AlreadyExistsEntityException
     */

    public Tier create(ClaudiaData claudiaData, String envName, Tier tier) throws InvalidEntityException,
            InfrastructureException, EntityNotFoundException, AlreadyExistsEntityException {
        log.info("Create tier name " + tier.getName() + " image " + tier.getImage() + " flavour " + tier.getFlavour()
                + " initial_number_instances " + tier.getInitialNumberInstances() + " maximum_number_instances "
                + tier.getMaximumNumberInstances() + " minimum_number_instances " + tier.getMinimumNumberInstances()
                + " floatingIp " + tier.getFloatingip() + " keyPair " + tier.getKeypair() + " icon " + tier.getIcono()
                + " product releases " + tier.getProductReleases() + "  vdc " + claudiaData.getVdc() + " networks "
                + tier.getNetworks());

        if (exists(tier.getName(), tier.getVdc(), envName)) {
            return load(tier.getName(), claudiaData.getVdc(), envName);
        } else {

            // check if exist product or need sync with SDC
            existProductOrSyncWithSDC(claudiaData, tier);

            // createSecurityGroups(claudiaData, tier);

            createNetworks(tier);

            return tierInsertBD(tier, claudiaData);

        }
    }

    private void existProductOrSyncWithSDC(ClaudiaData data, Tier tier) throws InvalidEntityException {

        if (tier.getProductReleases() != null && tier.getProductReleases().size() != 0) {
            for (ProductRelease prod : tier.getProductReleases()) {
                try {
                    log.info("Sync product release " + prod.getProduct() + "-" + prod.getVersion());
                    prod = productReleaseManager
                            .loadFromSDCAndCreate(prod.getProduct() + "-" + prod.getVersion(), data);
                } catch (Exception e2) {
                    String errorMessage = "The ProductRelease Object " + prod.getProduct() + "-" + prod.getVersion()
                            + " not exist in database";
                    log.error(errorMessage);
                    throw new InvalidEntityException(e2);
                }
            }
        }
    }

    public void createNetworks(Tier tier) throws EntityNotFoundException, InvalidEntityException,
            AlreadyExistsEntityException {
        List<Network> networkToBeDeployed = new ArrayList<Network>();
        for (Network network : tier.getNetworks()) {
            networkToBeDeployed.add(network);
        }

        for (Network network : networkToBeDeployed) {
            if (networkManager.exists(network.getNetworkName(), network.getVdc(), tier.getRegion())) {
                log.info("the network " + network.getNetworkName() + " already exists");
                network = networkManager.load(network.getNetworkName(), network.getVdc(), tier.getRegion());
                for (SubNetwork subnet : network.getSubNets()) {
                    networkManager.createSubNetwork(network, subnet);
                }

            } else {
                network = networkManager.create(network);
            }
            tier.addNetwork(network);
        }
    }

    /**
     * It deletes the tier.
     * 
     * @param claudiaData
     * @param tier
     */
    public void delete(ClaudiaData claudiaData, Tier tier) throws EntityNotFoundException, InvalidEntityException,
            InfrastructureException {

        try {
            tier = loadTierWithNetworks(tier.getName(), tier.getVdc(), tier.getEnviromentName());
            log.info("Deleting tier " + tier.getName() + " from vdc " + tier.getVdc() + "  env  "
                    + tier.getEnviromentName() + " " + tier.getNetworks());
        } catch (EntityNotFoundException e) {

            String mens = "It is not possible to delete the tier " + tier.getName() + " since it is not exist";
            log.error(mens);
            throw new com.telefonica.fiware.commons.dao.EntityNotFoundException(Tier.class, mens, tier);
        }

        log.info("Deleting the networks " + tier.getNetworks());

        List<Network> netsAux = new ArrayList<Network>();
        for (Network netNet : tier.getNetworks()) {
            netsAux.add(netNet);
        }

        tier.setNetworks(null);
        tierDao.update(tier);

        for (Network net : netsAux) {

            if (isAvailableToBeDeleted(net)) {
                log.info("Deleting network " + net.getNetworkName());
                try {
                    networkManager.delete(net);
                } catch (Exception e) {
                    log.info("There is an error to delete the network");
                }
            }

        }
        log.info("Networks deleted");

        try {
            tierDao.remove(tier);
        } catch (Exception e) {
            String mens = "It is not possible to delete the tier since it is not exist " + e.getMessage();
            log.error(mens);
            throw new InvalidEntityException(tier, e);
        }

    }

    private boolean isAvailableToBeDeleted(Network net) {

        List<Tier> tiers = tierDao.findAllWithNetwork(net.getNetworkName());
        if (tiers.isEmpty()) {
            log.info("The network " + net + " can be deleted");
            return true;
        } else {
            log.info("The network " + net.getNetworkName() + " cannot be deleted. The following tiers are using it");
            for (Tier tier : tiers) {
                log.info(tier.getName());
            }
            return false;
        }

    }

    public List<Tier> findAll() {

        return tierDao.findAll();
    }

    public List<Tier> findByCriteria(TierSearchCriteria criteria) throws EntityNotFoundException {
        return tierDao.findByCriteria(criteria);
    }

    public List<Tier> findByEnvironment(Environment environment) {
        return null;
    }

    public Tier load(String name, String vdc, String environmentName) throws EntityNotFoundException {
        try {
            return tierDao.load(name, vdc, environmentName);
        } catch (Exception e) {
            throw new EntityNotFoundException(Tier.class, "error", e.getMessage());
        }
    }

    public boolean exists(String name, String vdc, String environmentName) {
        try {
            tierDao.load(name, vdc, environmentName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Tier loadTierWithProductReleaseAndMetadata(String tierName, String environmentName, String vdc)
            throws EntityNotFoundException {
        try {
            return tierDao.loadTierWithProductReleaseAndMetadata(tierName, vdc, environmentName);
        } catch (Exception e) {
            String message = "Tier " + tierName + " not found";
            throw new EntityNotFoundException(Tier.class, message, e.getMessage());
        }
    }

    public Tier loadTierWithNetworks(String tierName, String vdc, String environmentName)
            throws EntityNotFoundException {
        try {
            return tierDao.loadTierWithNetworks(tierName, vdc, environmentName);

        } catch (Exception e) {
            String message = "Tier " + tierName + " not found";
            throw new EntityNotFoundException(Tier.class, message, e.getMessage());
        }
    }

    /*
     * private void restore(ClaudiaData claudiaData, Tier tier) throws InvalidEntityException, InfrastructureException {
     * if (tier.getSecurityGroup() != null) { securityGroupManager.destroy(tier.getRegion(), tier.getVdc(),
     * claudiaData.getUser().getToken(), tier.getSecurityGroup()); } }
     */

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setProductReleaseManager(ProductReleaseManager productReleaseManager) {
        this.productReleaseManager = productReleaseManager;

    }

    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {

        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    /**
     * @param tierDao
     *            the tierDao to set
     */
    public void setTierDao(TierDao tierDao) {
        this.tierDao = tierDao;
    }

    /**
     * It insert the tier in the database.
     * 
     * @param tier
     * @param data
     * @return
     * @throws InvalidEntityException
     * @throws InfrastructureException
     */
    public Tier tierInsertBD(Tier tier, ClaudiaData data) throws InvalidEntityException, InfrastructureException {
        List<Network> networskout = new ArrayList();
        try {
            return load(tier.getName(), tier.getVdc(), tier.getEnviromentName());
        } catch (EntityNotFoundException e) {

            // tier.setVdc(data.getVdc());
            // tier.setEnviromentName(data.getService());

            List<ProductRelease> productReleases = new ArrayList();
            if (tier.getProductReleases() != null && tier.getProductReleases().size() != 0) {
                for (ProductRelease p : tier.getProductReleases()) {
                    productReleases.add(p);
                }
            } else {

                log.warn("There is not any product release associated to the tier " + tier.getName());
            }

            for (Network p : tier.getNetworks()) {
                networskout.add(p);
            }

            try {
                tier.setProductReleases(null);
                tier.setNetworks(null);
                tier = tierDao.create(tier);
            } catch (Exception e2) {
                String errorMessage = "The Tier  " + tier.getName() + "  cannot be created " + e2.getMessage();
                log.error(errorMessage);
                // restore(data, tier);
                throw new InvalidEntityException(errorMessage);
            }

            if (productReleases.size() != 0) {
                for (ProductRelease product : productReleases) {

                    try {
                        ProductRelease templateProduct = productReleaseManager.load(product.getProduct() + "-"
                                + product.getVersion(), data);
                        log.info("Adding product release " + templateProduct.getProduct() + "-"
                                + templateProduct.getVersion() + " to tier " + templateProduct.getName());

                        mergeAttributes(product, templateProduct);
                        mergeMetadatas(product, templateProduct);

                        tier.addProductRelease(templateProduct);

                        tier = update(tier);
                    } catch (Exception e2) {
                        String errorMessage = "The ProductRelease Object " + product.getProduct() + "-"
                                + product.getVersion() + " is " + "NOT present in Database";
                        log.error(errorMessage);
                        throw new InvalidEntityException(e2);
                    }
                }
            }

        }

        for (Network net : networskout) {

            try {
                net = networkManager.load(net.getNetworkName(), net.getVdc(), net.getRegion());
                log.info("Adding network " + net.getNetworkName() + "-" + " to tier " + tier.getName());
                tier.addNetwork(net);
                update(tier);
            } catch (Exception e2) {
                String errorMessage = "The Network Object " + net.getNetworkName() + " is " + "NOT present in Database";
                log.error(errorMessage);
                throw new InvalidEntityException(e2);
            }
        }

        return tier;
    }

    public void mergeAttributes(ProductRelease product, ProductRelease templateProduct) {

        if (product.getAttributes() != null) {
            if ((templateProduct.getAttributes() != null) && (!templateProduct.getAttributes().isEmpty())) {

                for (Attribute attribute : product.getAttributes()) {

                    String key = attribute.getKey();
                    boolean notExistInTemplate = templateProduct.getAttribute(key) == null;
                    if (notExistInTemplate) {
                        templateProduct.addAttribute(attribute);
                    } else {
                        templateProduct.getAttribute(key).setValue(attribute.getValue());
                    }
                }

            } else {
                templateProduct.setAttributes(product.getAttributes());
            }
        }
    }

    public void mergeMetadatas(ProductRelease productRelease, ProductRelease newProductRelease) {
        if (productRelease.getMetadatas() != null) {
            if (!newProductRelease.getMetadatas().isEmpty()) {

                for (Metadata metadata : productRelease.getMetadatas()) {

                    String key = metadata.getKey();
                    boolean notExistInTemplate = newProductRelease.getMetadata(key) == null;
                    if (notExistInTemplate) {
                        newProductRelease.addMetadata(metadata);
                    } else {
                        newProductRelease.getMetadata(key).setValue(metadata.getValue());
                    }
                }

            } else {
                newProductRelease.setMetadatas(productRelease.getMetadatas());
            }
        }
    }

    public Tier update(Tier tier) throws InvalidEntityException {
        log.info("Update tier " + tier.getName());
        try {
            Tier newTier = tierDao.update(tier);
            return tierDao.loadComplete(newTier);
        } catch (Exception e) {
            log.error("It is not possible to update the tier " + tier.getName() + " : " + e.getMessage(), e);
            throw new InvalidEntityException("It is not possible to update the tier " + tier.getName() + " : "
                    + e.getMessage());
        }

    }

    public void updateTier(ClaudiaData data, Tier tierold, Tier tiernew) throws InvalidEntityException,
            EntityNotFoundException, AlreadyExistsEntityException {

        tierold.setFlavour(tiernew.getFlavour());
        tierold.setFloatingip(tiernew.getFloatingip());
        tierold.setIcono(tiernew.getIcono());
        tierold.setImage(tiernew.getImage());
        tierold.setInitialNumberInstances(tiernew.getInitialNumberInstances());
        tierold.setKeypair(tiernew.getKeypair());
        tierold.setMaximumNumberInstances(tiernew.getMaximumNumberInstances());
        tierold.setMinimumNumberInstances(tiernew.getMinimumNumberInstances());
        tierold.setRegion(tiernew.getRegion());

        update(tierold);

        // Get networks to be delete
        Set<Network> nets = new HashSet<Network>();
        for (Network net : tierold.getNetworks()) {
            nets.add(net);
        }

        // delete networks
        tierold.setNetworks(null);
        update(tierold);

        // adding networks
        for (Network net : tiernew.getNetworks()) {
            log.info("Creating new network " + net.getNetworkName());
            try {
                net = networkManager.create(net);
            } catch (AlreadyExistsEntityException e) {
                net = networkManager.load(net.getNetworkName(), net.getVdc(), net.getRegion());
            }
            tierold.addNetwork(net);
            update(tierold);
        }

        for (Network net : nets) {
            if (isAvailableToBeDeleted(net)) {
                networkManager.delete(net);
            }
        }

        tierold.setProductReleases(null);
        update(tierold);

        if (tiernew.getProductReleases() == null)
            return;

        for (ProductRelease productRelease : tiernew.getProductReleases()) {
            try {
                productRelease = productReleaseManager.load(
                        productRelease.getProduct() + "-" + productRelease.getVersion(), data);
            } catch (EntityNotFoundException e) {
                log.error("The new software " + productRelease.getProduct() + "-" + productRelease.getVersion()
                        + " is not found");

            }
            tierold.addProductRelease(productRelease);
            update(tierold);
        }

    }

    @Override
    public Tier loadComplete(Tier tier) throws EntityNotFoundException {
        return tierDao.loadComplete(tier);
    }

}
