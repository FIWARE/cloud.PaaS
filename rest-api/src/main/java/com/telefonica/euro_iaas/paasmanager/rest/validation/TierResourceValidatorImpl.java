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

package com.telefonica.euro_iaas.paasmanager.rest.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.claudia.QuotaClient;
import com.telefonica.euro_iaas.paasmanager.exception.AlreadyExistEntityException;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentInstanceException;
import com.telefonica.euro_iaas.paasmanager.exception.QuotaExceededException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.Limits;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.dto.TierDto;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.EnvironmentInstanceSearchCriteria;

/**
 * * @author Henar Munoz
 */
public class TierResourceValidatorImpl implements TierResourceValidator {

    private TierManager tierManager;

    private static Logger log = LoggerFactory.getLogger(TierResourceValidatorImpl.class);
    private EnvironmentInstanceManager environmentInstanceManager;
    private EnvironmentManager environmentManager;
    private QuotaClient quotaClient;
    private ResourceValidator resourceValidator;
    private ProductValidator productValidator;

    /**
     * Validate the request to create a Tier resource.
     *
     * @param claudiaData
     *            The information related to organization, vdc and service
     *            together with the user.
     * @param environmentDto
     *            The information of the environment.
     * @param vdc
     *            The vdc details.
     * @param environmentName
     *            The environment name.
     * @throws InvalidEntityException
     * @throws AlreadyExistEntityException
     * @throws InfrastructureException
     * @throws QuotaExceededException
     * @throws InvalidEnvironmentInstanceException
     */
    public void validateCreate(ClaudiaData claudiaData, TierDto environmentDto, String vdc, String environmentName)
            throws AlreadyExistEntityException, InfrastructureException, QuotaExceededException,
            InvalidEntityException, InvalidEnvironmentInstanceException {

        try {
            tierManager.load(environmentDto.getName(), vdc, environmentName);
            log.error("The tier " + environmentDto.getName() + " already exists in ");
            throw new AlreadyExistEntityException("The tier " + environmentDto.getName() + " already exists in vdc "
                    + vdc + " and environmentName " + environmentName);

        } catch (EntityNotFoundException e) {
            log.debug("Entity not found. It is possible to create it ");
            validateCreateTier(claudiaData, environmentDto);
        }
    }

    /**
     * Validate the request to create an abstract.
     * 
     * @param tierDto
     *            The tier data.
     * @param environmentName
     *            The name of the environment.
     * @throws InvalidEntityException
     * @throws AlreadyExistEntityException
     * @throws InvalidEnvironmentInstanceException
     */
    public void validateCreateAbstract(TierDto tierDto, String environmentName) throws InvalidEntityException,
            AlreadyExistEntityException, InvalidEnvironmentInstanceException {
        try {
            tierManager.load(tierDto.getName(), "", environmentName);
            log.error("The tier " + tierDto.getName() + " already exists in ");
            throw new AlreadyExistEntityException("The tier " + tierDto.getName() + " already exists in environment"
                    + environmentName);

        } catch (EntityNotFoundException e) {
            log.debug("Entity not found. It is possible to create it ");
            resourceValidator.validateName(tierDto.getName());
            validataDefaultTier(tierDto);
            productValidator.validateAttributes(tierDto);

        }

    }

    /**
     * @param claudiaData
     * @param tierDto
     * @throws InfrastructureException
     * @throws QuotaExceededException
     * @throws InvalidEntityException
     * @throws InvalidEnvironmentInstanceException
     */
    private void validateCreateTier(ClaudiaData claudiaData, TierDto tierDto) throws InfrastructureException,
            QuotaExceededException, InvalidEntityException, InvalidEnvironmentInstanceException {

        resourceValidator.validateName(tierDto.getName());

        validataDefaultTier(tierDto);

        productValidator.validateAttributes(tierDto);

        validateSecurityGroups(claudiaData, tierDto);

    }

