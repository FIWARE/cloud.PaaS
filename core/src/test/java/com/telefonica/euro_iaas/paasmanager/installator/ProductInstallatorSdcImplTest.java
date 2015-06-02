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

package com.telefonica.euro_iaas.paasmanager.installator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCClient;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtil;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.OS;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService;
import com.telefonica.euro_iaas.sdc.model.Task;
import com.telefonica.euro_iaas.sdc.model.dto.ProductInstanceDto;

/**
 * Unit test suite for ProductInstallatorSdcImpl.
 * 
 * @author Jesus M. Movilla
 */
public class ProductInstallatorSdcImplTest {

    private SystemPropertiesProvider systemPropertiesProvider;
    private SDCClient sdcClient;
    private ProductInstanceService service;
    private ProductReleaseManager productReleaseManager;
    private TierInstanceManager tierInstanceManager;
    private SDCUtil sDCUtil;

    private ProductInstance expectedProductInstance;
    private TierInstance tierInstance;
    private ProductRelease productRelease;
    private OS os;
    private Set<Attribute> lAttributes;
    private Attribute attribute;
    private final VM host = new VM("fqn", "ip", "hostname", "domain");
    private Task task;
    private com.telefonica.euro_iaas.sdc.model.ProductInstance pInstanceSDC;
    ClaudiaData data;
    private EnvironmentInstance environmentInstance;
    private TierInstance tierInstance2;
    private ProductRelease productRelease2;
    private Set<Attribute> lAttributes2;
    private Attribute attribute2;
    private ProductInstance expectedProductInstance2;
    private List<TierInstance> tierInstancesList;

    private static String SDC_SERVER_MEDIATYPE = "application/json";

