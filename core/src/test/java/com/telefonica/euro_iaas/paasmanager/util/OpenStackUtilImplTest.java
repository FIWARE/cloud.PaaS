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

package com.telefonica.euro_iaas.paasmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;
import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.RouterInstance;
import com.telefonica.euro_iaas.paasmanager.model.SubNetworkInstance;

public class OpenStackUtilImplTest {

    private OpenStackUtilImplTestable openStackUtil;
    private OpenStackConfigUtil openStackConfig;
    private CloseableHttpClient closeableHttpClientMock;
    private CloseableHttpResponse httpResponse;
    private PaasManagerUser paasManagerUser;
    private OpenOperationUtil openOperationUtil;

    private OpenStackRegion openStackRegion;

    String CONTENT_NETWORKS = "{ " + "\"networks\": [ " + "{ " + "\"status\": \"ACTIVE\", " + "\"subnets\": [ "
            + "\"81f10269-e0a2-46b0-9583-2c83aa4cc76f\" " + " ], " + "\"name\": \"jesuspg-net\", "
            + "\"provider:physical_network\": null, " + "\"admin_state_up\": true, "
            + "\"tenant_id\": \"67c979f51c5b4e89b85c1f876bdffe31\", " + "\"router:external\": false, "
            + "\"shared\": false, " + "\"id\": \"047e6dd3-3101-434e-af1e-eea571ab57a4\", "
            + "\"provider:segmentation_id\": 29 " + "}, " + "{ " + "\"status\": \"ACTIVE\", " + "\"subnets\": [ "
            + "\"e2d10e6b-33c3-400c-88d6-f905d4cd02f2\" " + " ], " + "\"name\": \"ext-net\", "
            + "\"provider:physical_network\": null, " + "\"admin_state_up\": true, "
            + "\"tenant_id\": \"08bed031f6c54c9d9b35b42aa06b51c0\", " + "\"router:external\": true, "
            + "\"shared\": false, " + "\"id\": \"080b5f2a-668f-45e0-be23-361c3a7d11d0\", "
            + "\"provider:segmentation_id\": 1 " + "} ]} ";

    String ROUTERS = "{ " + "\"routers\": [ {" + "\"status\": \"ACTIVE\", " + " \"external_gateway_info\": { "
            + " \"network_id\": \"080b5f2a-668f-45e0-be23-361c3a7d11d0\" " + " }, " + " \"name\": \"test-rt1\", "
            + "\"admin_state_up\": true, " + "\"tenant_id\": \"08bed031f6c54c9d9b35b42aa06b51c0\", "
            + "\"routes\": [], " + "\"id\": \"5af6238b-0e9c-4c20-8981-6e4db6de2e17\"" + "} ]} ";

    String FLOATING_IPS ="{\n" +
        "    \"floating_ips\": [\n" +
        "        {\n" +
        "            \"instance_id\": asdfasdfasdf,\n" +
        "            \"ip\": \"130.206.116.130\",\n" +
        "            \"fixed_ip\": \"10.9.8.8\",\n" +
        "            \"id\": \"619a167c-eb97-414d-bfc5-6328ddf8de43\",\n" +
        "            \"pool\": \"public-ext-net-01\"\n" +
        "        },\n" +
        "        {\n" +
        "            \"instance_id\": null,\n" +
        "            \"ip\": \"130.206.112.238\",\n" +
        "            \"fixed_ip\": null,\n" +
        "            \"id\": \"8918a7d2-9bdf-4717-b9b4-6ee98251e80e\",\n" +
        "            \"pool\": \"public-ext-net-01\"\n" +
        "        } " +
        "    ]\n" +
        "}";

