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

package com.telefonica.euro_iaas.paasmanager.dao.sdc.impl;

import static com.telefonica.euro_iaas.paasmanager.util.Configuration.SDC_SERVER_MEDIATYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.euro_iaas.paasmanager.exception.SdcException;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtil;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.sdc.client.services.ProductReleaseService;

/**
 * @author jesus.movilla
 */
public class ProductReleaseSdcDaoImplTest {
    private ProductReleaseSdcDaoImpl productReleaseSdcDaoImpl;
    Invocation.Builder builder;
    SDCUtil sdcUtils;
    SDCClient sdcClient;
    ProductReleaseService pIService;

    public static String jsonProducts = "{\"product\":[{\"name\":\"tomcat\",\"description\":\"tomcat" +
        " J2EE container\"," +
        "\"attributes\":{\"key\":\"clave\",\"value\":\"valor\"}},{\"name\":\"nodejs\"," +
        "\"description\":\"nodejs\"},{\"name\":\"mysql\",\"description\":\"mysql\"}," +
        "{\"name\":\"git\",\"description\":\"git\"},{\"name\":\"mongodbshard\",\"description\"" +
        ":\"mongodbshard\"},{\"name\":\"mongos\",\"description\":\"mongos\"},{\"name\":\"mongodbconfig\"," +
        "\"description\":\"mongodbconfig\"},{\"name\":\"contextbroker\",\"description\":\"contextbroker\"}," +
        "{\"name\":\"postgresql\",\"description\":\"db manager\",\"attributes\":[{\"key\":\"username\"," +
        "\"value\":\"postgres\",\"description\":\"The administrator usename\"},{\"key\":\"password\"," +
        "\"value\":\"postgres\",\"description\":\"The administrator password\"}]},{\"name\":\"haproxy\"," +
        "\"description\":\"balancer\",\"attributes\":[{\"key\":\"key1\",\"value\":\"value1\",\"description\":" +
        "\"keyvaluedesc1\"},{\"key\":\"key2\",\"value\":\"value2\",\"description\":\"keyvaluedesc2\"}," +
        "{\"key\":\"sdccoregroupid\",\"value\":\"app_server_role\",\"description\":\"idcoregroup\"}]}," +
        "{\"name\":\"test\",\"description\":\"test\",\"attributes\":{\"key\":\"clave\",\"value\":\"valor\"}}," +
        "{\"name\":\"mediawiki\",\"description\":\"MediaWiki Product\",\"attributes\":[{\"key\":\"wikiname\"," +
        "\"value\":\"Wiki to be shown\",\"description\":\"The name of the wiki\"},{\"key\":\"path\"," +
        "\"value\":\"/demo\",\"description\":\"The url context to be displayed\"}]}]}";

    @Before
    public void setUp() {
        productReleaseSdcDaoImpl = new ProductReleaseSdcDaoImpl();
        sdcUtils = mock(SDCUtil.class);
        sdcClient = mock(SDCClient.class);
        productReleaseSdcDaoImpl.setSDCUtil(sdcUtils);
        productReleaseSdcDaoImpl.setSDCClient(sdcClient);
        HttpClientConnectionManager httpConnectionManager = mock(HttpClientConnectionManager.class);
        Client client = mock(Client.class);
        productReleaseSdcDaoImpl.setClient(client);
        productReleaseSdcDaoImpl.setHttpConnectionManager(httpConnectionManager);
        pIService = mock (ProductReleaseService.class);
        WebTarget webResource = mock(WebTarget.class);
        builder = mock(Invocation.Builder.class);

        // when
        when(client.target(anyString())).thenReturn(webResource);
        when(webResource.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);

    }

    /**
     * Tests the findAllProducts functionality
     * 
     * @throws SdcException
     */
    @Test
     public void testFindAllProducts() throws SdcException {
        // given

        String jsonProducts = ProductReleaseSdcDaoImplTest.jsonProducts;
        // String productReleasesList =
        // "{\"productRelease\":{\"releaseNotes\":\"Tomcat server 6\",\"version\":\"6\",\"product\":{\"name\":\"tomcat\",\"description\":\"tomcat J2EE container\",\"attributes\":{\"key\":\"clave\",\"value\":\"valor\"}},\"supportedOOSS\":[{\"description\":\"Ubuntu 10.04\",\"name\":\"Ubuntu\",\"osType\":\"94\",\"version\":\"10.04\"},{\"description\":\"Debian 5\",\"name\":\"Debian\",\"osType\":\"95\",\"version\":\"5\"},{\"description\":\"Centos 2.9\",\"name\":\"Centos\",\"osType\":\"76\",\"version\":\"2.9\"}]}}\"";
        Response response = mock(Response.class);

        when(builder.get()).thenReturn(response);
        when(response.readEntity(String.class)).thenReturn(jsonProducts);

        List<String> products = productReleaseSdcDaoImpl.findAllProducts("token", "tenant");

        // then
        assertNotNull(products);

    }

