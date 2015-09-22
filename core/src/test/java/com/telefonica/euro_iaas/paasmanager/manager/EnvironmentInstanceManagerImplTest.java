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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidProductInstanceRequestException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import org.junit.Test;

import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentDao;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentInstanceDao;
import com.telefonica.euro_iaas.paasmanager.dao.ProductReleaseDao;
import com.telefonica.euro_iaas.paasmanager.dao.TierDao;
import com.telefonica.euro_iaas.paasmanager.dao.TierInstanceDao;
import com.telefonica.euro_iaas.paasmanager.installator.ProductInstallator;
import com.telefonica.euro_iaas.paasmanager.manager.impl.EnvironmentInstanceManagerImpl;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.OS;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

/**
 * @author jesus.movilla
 */
public class EnvironmentInstanceManagerImplTest {




    /**
     * It tests updating federated networks.
     * 
     * @throws InfrastructureException
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     */
    @Test
    public void testUpdateFederatedNetworks() throws InfrastructureException, EntityNotFoundException,
            InvalidEntityException {

        //Given
        EnvironmentInstanceManagerImpl environmentInstanceManager = new EnvironmentInstanceManagerImpl();
        ClaudiaData claudiaData = new ClaudiaData("org", "vdc", "service");
        Environment environment = new Environment();
        environment.setName("environmentName");
        Tier tier = new Tier();
        tier.setInitialNumberInstances(new Integer(1));
        tier.setMaximumNumberInstances(new Integer(5));
        tier.setMinimumNumberInstances(new Integer(1));
        tier.setName("tierName");
        tier.setRegion("region1");
        tier.addNetwork(new Network("uno", "VDC", "region1"));

        Tier tier2 = new Tier();
        tier2.setInitialNumberInstances(new Integer(1));
        tier2.setMaximumNumberInstances(new Integer(5));
        tier2.setMinimumNumberInstances(new Integer(1));
        tier2.setName("tierName2");
        tier2.setRegion("region2");
        tier2.addNetwork(new Network("uno", "VDC", "region2"));

        Set<Tier> tiers = new HashSet<Tier>();
        tiers.add(tier);
        tiers.add(tier2);
        environment.setTiers(tiers);


        InfrastructureManager infrastructureManager = mock(InfrastructureManager.class);
        environmentInstanceManager.setInfrastructureManager(infrastructureManager);

        NetworkManager networkManager = mock(NetworkManager.class);
        environmentInstanceManager.setNetworkManager(networkManager);


        //when
        when(infrastructureManager.getFederatedRange(any(ClaudiaData.class), any(String.class))).thenReturn("12");
        Network net2 = new Network("uno", "VDC", "region2");
        when(networkManager.load(any(String.class), any(String.class), any(String.class))).thenReturn(net2);


        environmentInstanceManager.updateFederatedNetworks(claudiaData, environment);
        //then
        verify(networkManager, times(2)).update(any(Network.class));

    }

