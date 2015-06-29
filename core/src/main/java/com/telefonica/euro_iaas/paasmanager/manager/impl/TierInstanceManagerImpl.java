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
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.dao.TierInstanceDao;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidProductInstanceRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidSecurityGroupRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.NotUniqueResultException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductReconfigurationException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentManager;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.SecurityGroupManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.Rule;
import com.telefonica.euro_iaas.paasmanager.model.SecurityGroup;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.TierInstanceSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

public class TierInstanceManagerImpl implements TierInstanceManager {

    private TierInstanceDao tierInstanceDao;
    private TierManager tierManager;
    private ProductInstanceManager productInstanceManager;
    private InfrastructureManager infrastructureManager;
    private ProductReleaseManager productReleaseManager;
    private EnvironmentManager environmentManager;
    private EnvironmentInstanceManager environmentInstanceManager;
    private SecurityGroupManager securityGroupManager;
    private SystemPropertiesProvider systemPropertiesProvider;
    
    private static Logger log = LoggerFactory.getLogger(TierInstanceManagerImpl.class);
    
    /**
     * Creates a TierInstance and returns the TierInstance object
     * @param data
     * @param envName
     * @param tierInstance
     * @return TierInstance
     * @throws InvalidEntityException
     * @throws InfrastructureException
     */
    public TierInstance create(ClaudiaData data, String envName, TierInstance tierInstance)
    		throws InvalidEntityException, InfrastructureException {
    
        log.info("Inserting in database for tier instance " + tierInstance.getName() + " "
                + tierInstance.getNetworkInstances().size() + " " + tierInstance.getTier().getFloatingip());

        TierInstance tierInstanceDB = new TierInstance();

        try {
            tierInstanceDao.load(tierInstance.getName());
            String message = "TierInstance Already exists [" + tierInstance.getName()
                    + "]";
            log.error(message);
            throw new InvalidEntityException(message);
        } catch (EntityNotFoundException e) {

        }
         
        Tier tierDB = null;
        try {
            tierDB = tierManager.loadTierWithProductReleaseAndMetadata(tierInstance.getTier().getName(), envName,
                    data.getVdc());
            log.info("The tier already exists " + tierDB.getName() + " " + tierDB.getFloatingip() + " " + envName + " "
                    + data.getVdc());
        } catch (EntityNotFoundException e) {
            log.warn("Error to load the Tier " + tierInstance.getTier().getName() + " : " + e.getMessage());
            throw new InvalidEntityException("Error to load the Tier " + tierInstance.getTier().getName() + " : "
                    + e.getMessage());
        }
        
        try {
            tierInstance.setTier(tierDB);
            createSecurityGroups(data, tierInstance);
        } catch (InvalidSecurityGroupRequestException isgre) {
            String secGroupMen = "The securityGroupRequest creation is invalid " + isgre.getMessage();
            log.warn(secGroupMen);
            throw new InvalidEntityException(secGroupMen, isgre);
        } catch (EntityNotFoundException enfe) {
            String men = "ProductRelease in tier " + tierInstance.getTier().getName() + " does not exist";
            log.warn(men);
            throw new InvalidEntityException(men + " : " + enfe.getMessage());
        }
        
       tierInstanceDB.setTier(tierDB);
       tierInstanceDB.setSecurityGroup(tierInstance.getSecurityGroup());
       
       List<ProductInstance> productInstances = tierInstance.getProductInstances();
       if (productInstances != null) {
    	   for (ProductInstance productInstance : productInstances) {
                ProductInstance productInstanceDB = null;
                try {
                    productInstanceDB = productInstanceManager.load(productInstance.getName());
                } catch (EntityNotFoundException e) {
                    try {
                        productInstanceDB = productInstanceManager.create(data, productInstance);
                    } catch (InvalidEntityException e1) {
                        throw new InvalidEntityException("Error to create the " + "product instance "
                                + productInstance.getName() + " : " + e1.getMessage());
                    } catch (AlreadyExistsEntityException e1) {
                        throw new InvalidEntityException("Error to create the " + "product instance "
                                + productInstance.getName() + " : " + e1.getMessage());
                    } catch (InvalidProductInstanceRequestException e1) {
                        // TODO Auto-generated catch block
                        throw new InvalidEntityException("Error to create the " + "product instance "
                                + productInstance.getName() + " : " + e1.getMessage());
                    }
                }
                tierInstanceDB.addProductInstance(productInstanceDB);
            }
        }

        tierInstanceDB.setName(tierInstance.getName());
        tierInstanceDB.setVM(tierInstance.getVM());
        tierInstanceDB.setVdc(tierInstance.getVdc());
        tierInstanceDB.setNetworkInstance(tierInstance.getNetworkInstances());
        if (tierInstance.getOvf() != null && tierInstance.getOvf().length() != 0) {
            tierInstanceDB.setOvf(tierInstance.getOvf());
        }
        if (tierInstance.getVApp() != null && tierInstance.getVApp().length() != 0) {
            tierInstanceDB.setVapp(tierInstance.getVApp());
        }

        tierInstanceDB.setNumberReplica(tierInstance.getNumberReplica());

        try {
            tierInstanceDB.setStatus(tierInstance.getStatus());
            tierInstanceDB = tierInstanceDao.create(tierInstanceDB);
        } catch (AlreadyExistsEntityException e) {
           throw new InvalidEntityException("Error to create the tier instance " + tierInstanceDB.getName() + " : "
                    + e.getMessage());
        }
        
        return tierInstanceDB;
    }

