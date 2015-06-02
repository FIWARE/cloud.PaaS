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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;
import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;

public class OpenStackConfigUtilImplTest {

    private OpenStackConfigUtilImplTestable openStackUtil;
    private CloseableHttpClient closeableHttpClientMock;
    private PaasManagerUser paasManagerUser;
    private OpenOperationUtil openOperationUtil;

    private OpenStackRegion openStackRegion;

    String CONTENT_NETWORKS = "{\"networks\": [{\"status\": \"ACTIVE\", \"subnets\":" +
        " [\"f948cae1-fec2-4afe-bde6-337ebf7ca522\"], \"name\": \"federation-ext-net-01\", " +
        "\"provider:physical_network\": \"phy-ex-2\", \"admin_state_up\": true, \"tenant_id\": " +
        "\"0ccaf1d0ed9e4b19bf4aba6e0f1e4d6f\", \"provider:network_type\": \"flat\", \"router:external\": " +
        "true, \"shared\": false, \"id\": \"2514b343-1913-41e4-9744-fbe34a446548\", " +
        "\"provider:segmentation_id\": null}, {\"status\": \"ACTIVE\", \"subnets\": " +
        "[\"074437be-4a13-42f7-bab9-f488ea7f9395\"], \"name\": \"public-ext-net-01\", " +
        "\"provider:physical_network\": \"phy-ex\", \"admin_state_up\": true, \"tenant_id\":" +
        " \"0ccaf1d0ed9e4b19bf4aba6e0f1e4d6f\", \"provider:network_type\": \"flat\", " +
        "\"router:external\": true, \"shared\": false, \"id\": \"83c3d979-4a43-4ce3-ac39-ef7bfb0b89e5\"," +
        " \"provider:segmentation_id\": null}, {\"status\": \"ACTIVE\", " +
        "\"subnets\": [\"3b1d048c-aa96-47bb-9449-383021324e68\"], \"name\": \"public-ext-net-02\"," +
        " \"provider:physical_network\": \"phy-ex-3\", \"admin_state_up\": true, \"tenant_id\": " +
        "\"0ccaf1d0ed9e4b19bf4aba6e0f1e4d6f\", \"provider:network_type\": \"flat\", \"router:external\":" +
        " true, \"shared\": false, \"id\": \"c3eb6a02-571b-4510-84af-d1ff82bc015a\", " +
        "\"provider:segmentation_id\": null}]}";

    String ROUTER = " { " + "\"routers\": [ {" + "\"status\": \"ACTIVE\", " + " \"external_gateway_info\": { "
            + " \"network_id\": \"080b5f2a-668f-45e0-be23-361c3a7d11d0\" " + " }, " + " \"name\": \"test-rt1\", "
            + "\"admin_state_up\": true, " + "\"tenant_id\": \"08bed031f6c54c9d9b35b42aa06b51c0\", "
            + "\"routes\": [], " + "\"id\": \"5af6238b-0e9c-4c20-8981-6e4db6de2e17\"" + "}  ]}";

    String ROUTERS = " {\"routers\": [{\"status\": \"ACTIVE\", \"external_gateway_info\":"
            + " { \"network_id\": \"080b5f2a-668f-45e0-be23-361c3a7d11d0\"}, "
            + "\"name\": \"router\", \"admin_state_up\": true,"
            + " \"tenant_id\": \"f8b9284b4a5f4875b591d22185ba835c\", \"routes\": "
            + "[], \"id\": \"084d97ec-a348-4907-94d4-95e339b1cdd4\"}, {\"status\": \"ACTIVE\", "
            + " \"external_gateway_info\": [{\"network_id\":\"080b5f2a-668f-45e0-be23-361c3a7d11d0\"}] ,"
            + " \"name\": \"test\", \"admin_state_up\": true, \"tenant_id\": \"f8b9284b4a5f4875b591d22185ba835c\","
            + "\"routes\": [], " + " \"id\": \"46a97147-27ed-4ee1-b88e-b74a5a831706\"}, {\"status\": \"ACTIVE\", "
            + "\"external_gateway_info\": {\"network_id\": "
            + "\"080b5f2a-668f-45e0-be23-361c3a7d11d0\"}, \"name\": \"test-rt1\", \"admin_state_up\": "
            + "true, \"tenant_id\": \"08bed031f6c54c9d9b35b42aa06b51c0\","
            + " \"routes\": [], \"id\": \"5af6238b-0e9c-4c20-8981-6e4db6de2e17\"}, {\"status\":"
            + "\"ACTIVE\", \"external_gateway_info\": {\"network_id\": "
            + " \"080b5f2a-668f-45e0-be23-361c3a7d11d0\"}, \"name\": \"prueba\", \"admin_state_up\": "
            + "true, \"tenant_id\": \"08bed031f6c54c9d9b35b42aa06b51c0\", "
            + " \"routes\": [], \"id\": \"89c6eca5-99d5-41bd-b6c6-deb8d03820ac\"}]} ";