    @Before
    public void setUp() throws OpenStackException, ClientProtocolException, IOException {
        openStackUtil = new OpenStackUtilImplTestable();
        openStackConfig = mock(OpenStackConfigUtil.class);
        openStackUtil.setOpenStackConfigUtil(openStackConfig);
        paasManagerUser = new PaasManagerUser("user", "aa");
        paasManagerUser.setToken("1234567891234567989");
        paasManagerUser.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");

        HttpClientConnectionManager httpClientConnectionManager = mock(HttpClientConnectionManager.class);
        openStackUtil.setHttpConnectionManager(httpClientConnectionManager);

        httpResponse = mock(CloseableHttpResponse.class);
        openOperationUtil = mock(OpenOperationUtil.class);
        closeableHttpClientMock = mock(CloseableHttpClient.class);
        openStackRegion = mock(OpenStackRegion.class);
        openStackUtil.setOpenStackRegion(openStackRegion);
        openStackUtil.setOpenOperationUtil(openOperationUtil);

        HttpPost httpPost = mock(HttpPost.class);
        HttpGet httpGet = mock(HttpGet.class);

        when(closeableHttpClientMock.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        when(openOperationUtil.createNovaGetRequest(anyString(), anyString(), anyString(), anyString(),
                        anyString())).thenReturn(httpGet);
        when(openOperationUtil.createNovaPostRequest(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString())).thenReturn(httpPost);

        when(openOperationUtil.createQuantumGetRequest(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(httpPost);

    }

    @Test
    public void shouldGetAbsoluteLimitsWithResponse204() throws OpenStackException, IOException {
        // Given
        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        // when
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");

        String response = openStackUtil.getAbsoluteLimits(paasManagerUser, "region");

        // then
        assertNotNull(response);

    }

    @Test
    public void shouldLoadSubNetwork() throws OpenStackException, IOException {

        String region = "RegionOne";
        SubNetworkInstance subNet = new SubNetworkInstance("SUBNET", "vdc", "region", "CIDR");
        subNet.setIdSubNet("ID");

        // when
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        String response = openStackUtil.getSubNetworkDetails(subNet.getIdSubNet(), region, "token", "vdc");

        // then
        assertNotNull(response);
        assertEquals("ok", response);
    }

    @Test
    public void shouldDeleteSubNetwork() throws OpenStackException, IOException {

        SubNetworkInstance subNet = new SubNetworkInstance("SUBNET", "vdc", "region", "CIDR");
        subNet.setIdSubNet("ID");

        String region = "RegionOne";

        // when
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        openStackUtil.deleteSubNetwork(subNet.getIdSubNet(), region, "token", "vdc");

    }

    @Test
    public void shouldDeleteNetwork() throws OpenStackException, IOException {

        NetworkInstance net = new NetworkInstance("NETWORK", "vdc", "region");
        net.setIdNetwork("ID");

        // when
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        openStackUtil.deleteSubNetwork(net.getIdNetwork(), "region", "token", "vdc");

        verify(openOperationUtil).executeNovaRequest(any(HttpUriRequest.class));

    }

    /**
     * It adds a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldAddNetworkInterfacetoPublicRouter() throws OpenStackException, IOException {
        // given
        NetworkInstance net = new NetworkInstance("NETWORK", "vdc", "region");
        SubNetworkInstance subNet = new SubNetworkInstance("SUBNET", "vdc", "region", "CIDR");
        net.addSubNet(subNet);
        RouterInstance router = new RouterInstance();

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(ROUTERS);
        when(openStackConfig.getPublicAdminNetwork(any(PaasManagerUser.class), anyString())).thenReturn("NETWORK");
        when(openStackConfig.getPublicRouter(any(PaasManagerUser.class), anyString(), anyString()))
                .thenReturn("router");
        // When
        String response = openStackUtil.addInterfaceToPublicRouter(paasManagerUser, net, "token");

        // then
        assertNotNull(response);
    }

    /**
     * It deletes a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldDeleteNetworkInterfacetoPublicRouter() throws OpenStackException, IOException {
        // given
        NetworkInstance net = new NetworkInstance("NETWORK", "vdc", "region");
        SubNetworkInstance subNet = new SubNetworkInstance("SUBNET", "vdc", "region", "CIDR");
        net.addSubNet(subNet);
        String region = "RegionOne";

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");

        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(ROUTERS);
        when(openStackConfig.getPublicAdminNetwork(any(PaasManagerUser.class), anyString())).thenReturn("NETWORK");
        when(openStackConfig.getPublicRouter(any(PaasManagerUser.class), anyString(), anyString()))
                .thenReturn("router");

        // when
        String response = openStackUtil.deleteInterfaceToPublicRouter(paasManagerUser, net, region);

        // then
        assertNotNull(response);

    }

    /**
     * It adds a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldDestroyNetwork() throws OpenStackException, IOException {
        // given
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");

        String response = openStackUtil.deleteNetwork("networkId", "region", "token", "vdc");

        // then
        assertNotNull(response);
    }

    @Test
    public void shouldAllocateFloatingIp() throws OpenStackException, IOException {
        // given
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");

        String response = openStackUtil.allocateFloatingIP("payload", "region", "token", "vdc");

        // then
        assertNotNull(response);
    }

    @Test
    public void shouldDisallocateFloatingIp() throws OpenStackException, IOException {
        // given
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).
            thenReturn(FLOATING_IPS);

        openStackUtil.disAllocateFloatingIP("region", "token", "vdc", "130.206.81.152");

    }

    /**
     * It adds a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldListNetworks() throws OpenStackException, IOException {

        // Given
        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");

        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        // when
        String response = openStackUtil.listNetworks(paasManagerUser, "region");

        // then
        assertNotNull(response);
    }

    /**
     * it lists the subnets.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldListSubNetworks() throws OpenStackException, IOException {
        // given
        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");

        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        // when
        String response = openStackUtil.listSubNetworks(paasManagerUser, "region");

        // then
        assertNotNull(response);
    }

    /**
     * It adds a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldListPorts() throws OpenStackException, IOException {
        // given
        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");

        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn("ok");
        // when
        String response = openStackUtil.listPorts(paasManagerUser, "region");

        // then
        assertNotNull(response);

    }

    @Test(expected = OpenStackException.class)
    public void testShouldDeployVMError() throws OpenStackException, IOException {
        // given
        String payload = "";
        String content = "<badRequest code=\"400\" xmlns=\"http://docs.openstack.org/compute/api/v1.1\">"
                + "<message>Invalid key_name provided.</message></badRequest>";

        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(content);
        String response = openStackUtil.createServer(payload, "region", "token", "vdc");

        // then
        assertNotNull(response);
    }

    @Test
    public void testGetFloatingIp() throws Exception {


        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).
            thenReturn(FLOATING_IPS);
        when(openStackConfig.getPublicFloatingPool(any(PaasManagerUser.class), anyString())).
            thenReturn("public-ext-net-01");
        String floatingip = openStackUtil.getFloatingIP(paasManagerUser, "RegionOne");
        assertEquals(floatingip, "130.206.112.238");



    }

    /**
     * OpenStackUtilImplTestable.
     * 
     * @author jesus
     */
    private class OpenStackUtilImplTestable extends OpenStackUtilImpl {

        public CloseableHttpClient getHttpClient() {

            return closeableHttpClientMock;
        }
    }
}
