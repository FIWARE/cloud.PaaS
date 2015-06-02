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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.claudia.ClaudiaClient;
import com.telefonica.euro_iaas.paasmanager.dao.EnvironmentInstanceDao;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.NetworkManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.util.ClaudiaResponseAnalyser;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

/**
 * @author jesus.movilla
 */
public class InfrastructureManagerClaudiaImplTest {

    private SystemPropertiesProvider propertiesProvider;
    private ClaudiaClient claudiaClient;
    private ClaudiaResponseAnalyser claudiaResponseAnalyser;

    private EnvironmentInstanceDao environmentInstanceDao;
    private NetworkInstanceManager networkInstanceManager;

    // private String ovfname = "Case01-01-initial-vapp-creation.xml";
    private PaasManagerUser user;
    private ClaudiaData claudiaData;
    private TierInstanceManager tierInstanceManager;
    private TierManager tierManager;
    private InfrastructureManagerClaudiaImpl manager;
    private NetworkManager networkManager;
    private Tier tier;
    private VM vm;

    @Before
    public void setUp() throws Exception {

        propertiesProvider = mock(SystemPropertiesProvider.class);
        claudiaClient = mock(ClaudiaClient.class);
        claudiaResponseAnalyser = mock(ClaudiaResponseAnalyser.class);
        tierInstanceManager = mock(TierInstanceManager.class);
        tierManager = mock(TierManager.class);
        networkManager = mock(NetworkManager.class);
        environmentInstanceDao = mock(EnvironmentInstanceDao.class);
        networkInstanceManager = mock(NetworkInstanceManager.class);
        manager = new InfrastructureManagerClaudiaImpl();
        manager.setSystemPropertiesProvider(propertiesProvider);
        manager.setClaudiaClient(claudiaClient);
        manager.setClaudiaResponseAnalyser(claudiaResponseAnalyser);
        manager.setTierInstanceManager(tierInstanceManager);
        manager.setEnvironmentInstanceDao(environmentInstanceDao);
        manager.setNetworkInstanceManager(networkInstanceManager);
        manager.setNetworkManager(networkManager);
        manager.setTierManager(tierManager);

        claudiaData = new ClaudiaData("org", "vdc", "service");

        tier = new Tier("name", new Integer(1), new Integer(1), new Integer(1), null, "flavour", "image", "icono",
                "keypair", "false", "payload");
        String hostname = claudiaData.getService() + "-" + tier.getName() + "-" + 1;
        String fqn = claudiaData.getOrg().replace("_", ".") + ".customers." + claudiaData.getVdc() + ".services."
                + claudiaData.getService() + ".vees." + tier.getName() + ".replicas." + 1;

        vm = new VM();
        vm.setFqn(fqn);
        vm.setHostname(hostname);
        vm.setIp("IP");

    }

    @Test
    public void testCreateInfrastructureEnvironmentInstance() throws Exception {

        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);

        Set<Tier> lTier = new HashSet<Tier>();
        lTier.add(tier);
        Environment env = new Environment("name", "description", lTier);
        EnvironmentInstance envInst = new EnvironmentInstance("blue", "des", env);
        when(propertiesProvider.getProperty("openstack-tcloud.cloudSystem")).thenReturn("4caast");
        when(propertiesProvider.getProperty("vmDeploymentDelay")).thenReturn("1");
        List<String> ips = new ArrayList<String>();
        ips.add("IP");
        List<String> ovfs = new ArrayList<String>();
        ovfs.add(null);

        String hostname = claudiaData.getService() + "-" + tier.getName() + "-" + 1 + "-vdc";
        String fqn = claudiaData.getOrg().replace("_", ".") + ".customers." + claudiaData.getVdc() + ".services."
                + claudiaData.getService() + ".vees." + tier.getName() + ".replicas." + 1;

        VM vm = new VM();

        vm.setFqn(fqn);
        vm.setHostname(hostname);
        vm.setIp("IP");

        tierInstance.setVM(vm);