    @Before
    public void setUp() throws OpenStackException, ClientProtocolException, IOException {
        openStackUtil = new OpenStackConfigUtilImplTestable();

        paasManagerUser = new PaasManagerUser("user", "aa");
        paasManagerUser.setToken("1234567891234567989");
        paasManagerUser.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        paasManagerUser.setUsername("08bed031f6c54c9d9b35b42aa06b51c0");

        HttpClientConnectionManager httpClientConnectionManager = mock(HttpClientConnectionManager.class);
        openStackUtil.setHttpConnectionManager(httpClientConnectionManager);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);

        openOperationUtil = mock(OpenOperationUtil.class);
        closeableHttpClientMock = mock(CloseableHttpClient.class);
        openStackRegion = mock(OpenStackRegion.class);
        openStackUtil.setOpenStackRegion(openStackRegion);
        openStackUtil.setOpenOperationUtil(openOperationUtil);

        String responseJSON = "{\"access\": {\"token\": {\"issued_at\": "
                + "\"2014-01-13T14:00:10.103025\", \"expires\": \"2014-01-14T14:00:09Z\","
                + "\"id\": \"ec3ecab46f0c4830ad2a5837fd0ad0d7\", \"tenant\": "
                + "{ \"description\": null, \"enabled\": true, \"id\": \"08bed031f6c54c9d9b35b42aa06b51c0\","
                + "\"name\": \"admin\" } },         \"serviceCatalog\": []}}}";

        HttpPost httpPost = mock(HttpPost.class);

