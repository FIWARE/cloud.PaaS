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

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.RouterInstance;
import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import com.telefonica.fiware.commons.util.RegionCache;

/**
 * @author jesus.movilla
 */
public class OpenStackConfigUtilImpl implements OpenStackConfigUtil {

    /**
     * The log.
     */

    private static Logger log = LoggerFactory.getLogger(OpenStackConfigUtilImpl.class);

    private HttpClientConnectionManager httpConnectionManager;

    private OpenStackRegion openStackRegion;

    private OpenOperationUtil openOperationUtil;

    private RegionCache regionCache;

    /**
     * The constructor.
     */
    public OpenStackConfigUtilImpl() {
        regionCache = new RegionCache();
    }

    public HttpClientConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    public void setHttpConnectionManager(HttpClientConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    /**
     * It gets the public admin network
     */
    public String getPublicAdminNetwork(PaasManagerUser user, String region) throws OpenStackException {
        log.debug("Obtain public admin network ");
        String type = "net";

        String networkId = regionCache.getUrl(region, type);
        if (networkId != null) {
            log.debug("in cache " + networkId);
            return networkId;
        }
        NetworkInstance net = this.getPublicAdmin(user, region);
        if (net != null) {
            log.debug("net " + net.getNetworkName() + " " + net.getIdNetwork());
            regionCache.putUrl(region, type, net.getNetworkName());
            return net.getIdNetwork();
        }
        return null;
    }

    public String getPublicFloatingPool(PaasManagerUser user, String region) throws OpenStackException {
        String type = "floating";
        log.debug("Obtain public admin network");
        String networkId = regionCache.getUrl(region, type);
        if (networkId != null) {
            return networkId;
        }
        NetworkInstance net = getPublicAdmin(user, region);
        if (net != null) {
           regionCache.putUrl(region, type, net.getNetworkName());
           return net.getNetworkName();
        }
        return null;
    }

    public NetworkInstance getPublicAdmin(PaasManagerUser user, String region) throws OpenStackException {
        log.debug("Obtain public admin network");
        NetworkInstance net = null;

        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();
        HttpUriRequest request;
        try {
            request = openOperationUtil.createQuantumGetRequest(RESOURCE_NETWORKS +
                    ".json?router%3Aexternal=True", APPLICATION_JSON, region,
                openStackAccess.getToken(), openStackAccess.getTenantId());
        } catch (OpenStackException e) {
            log.warn("Error to obtain the quantum request url ");
            return net;
        }

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            JSONObject lNetworkString = new JSONObject(response);
            JSONArray jsonNetworks = lNetworkString.getJSONArray("networks");
            java.util.List<NetworkInstance> nets = new java.util.ArrayList ();

            for (int i = 0; i< jsonNetworks.length(); i++) {

                JSONObject jsonNet = jsonNetworks.getJSONObject(i);
                NetworkInstance netInst = NetworkInstance.fromJson(jsonNet, region);
                nets.add(netInst);
            }

            net = this.getPublicNetwork(nets);

        } catch (OpenStackException e) {
            String errorMessage = "Error getting networks for obtaining the public network: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error getting networks from OpenStack for obtaining the public network: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return net;
    }

    /**
     * It gets the public router network
     */
    public String getPublicRouter(PaasManagerUser user, String region, String publicNetworkId)
            throws OpenStackException {
        log.debug("Obtain public router for external netwrk " + publicNetworkId);
        if (publicNetworkId == null) {
            String errorMessage = "There is not an external network valid for the router";
            log.warn(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        String type = "router";
        String routerId = regionCache.getUrl(region, type);
        if (routerId != null) {
            return routerId;
        }

        // String publicNetworkId = this.getPublicAdminNetwork(user, region).getIdNetwork();

        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();
        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_ROUTERS, APPLICATION_JSON, region,
                openStackAccess.getToken(), openStackAccess.getTenantId());

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            routerId = getPublicRouterId(response, openStackAccess.getTenantId(), publicNetworkId);
            if (routerId == null) {
                String errorMessage = "It is not possible to find a public router for network " + publicNetworkId
                        + ": ";
                log.error(errorMessage);
                throw new OpenStackException(errorMessage);
            }
            regionCache.putUrl(region, type, routerId);
            return routerId;

        } catch (OpenStackException e) {
            String errorMessage = "Error getting router for obtaining the public router: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error getting router from OpenStack for obtaining the public router: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

    }

    public String getPublicRouterId(String response, String vdc, String publicNetworkId) throws JSONException {
        JSONObject lRouterString = new JSONObject(response);
        JSONArray jsonRouters = lRouterString.getJSONArray("routers");

        for (int i = 0; i < jsonRouters.length(); i++) {

            JSONObject jsonNet = jsonRouters.getJSONObject(i);
            RouterInstance router = isPublicRouter(jsonNet, vdc, publicNetworkId);
            if (router != null) {
                log.debug(router.toString());

                return router.getIdRouter();
            }

        }
        return null;

    }

    private NetworkInstance getPublicNetwork(java.util.List<NetworkInstance> netInsts) {

        for (NetworkInstance net: netInsts) {
            if (net.getNetworkName().contains("public") &&
                !net.getNetworkName().contains("federation") ) {
                return net;
            }
        }

        for (NetworkInstance net: netInsts) {
            if (net.getNetworkName().contains("ext-net") &&
                !net.getNetworkName().contains("federation")) {
                return net;
            }
        }

        for (NetworkInstance net: netInsts) {
            if (net.getNetworkName().contains("ext")&&
                !net.getNetworkName().contains("federation")) {
                return net;
            }
        }
        return null;

    }

    private RouterInstance isPublicRouter(JSONObject jsonRouter, String vdc, String networkPublic) {

        RouterInstance routerInst;
        try {
            routerInst = RouterInstance.fromJson(jsonRouter);
        } catch (JSONException e) {
            log.warn("Error to parser the json for the router");
            return null;
        }
        log.debug("router " + routerInst.getName() + " " + routerInst.getTenantId() + " " + routerInst.getNetworkId()
                + " " + networkPublic);

        if (!routerInst.getNetworkId().equals(networkPublic)) {
            return null;
        } else {
            return routerInst;
        }
    }

    public OpenStackRegion getOpenStackRegion() {
        return openStackRegion;
    }

    public void setOpenStackRegion(OpenStackRegion openStackRegion) {
        this.openStackRegion = openStackRegion;
    }

    public void setOpenOperationUtil(OpenOperationUtil openOperationUtil) {
        this.openOperationUtil = openOperationUtil;
    }

}