        when(
                claudiaClient.getIP(any(ClaudiaData.class), any(String.class), Matchers.anyInt(), any(VM.class),
                        anyString())).thenReturn(ips);
        Mockito.doNothing().when(claudiaClient)
                .deployVM(any(ClaudiaData.class), any(TierInstance.class), Matchers.anyInt(), any(VM.class));
        when(propertiesProvider.getProperty("openstack-tcloud.cloudSystem")).thenReturn("4caast");
        when(propertiesProvider.getProperty("vmDeploymentDelay")).thenReturn("1");
        when(claudiaClient.browseVDC(any(ClaudiaData.class))).thenReturn("vdc");
        when(claudiaClient.getIP(any(ClaudiaData.class), any(String.class), anyInt(), any(VM.class), any(String.class)))
                .thenReturn(ips);
        when(claudiaClient.browseService(any(ClaudiaData.class))).thenReturn("vapp");
        when(environmentInstanceDao.update(any(EnvironmentInstance.class))).thenReturn(envInst);
        when(
                claudiaClient.browseVMReplica(any(ClaudiaData.class), any(String.class), anyInt(), any(VM.class),
                        anyString())).thenReturn("vapp");

        Mockito.doThrow(new EntityNotFoundException(TierInstance.class, "test", tierInstance))
                .when(tierInstanceManager).load(any(String.class));
        when(tierInstanceManager.create(any(ClaudiaData.class), any(String.class), any(TierInstance.class)))
                .thenReturn(tierInstance);
        when(tierManager.loadTierWithNetworks(any(String.class), any(String.class), any(String.class)))
                .thenReturn(tier);

        when(tierManager.loadTierWithProductReleaseAndMetadata(any(String.class), any(String.class), any(String.class)))
                .thenReturn(tier);

        EnvironmentInstance envInst2 = manager.createInfrasctuctureEnvironmentInstance(envInst, envInst
                .getEnvironment().getTiers(), claudiaData);
        assertEquals(envInst2.getBlueprintName(), "blue");
        // assertEquals(envInst2.getName(),
        // claudiaData.getVdc()+"-"+tier.getName());
        assertEquals(envInst2.getTierInstances().size(), 1);
        assertEquals(envInst2.getTierInstances().get(0).getTier().getName(), tier.getName());