    /**
     * Creates a TierInstance
     * @param claudiaData
     * @param envInstance
     * @param tierInstance
     * @param systemPropertiesProvider
     * @return 
     * @throws InvalidEntityException
     * @throws AlreadyExistsEntityException
     * @throws InvalidProductInstanceRequestException
     * @throws ProductInstallatorException
     */ 
    public void create(ClaudiaData claudiaData, TierInstance tierInstance, EnvironmentInstance envInstance,
            SystemPropertiesProvider systemPropertiesProvider) throws InfrastructureException, EntityNotFoundException,
            InvalidEntityException, AlreadyExistsEntityException, NotUniqueResultException,
            InvalidProductInstanceRequestException, ProductInstallatorException {

        log.info("Creating tier instance " + tierInstance.getName() + " from tier " + tierInstance.getTier().getName());
        if (tierInstance.getOvf() == null) {
            log.info("The OVF is null");
        }
        // Needed to recover the number of replicas TOBEDONE
        int replicaNumber = tierInstance.getNumberReplica();
        VM vm = new VM();
        String fqn = claudiaData.getOrg().replace("_", ".") + ".customers." + claudiaData.getVdc() + ".services."
                + claudiaData.getService() + ".vees." + tierInstance.getTier().getName() + ".replicas." + replicaNumber;

        String hostname = infrastructureManager.generateVMName(claudiaData.getService(),
                tierInstance.getTier().getName(), replicaNumber, tierInstance.getVdc()).toLowerCase();

        vm.setFqn(fqn);
        vm.setHostname(hostname);
        tierInstance.setVM(vm);
        tierInstance.setStatus(Status.DEPLOYING);

        tierInstance = create(claudiaData, envInstance.getEnvironment().getName(), tierInstance);
        envInstance.addTierInstance(tierInstance);
        envInstance.setStatus(Status.DEPLOYING);
        environmentInstanceManager.update(envInstance);

        infrastructureManager.deployVM(claudiaData, tierInstance, replicaNumber, vm);

        // if (systemPropertiesProvider.getProperty(
        // SystemPropertiesProvider.CLOUD_SYSTEM).equals("FIWARE")) {
        for (ProductRelease productRelease : tierInstance.getTier().getProductReleases()) {

            // infrastructureManager.deployVM(claudiaData, tierInstance,
            // replicaNumber, tierInstance.getOvf(),
            // vm);

            ProductInstance productInstance = null;
            try {
                tierInstance.setStatus(Status.INSTALLING);
                tierInstance = tierInstanceDao.update(tierInstance);
                envInstance.setStatus(Status.INSTALLING);
                environmentInstanceManager.update(envInstance);
                productInstance = productInstanceManager.install(tierInstance, claudiaData, envInstance,
                        productRelease);
            } catch (ProductInstallatorException e) {
                String mens = "Error to install the productINstance " + productRelease.getName() + " in "
                        + tierInstance.getName() + " " + e.getMessage();
                log.error(mens);
                throw new ProductInstallatorException(mens, e);
            } catch (Exception e) {
                String mens = "Error to install the productINstance " + productRelease.getName() + " in "
                        + tierInstance.getName() + " " + e.getMessage();

                productInstance = productInstanceManager.install(tierInstance, claudiaData, envInstance,
                        productRelease);
            }
            tierInstance.addProductInstance(productInstance);
        }

        tierInstance.setStatus(Status.INSTALLED);
        tierInstance = tierInstanceDao.update(tierInstance);
        envInstance.setStatus(Status.INSTALLED);
        environmentInstanceManager.update(envInstance);

        try {

            reconfigure(claudiaData, envInstance, tierInstance);

        } catch (ProductReconfigurationException e) {
            log.error("Error to configure the tier instance " + e.getMessage());
        }

    }

