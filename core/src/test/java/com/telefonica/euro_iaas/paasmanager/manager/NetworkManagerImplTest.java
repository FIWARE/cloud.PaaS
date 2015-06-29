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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.telefonica.euro_iaas.paasmanager.dao.NetworkDao;
import com.telefonica.euro_iaas.paasmanager.manager.impl.NetworkManagerImpl;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.SubNetwork;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * Network, SubNetwork and Router Manager.
 * 
 * @author henar
 */
public class NetworkManagerImplTest extends TestCase {

    private static String NETWORK_NAME = "name";
    private static String SUB_NETWORK_NAME = "subname";
    private static String CIDR = "10.100.1.0/24";
    private static String CIDR_ID = "1";

    private NetworkManagerImpl networkManager;
    private NetworkInstanceManager networkInstanceManager;
    private NetworkDao networkDao;
    private SubNetworkManager subNetworkManager = null;
    private ClaudiaData claudiaData = null;

    @Override
    @Before
    public void setUp() throws Exception {

        networkManager = new NetworkManagerImpl();
        networkDao = mock(NetworkDao.class);
        networkManager.setNetworkDao(networkDao);
        subNetworkManager = mock(SubNetworkManager.class);
        networkInstanceManager = mock(NetworkInstanceManager.class);
        networkManager.setSubNetworkManager(subNetworkManager);
        networkManager.setNetworkInstanceManager(networkInstanceManager);
        claudiaData = new ClaudiaData("dd", "dd", "service");
    }

    /**
     * It tests the creation of a network.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateNetwork() throws Exception {
        // Given
        Network net = new Network(NETWORK_NAME, "vdc", "region");
        SubNetwork subNet = new SubNetwork("sub-net-" + NETWORK_NAME + "-1", "vdc", "region");

        net.addSubNet(subNet);

        when(networkDao.load(any(String.class), any(String.class), any(String.class))).thenThrow(
                new EntityNotFoundException(Network.class, "test", net));
        when(networkDao.create(any(Network.class))).thenReturn(net);
        when(networkInstanceManager.getNumberDeployedNetwork(any(ClaudiaData.class), anyString())).thenReturn(0);
        // When

        networkManager.create(net);
        // Verity

        verify(networkDao).create(net);
        assertEquals(net.getNetworkName(), NETWORK_NAME);
        assertEquals(net.getSubNets().size(), 1);

        for (SubNetwork subNet2 : net.getSubNets()) {
            assertEquals(subNet2.getName(), "sub-net-" + NETWORK_NAME + "-1");
        }

    }

    /**
     * It tests the creation of a network.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateNetworkSubNetSpecified() throws Exception {
        // Given
        Network net = new Network(NETWORK_NAME, "vdc", "region");
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, "vdc", "region");
        net.addSubNet(subNet);

        // When
        when(networkDao.load(any(String.class), any(String.class), any(String.class))).thenThrow(
                new EntityNotFoundException(Network.class, "test", net));
        when(subNetworkManager.create(any(SubNetwork.class))).thenReturn(subNet);
        when(networkDao.create(any(Network.class))).thenReturn(net);

        // Verity
        networkManager.create(net);
        assertEquals(net.getNetworkName(), NETWORK_NAME);
        assertEquals(net.getSubNets().size(), 1);
        for (SubNetwork subNet2 : net.getSubNets()) {
            assertEquals(subNet2.getName(), SUB_NETWORK_NAME);
        }

    }

    @Test
    public void testCreateNetworkSubNetempty() throws EntityNotFoundException, InvalidEntityException,
            AlreadyExistsEntityException {
        Network net = new Network(NETWORK_NAME + "empty", null, "region");
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, "vdc", "region");
        when(subNetworkManager.create(any(SubNetwork.class))).thenReturn(subNet);
        when(networkDao.load(any(String.class), any(String.class), any(String.class))).thenReturn(net);
        networkManager.create(net);
    }

    @Test
    public void testCreateNetworkAnotherSubNetempty() throws EntityNotFoundException, InvalidEntityException,
            AlreadyExistsEntityException {
        Network net = new Network(NETWORK_NAME + "empty", null, "region");

        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, "vdc", "region");
        net.addSubNet(subNet);
        SubNetwork subNet2 = new SubNetwork(SUB_NETWORK_NAME + 2, "vdc", "region");
        when(subNetworkManager.create(any(SubNetwork.class))).thenReturn(subNet);
        when(networkDao.load(any(String.class), any(String.class), any(String.class))).thenReturn(net);
        Network net2 = new Network(NETWORK_NAME + "empty", null, "region");
        net2.addSubNet(subNet2);
        networkManager.create(net2);
    }

    /**
     * It tests the creation of a network.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateNetworkAlreadyexist() throws Exception {
        // Given
        Network net = new Network(NETWORK_NAME, "vdc", "region");
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, "vdc", "region");
        net.addSubNet(subNet);

        // When
        when(networkDao.load(any(String.class), any(String.class), any(String.class))).thenReturn(net);

        when(subNetworkManager.create(any(SubNetwork.class))).thenReturn(subNet);
        when(networkDao.create(any(Network.class))).thenReturn(net);

        // Verity
        networkManager.create(net);
        assertEquals(net.getNetworkName(), NETWORK_NAME);
        assertEquals(net.getSubNets().size(), 1);
        for (SubNetwork subNet2 : net.getSubNets()) {
            assertEquals(subNet2.getName(), SUB_NETWORK_NAME);
        }

    }

    /**
     * It tests the destruction of a network.
     * 
     * @throws Exception
     */
    @Test
    public void testDestroyNetwork() throws Exception {
        // Given
        Network net = new Network(NETWORK_NAME, "vdc", "region");
        SubNetwork subNet = new SubNetwork(SUB_NETWORK_NAME, "vdc", "region");
        net.addSubNet(subNet);

        // When
        when(networkDao.load(any(String.class))).thenThrow(new EntityNotFoundException(Network.class, "test", net));
        Mockito.doNothing().when(networkDao).remove(any(Network.class));
        when(networkDao.create(any(Network.class))).thenReturn(net);

        // Verity
        networkManager.delete(net);

    }

}