        assertEquals(envInst2.getTierInstances().get(0).getVM().getHostname(), hostname);
        verify(tierInstanceManager, times(1)).create(claudiaData, "name", tierInstance);

    }

    @Test
    public void testDeleteInfrastructureEnvironmentInstance() throws Exception {

        Network net = new Network("NET", "vdc", "region");
        tier.addNetwork(net);
        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);
        tierInstance.addNetworkInstance(net.toNetworkInstance());

        Set<Tier> lTier = new HashSet<Tier>();
        lTier.add(tier);
        Environment env = new Environment("name", "description", lTier);
        EnvironmentInstance envInst = new EnvironmentInstance("blue", "des", env);

        String ovf = null;
        tierInstance.setOvf(ovf);
        tierInstance.setVapp("vapp");
        tierInstance.setVM(vm);
        envInst.addTierInstance(tierInstance);

        when(
                claudiaClient.browseVMReplica(any(ClaudiaData.class), any(String.class), anyInt(), any(VM.class),
                        anyString())).thenReturn("vapp");
        Mockito.doNothing().when(claudiaClient).undeployVMReplica(any(ClaudiaData.class), any(TierInstance.class));
        Mockito.doNothing().when(networkInstanceManager)
                .delete(any(ClaudiaData.class), any(NetworkInstance.class), anyString());
        when(tierInstanceManager.loadNetworkInstnace(any(String.class))).thenReturn(tierInstance);
        manager.deleteEnvironment(claudiaData, envInst);

    }

    @Test
    public void testDeployVMNoFIWARE() throws Exception {
        Tier tier = new Tier("name", new Integer(1), new Integer(1), new Integer(1), null, "flavour", "image", "icono",
                "keypair", "floatingip", "payload");
        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);
        when(propertiesProvider.getProperty(any(String.class))).thenReturn("1");
        List<String> ips = new ArrayList();
        ips.add("IP");

        String hostname = claudiaData.getService() + "-" + tier.getName() + "-" + 1;
        String fqn = claudiaData.getOrg().replace("_", ".") + ".customers." + claudiaData.getVdc() + ".services."
                + claudiaData.getService() + ".vees." + tier.getName() + ".replicas." + 1;

        VM vm = new VM();

        vm.setFqn(fqn);
        vm.setHostname(hostname);

        when(
                claudiaClient.getIP(any(ClaudiaData.class), any(String.class), Matchers.anyInt(), any(VM.class),
                        anyString())).thenReturn(ips);
        Mockito.doNothing().when(claudiaClient)
                .deployVM(any(ClaudiaData.class), any(TierInstance.class), Matchers.anyInt(), any(VM.class));
        manager.deployVM(claudiaData, tierInstance, 1, vm);
        assertEquals(vm.getDomain(), "");

        assertEquals(vm.getHostname(), hostname);
        assertEquals(vm.getIp(), "IP");

    }

    @Test
    public void testDeployNetwork() throws Exception {

        Network net = new Network("NET", "vdc", "region");
        tier.addNetwork(net);
        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);

        when(tierInstanceManager.update(any(TierInstance.class))).thenReturn(tierInstance);
        when(networkInstanceManager.create(any(ClaudiaData.class), any(NetworkInstance.class), anyString()))
                .thenReturn(net.toNetworkInstance());
        when(networkManager.load(any(String.class), any(String.class), any(String.class))).thenReturn(net);
        when(tierManager.loadTierWithNetworks(any(String.class), any(String.class), any(String.class)))
                .thenReturn(tier);

        Mockito.doThrow(EntityNotFoundException.class).when(networkInstanceManager)
                .load(any(String.class), any(ClaudiaData.class), any(String.class));

        manager.deployNetworks(claudiaData, tierInstance);

        assertEquals(tierInstance.getNetworkInstances().size(), 1);

    }

    @Test
    public void testDeployNetworkInternet() throws Exception {

        Network net = new Network("NET", "vdc", "region");
        Network net2 = new Network("Internet", "vdc", "region");
        tier.addNetwork(net);
        tier.addNetwork(net2);
        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);

        when(tierManager.loadTierWithNetworks(any(String.class), any(String.class), any(String.class)))
                .thenReturn(tier);
        when(tierManager.update(any(Tier.class))).thenReturn(tier);
        when(networkInstanceManager.create(any(ClaudiaData.class), any(NetworkInstance.class), anyString()))
                .thenReturn(net.toNetworkInstance());
        when(networkManager.load(any(String.class), any(String.class), any(String.class))).thenReturn(net);
        Mockito.doThrow(EntityNotFoundException.class).when(networkInstanceManager)
                .load(any(String.class), any(ClaudiaData.class), any(String.class));

        manager.deployNetworks(claudiaData, tierInstance);

        assertEquals(tierInstance.getNetworkInstances().size(), 1);
        assertEquals(tierInstance.getTier().getFloatingip(), "true");

    }

    @Test
    public void testDeleteNetworkWithoutEnv() throws Exception {

        Network net = new Network("NET", "vdc", "region");
        Network net2 = new Network("Internet", "vdc", "region");
        tier.addNetwork(net);
        tier.addNetwork(net2);
        TierInstance tierInstance = new TierInstance();
        tierInstance.setTier(tier);
        Environment env = new Environment("name", "description", null);
        env.addTier(tier);
        EnvironmentInstance envInst = new EnvironmentInstance("blue", "des", env);
        envInst.addTierInstance(tierInstance);
        String region = "RegionOne";

        when(tierInstanceManager.update(any(TierInstance.class))).thenReturn(tierInstance);
        when(networkInstanceManager.create(any(ClaudiaData.class), any(NetworkInstance.class), anyString()))
                .thenReturn(net.toNetworkInstance());

        Mockito.doThrow(EntityNotFoundException.class).when(networkInstanceManager)
                .load(any(String.class), any(ClaudiaData.class), any(String.class));

        manager.deleteNetworksInTierInstance(claudiaData, tierInstance);

        assertEquals(envInst.getTierInstances().get(0).getNetworkInstances().size(), 0);

    }

    @Test
    public void shouldGenerateValidTierInstanceName() {
        // given

        // when
        String name = manager.generateVMName("bpName", "tierName", 1, "00000000000000vdc");

        // then
        assertNotNull(name);
        assertEquals("bpName-tierName-1-000vdc", name);
    }

    @Test
    public void shouldGenerateValidTierInstanceNameWithShortVdc() {
        // given

        // when
        String name = manager.generateVMName("bpName", "tierName", 1, "vdc");

        // then
        assertNotNull(name);
        assertEquals("bpName-tierName-1-vdc", name);
    }

}