    /**
     * It check the default data for the tier.
     * 
     * @param tierDto
     * @throws InvalidEntityException
     */
    private void validataDefaultTier(TierDto tierDto) throws InvalidEntityException {
        if (tierDto.getMaximumNumberInstances() == null || tierDto.getMinimumNumberInstances() == null
                || tierDto.getInitialNumberInstances() == null) {
        	String menError ="Error in the initial number of instances, maximun o minimum from environmentDto. Some of them are null";
        	log.error(menError);
            throw new InvalidEntityException(menError);
        }

        if (!(tierDto.getMinimumNumberInstances() <= tierDto.getInitialNumberInstances() && tierDto
                .getMaximumNumberInstances() >= tierDto.getInitialNumberInstances())) {
            String men = "Error in the initial number of instances " + tierDto.getInitialNumberInstances() + " with number min "
                    + tierDto.getMinimumNumberInstances() + " and number max " + tierDto.getMaximumNumberInstances();
            log.error(men);
            throw new InvalidEntityException(men);
        }

        if (tierDto.getImage() == null) {
            throw new InvalidEntityException("Tier Image from environmentDto is null");
        }
        if (tierDto.getFlavour() == null) {
            throw new InvalidEntityException("Tier Flavour from environmentDto is null");
        }
    }

    /**
     * It validates the security groups.
     * 
     * @param claudiaData
     * @param tierDto
     * @throws InfrastructureException
     * @throws QuotaExceededException
     */
    public void validateSecurityGroups(ClaudiaData claudiaData, TierDto tierDto) throws InfrastructureException,
            QuotaExceededException {

        Map<String, Limits> limits = new HashMap<String, Limits>();

        String region = tierDto.getRegion();
        if (!limits.containsKey(region)) {
            try {
                limits.put(region, quotaClient.getLimits(claudiaData, region));
            } catch (InfrastructureException e) {
                throw new InfrastructureException("Failed in getLimits " + e.getMessage());
            }
        }

        Limits limitsRegion = limits.get(region);

        if (limitsRegion.checkTotalSecurityGroupsUsed()) {
            if (1 + limitsRegion.getTotalSecurityGroups() > limitsRegion.getMaxSecurityGroups()) {
                throw new QuotaExceededException("max number of security groups exceeded: "
                        + limitsRegion.getMaxSecurityGroups());
            }
        }

    }

    /**
     * Validate the update of the Tier resource.
     * 
     * @param vdc
     *            The vdc details.
     * @param environmentName
     *            The environment name.
     * @param tierName
     *            The Tier name.
     * @param environmentDto
     *            The information of the environment.
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     */
    public void validateUpdate(String vdc, String environmentName, String tierName, TierDto environmentDto)
            throws InvalidEntityException, EntityNotFoundException {

        try {
            tierManager.load(tierName, vdc, environmentName);
        } catch (EntityNotFoundException e1) {
            throw new EntityNotFoundException(Tier.class, "The tier " + tierName + " does not  exists vdc " + vdc
                    + " environmentName " + environmentName, e1.getMessage());
        }

        if (!tierName.equals(environmentDto.getName())) {
            throw new InvalidEntityException("it is not possible to change the tier Name");
        }

        resourceValidator.validateName(environmentDto.getName());
        validataDefaultTier(environmentDto);
        validateTierInEnvInstance(environmentName, vdc);

    }

    /**
     * Validate the delete of the Tier resource.
     * 
     * @param vdc
     *            The vdc details.
     * @param environmentName
     *            The environment name.
     * @param tierName
     *            The Tier name.
     * @throws InvalidEntityException
     * @throws EntityNotFoundException
     */
    public void validateDelete(String vdc, String environmentName, String tierName) throws InvalidEntityException,
            EntityNotFoundException {

        try {
            tierManager.load(tierName, vdc, environmentName);
        } catch (EntityNotFoundException e) {
            log.error("The tier " + environmentName + " does not exist");
            throw new EntityNotFoundException(Tier.class, "The tier " + tierName + " does not exist", tierName);
        }

        validateTierInEnvInstance(environmentName, vdc);

    }