        when(closeableHttpClientMock.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        when(
                openOperationUtil.createNovaPostRequest(anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString())).thenReturn(httpPost);

        when(openOperationUtil.createQuantumGetRequest(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(httpPost);

    }

    /**
     * It deletes a network interface to a public router.
     * 
     * @throws OpenStackException
     * @throws IOException
     */
    @Test
    public void shouldObtainDefaultNetwork() throws OpenStackException, IOException {
        // given
        String region = "RegionOne";

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(CONTENT_NETWORKS);
        // when

        String net = openStackUtil.getPublicAdminNetwork(paasManagerUser, region);

        // then
        assertNotNull(net);
        assertEquals(net, "83c3d979-4a43-4ce3-ac39-ef7bfb0b89e5");
    }

    @Test
    public void shouldObtainDefaultPool() throws OpenStackException, IOException {
        // given
        String region = "RegionOne2";

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(CONTENT_NETWORKS);
        // when

        String net = openStackUtil.getPublicFloatingPool(paasManagerUser, region);

        // then
        assertNotNull(net);
        assertEquals(net, "public-ext-net-01");

    }


    @Test
    public void shouldObtainPublicRouter() throws OpenStackException, IOException {
        // given
        String region = "RegionOne3";

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);

        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(ROUTERS);
        // when

        String router = openStackUtil.getPublicRouter(paasManagerUser, region, "080b5f2a-668f-45e0-be23-361c3a7d11d0");

        // then
        assertNotNull(router);

    }

    /**
     * There is a bug if the network is null. Just lanch an exception in this case.
     * 
     * @throws OpenStackException
     */
    @Test
    public void testBugFindRouterNetworkNull() throws OpenStackException {
        // given
        String region = "RegionOne4";
        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(ROUTERS);
        // when

        try {
            openStackUtil.getPublicRouter(paasManagerUser, region, null);
            fail("The method should have lanch an exception");
        } catch (Exception e) {

        }
    }

    /**
     * Obtaining the external network if the tenant id is not a keystone valid one.
     * 
     * @throws OpenStackException
     */
    @Test
    public void testObtainPublicNetworkSeveralExternalNetworks() throws OpenStackException {
        String CONTENT_NETWORKS = "{\"networks\": [{\"status\": \"ACTIVE\", "
                + "\"subnets\": [\"eb602a2b-13cd-46c8-9055-99ad45180730\"],"
                + "\"name\": \"net04_ext\", \"provider:physical_network\": null, "
                + "\"admin_state_up\": true, \"tenant_id\": "
                + "\"eab8403e83e048719ab220353c10806f\", \"provider:network_type\": "
                + "\"gre\", \"router:external\": true, \"shared\": "
                + "true, \"id\": \"04c9aedf-aeb6-4553-8ddb-845701da71bd\", " + "\"provider:segmentation_id\": 3}, "
                + "{\"status\": \"ACTIVE\", \"subnets\": [\"6ba86d6b-b7ad-4d0b-b552-cb63b36c9ef2\"], "
                + "\"name\": \"sec_ext_net\", "
                + "\"provider:physical_network\": \"physnet1\", \"admin_state_up\": true, \"tenant_id\":"
                + "\"eab8403e83e048719ab220353c10806f\", "
                + "\"provider:network_type\": \"flat\", \"router:external\": true, \"shared\": false,"
                + " \"id\": \"063f1075-77eb-45f9-be7a-205a591840ee\"," + "\"provider:segmentation_id\": null}]}";

        System.out.print(CONTENT_NETWORKS);

        String region = "RegionOne5";

        OpenStackAccess openStackAccess = new OpenStackAccess();
        openStackAccess.setToken("1234567891234567989");
        openStackAccess.setTenantId("08bed031f6c54c9d9b35b42aa06b51c0");
        when(openStackRegion.getTokenAdmin()).thenReturn(openStackAccess);
        when(openOperationUtil.executeNovaRequest(any(HttpUriRequest.class))).thenReturn(CONTENT_NETWORKS);
        // when

        String net = openStackUtil.getPublicAdminNetwork(paasManagerUser, region);

        // then
        assertNotNull(net);
        assertEquals(net, "04c9aedf-aeb6-4553-8ddb-845701da71bd");
    }

    @Test
    public void shouldGetPublicRouterId() throws JSONException {

        String response = "{\"routers\": [{\"status\": \"ACTIVE\", \"external_gateway_info\":"
                + " {\"network_id\": \"e8892de7-38f9-4002-90f9-eedf0e72f5fc\"}, \"name\": "
                + "\"router-1137229409\", \"admin_state_up\": false, "
                + "\"tenant_id\": \"00000000000000000000000000000017\", \"routes\": [], "
                + "\"id\": \"2fe38e4d-a4cb-4c0a-b1b9-e87e0d147f9c\"}, {\"status\": \"ACTIVE\", "
                + "\"external_gateway_info\": {\"network_id\": \"e5892de7-38f9-4002-90f9-eedf0e72f5fc\","
                + " \"enable_snat\": true}, \"name\": \"ext-rt\", \"admin_state_up\": "
                + " true, \"tenant_id\": \"00000000000000000000000000000001\", \"routes\": [], \"id\":"
                + "\"35da5189-03f8-4167-868d-932637d83105\"}]}";

        String routerId = openStackUtil.getPublicRouterId(response, "00000000000000000000000000000001",
                "e5892de7-38f9-4002-90f9-eedf0e72f5fc");
        assertEquals(routerId, "35da5189-03f8-4167-868d-932637d83105");

    }

    /**
     * OpenStackUtilImplTestable.
     * 
     * @author jesus
     */
    private class OpenStackConfigUtilImplTestable extends OpenStackConfigUtilImpl {

        public CloseableHttpClient getHttpClient() {

            return closeableHttpClientMock;
        }
    }
}
