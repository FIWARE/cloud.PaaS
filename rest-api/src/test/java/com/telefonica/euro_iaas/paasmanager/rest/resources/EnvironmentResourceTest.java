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

package com.telefonica.euro_iaas.paasmanager.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.exception.AlreadyExistEntityException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentRequestException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.dto.EnvironmentDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.ProductReleaseDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.TierDto;
import com.telefonica.euro_iaas.paasmanager.rest.exception.APIException;
import com.telefonica.euro_iaas.paasmanager.rest.util.OVFGeneration;
import com.telefonica.euro_iaas.paasmanager.rest.validation.EnvironmentResourceValidator;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;

public class EnvironmentResourceTest extends TestCase {

    public EnvironmentResourceImpl environmentResource;
    public EnvironmentManager environmentManager;
    public SystemPropertiesProvider systemPropertiesProvider;
    public EnvironmentResourceValidator validator;
    public OVFGeneration ovfGeneration;

    @Override
    @Before
    public void setUp() throws Exception {
        environmentResource = new EnvironmentResourceImpl();
        environmentManager = mock(EnvironmentManager.class);
        ovfGeneration = mock(OVFGeneration.class);
        systemPropertiesProvider = mock(SystemPropertiesProvider.class);
        validator = mock(EnvironmentResourceValidator.class);
        environmentResource.setEnvironmentManager(environmentManager);

        environmentResource.setSystemPropertiesProvider(systemPropertiesProvider);
        environmentResource.setEnvironmentResourceValidator(validator);
        environmentResource.setOvfGeneration(ovfGeneration);

        when(ovfGeneration.createOvf(any(EnvironmentDto.class))).thenReturn("ovf");

       

        Environment environment = new Environment();
        environment.setName("Name");
        environment.setDescription("Description");

        List<ProductRelease> productRelease = new ArrayList<ProductRelease>();
        productRelease.add(new ProductRelease("test", "0.1"));

        Tier tier = new Tier("tiername", new Integer(1), new Integer(1), new Integer(1), productRelease);

        tier.setImage("image");
        tier.setIcono("icono");
        tier.setFlavour("flavour");
        tier.setFloatingip("floatingip");
        tier.setKeypair("keypair");

        Set<Tier> tiers = new HashSet<Tier>();
        tiers.add(tier);
        environment.setTiers(tiers);

        when(environmentManager.create(any(ClaudiaData.class), any(Environment.class))).thenReturn(environment);
        when(environmentManager.load(any(String.class), any(String.class))).thenThrow(
                new EntityNotFoundException(Environment.class, "", environment));
        when(systemPropertiesProvider.getProperty(any(String.class))).thenReturn("FIWARE2");

    }

    @Test
    public void testCreateEnvironmentNoTiers() throws Exception {
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setDescription("Description");
        environmentDto.setName("Name");
        Mockito.doNothing()
        .doThrow(new RuntimeException())
        .when(validator)
        .validateCreate(any(ClaudiaData.class), any(EnvironmentDto.class), any(String.class));

        boolean thrown = false;
        try {
            environmentResource.insert("org", "vdc", environmentDto);
        } catch (Exception e) {
            thrown = true;
        }

    }

    @Test
    public void testInsertEnvironment() throws InvalidEnvironmentRequestException, AlreadyExistEntityException, InvalidEntityException, com.telefonica.euro_iaas.paasmanager.exception.InvalidEntityException {
    	 Mockito.doNothing()
         .doThrow(new RuntimeException())
         .when(validator)
         .validateCreate(any(ClaudiaData.class), any(EnvironmentDto.class), any(String.class));
        EnvironmentDto environmentDto = new EnvironmentDto();
        environmentDto.setName("Name");
        environmentDto.setDescription("Description");

        List<ProductReleaseDto> productReleaseDto = new ArrayList<ProductReleaseDto>();
        productReleaseDto.add(new ProductReleaseDto("test", "0.1"));

        TierDto tierDto = new TierDto("tiername", new Integer(1), new Integer(1), new Integer(1), productReleaseDto);

        tierDto.setImage("image");
        tierDto.setIcono("icono");
        tierDto.setFlavour("flavour");
        tierDto.setFloatingip("floatingip");
        tierDto.setKeypair("keypair");

        tierDto.setSecurityGroup("security_group");

        Set<TierDto> tiers = new HashSet<TierDto>();
        tiers.add(tierDto);

        environmentDto.setTierDtos(tiers);

        try {
            environmentResource.insert("org", "vdc", environmentDto);
        } catch (APIException e) {
            fail();
        }
    }
    
    

}
