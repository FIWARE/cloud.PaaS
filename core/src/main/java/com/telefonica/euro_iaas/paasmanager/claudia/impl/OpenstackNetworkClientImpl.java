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

package com.telefonica.euro_iaas.paasmanager.claudia.impl;

import java.util.ArrayList;
import java.util.List;

import com.telefonica.fiware.commons.openstack.auth.OpenStackAccess;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.claudia.NetworkClient;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Network;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.Port;
import com.telefonica.euro_iaas.paasmanager.model.RouterInstance;
import com.telefonica.euro_iaas.paasmanager.model.SubNetworkInstance;
import com.telefonica.euro_iaas.paasmanager.util.OpenStackUtil;

/**
 * @author henar.munoz
 */
public class OpenstackNetworkClientImpl implements NetworkClient {

    private OpenStackUtil openStackUtil = null;
    private static Logger log = LoggerFactory.getLogger(OpenstackNetworkClientImpl.class);

    /**
     * It adds the network to the router.
     * 
     * @params claudiaData
     * @params router
     * @params network
     * @throws InfrastructureException
     */

    public void addNetworkToRouter(ClaudiaData claudiaData, RouterInstance router, NetworkInstance netInstance)
            throws InfrastructureException {
        log.info("Add Interfact from net " + netInstance.getNetworkName() + " to router " + router.getName()
                + " for user " + claudiaData.getUser().getTenantName());
        try {
            String region = "";
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            String response = openStackUtil.addInterface(router.getIdRouter(), netInstance, region, token, vdc);
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + router.getIdRouter() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * It adds the network to the public router.
     * 
     * @params net
     * @throws InfrastructureException
     */

    public void addNetworkToPublicRouter(ClaudiaData claudiaData, NetworkInstance netInstance, String region)
            throws InfrastructureException {
        log.info("Add Interfact from net " + netInstance.getNetworkName() + " to public router ");

        try {
            String response = openStackUtil.addInterfaceToPublicRouter(claudiaData.getUser(), netInstance, region);
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to add the network " + netInstance.getNetworkName() + " to the public router :"
                    + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * It deletes the interface of the network in the router.
     */

    public void deleteNetworkFromRouter(ClaudiaData claudiaData, RouterInstance router, NetworkInstance net,
            String region) throws InfrastructureException {
        log.info("Delete Interfact net " + net.getNetworkName() + " " + net.getIdNetRouter() + " from router "
                + router.getName() + " for user " + claudiaData.getUser().getTenantName());
        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            String response = openStackUtil.removeInterface(router, net.getIdNetRouter(), region, token, vdc);
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * The deploy the network in Openstack.
     * 
     * @params claudiaData
     * @params network
     */

    public NetworkInstance deployDefaultNetwork(ClaudiaData claudiaData, String region) throws InfrastructureException {

        log.info("Deploy default network  for user " + claudiaData.getUser().getTenantName());
        String payload = "{" + " \"network\":{" + "    \"name\": \"net_" + claudiaData.getUser().getTenantName()
                + "\"," + "    \"admin_state_up\": true," + "    \"shared\": false" + "  }" + "}";
        log.debug("Payload " + payload);
        NetworkInstance networkInstance = null;
        String response;
        String token = claudiaData.getUser().getToken();
        String vdc = claudiaData.getVdc();

        try {
            response = openStackUtil.createNetwork(payload, region, token, vdc);
            log.debug(response);
            // "network-" + claudiaData.getUser().getTenantName()
            JSONObject jsonNetworks = new JSONObject(response).getJSONObject("network");
            networkInstance = NetworkInstance.fromJson(jsonNetworks, region);
            log.debug("Network id " + networkInstance.getIdNetwork() + " for network name "
                    + networkInstance.getNetworkName());
        } catch (OpenStackException e) {
            String msm = "Error to deploy the defaul network " + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network:" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
        return networkInstance;
    }

    /**
     * The deploy the network in Openstack.
     * 
     * @params claudiaData
     * @params network
     */

    public void deployNetwork(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws InfrastructureException {
        String token = claudiaData.getUser().getToken();
        String vdc = claudiaData.getVdc();
        log.info("Deploy network " + networkInstance.getNetworkName() + " for user "
                + claudiaData.getUser().getTenantName() + " with token " + token + " and vdc " + vdc);
        log.debug("Payload " + networkInstance.toJson());
        String response;
        try {

            response = openStackUtil.createNetwork(networkInstance.toJson(), region, token, vdc);

            log.debug(response);
            // "network-" + claudiaData.getUser().getTenantName()
            JSONObject networkString = new JSONObject(response);
            String id = networkString.getJSONObject("network").getString("id");
            log.debug("Network id " + id);
            networkInstance.setIdNetwork(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + networkInstance.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + networkInstance.getNetworkName() + ":"
                    + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * The deploy the network in Openstack.
     * 
     * @params claudiaData
     * @params network
     */
    public void deployRouter(ClaudiaData claudiaData, RouterInstance router, String region)
            throws InfrastructureException {
        log.info("Deploy router " + router.getName() + " for user " + claudiaData.getUser().getTenantName());

        try {
            log.debug("Payload " + router.toJson());
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            String response = openStackUtil.createRouter(router, region, token, vdc);

            JSONObject networkString = new JSONObject(response);
            String id = networkString.getJSONObject("router").getString("id");
            log.debug("Router id " + id);
            router.setIdRouter(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * The deploy the subnet in Openstack.
     * 
     * @params claudiaData
     * @params subNet
     */
    public void deploySubNetwork(ClaudiaData claudiaData, SubNetworkInstance subNet, String region)
            throws InfrastructureException {
        log.info("Deploy subNetworknetwork " + subNet.getName() + " for user " + claudiaData.getUser().getTenantName());
        log.debug("Payload " + subNet.toJson());
        String response;
        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            response = openStackUtil.createSubNet(subNet, region, token, vdc);
            // "network-" + claudiaData.getUser().getTenantName()
            JSONObject networkString = new JSONObject(response);
            log.debug(response);

            String id = networkString.getJSONObject("subnet").getString("id");
            log.debug("Network id " + id);
            subNet.setIdSubNet(id);
        } catch (OpenStackException e) {
            String msm = "Error to deploy the network " + subNet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to obtain the id of the network " + subNet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It destroys the network.
     * 
     * @params claudiaData
     * @params network
     */
    public void destroyNetwork(ClaudiaData claudiaData, NetworkInstance networkInstance, String region)
            throws InfrastructureException {

        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            log.info("Destroy network " + networkInstance.getNetworkName() + " for user "
                    + claudiaData.getUser().getTenantName() + " with token " + token + " and vdc " + vdc);
            openStackUtil.deleteNetwork(networkInstance.getIdNetwork(), region, token, vdc);
        } catch (OpenStackException e) {
            String msm = "Error to delete the network " + networkInstance.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It delete the router in Openstack.
     */
    public void destroyRouter(ClaudiaData claudiaData, RouterInstance router, String region)
            throws InfrastructureException {
        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            openStackUtil.deleteRouter(router.getIdRouter(), region, token, vdc);
        } catch (OpenStackException e) {
            String msm = "Error to delete the router " + router.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

    /**
     * It destroys the network.
     * 
     * @params claudiaData
     * @params network
     */
    public void destroySubNetwork(ClaudiaData claudiaData, SubNetworkInstance subnet, String region)
            throws InfrastructureException {

        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            log.info("Destroy subnetwork " + subnet.getName() + " for user " + claudiaData.getUser().getTenantName()
                    + " with token " + token + " and vdc " + vdc);
            openStackUtil.deleteSubNetwork(subnet.getIdSubNet(), region, vdc, token);
        } catch (OpenStackException e) {
            String msm = "Error to delete the network " + subnet.getName() + ":" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It loads all networks.
     * 
     * @params claudiaData
     */
    public List<NetworkInstance> loadAllNetwork(ClaudiaData claudiaData, String region) throws InfrastructureException {
        String token = claudiaData.getUser().getToken();
        String vdc = claudiaData.getVdc();
        log.info("Get network for user " + claudiaData.getUser().getTenantName() + " with token " + token + " and vdc "
                + vdc);
        List<NetworkInstance> networks = new ArrayList<NetworkInstance>();
        try {

            String response = openStackUtil.listNetworks(claudiaData.getUser(), region);
            JSONObject lNetworkString = new JSONObject(response);
            JSONArray jsonNetworks = lNetworkString.getJSONArray("networks");

            for (int i = 0; i < jsonNetworks.length(); i++) {

                JSONObject jsonNet = jsonNetworks.getJSONObject(i);
                NetworkInstance netInst = NetworkInstance.fromJson(jsonNet, region);
                networks.add(netInst);

            }

        } catch (OpenStackException e) {
            String msm = "Error to get the networks in region " + region + ": " + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to process JSON in load network in region " + region + ": " + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
        return networks;
    }

    /**
     * It loads all the subnets.
     * @param claudiaData
     * @param region
     * @return
     * @throws InfrastructureException
     */
    public List<SubNetworkInstance> loadAllSubNetworks(ClaudiaData claudiaData,
                                                      String region)
        throws InfrastructureException {
        String token = claudiaData.getUser().getToken();
        String vdc = claudiaData.getVdc();
        log.info("Get subnetworks for user "
            + claudiaData.getUser().getTenantName()
            + " with token " + token + " and vdc " + vdc);
        List<SubNetworkInstance> subNetworks =
            new ArrayList<SubNetworkInstance>();
        try {

            String response = openStackUtil.
                listSubNetworks(claudiaData.getUser(), region);
            JSONObject lSubNetworkString = new JSONObject(response);
            JSONArray jsonSubNetworks =
                lSubNetworkString.getJSONArray("subnets");

            for (int i = 0; i < jsonSubNetworks.length(); i++) {

                JSONObject jsonSubNet = jsonSubNetworks.getJSONObject(i);
                SubNetworkInstance subNetInst =
                    SubNetworkInstance.fromJson(jsonSubNet, region);
                subNetworks.add(subNetInst);

            }

        } catch (OpenStackException e) {
            String msm = "Error to get the subnetworks in region " + region +
                ": " + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to process JSON in load subnetwork in region " +
                region + ": " + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
        return subNetworks;
    }


    /**
     * It loads all networks.
     * 
     * @params claudiaData
     */
    public List<Port> listPortsFromNetwork(ClaudiaData claudiaData, String region, String networkId)
            throws InfrastructureException {
        String token = claudiaData.getUser().getToken();
        String vdc = claudiaData.getVdc();
        log.info("Get ports  for user with token " + token + " and vdc " + vdc + " and region " + region);
        List<Port> ports = new ArrayList<Port>();
        try {

            String response = openStackUtil.listPorts(claudiaData.getUser(), region);
            JSONObject lPortsString = new JSONObject(response);
            JSONArray jsonPorts = lPortsString.getJSONArray("ports");

            for (int i = 0; i < jsonPorts.length(); i++) {
                String network_id = (String) jsonPorts.getJSONObject(i).get("network_id");
                String tenant_id = (String) jsonPorts.getJSONObject(i).get("tenant_id");
                String device_owner = (String) jsonPorts.getJSONObject(i).get("device_owner");

                if (network_id.equals(networkId) && tenant_id.equals(vdc) && device_owner.equals("compute:None")) {
                    Port port = new Port((String) jsonPorts.getJSONObject(i).get("name"), network_id, tenant_id,
                            device_owner, (String) jsonPorts.getJSONObject(i).get("id"));
                    ports.add(port);
                }
            }

        } catch (OpenStackException e) {
            String msm = "Error to get the ports :" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        } catch (JSONException e) {
            String msm = "Error to get the ports :" + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
        log.info("Results Get ports  for user with token " + token + " and vdc " + vdc + " and region " + region + ": "
                + ports.size());
        return ports;
    }

    /**
     * It obtains information about the network.
     * 
     * @params claudiaData
     * @params network
     * @return network information
     */
    public NetworkInstance loadNetwork(ClaudiaData claudiaData, NetworkInstance network, String region)
            throws EntityNotFoundException {

        String message="Error loading the network: ";
        if (network.getIdNetwork() == null ) {
            String netId = getNetworkId (claudiaData, network, region);
            if (netId == null) {
                String msm = message + network.getNetworkName();
                log.error(msm);
                throw new EntityNotFoundException(Network.class, msm, new Exception());
            }
            network.setIdNetwork(netId);
        }

        log.debug("Load network " + network.getIdNetwork() + " in region " + region + " vdc " + claudiaData.getVdc());
        String response = "";
        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            response = openStackUtil.getNetworkDetails(network.getIdNetwork(), region, token, vdc);
            JSONObject jsonNet = new JSONObject(response);
            JSONObject aux =(JSONObject)jsonNet.get("network");
            NetworkInstance netInst = NetworkInstance.fromJson(aux, region);
            JSONArray jsonSubNetworks = aux.getJSONArray("subnets");
            for (int i = 0; i < jsonSubNetworks.length(); i++) {
                SubNetworkInstance subNet = loadSubNetwork(claudiaData, jsonSubNetworks.getString (i), region);
                subNet.setIdNetwork(network.getIdNetwork());
                netInst.addSubNet(subNet);
            }
            return netInst;

        } catch (OpenStackException e) {
            String msm = message + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);
            throw new EntityNotFoundException(Network.class, msm, e);
        } catch (JSONException e) {
            String msm = message + network.getNetworkName() + ":" + e.getMessage();
            log.error(msm);

        }

        return network;

    }

    /**
     * It gets the network id in Openstack.
     * @param claudiaData
     * @param network
     * @param region
     * @return
     */
    public String getNetworkId (ClaudiaData claudiaData, NetworkInstance network, String region)  {
        String networkId = null;
        try {
            List<NetworkInstance> loadAllNetworks = this.loadAllNetwork(claudiaData, region);
            for (NetworkInstance net : loadAllNetworks) {
                if (net.getNetworkName().equals(network.getNetworkName()) &&
                    (net.getShared() || net.getTenantId().equals(claudiaData.getVdc()))) {
                    networkId = net.getIdNetwork();
                }
            }
        } catch (InfrastructureException e) {
            return networkId;
        }
        return networkId;
    }

    /**
     * Set the variable.
     * 
     * @params openStackUtil
     */
    public void setOpenStackUtil(OpenStackUtil openStackUtil) {
        this.openStackUtil = openStackUtil;
    }

    /**
     * It load the subNet.
     */
    public SubNetworkInstance loadSubNetwork(ClaudiaData claudiaData, String subNetId, String region)
        throws EntityNotFoundException{
        SubNetworkInstance subNetInst = null;
        String response;
        try {
            String token = claudiaData.getUser().getToken();
            String vdc = claudiaData.getVdc();
            response = openStackUtil.getSubNetworkDetails(subNetId, region, token, vdc);
            JSONObject jsonNet = new JSONObject(response);
            subNetInst = SubNetworkInstance.fromJson((JSONObject)jsonNet.get("subnet"), region);
        } catch (OpenStackException e) {
            String msm = "Error to obtain the network infromation " + subNetId + ":" + e.getMessage();
            log.error(msm);
            throw new EntityNotFoundException(Network.class, msm, e);
        } catch (JSONException  e) {
            String msm = "Error to obtain the network infromation " + subNetId + ":" + e.getMessage();
            log.error(msm);
            throw new EntityNotFoundException(Network.class, msm, e);
        }
        return subNetInst;
    }

    /**
     * it delete the interface in the public router
     * 
     * @throws InfrastructureException
     */
    public void deleteNetworkToPublicRouter(ClaudiaData claudiaData, NetworkInstance netInstance, String region)
            throws InfrastructureException {
        log.info("Delete Interfact from net " + netInstance.getNetworkName() +
            " to public router in region " + region);

        List<Port> ports = listPortsFromNetwork(claudiaData, region, netInstance.getIdNetwork());
        if (ports.size() > 0) {
            String mns = "Error to delete the interface from public router  " + netInstance.getNetworkName() +
                " to the public router : They are still used ports";
            log.warn(mns);
            return;
        }

        try {
            openStackUtil.deleteInterfaceToPublicRouter(claudiaData.getUser(), netInstance, region);

        } catch (OpenStackException e) {
            String msm = "Error to delete the network " + netInstance.getNetworkName() + " to the public router :"
                    + e.getMessage();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }
    }

    /**
     * It obtains the not shared networks.
     * 
     * @throws InfrastructureException
     */
    public List<NetworkInstance> loadNotSharedNetworks(ClaudiaData claudiaData, String region)
            throws InfrastructureException {
        List<NetworkInstance> networksNotShared = new ArrayList<NetworkInstance>();
        List<NetworkInstance> networks = this.loadAllNetwork(claudiaData, region);
        for (NetworkInstance net : networks) {
            if (!net.getShared()) {
                networksNotShared.add(net);
            }
        }
        return networksNotShared;
    }

    public void joinNetworks(ClaudiaData claudiaData, NetworkInstance networkInstance, NetworkInstance networkInstance2)
            throws InfrastructureException {
        log.info("Join federated networks " + networkInstance.getNetworkName() + " with ids "
                + networkInstance.getIdNetwork() + " " + networkInstance2.getIdNetwork());

        try {
            String response = openStackUtil.joinNetworks(networkInstance, networkInstance2, claudiaData.getUser()
                    .getToken());
            log.debug(response);
        } catch (OpenStackException e) {
            String msm = "Error to join the federated network  " + networkInstance.getNetworkName();
            log.error(msm);
            throw new InfrastructureException(msm, e);
        }

    }

}