    @Test
    public void testLoadProductWithAttributes() throws Exception {
        // given

        String productReleaseJson = "{\"productRelease\":{" + "\"version\":\"0.6.0\"," + "\"product\":{"
            + "\"name\":\"orion\"," + "\"description\":\"\"," + "\"attributes\":["
            + "{\"key\":\"openports\",\"value\":\"1026 27017 27018 27019 28017\"},"
            + "{\"key\":\"dd\",\"value\":\"dd\",\"description\":\"dd\"}" + "]," + "\"metadatas\":["
            + "{\"key\":\"image\",\"value\":\"df44f62d-9d66-4dc5-b084-2d6c7bc4cfe4\"},"
            + "{\"key\":\"cookbook_url\",\"value\":\"\"}," + "{\"key\":\"cloud\",\"value\":\"yes\"},"
            + "{\"key\":\"installator\",\"value\":\"chef\"}," + "{\"key\":\"open_ports\",\"value\":\"80 22\"}"
            + "]" + "}}}";

        Response response = mock(Response.class);
        com.telefonica.euro_iaas.sdc.model.ProductRelease productRelease = new com.telefonica.euro_iaas.sdc.model.ProductRelease();
        productRelease.setProduct(new com.telefonica.euro_iaas.sdc.model.Product("orion", "descr"));
        productRelease.setVersion("0.6.0");

        when(builder.get()).thenReturn(response);
        when(pIService.load(anyString(), anyString(), anyString(), anyString())).thenReturn(productRelease);
        when(sdcClient.getProductReleaseService(anyString(), anyString())).thenReturn(pIService);
        when(response.readEntity(String.class)).thenReturn(productReleaseJson);

        ClaudiaData claudiaData = new ClaudiaData("org", "tenantId", "service");
        PaasManagerUser user = new PaasManagerUser("user", "pass");
        user.setToken("token");
        claudiaData.setUser(user);
        ProductRelease product = productReleaseSdcDaoImpl.load("orion", "0.6.0",
            claudiaData);

        // then
        assertNotNull(product);

    }

    @Test
    public void testFromStringToProductReleasesTwoProductReleases() throws Exception {
        // given

        String twoProductReleaseString = "{\"productRelease\":[{" + "\"version\":\"0.8.1\"," + "\"product\":{"
                + "\"name\":\"orion\"," + "\"description\":\"\"," + "\"attributes\":["
                + "{\"key\":\"openports\",\"value\":\"1026 27017 27018 27019 28017\"},"
                + "{\"key\":\"dd\",\"value\":\"dd\",\"description\":\"dd\"}" + "]," + "\"metadatas\":["
                + "{\"key\":\"image\",\"value\":\"df44f62d-9d66-4dc5-b084-2d6c7bc4cfe4\"},"
                + "{\"key\":\"cookbook_url\",\"value\":\"\"}," + "{\"key\":\"cloud\",\"value\":\"yes\"},"
                + "{\"key\":\"installator\",\"value\":\"chef\"}," + "{\"key\":\"open_ports\",\"value\":\"80 22\"}"
                + "]" + "}}," + "{\"version\":\"0.6.0\"," + "\"product\":{" + "\"name\":\"orion\","
                + "\"description\":\"\"," + "\"attributes\":["
                + "{\"key\":\"openports\",\"value\":\"1026 27017 27018 27019 28017\"},"
                + "{\"key\":\"dd\",\"value\":\"dd\",\"description\":\"dd\"}" + "]," + "\"metadatas\":["
                + "{\"key\":\"image\",\"value\":\"df44f62d-9d66-4dc5-b084-2d6c7bc4cfe4\"},"
                + "{\"key\":\"cookbook_url\",\"value\":\"\"}," + "{\"key\":\"cloud\",\"value\":\"yes\"},"
                + "{\"key\":\"installator\",\"value\":\"chef\"}," + "{\"key\":\"open_ports\",\"value\":\"80 22\"}"
                + "]" + "}}]}";
        // when
        List<ProductRelease> productReleases = productReleaseSdcDaoImpl
                .fromStringToProductReleases(twoProductReleaseString);
        // then
        assertEquals(productReleases.size(), 2);
    }

    @Test
    public void testFromStringToProductReleasesOneProductRelease() throws Exception {
        // given

        String twoProductReleaseString = "{\"productRelease\":{" + "\"version\":\"0.6.0\"," + "\"product\":{"
                + "\"name\":\"orion\"," + "\"description\":\"\"," + "\"attributes\":["
                + "{\"key\":\"openports\",\"value\":\"1026 27017 27018 27019 28017\"},"
                + "{\"key\":\"dd\",\"value\":\"dd\",\"description\":\"dd\"}" + "]," + "\"metadatas\":["
                + "{\"key\":\"image\",\"value\":\"df44f62d-9d66-4dc5-b084-2d6c7bc4cfe4\"},"
                + "{\"key\":\"cookbook_url\",\"value\":\"\"}," + "{\"key\":\"cloud\",\"value\":\"yes\"},"
                + "{\"key\":\"installator\",\"value\":\"chef\"}," + "{\"key\":\"open_ports\",\"value\":\"80 22\"}"
                + "]" + "}}}";
        // when
        List<ProductRelease> productReleases = productReleaseSdcDaoImpl
                .fromStringToProductReleases(twoProductReleaseString);

        // then
        assertEquals(productReleases.size(), 1);

    }

}
