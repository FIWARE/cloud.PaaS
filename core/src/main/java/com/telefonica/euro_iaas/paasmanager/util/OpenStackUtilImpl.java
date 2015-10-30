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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.RouterInstance;
import com.telefonica.euro_iaas.paasmanager.model.SubNetworkInstance;
import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;

/**
 * @author jesus.movilla
 */
public class OpenStackUtilImpl implements OpenStackUtil {

    /**
     * The log.
     */

    private static Logger log = LoggerFactory.getLogger(OpenStackUtilImpl.class);

    /**
     * tenant to be used.
     */
    private String tenant;

    private HttpClientConnectionManager httpConnectionManager;

    private OpenStackRegion openStackRegion;

    private OpenOperationUtil openOperationUtil;

    private OpenStackConfigUtil openStackConfigUtil;

    /**
     * The constructor.
     */
    public OpenStackUtilImpl() {
    }

    public HttpClientConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    public void setHttpConnectionManager(HttpClientConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    /**
     * It adds an interface to the router.
     */

    public String addInterface(String idRouter, NetworkInstance net, String region, String token, String vdc)
            throws OpenStackException {
        // PUT /v2.0/routers/8604a0de-7f6b-409a-a47c-a1cc7bc77b2e/add_router_interface
        // Accept: application/json

        log.debug("Adding an interface from network " + net.getNetworkName() + " to router " + idRouter);
        String response = null;

        try {
            String payload = net.toAddInterfaceJson();
            log.debug(payload);

            HttpUriRequest request = openOperationUtil.createQuantumPutRequest(RESOURCE_ROUTERS + "/" + idRouter + "/"
                    + RESOURCE_ADD_INTERFACE, payload, APPLICATION_JSON, region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error creating router in " + idRouter + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error creating router " + idRouter + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;

    }

    /**
     * It adds an interface to the router.
     */
    public String addInterfaceToPublicRouter(PaasManagerUser user, NetworkInstance net, String region)
            throws OpenStackException {

        String networkId = openStackConfigUtil.getPublicAdminNetwork(user, region);
        String idRouter = openStackConfigUtil.getPublicRouter(user, region, networkId);
        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        log.debug("tenantId " + openStackAccess.getTenantId());
        log.debug("token " + openStackAccess.getToken());

        log.debug("Adding an interface from network " + net.getNetworkName() + " to router " + idRouter);
        String response;

        try {
            String payload = net.toAddInterfaceJson();
            log.debug(payload);

            HttpUriRequest request = openOperationUtil.createQuantumPutRequest(RESOURCE_ROUTERS + "/" + idRouter + "/"
                    + RESOURCE_ADD_INTERFACE, payload, APPLICATION_JSON, region, openStackAccess.getToken(),
                    openStackAccess.getTenantId());
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error creating router in " + idRouter + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error creating router " + idRouter + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;

    }

    /**
     * It add a network interface to the router.
     */
    public String addRouterInterface(String routerId, String subNetId, String region, String token, String vdc)
            throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/xml"
        // -X POST "http://10.95.171.115:9696/v2.0/subnets"
        // -d '{"network" : {"name" : "testNetwork", "admin_state_up": false}}'

        String response = null;

        try {
            String payload = "{\"subnet_id\": \"" + subNetId + "\"}";

            HttpUriRequest request = openOperationUtil.createQuantumPutRequest(RESOURCE_ROUTERS + "/" + routerId + "/"
                    + RESOURCE_ADD_INTERFACE, payload, APPLICATION_JSON, region, token, vdc);
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error adding interface " + subNetId + " to router " + routerId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error adding interface " + subNetId + " to router " + routerId + " from OpenStack: "
                    + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * Add a floatingIP to a full ip pool.
     * 
     * @param payload
     * @param region
     * @return
     * @throws OpenStackException
     */
    public String allocateFloatingIP(String payload, String region, String token, String vdc) throws OpenStackException {
        String response = null;

        try {
            HttpUriRequest request = openOperationUtil.createNovaPostRequest("/" + RESOURCE_FLOATINGIP, payload,
                    APPLICATION_JSON, APPLICATION_JSON, region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);
            // deletion.setMessage(response);

        } catch (OpenStackException e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + " from OpenStack: "
                    + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    public String getFloatingIPs(String region, String token, String vdc, String dd) throws OpenStackException {
        String response = null;

        try {
            HttpUriRequest request = openOperationUtil.createNovaGetRequest("/" + RESOURCE_FLOATINGIP,
                    APPLICATION_JSON, region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);
            // deletion.setMessage(response);

        } catch (OpenStackException e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + " from OpenStack: "
                    + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    public void disAllocateFloatingIP(String region, String token, String vdc, String floatingIp)
            throws OpenStackException {
        log.debug("disAllocateFloatingIP " + floatingIp);

        try {
            String floatingIpsXML = this.getFloatingIPs(region, token, vdc);
            String idFloatingIp = this.getFloatingIpId(floatingIpsXML, floatingIp);

            HttpUriRequest request = openOperationUtil.createNovaDeleteRequest("/" + RESOURCE_FLOATINGIP + "/"
                    + idFloatingIp, region, token, vdc);

            openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error disallocating the ip with id" + " from tenant " + tenant + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + " from OpenStack: "
                    + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }
    }

    /**
     * Assign certain floatingIP to a serverId.
     * 
     * @param serverId
     * @param floatingIP
     * @return
     * @throws OpenStackException
     */
    public String assignFloatingIP(String serverId, String floatingIP, String region, String token, String vdc)
            throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/json"
        // -d "{"reboot" : {"type" : "SOFT" }}"
        // -X POST
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/servers/6570eca2-21e2-4942-bede-f556c57af2b4/action"

        String response;
        // TaskResult deletion = new TaskResult();
        String payload = buildPayloadFloatingIP(floatingIP);
        try {
            HttpUriRequest request = openOperationUtil.createNovaPostRequest(RESOURCE_SERVERS + "/" + serverId + "/"
                    + RESOURCE_ACTION, payload, APPLICATION_JSON, APPLICATION_JSON, region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);
            // deletion.setMessage(response);

        } catch (OpenStackException e) {
            String errorMessage = "Error Assigning Floating IP " + floatingIP + " to server " + serverId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error Assigning Floating IP " + floatingIP + " to server " + serverId
                    + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    private String buildAllocateFloatingIPPayload(String floatingIPPool) {
        return "{ \"pool\": \"" + floatingIPPool  + "\"}";
    }

    /**
     * Building the payload to assign FloatingIP.
     * 
     * @param floatingIp
     * @return
     */
    private String buildPayloadFloatingIP(String floatingIp) {
        return "{ \"addFloatingIp\": { \"address\": \"" + floatingIp + "\" } }";
    }

    /**
     * Checks if metadatas (authToken and tenant) were initialized.
     * 
     * @throws OpenStackException
     */
    private void checkParam(String tenantId, String token) throws OpenStackException {
        if (token == null || tenantId == null) {
            throw new OpenStackException(ERROR_AUTHENTICATION_HEADERS);
        }
    }

    public String createNetwork(String payload, String region, String token, String vdc) throws OpenStackException {

        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/xml"
        // -X POST "http://10.95.171.115:9696/v2/networks"
        // -d '{"network" : {"name" : "testNetwork", "admin_state_up": false}}'
        String response = null;

        try {

            log.debug("Payload " + payload);

            HttpUriRequest request = openOperationUtil.createQuantumPostRequest(RESOURCE_NETWORKS, payload,
                    APPLICATION_JSON, region, token, vdc);
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error creating network " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error creating network from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /*
     * (non-Javadoc)
     * @see com.telefonica.claudia.smi.OpenStackClient#createRouter(java.lang.String,
     * com.telefonica.euro_iaas.paasmanager.model.dto.PaasManagerUser )
     */
    public String createRouter(RouterInstance router, String region, String token, String vdc)
            throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/xml"
        // -X POST "http://10.95.171.115:9696/v2.0/subnets"

        // -d '{"network" : {"name" : "testNetwork", "admin_state_up": false}}'
        log.debug("Creating a router " + router.getName());

        String response = null;

        try {
            String payload = router.toJson();
            log.debug(payload);

            HttpUriRequest request = openOperationUtil.createQuantumPostRequest(RESOURCE_ROUTERS, payload,
                    APPLICATION_JSON, region, token, vdc);
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {

            String errorMessage = "Error creating router in " + router.getName() + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error creating router " + router.getName() + " from OpenStack: " + e;
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * It creates a vm in Openstack.
     */
    public String createServer(String payload, String region, String token, String vdc) throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/xml" -H "Accept: application/json"
        // -X GET
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/images/88y6ga216ad4s33ra6asd5fgrg7"
        log.debug("create server " + token + " vdc " + vdc);
        HttpUriRequest request;
        String response = null;
        // Server server = null;
        String getResponse = "";
        String id = "";

        try {
            request = openOperationUtil.createNovaPostRequest(RESOURCE_SERVERS, payload, APPLICATION_JSON,
                    APPLICATION_JSON, region, token, vdc);
        } catch (OpenStackException ex) {
            throw new OpenStackException(ex.getMessage());
        }

        try {
            response = openOperationUtil.executeNovaRequest(request);
            log.debug(response);
            JSONObject serverJson = new JSONObject(response);
            id = serverJson.getJSONObject("server").getString("id");
            // String id = response.split(",")[1];

            // server = JAXBUtils.unmarshall(response, false, Server.class);

            // Mecanismo de sondeo.
            while (!(getResponse.contains("ACTIVE") || getResponse.contains("ERROR"))) {
                getResponse = getServer(id, region, token, vdc);
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            String errorMessage = "Error creating server: " + e.getMessage();
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        if (getResponse.contains("ERROR")) {
            log.debug(getResponse);
            throw new OpenStackException("Error in parse response after deploy the VM " + id + " " + getResponse);

            // parse error
            /*
             * try { JSONObject networkString = new JSONObject(response); String id =
             * networkString.getJSONObject("network").getString("id"); log.debug("Network id " + id); //server =
             * JAXBUtils.unmarshall(getResponse, false, Server.class); } catch (IOException e) { throw new
             * OpenStackException("Error in parse response after deploy the VM " + id); } throw new
             * OpenStackException("Fault (" + server.getFault().getCode() + "):" + server.getFault().getMessage());
             */

        }
        return id;
    }

    /**
     * It creates a subnet in Openstack.
     */
    public String createSubNet(SubNetworkInstance subNet, String region, String token, String vdc)
            throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/xml"
        // -X POST "http://10.95.171.115:9696/v2.0/subnets"
        // -d '{"network" : {"name" : "testNetwork", "admin_state_up": false}}'

        log.debug("Creating sub net " + subNet.getName());
        String response = null;

        try {
            String payload = subNet.toJson();
            log.info("Payload " + payload);

            HttpUriRequest request = openOperationUtil.createQuantumPostRequest(RESOURCE_SUBNETS, payload,
                    APPLICATION_JSON, region, token, vdc);
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error creating subNetwork in " + subNet.getIdNetwork() + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error creating subNetwork " + subNet.getIdNetwork() + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * It deletes a network.
     */
    public String deleteNetwork(String networkId, String region, String token, String vdc) throws OpenStackException {
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757...'
        // -H "Accept: application/json"
        // -X DELETE "http://10.95.171.115:9696/v2/networks/5867b6bd-ba18-4ae3-a34f-dd0f2e189eb6"

        HttpUriRequest request = openOperationUtil.createQuantumDeleteRequest(RESOURCE_NETWORKS + "/" + networkId,
                region, vdc, token);

        String response = null;

        try {

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting network " + networkId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting network " + networkId + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    public String deleteRouter(String routerId, String region, String token, String vdc) throws OpenStackException {
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757...'
        // -H "Accept: application/json"
        // -X DELETE "http://10.95.171.115:9696/v2/networks/5867b6bd-ba18-4ae3-a34f-dd0f2e189eb6"
        HttpUriRequest request = openOperationUtil.createQuantumDeleteRequest(RESOURCE_ROUTERS + "/" + routerId,
                region, token, vdc);

        String response = null;

        try {

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting router " + routerId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting router " + routerId + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * It deletes a server.
     */

    public String deleteServer(String serverId, String region, String token, String vdc) throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/json" -H "Accept: application/json"
        // -d "{"reboot" : {"type" : "SOFT" }}"
        // -X POST
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/servers/
        // 6570eca2-21e2-4942-bede-f556c57af2b4/action"

        String response = null;

        try {
            HttpUriRequest request = openOperationUtil.createNovaDeleteRequest(RESOURCE_SERVERS + "/" + serverId,
                    region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting server " + serverId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting server " + serverId + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    public void deleteSubNetwork(String idSubNet, String region, String vdc, String token) throws OpenStackException {
        String response = null;

        try {
            HttpUriRequest request = openOperationUtil.createQuantumDeleteRequest(RESOURCE_SUBNETS + "/" + idSubNet,
                    region, vdc, token);

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting subNet " + idSubNet + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting subNet " + idSubNet + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

    }

    protected CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setConnectionManager(httpConnectionManager).build();
    }


    public String getFloatingIP(PaasManagerUser user, String region) throws OpenStackException {
        String floatingIP = null;

        String getFloatingIPsResponse = getFloatingIPs(region, user.getToken(), user.getTenantId());
        String floatingIpPool = openStackConfigUtil.getPublicFloatingPool(user, region);

        try {
            if (isAnyFloatingIPFreeToBeAssigned(getFloatingIPsResponse)) {
                floatingIP = getFloatingIPFree(getFloatingIPsResponse);
            } else {
                floatingIP = allocateFloatingIP(buildAllocateFloatingIPPayload(floatingIpPool), region, user.getToken(),
                    user.getTenantId());
                getFloatingIPsResponse = getFloatingIPs(region, user.getToken(), user.getTenantId());
                floatingIP = getFloatingIPFree(getFloatingIPsResponse);
            }
        } catch (JSONException e) {
            floatingIP = null;
            log.debug("Error to obtain the IP " + e.getMessage());
        }

        return floatingIP;
    }

    /**
     * Get a Free FloatingIP.
     * 
     * @param response
     * @return
     * @throws Exception
     */
    private String getFloatingIPFree(String response) throws JSONException {
        String floatingIP = null;
        JSONArray jsonFloatingIps = new JSONObject(response).getJSONArray("floating_ips");

        for (int i = 0; i < jsonFloatingIps.length(); i++) {  // **line 2**
            JSONObject childJSONObject = jsonFloatingIps.getJSONObject(i);
            if (childJSONObject.getString("instance_id").equals("null")) {
                floatingIP = childJSONObject.getString("ip");
            }
        }
        return floatingIP;
    }

    /**
     * Obtain the xml including all FloatingIPs of a certain tenant.
     * 
     * @return
     * @throws OpenStackException
     */
    private String getFloatingIPs(String region, String token, String vdc) throws OpenStackException {
        String response = null;

        try {
            HttpUriRequest request = openOperationUtil.createNovaGetRequest("/" + RESOURCE_FLOATINGIP, APPLICATION_JSON,
                    region, token, vdc);

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error Getting Floating IPs " + " from tenant " + tenant + ": " + " from OpenStack: "
                    + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * It obtains network details.
     */
    public String getNetworkDetails(String networkId, String region, String token, String vdc)
            throws OpenStackException {
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Accept: application/xml"
        // -X GET "http://10.95.171.115:9696/v2/networks/5867b6bd-ba18-4ae3-a34f-dd0f2e189eb6"
        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_NETWORKS + "/" + networkId,
                APPLICATION_JSON, region, token, vdc);

        String response = null;

        try {

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error getting network " + networkId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error getting network " + networkId + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /**
     * It obtains subnetwork details.
     */
    public String getSubNetworkDetails(String subNetworkId, String region, String token, String vdc)
            throws OpenStackException {
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Accept: application/xml"
        // -X GET "http://10.95.171.115:9696/v2/networks/5867b6bd-ba18-4ae3-a34f-dd0f2e189eb6"
        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_SUBNETS + "/" + subNetworkId,
                APPLICATION_JSON, region, token, vdc);

        String response;

        try {

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error getting network " + subNetworkId + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error getting network " + subNetworkId + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    /*
     * (non-Javadoc)
     * @see com.telefonica.claudia.smi.OpenStackClient#deleteNetwork(java.lang.String,
     * com.telefonica.euro_iaas.paasmanager.model.dto.PaasManagerUser)
     */

    /**
     * It obtains network list.
     */
    public String getNetworks(String region, String token, String vdc) throws OpenStackException {
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/xml" -H "Accept: application/json"
        // -X GET "http://10.95.171.115:9696/v2/networks"
        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_NETWORKS, APPLICATION_JSON, region,
                token, vdc);

        String response = null;

        try {

            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error getting networks: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error getting networks from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
    }

    public String getServer(String serverId, String region, String token, String vdc) throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/xml" -H "Accept: application/json"
        // -X GET
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/servers/88y6ga216ad4s33ra6asd5fgrg7"

        HttpUriRequest request = openOperationUtil.createNovaGetRequest(RESOURCE_SERVERS + "/" + serverId,
                APPLICATION_JSON, region, token, vdc);

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            // server.setMessage(response);

        } catch (Exception e) {
            String errorMessage = "Error getting server " + serverId + " from OpenStack: " + e;
            log.warn(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        return response;
    }

    public boolean existsServerForDelete(String serverId, String region, String token, String vdc) {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/xml" -H "Accept: application/json"
        // -X GET
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/servers/88y6ga216ad4s33ra6asd5fgrg7"


        try {
            HttpUriRequest request = openOperationUtil.createNovaGetRequest(RESOURCE_SERVERS + "/" + serverId,
                    APPLICATION_JSON, region, token, vdc);
            openOperationUtil.executeNovaRequest(request);
        } catch (Exception e) {
            String errorMessage = "Server " + serverId + "  no longer exists in OpenStack: " + e;
            log.info(errorMessage);
            return false;
        }
        return true;
    }

    /**
     * Is Any FloatingIP Free.
     * 
     * @param response
     * @return
     * @throws Exception
     */
    private boolean isAnyFloatingIPFreeToBeAssigned(String response) throws JSONException {

        JSONArray jsonFloatingIps = new JSONObject(response).getJSONArray("floating_ips");

        for (int i = 0; i < jsonFloatingIps.length(); i++) {  // **line 2**
            JSONObject childJSONObject = jsonFloatingIps.getJSONObject(i);
            if (childJSONObject.getString("instance_id").equals("null")) {
                return true;
            }
        }

        return false;
    }

    private String getFloatingIpId(String response, String ip) throws JSONException {
        JSONArray jsonFloatingIps = new JSONObject(response).getJSONArray("floating_ips");

        for (int i = 0; i < jsonFloatingIps.length(); i++) {  // **line 2**
            JSONObject childJSONObject = jsonFloatingIps.getJSONObject(i);
            if (childJSONObject.getString("ip").equals(ip)) {
                return childJSONObject.getString("id");
            }
        }
        return null;
    }

    public String listServers(String region, String token, String vdc) throws OpenStackException {
        // throw new UnsupportedOperationException("Not supported yet.");
        // I need to know X-Auth-Token, orgID-Tennat, IP and Port
        // curl -v -H 'X-Auth-Token: a92287ea7c2243d78a7180ef3f7a5757'
        // -H "Content-Type: application/xml" -H "Accept: application/json"
        // -X GET
        // "http://10.95.171.115:8774/v2/30c60771b6d144d2861b21e442f0bef9/servers"

        HttpUriRequest request = openOperationUtil.createNovaGetRequest(RESOURCE_SERVERS, APPLICATION_JSON, region,
                token, vdc);

        String response = null;
        // TaskResult server = new TaskResult();

        try {
            response = openOperationUtil.executeNovaRequest(request);
            // server.setMessage(response);

        } catch (Exception e) {
            String errorMessage = "Error getting lis of servers " + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
            // server.setMessage(e.getMessage());
            // server.setSuccess(false);
        }
        return response;
    }

    /**
     * It adds an interface to the router
     */
    public String removeInterface(RouterInstance router, String net, String region, String token, String vdc)
            throws OpenStackException {

        // PUT /v2.0/routers/8604a0de-7f6b-409a-a47c-a1cc7bc77b2e/remove_router_interface
        // Accept: application/json

        // {"subnet_id": "a2f1f29d-571b-4533-907f-5803ab96ead1"}

        log.debug("Removing an interface from network " + net + " to router " + router.getName());
        String response = null;

        try {
            String payload = "{\"subnet_id\": \"" + net + "\"}";
            log.debug(payload);

            HttpUriRequest request = openOperationUtil.createQuantumPutRequest(
                    RESOURCE_ROUTERS + "/" + router.getIdRouter() + "/" + RESOURCE_REMOVE_INTERFACE, payload,
                    APPLICATION_JSON, region, token, vdc);
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting interface in router " + router.getName() + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting interface in router " + router.getName() + " from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;

    }

    /**
     * Return a string with absolute limits values by tenantId.
     * 
     * <pre>
     * GET http://host:port/v2/tenantId/limits
     * Accept: application/json
     * X-Auth-Token: ea90309ce14b4da490fe035c618515db
     * </pre>
     */
    public String getAbsoluteLimits(PaasManagerUser user, String region) throws OpenStackException {
        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        log.debug("tenantid " + user.getTenantId());
        log.debug("token " + openStackAccess.getToken());

        HttpUriRequest request = openOperationUtil.createNovaGetRequest("limits", APPLICATION_JSON, region,
                user.getToken(), user.getTenantId());

        String response = openOperationUtil.executeNovaRequest(request);

        return response;
    }

    public String listNetworks(PaasManagerUser user, String region) throws OpenStackException {
        log.debug("List networks from user " + user.getUserName());

        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        log.debug("tenantid " + openStackAccess.getTenantId());
        log.debug("token " + openStackAccess.getToken());

        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_NETWORKS, APPLICATION_JSON, region,
                openStackAccess.getToken(), user.getTenantId());

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            log.debug("List network response");
            log.debug(response);

        } catch (Exception e) {
            String errorMessage = "Error getting list of networks from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        return response;

    }

    /**
     * It lists the subnets for the user in a concrete region.
     * 
     * @param user
     *            : the user
     * @param region
     *            : the region
     * @return
     * @throws OpenStackException
     */
    public String listSubNetworks(PaasManagerUser user, String region) throws OpenStackException {
        log.debug("List subnetworks from user " + user.getUserName());

        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_SUBNETS, APPLICATION_JSON, region,
                openStackAccess.getToken(), openStackAccess.getTenantId());

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            log.debug("List network response");
            log.debug(response);

        } catch (Exception e) {
            String errorMessage = "Error getting list of subnetworks " + "from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        return response;

    }

    public String listPorts(PaasManagerUser user, String region) throws OpenStackException {
        log.debug("List ports from user " + user.getUserName());
        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        HttpUriRequest request = openOperationUtil.createQuantumGetRequest(RESOURCE_PORTS, APPLICATION_JSON, region,
                openStackAccess.getToken(), openStackAccess.getTenantId());

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);

        } catch (Exception e) {
            String errorMessage = "Error getting list of networks from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        return response;

    }

    public String deleteInterfaceToPublicRouter(PaasManagerUser user, NetworkInstance net, String region)
            throws OpenStackException {
        log.debug("Delete interface in public router");
        String networkId = openStackConfigUtil.getPublicAdminNetwork(user, region);
        String idRouter = openStackConfigUtil.getPublicRouter(user, region, networkId);
        OpenStackAccess openStackAccess = openStackRegion.getTokenAdmin();

        log.debug("tenantid " + openStackAccess.getTenantId());
        log.debug("token " + openStackAccess.getToken());

        log.debug("Deleting an interface from network " + net.getNetworkName() + " to router " + idRouter);
        String response = null;

        try {
            String payload = net.toAddInterfaceJson();
            log.debug(payload);

            HttpUriRequest request = openOperationUtil.createQuantumPutRequest(RESOURCE_ROUTERS + "/" + idRouter + "/"
                    + RESOURCE_REMOVE_INTERFACE, payload, APPLICATION_JSON, region, openStackAccess.getToken(),
                    openStackAccess.getTenantId());
            response = openOperationUtil.executeNovaRequest(request);

        } catch (OpenStackException e) {
            String errorMessage = "Error deleting interface in  public router " + idRouter + ": " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Error deleting interface in  public router from OpenStack: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }

        return response;
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

    public void setOpenStackConfigUtil(OpenStackConfigUtil openStackConfigUtil) {
        this.openStackConfigUtil = openStackConfigUtil;
    }

    public String joinNetworks(NetworkInstance networkInstance, NetworkInstance networkInstance2, String token)
            throws OpenStackException {

        String payload = "{\"net_id_1\":" + networkInstance.getIdNetwork() + " , \"net_id_2\":"
                + networkInstance2.getIdNetwork() + " }";
        HttpUriRequest request = null;
        try {
            request = openOperationUtil.createJoinQuantumPostRequestRequest(RESOURCE_NETWOKS_FEDERATED, payload,
                    APPLICATION_JSON, token);
        } catch (Exception e) {
            log.warn("Errot to obtain the federated url ");
            return null;
        }

        String response = null;

        try {
            response = openOperationUtil.executeNovaRequest(request);
            log.debug("Join networks");
            log.debug(response);

        } catch (Exception e) {
            String errorMessage = "Error to join the two networks: " + e;
            log.error(errorMessage);
            throw new OpenStackException(errorMessage);
        }
        return response;
    }

}