    /**
     * It tests the creationg of the env instance.
     * 
     * @throws InfrastructureException
     * @throws EntityNotFoundException
     * @throws InvalidEntityException
     * @throws ProductInstallatorException
     * @throws AlreadyExistsEntityException
     */
    @Test
    public void testCreateEnvironmentInstance() throws InfrastructureException, EntityNotFoundException,
            InvalidEntityException,
            AlreadyExistsEntityException, InvalidProductInstanceRequestException, ProductInstallatorException {

        //Given

        ClaudiaData claudiaData = new ClaudiaData("org", "vdc", "service");
        PaasManagerUser user = new PaasManagerUser("user", "password");
        claudiaData.setUser(user);

        // Environment
        ProductRelease productRelease = new ProductRelease("product", "2.0");
        OS os = new OS("94", "ip", "hostname", "domain");
        List<OS> oss = new ArrayList<OS>();
        oss.add(os);
        productRelease.setSupportedOOSS(oss);

        List<ProductRelease> productReleases = new ArrayList<ProductRelease>();
        productReleases.add(productRelease);

        Tier tier = new Tier();
        tier.setInitialNumberInstances(new Integer(1));
        tier.setMaximumNumberInstances(new Integer(5));
        tier.setMinimumNumberInstances(new Integer(1));
        tier.setName("tierName");
        tier.setProductReleases(productReleases);
        tier.setRegion("region1");
        tier.addNetwork(new Network("uno", "VDC", "region1"));

        Tier tier2 = new Tier();
        tier2.setInitialNumberInstances(new Integer(1));
        tier2.setMaximumNumberInstances(new Integer(5));
        tier2.setMinimumNumberInstances(new Integer(1));
        tier2.setName("tierName2");
        tier2.setProductReleases(productReleases);
        tier2.setRegion("region2");
        tier2.addNetwork(new Network("uno", "VDC", "region2"));

        Tier tier3 = new Tier();
        tier3.setInitialNumberInstances(new Integer(1));
        tier3.setMaximumNumberInstances(new Integer(5));
        tier3.setMinimumNumberInstances(new Integer(1));
        tier3.setName("tierName3");
        tier3.setRegion("region2");
        tier3.addNetwork(new Network("uno", "VDC", "region2"));


        Set<Tier> tiers = new HashSet<Tier>();
        tiers.add(tier);
        tiers.add(tier2);

        ProductReleaseDao productReleaseDao = mock(ProductReleaseDao.class);
        when(productReleaseDao.load(any(String.class))).thenReturn(productRelease);

        TierDao tierDao = mock(TierDao.class);
        when(tierDao.load(any(String.class))).thenReturn(tier);

        Environment environment = new Environment();
        environment.setName("environemntName");

        environment.setTiers(tiers);

        EnvironmentDao environmentDao = mock(EnvironmentDao.class);
        when(environmentDao.create(any(Environment.class))).thenReturn(environment);

        EnvironmentManager environmentManager = mock(EnvironmentManager.class);
        when(environmentManager.load(any(String.class), any(String.class))).thenReturn(environment);

        // Instance
        ArrayList<VM> vms = new ArrayList<VM>();
        vms.add(new VM("fqn1", "ip1", "hostname1", "domain"));
        vms.add(new VM("fqn2", "ip2", "hostname2", "domain2"));

        ProductInstance productInstance = new ProductInstance();
        productInstance.setProductRelease(productRelease);
        productInstance.setStatus(Status.INSTALLED);
        productInstance.setName("name");
        productInstance.setVdc("vdc");

        ProductInstanceManager productInstanceManager = mock(ProductInstanceManager.class);
        when(
                productInstanceManager.install(any(TierInstance.class), any(ClaudiaData.class),
                        any(EnvironmentInstance.class), any(ProductRelease.class))).thenReturn(productInstance);

        List<ProductInstance> productInstances = new ArrayList<ProductInstance>();
        productInstances.add(productInstance);

        TierInstance tierInstance = new TierInstance();
        tierInstance.setName("nametierInstance");
        tierInstance.setTier(tier);
        tierInstance.setVdc("vdc");
        tierInstance.setStatus(Status.INSTALLED);
        tierInstance.setProductInstances(productInstances);
        tierInstance.setVM(new VM("dd", "d", "d"));

        List<TierInstance> tierInstances = new ArrayList<TierInstance>();
        tierInstances.add(tierInstance);

        EnvironmentInstance environmentInstance = new EnvironmentInstance();
        environmentInstance.setName("name");
        environmentInstance.setTierInstances(tierInstances);
        environmentInstance.setVdc("vdc");
        environmentInstance.setStatus(Status.INSTALLED);
        environmentInstance.setEnvironment(environment);

        EnvironmentInstanceDao environmentInstanceDao = mock(EnvironmentInstanceDao.class);
        when(environmentInstanceDao.load(any(String.class))).thenReturn(environmentInstance);
        SystemPropertiesProvider systemPropertiesProvider = mock(SystemPropertiesProvider.class);
        EnvironmentInstanceManagerImpl environmentInstanceManager = new EnvironmentInstanceManagerImpl();
        InfrastructureManager infrastructureManager = mock(InfrastructureManager.class);
        NetworkManager networkManager = mock(NetworkManager.class);
        environmentManager = mock(EnvironmentManager.class);
        environmentInstanceManager.setInfrastructureManager(infrastructureManager);
        environmentInstanceManager.setNetworkManager(networkManager);
        environmentInstanceManager.setEnvironmentManager(environmentManager);
        TierManager tierManager = mock(TierManager.class);
        ProductReleaseManager productReleaseManager = mock(ProductReleaseManager.class);
        environmentInstanceManager.setTierManager(tierManager);
        environmentInstanceManager.setProductReleaseManager(productReleaseManager);
        environmentInstanceManager.setSystemPropertiesProvider(systemPropertiesProvider);
        TierInstanceDao tierInstanceDao = mock(TierInstanceDao.class);
        environmentInstanceManager.setTierInstanceDao(tierInstanceDao);
        environmentInstanceManager.setEnvironmentInstanceDao(environmentInstanceDao);
        ProductInstallator productInstallator = mock(ProductInstallator.class);
        environmentInstanceManager.setProductInstallator(productInstallator);
        TierInstanceManager tierInstanceManager = mock(TierInstanceManager.class);
        environmentInstanceManager.setTierInstanceManager(tierInstanceManager);
        when(systemPropertiesProvider.getProperty(any(String.class))).thenReturn("FIWARE");
        when(
                infrastructureManager.createInfrasctuctureEnvironmentInstance(any(EnvironmentInstance.class), anySet(),
                        any(ClaudiaData.class))).thenReturn(environmentInstance);
        when(environmentManager.load(any(String.class), any(String.class))).thenReturn(environment);
        when(tierManager.loadComplete(any(Tier.class))).thenReturn(tier);
        when(tierManager.update(any(Tier.class))).thenReturn(tier);
        when(productReleaseManager.load(any(String.class), any(ClaudiaData.class))).thenReturn(productRelease);
        when(environmentInstanceDao.create(any(EnvironmentInstance.class))).thenReturn(environmentInstance);

        when(environmentInstanceDao.update(any(EnvironmentInstance.class))).thenReturn(environmentInstance);

        when(environmentManager.load(any(String.class), any(String.class))).thenReturn(environment);
        when(tierManager.loadTierWithProductReleaseAndMetadata(any(String.class), any(String.class), any(String.class)))
                .thenReturn(tier3);
        //When

        environmentInstanceManager.create(claudiaData, environmentInstance);

    }

    /**
     * It tests the deletion of the env instance.
     */
    @Test
    public void testDestroyEnvironmentInstance() throws Exception {
        //given
        EnvironmentInstanceManagerImpl environmentInstanceManager = new EnvironmentInstanceManagerImpl();
        ClaudiaData claudiaData = new ClaudiaData("org", "vdc", "service");
        EnvironmentInstance environmentInstance = new EnvironmentInstance();
        environmentInstance.setName("name");
        Environment environment = new Environment();
        environment.setName("environmentName");
        environmentInstance.setEnvironment(environment);
        environmentInstance.setBlueprintName("blueprint name");
        environmentInstance.setTierInstances(new ArrayList<TierInstance>());
        EnvironmentInstanceDao environmentInstanceDao= mock(EnvironmentInstanceDao.class);
        environmentInstanceManager.setEnvironmentInstanceDao(environmentInstanceDao);
        when(environmentInstanceDao.update(environmentInstance)).thenReturn(environmentInstance);

        //When


        environmentInstanceManager.destroy(claudiaData, environmentInstance);
        //then

        verify(environmentInstanceDao).remove(any(EnvironmentInstance.class));

    }

}