    /**
     * It checks the relation with environments.
     */
    private void validateTierInEnvInstance(String environmentName, String vdc) throws InvalidEntityException {
        Environment environment;
        try {
            environment = environmentManager.load(environmentName, vdc);
        } catch (EntityNotFoundException e) {
            log.error("The enviornment " + environmentName + " does not exist");
            throw new InvalidEntityException("The enviornment " + environmentName + " does not exist");
        }
        EnvironmentInstanceSearchCriteria criteria = new EnvironmentInstanceSearchCriteria();

        criteria.setVdc(vdc);
        criteria.setEnvironment(environment);

        List<EnvironmentInstance> envInstances = environmentInstanceManager.findByCriteria(criteria);

        if (envInstances != null && envInstances.size() != 0) {
            throw new InvalidEntityException("The environment is being used by an env instance");
        }

    }

    /**
     * Validate the Tiers software dependencies.
     * 
     * @param environmentName
     *            The environment name.
     * @param vdc
     *            The vdc details.
     * @param tierDtoList
     *            Set of tiers.
     * @throws InvalidEntityException
     */
    public void validateTiersDependencies(String environmentName, String vdc, Set<TierDto> tierDtoList)
            throws InvalidEntityException {

        List<Tier> tiers = new ArrayList<Tier>(2);
        try {
            for (TierDto tierDto : tierDtoList) {

                Tier tier = tierManager.loadTierWithProductReleaseAndMetadata(tierDto.getName(), environmentName, vdc);
                tiers.add(tier);
            }
        } catch (EntityNotFoundException e) {
            String message = "Some tier in vdc " + vdc + " with environment " + environmentName + " does not exist";
            log.error(message);
            throw new InvalidEntityException(message);
        }

        List<String> allDependencies = createDependenciesForTiers(tiers);
        Map<String, String> allProducts = createProductList(tiers);
        boolean result = checkTierProductsInDependencyList(allDependencies, allProducts);
        if (!result) {
            String message = "Please review dependencies. Some productrelease is mandatory";
            throw new InvalidEntityException(message);
        }

    }

    /**
     * Check all the dependencies for a product in a map of dependencies.
     * 
     * @param productDependencies
     * @param dependenciesMap
     * @return
     */
    public boolean checkTierProductsInDependencyList(List<String> productDependencies,
            Map<String, String> dependenciesMap) {

        boolean exist = true;

        final Iterator<String> iterator = productDependencies.iterator();
        while (exist && iterator.hasNext()) {
            String productDependency = iterator.next();

            if (!dependenciesMap.containsKey(productDependency)) {
                exist = false;
            }
        }

        return exist;

    }

    /**
     * Create the software dependencies for a Tier list.
     * 
     * @param tiers
     *            Tier List
     * @return The list of dependencies.
     */
    List<String> createDependenciesForTiers(List<Tier> tiers) {
        List<String> dependenciesList = new ArrayList<String>();

        for (Tier tier : tiers) {
            for (ProductRelease productRelease : tier.getProductReleases()) {

                Set<Metadata> metadataList = productRelease.getMetadatas();
                for (Metadata metadata : metadataList) {
                    if (metadata.getKey().startsWith("dep")) {
                        dependenciesList.add(metadata.getValue());
                    }
                }
            }
        }

        return dependenciesList;
    }

    /**
     * Create the products list associate to a Tiers list.
     * 
     * @param tiers
     *            Tier List
     * @return The product list associated to each Tier.
     */
    Map<String, String> createProductList(List<Tier> tiers) {
        Map<String, String> productNameList = new HashMap<String, String>();

        for (Tier tier : tiers) {
            for (ProductRelease productRelease : tier.getProductReleases()) {

                productNameList.put(productRelease.getName() + "-" + productRelease.getVersion(), "");
            }
        }

        return productNameList;
    }

    public void setTierManager(TierManager tierManager) {
        this.tierManager = tierManager;
    }

    public void setEnvironmentInstanceManager(EnvironmentInstanceManager environmentInstanceManager) {
        this.environmentInstanceManager = environmentInstanceManager;
    }

    public void setEnvironmentManager(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;
    }

    public QuotaClient getQuotaClient() {
        return quotaClient;
    }

    public void setQuotaClient(QuotaClient quotaClient) {
        this.quotaClient = quotaClient;
    }

    public void setResourceValidator(ResourceValidator resourceValidator) {
        this.resourceValidator = resourceValidator;
    }

    public void setProductValidator(ProductValidator productValidator) {
        this.productValidator = productValidator;
    }

}
