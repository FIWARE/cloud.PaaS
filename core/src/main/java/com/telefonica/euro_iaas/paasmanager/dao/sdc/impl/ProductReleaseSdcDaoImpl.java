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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.conn.HttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.dao.sdc.ProductReleaseSdcDao;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.exception.SdcException;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCClient;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtil;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.fiware.commons.util.PoolHttpClient;
import com.telefonica.euro_iaas.sdc.client.exception.ResourceNotFoundException;

/**
 * @author jesus.movilla
 */
public class ProductReleaseSdcDaoImpl implements ProductReleaseSdcDao {

    private static Logger log = LoggerFactory.getLogger(ProductReleaseSdcDaoImpl.class);
    private Client client = null;
    private SDCUtil sDCUtil;
    private SDCClient sDCClient;

    /**
     * connection manager.
     */
    private HttpClientConnectionManager httpConnectionManager;

    public HttpClientConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    public void setHttpConnectionManager(HttpClientConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    ProductReleaseSdcDaoImpl() {

    }

    /**
     * load product from sdc
     * 
     * @param product
     * @param version
     * @param data
     * @return
     * @throws EntityNotFoundException
     * @throws SdcException
     */
    public ProductRelease load(String product, String version, ClaudiaData data) throws EntityNotFoundException,
            SdcException {
        log.debug("Load by name " + product + " " + version);
        ProductRelease p = null;

        try {
            String url = sDCUtil.getSdcUtil();
            log.info("Loading from SDC in url: " + url);

            com.telefonica.euro_iaas.sdc.client.services.ProductReleaseService pIService = sDCClient
                    .getProductReleaseService(url, SDC_SERVER_MEDIATYPE);
            com.telefonica.euro_iaas.sdc.model.ProductRelease prod = pIService.load(product, version, data.getUser()
                    .getToken(), data.getUser().getTenantId());
            p = new ProductRelease(prod.getProduct().getName(), prod.getVersion());

            for (com.telefonica.euro_iaas.sdc.model.Metadata sdcMetadata : prod.getProduct().getMetadatas()) {
                Metadata metadata = new Metadata();
                metadata.setKey(sdcMetadata.getKey());
                metadata.setValue(sdcMetadata.getValue());
                metadata.setDescription(sdcMetadata.getDescription());
                p.addMetadata(metadata);
            }
            for (com.telefonica.euro_iaas.sdc.model.Attribute att : prod.getProduct().getAttributes()) {
                Attribute attribute = new Attribute();
                attribute.setKey(att.getKey());
                attribute.setValue(att.getValue());
                attribute.setType(att.getType());
                attribute.setDescription(att.getDescription());
                p.addAttribute(attribute);
            }

        } catch (OpenStackException e) {
            String message = "Error calling SDC to obtain the product: " + e.getMessage();
            log.error(message);
            throw new SdcException(message);
        } catch (ResourceNotFoundException e) {
            String message = "The Product Release " + product + "-" + version + " is not present in SDC DataBase ";
            log.error(message);
            throw new EntityNotFoundException(ProductRelease.class, "name", "product" + "-" + "version");
        }

        return p;
    }

    public List<String> findAllProducts(String token, String tenant) throws SdcException {
        Response response = null;

        try {
            String url = sDCUtil.getSdcUtil() + "/catalog/product";
            log.debug("url: " + url);

            Invocation.Builder builder = createWebResource(url, token, tenant);
            builder = builder.accept(MediaType.APPLICATION_JSON);
            response = builder.get();
            String responseJSON = response.readEntity(String.class);
            return fromSDCToProductNames(responseJSON);
        } catch (Exception e) {
            String message = "Error calling SDC to obtain the all product list " + e.getMessage();
            ;
            log.error(message);
            throw new SdcException(message);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    private List<String> fromSDCToProductNames(String sdcproducts) {

        JSONObject jsonNodeProducts = JSONObject.fromObject(sdcproducts);
        List<String> productNames = new ArrayList<String>();

        JSONArray jsonproductList = jsonNodeProducts.getJSONArray("product");

        for (Object o : jsonproductList) {
            JSONObject jsonProduct = (JSONObject) o;
            String productName = jsonProduct.getString("name");
            productNames.add(productName);
        }
        return productNames;
    }

    private List<ProductRelease> fromSDCToPaasManager(String sdcproductReleases) {
        List<ProductRelease> paasManagerProductReleases = fromStringToProductReleases(sdcproductReleases);
        return paasManagerProductReleases;
    }

    /**
     * Converting from a string (list of secGroups in json) to a list of SecurityGroups.
     * 
     * @return List of ProductReleases
     */
    public List<ProductRelease> fromStringToProductReleases(String sdcproductReleases) {
        List<ProductRelease> productReleases = new ArrayList<ProductRelease>();
        JSONObject json = (JSONObject) JSONSerializer.toJSON(sdcproductReleases);
        Object obj = json.get("productRelease");
        if (obj instanceof JSONObject) {
            JSONObject jsonProductRelease = (JSONObject) obj;
            ProductRelease productRelease = new ProductRelease();
            productRelease.fromSdcJson(jsonProductRelease);
            productReleases.add(productRelease);
        } else {
            JSONArray ja = (JSONArray) obj;
            JSONArray jsonproductReleasesList = ja;
            for (Object o : jsonproductReleasesList) {
                ProductRelease productRelease = new ProductRelease();
                JSONObject jsonProductRelease = (JSONObject) o;
                productRelease.fromSdcJson(jsonProductRelease);
                productReleases.add(productRelease);
            }
        }

        return productReleases;
    }

    /**
     * @param client
     *            the client to set
     */
    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        if (this.client == null) {
            this.client = PoolHttpClient.getInstance(httpConnectionManager).getClient();
        }
        return this.client;
    }

    public void setSDCUtil(SDCUtil sDCUtil) {
        this.sDCUtil = sDCUtil;
    }

    public void setSDCClient(SDCClient sDCClient) {
        this.sDCClient = sDCClient;
    }

    private Invocation.Builder createWebResource(String url, String token, String tenant) {

        WebTarget webResource = getClient().target(url);
        Invocation.Builder builder = webResource.request(MediaType.APPLICATION_JSON);

        builder.header("X-Auth-Token", token);
        builder.header("Tenant-Id", tenant);

        return builder;

    }

}