    /**
     * Delete a TierInstance
     * @param claudiaData
     * @param envInstance
     * @param tierInstance
     * @return 
     * @throws InfrastructureException
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     */ 
    public void delete(ClaudiaData claudiaData, TierInstance tierInstance, EnvironmentInstance envInstance)
            throws InfrastructureException, InvalidEntityException, EntityNotFoundException {

    	infrastructureManager.deleteVMReplica(claudiaData, tierInstance);
    	envInstance.removeTierInstance(tierInstance);
    	environmentInstanceManager.update(envInstance);
    	removeSecurityGroup(claudiaData, tierInstance);
    	remove(tierInstance);
    }

    /**
     * Finds a list of TierInstance from a certain searching criteria
     * @param criteria
     * @return List<TierInstance>
     * @throws EntityNotFoundException
     */ 
    public List<TierInstance> findByCriteria(TierInstanceSearchCriteria criteria) throws EntityNotFoundException {

        return tierInstanceDao.findByCriteria(criteria);
    }

    /**
     * Finds a list of TierInstance associated to a environment
     * @param vdc
     * @param environmentInstance
     * @return List<TierInstance>
     * @throws EntityNotFoundException
     */
    public List<TierInstance> findByEnvironment(String vdc, EnvironmentInstance environmentInstance)
            throws EntityNotFoundException {
        TierInstanceSearchCriteria criteria = new TierInstanceSearchCriteria();
        criteria.setEnvironmentInstance(environmentInstance);
        criteria.setVdc(vdc);

        List<TierInstance> tierInstances = findByCriteria(criteria);

        return tierInstances;
    }

    private List<Attribute> getPICAttributesOVF(String vmOVF) {
        List<Attribute> picAtributes = new ArrayList<Attribute>();
        String first, key, value, property;
        String[] lValue, lKey;
        if (vmOVF == null)
            return null;

        if (vmOVF.contains("ovfenvelope:value=\"PIC\"")) {
            first = vmOVF.split("ovfenvelope:value=\"PIC\"", 2)[1];
            property = first.split("Product Specific Attributes</ovfenvelope:Category>", 2)[1];
            while (property.contains("<ovfenvelope:Property")) {
                lValue = property.split("ovfenvelope:value=\"", 2)[1].split("\"", 2);
                value = lValue[0];
                lKey = lValue[1].split("key=\"", 2)[1].split("\"", 2);
                key = lKey[0];
                property = lKey[1];
                Attribute attrib = new Attribute(key, value);
                picAtributes.add(attrib);
            }
            return picAtributes;
        } else
            return null;
    }

    /**
     * Get the Value of attribute "balancer" of a product from a Tier
     * @param tier
     * @param environmentInstance
     * @return attribute balancer value
     */
    public String getProductNameBalanced(Tier tier) {
        if (tier.getProductReleases() != null) {
            for (ProductRelease productRelease : tier.getProductReleases()) {
                Attribute attBalancer = productRelease.getAttribute("balancer");
                if (attBalancer == null) {
                    return null;
                }
                // Attribute attBalancer = getAttribute(
                // productRelease.getAttributes(), "balancer");
                return attBalancer.getValue();
            }
        }
        return null;
    }
    