    @Before
    public void setUp() throws Exception {

        os = new OS("os1", "1", "os1 description", "v1");
        host.setOsType(os.getOsType());
        host.setFqn("xx.vees.xx");
        Set<Attribute> lAttributes = new HashSet<Attribute>();
        attribute = new Attribute("key", "value", "Plain");
        lAttributes.add(attribute);

        tierInstance = new TierInstance();
        // productRelease = new ProductRelease("productPrueba", "1.0",
        // "Product Prueba desc", Arrays.asList(attribute),
        // null, Arrays.asList(os), true, productType);
        productRelease = new ProductRelease("productPrueba", "1.0");

        productRelease.addAttribute(attribute);
        productRelease.addAttribute(new Attribute("sdcgroupid", "sdcgroupid"));
        productRelease.addAttribute(new Attribute("idcoregroup", "idcoregroup"));
        productRelease.addAttribute(new Attribute("id_web_server", "id_web_server"));
        productRelease.setDescription("Product Prueba desc");
        productRelease.setSupportedOOSS(Arrays.asList(os));
        productRelease.setWithArtifact(true);

        expectedProductInstance = new ProductInstance(productRelease, Status.INSTALLED, "vdc");
        expectedProductInstance.setPrivateAttributes(lAttributes);

        tierInstance.addProductInstance(expectedProductInstance);
        tierInstance.setVM(host);

        systemPropertiesProvider = mock(SystemPropertiesProvider.class);

        tierInstanceManager = mock(TierInstanceManager.class);

        productReleaseManager = mock(ProductReleaseManager.class);
        when(productReleaseManager.load(any(String.class), any(ClaudiaData.class))).thenReturn(productRelease);
        when(productReleaseManager.load(any(String.class), any(String.class))).thenReturn(productRelease);

        task = new Task();
        task.setHref("http://130.206.80.119:8081/sdc2/rest/vdc/60b4125450fc4a109f50357894ba2e28/task/581");

        pInstanceSDC = mock(com.telefonica.euro_iaas.sdc.model.ProductInstance.class);
        when(pInstanceSDC.getVm()).thenReturn(new com.telefonica.euro_iaas.sdc.model.dto.VM("aa", "bb", "cc", "dd"));

        service = mock(ProductInstanceService.class);
        when(
                service.install(Mockito.anyString(), Mockito.any(ProductInstanceDto.class), Mockito.anyString(),
                        Mockito.anyString())).thenReturn(task);
        when(service.load(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(pInstanceSDC);

        sdcClient = mock(SDCClient.class);
        when(sdcClient.getProductInstanceService(Mockito.anyString(), Mockito.anyString())).thenReturn(service);

        sDCUtil = mock(SDCUtil.class);

        data = mock(ClaudiaData.class);
        PaasManagerUser user = mock(PaasManagerUser.class);

        when(data.getUser()).thenReturn(user);
        when(data.getOrg()).thenReturn("FIWARE");
        when(data.getService()).thenReturn("deploytm");
        when(data.getVdc()).thenReturn("60b4125450fc4a109f50357894ba2e28");
        when(user.getToken()).thenReturn("any");

        environmentInstance = mock(EnvironmentInstance.class);
        tierInstancesList = new ArrayList<TierInstance>();
        tierInstancesList.add(tierInstance);
        productRelease2 = new ProductRelease("productPrueba", "1.0");

        productRelease2.addAttribute(attribute);
        productRelease2.addAttribute(new Attribute("sdcgroupid", "sdcgroupid"));
        productRelease2.addAttribute(new Attribute("idcoregroup", "idcoregroup"));
        productRelease2.addAttribute(new Attribute("id_web_server", "id_web_server"));
        productRelease2.setDescription("Product Prueba desc");
        productRelease2.setSupportedOOSS(Arrays.asList(os));
        productRelease2.setWithArtifact(true);
        Set<Attribute> lAttributes2 = new HashSet<Attribute>();
        attribute2 = new Attribute("key2", "value2", "PLAIN");
        lAttributes2.add(attribute2);
        expectedProductInstance2 = new ProductInstance(productRelease2, Status.INSTALLED, "vdc");
        expectedProductInstance2.setPrivateAttributes(lAttributes2);

        tierInstance.addProductInstance(expectedProductInstance);
        tierInstance.setVM(host);

        /*
         * when(sDCUtil.checkTaskStatus(any(Task.class), Mockito.anyString()));
         */
    }

    @Test
    public void testConfigureWhenEverithingIsOk() throws Exception {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        installator.setSDCClient(sdcClient);
        installator.setSystemPropertiesProvider(systemPropertiesProvider);
        installator.setSDCUtil(sDCUtil);

        ProductInstance productInstance = new ProductInstance(productRelease, Status.INSTALLED, "vdc");
        productInstance.setName("service-tier-1_" + productRelease.getProduct() + "_" + productRelease.getVersion());

        productInstance.setPrivateAttributes(lAttributes);
        List<Attribute> attributes = new ArrayList();
        attributes.add(new Attribute("dd", "ddd"));

        installator.configure(data, productInstance, attributes);

    }

    @Test
    public void testInstallWhenEverithingIsOk() throws Exception {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        installator.setSDCClient(sdcClient);
        installator.setSystemPropertiesProvider(systemPropertiesProvider);
        installator.setProductReleaseManager(productReleaseManager);
        installator.setSDCUtil(sDCUtil);
        installator.setTierInstanceManager(tierInstanceManager);

        ProductInstance installedProduct = installator.install(data, environmentInstance, tierInstance,
                expectedProductInstance.getProductRelease());
        // make verifications
        assertEquals(expectedProductInstance, installedProduct);

    }

    @Test
    public void testInstallWithOutAttributesWhenEverithingIsOk() throws Exception {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        installator.setSDCClient(sdcClient);
        installator.setSystemPropertiesProvider(systemPropertiesProvider);
        installator.setProductReleaseManager(productReleaseManager);
        installator.setSDCUtil(sDCUtil);
        installator.setTierInstanceManager(tierInstanceManager);

        ProductRelease productReleaseWithoutAttrs = new ProductRelease("productPrueba", "1.0");

        productReleaseWithoutAttrs.setDescription("Product Prueba desc");
        productReleaseWithoutAttrs.setSupportedOOSS(Arrays.asList(os));
        productReleaseWithoutAttrs.setWithArtifact(true);

        ProductInstance installedProduct = installator.install(data, environmentInstance, tierInstance,
                productReleaseWithoutAttrs);
        // make verifications
        assertEquals(expectedProductInstance, installedProduct);
    }

    @Test
    public void testInstallWithoutAttributesWhenEverithingIsOk() throws Exception {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        installator.setSDCClient(sdcClient);
        installator.setSystemPropertiesProvider(systemPropertiesProvider);
        installator.setProductReleaseManager(productReleaseManager);
        installator.setSDCUtil(sDCUtil);
        installator.setTierInstanceManager(tierInstanceManager);

        ProductInstance installedProduct = installator.install(data, environmentInstance, tierInstance,
                expectedProductInstance.getProductRelease());
        // make verifications
        assertEquals(expectedProductInstance, installedProduct);
    }

    @Test
    public void testObtainProductInstanceName() throws Exception {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        installator.setSDCClient(sdcClient);
        installator.setSystemPropertiesProvider(systemPropertiesProvider);
        installator.setProductReleaseManager(productReleaseManager);
        installator.setSDCUtil(sDCUtil);
        installator.setTierInstanceManager(tierInstanceManager);

        expectedProductInstance.setName("deploytm-contextbrokr-1_mongos_2.2.3");
        String name = installator.getProductInstanceName(data, expectedProductInstance);
        // make verifications
        assertEquals(name, "FIWARE.customers.60b4125450fc4a109f50357894ba2e28.services."
                + "deploytm.vees.contextbrokr.replicas.1_mongos_2.2.3");

        // only one product will be installed, the other one causes error.
        // verify(productInstanceDao,
        // times(1)).update(any(ProductInstance.class));
        // verify(productInstanceDao, times(1)).findUniqueByCriteria(
        // any(ProductInstanceSearchCriteria.class));
    }

    @Test
    public void testResolveMacroNamesIP() {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        com.telefonica.euro_iaas.sdc.model.Attribute att = new com.telefonica.euro_iaas.sdc.model.Attribute("key",
                "IP(pepe)", "desc", "IP");
        Attribute attRel = new Attribute("key", "IP(pepe)", "desc", "IP");

        Set<Attribute> lAtt = new HashSet<Attribute>();
        lAtt.add(attRel);

        ProductRelease productRelease1 = new ProductRelease("productPruebaAttr", "1.0");
        productRelease1.addAttribute(attRel);
        productRelease1.addAttribute(new Attribute("sdcgroupid", "sdcgroupid"));

        ProductInstance productInst = new ProductInstance(productRelease, Status.INSTALLED, "vdc");
        productInst.setPrivateAttributes(lAtt);

        TierInstance tierIns = new TierInstance();
        tierIns.setName("inst-qag-pepe-1-000129");
        tierIns.setNumberReplica(1);
        tierIns.addProductInstance(productInst);
        VM hostAtt = new VM("fqn", "111.111.111.111", "testATT", "domain");
        tierIns.setVM(hostAtt);

        List<TierInstance> til = new ArrayList<TierInstance>();
        til.add(tierIns);
        when(environmentInstance.getTierInstances()).thenReturn(til);

        InfrastructureManager infrastructureManager = mock(InfrastructureManager.class);

        when(infrastructureManager.generateVMName(anyString(), anyString(), anyInt(), anyString())).thenReturn(
                "inst-qag-pepe-1-000129");

        installator.setInfrastructureManager(infrastructureManager);

        com.telefonica.euro_iaas.sdc.model.Attribute newAtt = installator.resolveAttributeTypeValue(att, tierInstance,
                environmentInstance);

        assertEquals("111.111.111.111", newAtt.getValue());

    }

    @Test
    public void testResolveMacroNamesIpAll() {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        com.telefonica.euro_iaas.sdc.model.Attribute att = new com.telefonica.euro_iaas.sdc.model.Attribute("key",
                "IPALL(pepe)", "desc", "IPALL");
        Attribute attRel = new Attribute("key", "IP(pepe)", "desc", "IP");

        Set<Attribute> lAtt = new HashSet<Attribute>();
        lAtt.add(attRel);

        ProductRelease productRelease1 = new ProductRelease("productPruebaAttr", "1.0");
        productRelease1.addAttribute(attRel);
        productRelease1.addAttribute(new Attribute("sdcgroupid", "sdcgroupid"));

        ProductInstance productInst = new ProductInstance(productRelease, Status.INSTALLED, "vdc");
        productInst.setPrivateAttributes(lAtt);

        TierInstance tierIns = new TierInstance();
        tierIns.setName("inst-qag-pepe-1-000129");
        tierIns.setNumberReplica(1);
        tierIns.addProductInstance(productInst);
        VM hostAtt = new VM("fqn", "111.111.111.111", "testATT", "domain");
        tierIns.setVM(hostAtt);

        TierInstance tierIns2 = new TierInstance();
        tierIns2.setName("inst-qag-pepe2-1-000129");
        tierIns2.setNumberReplica(1);
        tierIns2.addProductInstance(productInst);
        VM hostAtt2 = new VM("fqn", "222.222.222.222", "testATT2", "domain");
        tierIns2.setVM(hostAtt2);

        List<TierInstance> til = new ArrayList<TierInstance>();
        til.add(tierIns);
        til.add(tierIns2);
        when(environmentInstance.getTierInstances()).thenReturn(til);

        InfrastructureManager infrastructureManager = mock(InfrastructureManager.class);

        when(infrastructureManager.generateVMName(anyString(), anyString(), anyInt(), anyString())).thenReturn(
                "inst-qag-pepe-1-000129").thenReturn("inst-qag-pepe2-1-000129");

        installator.setInfrastructureManager(infrastructureManager);

        com.telefonica.euro_iaas.sdc.model.Attribute newAtt = installator.resolveAttributeTypeValue(att, tierInstance,
                environmentInstance);

        assertEquals("111.111.111.111,222.222.222.222", newAtt.getValue());

    }

    @Test
    public void testResolveMacroDefaultValue() {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        com.telefonica.euro_iaas.sdc.model.Attribute att = new com.telefonica.euro_iaas.sdc.model.Attribute("key",
            "default", "desc", "IP");
        com.telefonica.euro_iaas.sdc.model.Attribute newAtt = installator.resolveAttributeTypeValue(att, tierInstance,
            environmentInstance);
        assertEquals("default", newAtt.getValue());

    }

    @Test
    public void testResolveMacroWrongValue() {
        ProductInstallatorSdcImpl installator = new ProductInstallatorSdcImpl();
        com.telefonica.euro_iaas.sdc.model.Attribute att = new com.telefonica.euro_iaas.sdc.model.Attribute("key",
            "IP(wrong", "desc", "IP");
        com.telefonica.euro_iaas.sdc.model.Attribute newAtt = installator.resolveAttributeTypeValue(att, tierInstance,
            environmentInstance);
        assertEquals("IP(wrong", newAtt.getValue());

    }
}
