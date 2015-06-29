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

package com.telefonica.euro_iaas.paasmanager.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * @author jesus.movilla
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring-test-db-config.xml", "classpath:/spring-dao-config.xml" })
public class NetworkandSubNetDaoJpaImplTest {

    @Autowired
    private NetworkDao networkDao;
    @Autowired
    private SubNetworkDao subNetworkDao;
    public static String NETWORK_NAME = "network_name";
    public static String SUB_NETWORK_NAME = "subnetwork_name";
    public static String VDC = "vdc";
    public static String REGION = "region";

    @Test
    public void testNetworkNoSubNet() throws Exception {

        Network network = new Network(NETWORK_NAME, VDC, REGION);

        network = networkDao.create(network);
        assertNotNull(network);
        assertEquals(network.getNetworkName(), NETWORK_NAME);
        assertEquals(network.getSubNets().size(), 0);

        List<Network> networks = networkDao.findAll();
        assertNotNull(networks);

        Network networkOut = networkDao.load(NETWORK_NAME);
        assertNotNull(networkOut);
        assertEquals(networkOut.getNetworkName(), NETWORK_NAME);
        assertEquals(networkOut.getSubNets().size(), 0);

    }

    @Test(expected = com.telefonica.fiware.commons.dao.EntityNotFoundException.class)
    public void testNetworkLoadError() throws Exception {
        networkDao.load("other", VDC, REGION);
    }

    @Test
    public void testNetworkVDCNull() throws Exception {

        Network network = new Network(NETWORK_NAME, "", REGION);

        network = networkDao.create(network);
        assertNotNull(network);
        assertEquals(network.getNetworkName(), NETWORK_NAME);
        assertEquals(network.getSubNets().size(), 0);

        List<Network> networks = networkDao.findAll();
        assertNotNull(networks);

        Network networkOut = networkDao.load(NETWORK_NAME, null, REGION);
        assertNotNull(networkOut);
        assertEquals(networkOut.getNetworkName(), NETWORK_NAME);
        assertEquals(networkOut.getSubNets().size(), 0);

    }

    @Test
    public void testDestroyNetworkNoSubNet() throws InvalidEntityException, AlreadyExistsEntityException {

        Network network = new Network(NETWORK_NAME, VDC, REGION);

        network = networkDao.create(network);
        networkDao.remove(network);
        try {
            networkDao.load(NETWORK_NAME);
            fail("Should have thrown an EntityNotFoundException because the network does not exit!");
        } catch (EntityNotFoundException e) {
            assertNotNull(e);
        } catch (Exception e) {
        }

    }

    @Test
    public void testDestroySubNet() throws AlreadyExistsEntityException, InvalidEntityException {

        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, VDC, REGION);
        subNet = subNetworkDao.create(subNet);
        subNetworkDao.remove(subNet);

        try {
            subNetworkDao.load(SUB_NETWORK_NAME, VDC, REGION);
            fail("Should have thrown an EntityNotFoundException because the subnet does not exit!");
        } catch (EntityNotFoundException e) {
            assertNotNull(e);
        }

    }

    @Test(expected = com.telefonica.fiware.commons.dao.EntityNotFoundException.class)
    public void testSubNetworkLoadError2() throws Exception {
        subNetworkDao.load("other", VDC, REGION);
    }

    @Test
    public void testNetworkWithSubNets() throws Exception {

        List<Network> networks = networkDao.findAll();
        assertNotNull(networks);

        int number = networks.size();
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, VDC, REGION);

        Set<SubNetwork> subNets = new HashSet<SubNetwork>();
        subNets.add(subNet);
        Network network = new Network(NETWORK_NAME + "aa", VDC, REGION);
        network.setSubNets(subNets);

        networkDao.create(network);

        networks = networkDao.findAll();
        assertNotNull(networks);
        assertEquals(networks.size(), number + 1);

        Network networkOut = networkDao.load(NETWORK_NAME + "aa");
        assertNotNull(networkOut);
        assertEquals(networkOut.getNetworkName(), NETWORK_NAME + "aa");
        assertEquals(networkOut.getSubNets().size(), 1);

        for (SubNetwork subNet2 : networkOut.getSubNets()) {
            assertEquals(subNet2.getName(), SUB_NETWORK_NAME);
        }
    }

    @Test
    public void testDeleteNetworkWithSubNets() throws InvalidEntityException, AlreadyExistsEntityException {

        // Given
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, VDC, REGION);
        Set<SubNetwork> subNets = new HashSet<SubNetwork>();
        subNets.add(subNet);
        Network network = new Network(NETWORK_NAME, VDC, REGION);
        network.setSubNets(subNets);

        network = networkDao.create(network);

        // When
        networkDao.remove(network);

        // then
        try {
            networkDao.load(NETWORK_NAME);
            fail("Should have thrown an EntityNotFoundException because the network does not exit!");
        } catch (EntityNotFoundException e) {
            assertNotNull(e);
        }

    }

}