    /**
     * Get TierInstance to Configure
     * @param tier
     * @param environmentInstance
     * @return TierInstance
     */
    public TierInstance getTierInstanceToConfigure(EnvironmentInstance environmentInstance, Tier tier) {
        log.info("getTierInstanceToConfigure for tier " + tier.getName());
        String productName = getProductNameBalanced(tier);
        if (productName == null) {

            log.warn("No attribute balancer found in a product for tier " + tier.getName());
            return null;
        }
        Tier tierProduct = null;
        try {

            tierProduct = getTierProductWithName(environmentInstance.getEnvironment(), productName);
        } catch (InvalidEntityException e) {
            return null;
        }
        log.info("Reconfiguring the tier " + tier.getName() + " with the productName " + productName
                + " and the tier to configure is " + tierProduct.getName());

        return getTierInstanceWithTier(environmentInstance, tierProduct);

    }

    /**
     * Get TierInstance with Tiers associated
     * @param tier
     * @param environmentInstance
     * @return TierInstance
     */
   public TierInstance getTierInstanceWithTier(EnvironmentInstance enviromentInstance, Tier tier) {
        if (enviromentInstance.getTierInstances() != null) {
            for (TierInstance tierInstance : enviromentInstance.getTierInstances()) {
                log.info("Looking for " + tier.getName() + " tier instance " + tierInstance.getName());
                if (tierInstance.getTier().getName().equals(tier.getName())) {
                    return tierInstance;
                }
            }
        }
        return null;
    }

