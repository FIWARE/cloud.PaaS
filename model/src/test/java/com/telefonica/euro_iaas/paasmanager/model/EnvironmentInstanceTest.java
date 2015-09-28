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

package com.telefonica.euro_iaas.paasmanager.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.telefonica.euro_iaas.paasmanager.model.dto.EnvironmentInstanceDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.EnvironmentInstancePDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for environmetn instances.
 *
 * @author henar
 */
public class EnvironmentInstanceTest extends TestCase {

    private EnvironmentInstance envIns = null;
    private Environment envResult;


    @Override
    @Before
    public void setUp() throws Exception {

        ProductRelease productRelease = new ProductRelease("product", "2.0");
        List<ProductRelease> productReleases = new ArrayList<ProductRelease>();
        productReleases.add(productRelease);

        Tier tier = new Tier();
        tier.setInitialNumberInstances(new Integer(1));
        tier.setMaximumNumberInstances(new Integer(5));
        tier.setMinimumNumberInstances(new Integer(1));
        tier.setName("tierName");
        tier.setProductReleases(productReleases);
        tier.setFlavour("3");
        tier.setFloatingip("true");
        tier.setImage("image");

        Tier tier2 = new Tier("tierName2", new Integer(1), new Integer(1), new Integer(1), productReleases, "2",
                "image", "icone");

        Set<Tier> tiers = new HashSet<Tier>();
        tiers.add(tier);
        tiers.add(tier2);

        envResult = new Environment();
        envResult = new Environment();
        envResult.setName("environemntName");
        envResult.setTiers(tiers);

        envIns = new EnvironmentInstance("blue", "description");

        envIns.setEnvironment(envResult);

        VM vm = new VM("fqn", "ip", "hostname", "domain");
        ProductInstance productInstance = new ProductInstance();
        productInstance.setName("produInst");
        productInstance.setProductRelease(productRelease);
        TierInstance tierInstance = new TierInstance(tier, "ovf", "tierInstance", vm);
        List<ProductInstance> lProductInstance = new ArrayList<ProductInstance>();
        lProductInstance.add(productInstance);
        tierInstance.setProductInstances(lProductInstance);
        List<TierInstance> tieInstances = new ArrayList<TierInstance>();
        tieInstances.add(tierInstance);

        envIns.setTierInstances(tieInstances);
    }

    /**
     * Test the creation of a Environment.
     * @throws Exception    Any exception launched during the creation of the Environment.
     */
    @Test
    public void testCreateEnvironment() throws Exception {

        assertEquals(envIns.getEnvironment().getTiers().size(), 2);
        assertEquals(envIns.getTierInstances().size(), 1);
        assertEquals(envIns.getTierInstances().get(0).getProductInstances().size(), 1);
        assertEquals(envIns.getTierInstances().get(0).getProductInstances().get(0).getName(), "produInst");
        assertEquals(envIns.getTierInstances().get(0).getProductInstances().get(0).getProductRelease().getProduct(),
                "product");

        EnvironmentInstanceDto envInstDto = envIns.toDto();
        envInstDto.setEnvironmentDto(envResult.toDto());
        EnvironmentInstance envInst2 = envInstDto.fromDto();

    }

    /**
     * Test for create environment dto.
     *
     * @throws Exception
     */
    @Test
    public void testCreateEnvironmentToPDto() throws Exception {

        EnvironmentInstancePDto enviromentIsntanceDto = envIns.toPDto();

        assertEquals(enviromentIsntanceDto.getTiers().size(), envResult.getTiers().size());
        assertEquals(enviromentIsntanceDto.getTiers().get(0).getProductReleaseDtos().size(), 1);
        assertEquals(enviromentIsntanceDto.getTiers().get(1).getTierInstances().size(), 1);
        assertEquals(enviromentIsntanceDto.getTiers().get(1).getTierInstances().get(0).getTierInstanceName(),
                "tierInstance");
        assertEquals(enviromentIsntanceDto.getTiers().get(1).getTierInstances().get(0).getVM().getFqn(), "fqn");

    }

    /**
     * Create an environment without vm.
     *
     * @throws Exception
     */
    @Test
    public void testCreateEnvironmentToPDtoNoVM() throws Exception {
        envIns.getTierInstances().get(0).setVM(null);

        EnvironmentInstancePDto enviromentIsntanceDto = envIns.toPDto();

        assertEquals(enviromentIsntanceDto.getTiers().size(), envResult.getTiers().size());
        assertEquals(enviromentIsntanceDto.getTiers().get(0).getProductReleaseDtos().size(), 1);
     //   assertEquals(enviromentIsntanceDto.getTiers().get(0).getTierInstances().size(), 1);
      //  assertEquals(enviromentIsntanceDto.getTiers().get(0).getTierInstances().get(0).getTierInstanceName(),
        //        "tierInstance");

    }



}