   /**
    * Find Tier defined in Environment with a particular productName 
    * @param environment
    * @param productName
    * @return Tier
    */
    public Tier getTierProductWithName(Environment environment, String productName) throws InvalidEntityException {
        if (productName == null) {
            return null;
        }
        try {
            environment = environmentManager.load(environment.getName(), environment.getVdc());
        } catch (EntityNotFoundException e) {

            throw new InvalidEntityException(e);
        }
        if (environment.getTiers() != null) {
            for (Tier tier : environment.getTiers()) {
                if (tier.getProductReleases() != null) {
                    for (ProductRelease productRelease : tier.getProductReleases()) {
                        if (productRelease.getProduct().equals(productName)) {
                            return tier;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Load TierInstance by id 
     * @param id
     * @return TierInstance
     * @throws EntityNotFoundException
     */
    public TierInstance load(long id) throws EntityNotFoundException {
        return tierInstanceDao.findByTierInstanceId(id);
    }
    
    /**
     * Load TierInstance by name 
     * @param id
     * @return TierInstance
     * @throws EntityNotFoundException
     */
    public TierInstance load(String name) throws EntityNotFoundException {
        return tierInstanceDao.load(name);
    }

    /**
     * Update TierInstance in database 
     * @param TierInstance
     * @return TierInstance
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     */
    public TierInstance update(TierInstance tierInstance) throws EntityNotFoundException, InvalidEntityException {
        return tierInstanceDao.update(tierInstance);
    }
   
    /**
     * Find TierInstance by name 
     * @param name
     * @return TierInstance
     * @throws EntityNotFoundException
     */
    public TierInstance loadByName(String name) throws EntityNotFoundException {
        return tierInstanceDao.findByTierInstanceName(name);
    }

    /**
     * Find TierInstance with NetworkInstances by name 
     * @param name
     * @return TierInstance
     * @throws EntityNotFoundException
     */
    public TierInstance loadNetworkInstnace(String name) throws EntityNotFoundException {
        return tierInstanceDao.findByTierInstanceNameNetworkInst(name);
    }

    /**
     * Reconfigure TierInstance
     * @param claudiaData
     * @param environmentInstance
     * @param tierInstance
     * @throws ProductReconfigurationException
     */
    public void reconfigure(ClaudiaData claudiaData, EnvironmentInstance environmentInstance, TierInstance tierInstance)
            throws ProductReconfigurationException {
        log.info("Reconfiguring the balancer for the tier Instance " + tierInstance.getName());

        TierInstance tierInstanceToConfigured = getTierInstanceToConfigure(environmentInstance, tierInstance.getTier());
        if (tierInstanceToConfigured == null) {
            log.warn("It is not possible to reconfigure the tier instance " + tierInstance.getTier());
            return;
        }

        log.info("Tier instance to reconfigure since it is a balancer " + tierInstanceToConfigured.getName()
                + " with tier " + tierInstanceToConfigured.getTier().getName());
        try {
            update(claudiaData, tierInstanceToConfigured, environmentInstance);
        } catch (Exception e) {

            log.error("Error to reconfigure the tier " + tierInstanceToConfigured.getName() + " from tier "
                    + tierInstanceToConfigured.getTier().getName() + " " + e.getMessage());
            throw new ProductReconfigurationException(
                    "Error to reconfigure the tier " + tierInstanceToConfigured.getName() + " from tier "
                            + tierInstanceToConfigured.getTier().getName(), e);
        }

        String[] name = tierInstance.getName().split("-", 3);
        try {
            int replicaNumberAnt = (Integer.parseInt(name[2])) - 1;
            if (replicaNumberAnt > 0) {
                String antName = name[0] + "-" + name[1] + "-" + (replicaNumberAnt);
                TierInstance tierInstanceAnt = null;
                try {
                    tierInstanceAnt = load(antName);
                } catch (EntityNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                tierInstance.setProductInstances(tierInstanceAnt.getProductInstances());
            }
        } catch (IllegalArgumentException e) {
            log.error("Cannot parse the number of replica in the reconfigutation option: " + e);
        }
    }

    /**
     * Remove TierInstance
     * @param tierInstance
     * @throws InvalidEntityException
     */
    public void remove(TierInstance tierInstance) throws InvalidEntityException {

        try {
            tierInstance = load(tierInstance.getName());
        } catch (EntityNotFoundException e1) {
            e1.printStackTrace();
        }
        List<ProductInstance> productInstances = tierInstance.getProductInstances();
        tierInstance.setProductInstances(null);
        tierInstanceDao.update(tierInstance);

        if (productInstances != null && productInstances.size() > 0) {
            for (ProductInstance productInstance : productInstances) {
                try {
                    productInstanceManager.remove(productInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }      
        tierInstanceDao.remove(tierInstance);
    }

    /**
     * Update a TierInstance and returns the TierInstance updated
     * @param tierInstance
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     * @throws AlreadyExistsEntityException
     */
    public TierInstance update(ClaudiaData claudiaData, String envName, TierInstance tierInstance) 
    		throws EntityNotFoundException, InvalidEntityException, AlreadyExistsEntityException, 
    		InfrastructureException {

        if (tierInstance.getId() != null) {
            tierInstance = tierInstanceDao.update(tierInstance);
            tierInstance = tierInstanceDao.findByTierInstanceIdWithMetadata(tierInstance.getId());
        }
        else {
            tierInstance = create(claudiaData, envName, tierInstance);
        }
        return tierInstance;
    }


    /**
     * Updates the TierInstance
     * @param claudiaData
     * @param tierInstance
     * @param envInstance
     * @throws ProductInstallatorException
     * @throws InvalidEntityException
     * @throws AlreadyExistsEntityException
     * @throws EntityNotFoundException
     * @throws ProductReconfigurationException
     */
    public void update(ClaudiaData claudiaData, TierInstance tierInstance, EnvironmentInstance envInstance)
            throws ProductInstallatorException, InvalidEntityException, AlreadyExistsEntityException,
            EntityNotFoundException, ProductReconfigurationException {
        log.info("Updating the tier instance " + tierInstance.getTier().getName());
        Tier tier = null;
        try {
            tier = tierManager.load(tierInstance.getTier().getName(), claudiaData.getVdc(), envInstance
                    .getEnvironment().getName());
        } catch (EntityNotFoundException e) {
            log.error("No entity found " + tierInstance.getTier().getName() + " wit vdc " + claudiaData.getVdc()
                    + " and entiroment " + envInstance.getEnvironment().getName());
            throw new InvalidEntityException(e);
        }

        if ((tier.getProductReleases() != null) && (tier.getProductReleases().size() != 0)) {

            for (ProductInstance productInstance : tierInstance.getProductInstances()) {

                log.info("Configure productInstance " + productInstance.getProductRelease().getProduct());
                List<Attribute> atributes = getPICAttributesOVF(tierInstance.getOvf());
                productInstanceManager.configure(claudiaData, productInstance, atributes);
            }
        }
    }
    
   /**
     * It creates the specified security groups.
     * 
     * @param claudiaData
     * @param tierInstance
     * @return
     * @throws InvalidSecurityGroupRequestException
     * @throws EntityNotFoundException
     */
    private TierInstance createSecurityGroups(ClaudiaData claudiaData, TierInstance tierInstance) 
    		throws InvalidSecurityGroupRequestException, EntityNotFoundException {
    	
        if ((systemPropertiesProvider.getProperty(SystemPropertiesProvider.CLOUD_SYSTEM).equals("FIWARE")
                && claudiaData.getVdc() != null && claudiaData.getVdc().length() > 0)) {

            SecurityGroup securityGroup = generateSecurityGroup(claudiaData, tierInstance);
            try {
                securityGroup = securityGroupManager.create(tierInstance.getTier().getRegion(), 
                		claudiaData.getUser().getToken(), claudiaData.getVdc(), securityGroup);
                tierInstance.setSecurityGroup(securityGroup);
            } catch (InvalidEntityException e) {
                String messg_e = "[InvalidEntityException] It is not possible to create the security group " + securityGroup.getName() + " "
                        + e.getMessage(); 
            	log.error(messg_e);
                throw new InvalidSecurityGroupRequestException(messg_e, e);
            } catch (InvalidEnvironmentRequestException iere) {
            	String messg_iere = "[InvalidEnvironmentRequestException] It is not possible to create the security group " + securityGroup.getName() + " "
                        + iere.getMessage(); 
                log.error(messg_iere);
                throw new InvalidSecurityGroupRequestException(messg_iere, iere);
            } catch (AlreadyExistsEntityException aeee) {
            	String messg_aeee = "[AlreadyExistsEntity] It is not possible to create the security group " + securityGroup.getName() + " "
                        + aeee.getMessage(); 
            	log.error(messg_aeee);
                throw new InvalidSecurityGroupRequestException(messg_aeee, aeee);
            } catch (InfrastructureException ie) {
            	String messg_ie = "[InfrastructureException] It is not possible to create the security group " + securityGroup.getName() + " "
                        + ie.getMessage(); 
            	log.error(messg_ie);
                throw new InvalidSecurityGroupRequestException(messg_ie, ie);
            }
            tierInstance.setSecurityGroup(securityGroup);
        }
        return tierInstance;

    }
    
   private SecurityGroup generateSecurityGroup(ClaudiaData claudiaData, TierInstance tierInstance) 
		   throws EntityNotFoundException {

        SecurityGroup securityGroup = new SecurityGroup();
        String secGroupName = "sg_" + claudiaData.getVdc() + "_" + tierInstance.getName();
        securityGroup.setName(secGroupName);
        log.info("Generate Object Security Group " + secGroupName);

        List<Rule> rules = getDefaultRules();
        List<ProductRelease> productReleases = tierInstance.getTier().getProductReleases();
        if (productReleases != null) {
            for (ProductRelease productRelease : tierInstance.getTier().getProductReleases()) {
                getRulesFromProduct(productRelease, rules);
            }
        }
        securityGroup.setRules(rules);
        return securityGroup;
    }
    
    private List<Rule> getDefaultRules() {
        List<Rule> rules = new ArrayList<Rule>();
        log.info("Generate TCP rule " + 9990);
        Rule rule2 = new Rule("TCP", "22", "22", "", "0.0.0.0/0");
        rules.add(rule2);
        return rules;
    }

    private void getRulesFromProduct(ProductRelease productRelease, List<Rule> rules) throws EntityNotFoundException {

        productRelease = productReleaseManager.loadWithMetadata(productRelease.getProduct() + "-"
                + productRelease.getVersion());
        getRules(productRelease, rules, "open_ports", "TCP");
        getRules(productRelease, rules, "open_ports_udp", "UDP");

    }
    
    private void getRules(ProductRelease productRelease, List<Rule> rules, String pathrules, String protocol) {
        Metadata openPortsAttribute = productRelease.getMetadata(pathrules);
        if (openPortsAttribute != null) {
            log.info("Adding product rule " + openPortsAttribute.getValue());
            StringTokenizer st = new StringTokenizer(openPortsAttribute.getValue());
            while (st.hasMoreTokens()) {
                Rule rule = createRulePort(st.nextToken(), protocol);
                if (!rules.contains(rule)) {
                    rules.add(rule);
                }
            }
        }
    }
   	
    /**
     * It creates the rule port for ssh.
     * 
     * @param port
     * @return
     */
    private Rule createRulePort(String port, String protocol) {
        log.info("Generate security rule " + port);
        if (port.contains("-")) {
            return new Rule(protocol, port.substring(0, port.indexOf("-")), port.substring(port.indexOf("-") + 1), "",
                    "0.0.0.0/0");
        } else {
            return new Rule(protocol, port, port, "", "0.0.0.0/0");
        }
    }
    
    private void removeSecurityGroup (ClaudiaData data, TierInstance tierInstance) 
    		throws InvalidEntityException, InfrastructureException {
    	log.info("Deleting security group " + tierInstance.getSecurityGroup().getName() 
    			+ " in tierInstance " + tierInstance.getName());
        if (tierInstance.getSecurityGroup() != null && !tierInstance.getVdc().isEmpty()) {
             SecurityGroup sec = tierInstance.getSecurityGroup();
             tierInstance.setSecurityGroup(null);
             tierInstanceDao.update(tierInstance);
             securityGroupManager.destroy(tierInstance.getTier().getRegion(), data.getUser().getToken(), 
             		tierInstance.getVdc(), sec);
         }
    }
    /**
     * @param tierInstanceDao
     *            the tierInstanceDao to set
     */
    public void setTierInstanceDao(TierInstanceDao tierInstanceDao) {
        this.tierInstanceDao = tierInstanceDao;
    }
    
    /**
     * @param tierManager
     *            the tierManager to set
     */
    public void setTierManager(TierManager tierManager) {
        this.tierManager = tierManager;
    }

    /**
     * @param productInstanceManager
     *            the productInstanceManager to set
     */
    public void setProductInstanceManager(ProductInstanceManager productInstanceManager) {
        this.productInstanceManager = productInstanceManager;
    }
    
    /**
     * @param infrastructureManager
     *            the infrastructureManager to set <property name="tierInstanceDao" ref="tierInstanceDao"/>
     */
    public void setInfrastructureManager(InfrastructureManager infrastructureManager) {
        this.infrastructureManager = infrastructureManager;
    }
    
    /**
     * @param productReleaseManager
     *            the productReleaseManager to set
     */
    public void setProductReleaseManager(ProductReleaseManager productReleaseManager) {
        this.productReleaseManager = productReleaseManager;
    }

    /**
     * @param environmentInstanceManager
     *            the environmentInstanceManager to set
     */
    public void setEnvironmentInstanceManager(EnvironmentInstanceManager environmentInstanceManager) {
        this.environmentInstanceManager = environmentInstanceManager;
    }

    /**
     * @param environmentManager
     *            the environmentManager to set
     */
    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    /**
     * @param securityGroupManager
     *            the securityGroupManager to set
     */
    public void setSecurityGroupManager(SecurityGroupManager securityGroupManager) {
        this.securityGroupManager = securityGroupManager;
    }
 
    /**
     * @param systemPropertiesProvider
     *            the systemPropertiesProvider to set
     */
    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {

        this.systemPropertiesProvider = systemPropertiesProvider;
    }
}
