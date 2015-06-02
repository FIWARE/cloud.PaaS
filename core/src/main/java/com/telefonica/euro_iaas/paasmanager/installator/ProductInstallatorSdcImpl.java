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

package com.telefonica.euro_iaas.paasmanager.installator;

import static com.telefonica.euro_iaas.paasmanager.util.Configuration.SDC_SERVER_MEDIATYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCClient;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtil;
import com.telefonica.euro_iaas.paasmanager.manager.InfrastructureManager;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.manager.TierInstanceManager;
import com.telefonica.euro_iaas.paasmanager.model.Artifact;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.ProductInstance;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.euro_iaas.sdc.client.exception.ResourceNotFoundException;
import com.telefonica.euro_iaas.sdc.client.services.ChefClientService;
import com.telefonica.euro_iaas.sdc.model.dto.ChefClient;

public class ProductInstallatorSdcImpl implements ProductInstallator {

    public static final String TYPE_PLAIN = "PLAIN";
    public static final String TYPE_IP = "IP";
    public static final String TYPE_IPALL = "IPALL";

    private SDCClient sDCClient;
    private SystemPropertiesProvider systemPropertiesProvider;
    private ProductReleaseManager productReleaseManager;
    private TierInstanceManager tierInstanceManager;
    private InfrastructureManager infrastructureManager;

    private SDCUtil sDCUtil;

    private static Logger log = LoggerFactory.getLogger(ProductInstallatorSdcImpl.class);

    public ProductInstance install(ClaudiaData claudiaData, EnvironmentInstance environmentInstance,
            TierInstance tierInstance, ProductRelease productRelease) throws ProductInstallatorException,
            OpenStackException {

        log.info("Install software " + productRelease.getProduct() + "-" + productRelease.getVersion());
        String sdcServerUrl = sDCUtil.getSdcUtil();

        // From Paasmanager ProductRelease To SDC ProductInstanceDto
        com.telefonica.euro_iaas.sdc.model.dto.ProductInstanceDto productInstanceDto = new com.telefonica.euro_iaas.sdc.model.dto.ProductInstanceDto();
        List<com.telefonica.euro_iaas.sdc.model.Attribute> attrs = new ArrayList<com.telefonica.euro_iaas.sdc.model.Attribute>();

        Set<Attribute> attributes = productRelease.getAttributes();
        if (!(attributes.isEmpty())) {
            for (Attribute attrib : attributes) {
                com.telefonica.euro_iaas.sdc.model.Attribute sdcAttr = resolveAttributeTypeValue(
                        new com.telefonica.euro_iaas.sdc.model.Attribute(attrib.getKey(), attrib.getValue(),
                                attrib.getDescription(), attrib.getType()), tierInstance, environmentInstance);
                attrs.add(sdcAttr);
            }
            productInstanceDto.setAttributes(attrs);
        }

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService pIService = sDCClient
                .getProductInstanceService(sdcServerUrl, SDC_SERVER_MEDIATYPE);
        log.info("sdc url " + sdcServerUrl);

        com.telefonica.euro_iaas.sdc.model.Task task = null;

        productInstanceDto.setVm(new com.telefonica.euro_iaas.sdc.model.dto.VM(tierInstance.getVM().getFqn(),
                tierInstance.getVM().getIp(), tierInstance.getVM().getHostname(), tierInstance.getVM().getDomain(),
                tierInstance.getVM().getOsType()));

        productInstanceDto.setProduct(new com.telefonica.euro_iaas.sdc.model.dto.ReleaseDto(
                productRelease.getProduct(), productRelease.getVersion(), "product"));

        if (tierInstance.getVdc() != null)
            productInstanceDto.setVdc(tierInstance.getVdc());

        productInstanceDto.setAttributes(attrs);

        Attribute attSdcGroup = getAttribute(productRelease.getAttributes(), "sdcgroupid");
        if (attSdcGroup != null) {

            productInstanceDto.getAttributes().add(

                    new com.telefonica.euro_iaas.sdc.model.Attribute(attSdcGroup.getValue(), tierInstance.getVM()
                            .getFqn().substring(0, tierInstance.getVM().getFqn().indexOf(".vees."))));
        }

        Attribute attCoreSdcGroup = getAttribute(productRelease.getAttributes(), "sdccoregroupid");
        if (attCoreSdcGroup != null) {

            productInstanceDto.getAttributes().add(

                    new com.telefonica.euro_iaas.sdc.model.Attribute(attCoreSdcGroup.getValue(), tierInstance.getVM()
                            .getFqn().substring(0, tierInstance.getVM().getFqn().indexOf(".vees."))));
        }

        Attribute attId_web_server = getAttribute(productRelease.getAttributes(), "id_web_server");
        if (attId_web_server != null) {

            productInstanceDto.getAttributes().add(

                    new com.telefonica.euro_iaas.sdc.model.Attribute(attId_web_server.getValue(), tierInstance.getVM()
                            .getFqn().substring(0, tierInstance.getVM().getFqn().indexOf(".vees."))));
        }

        Attribute attApp_server_role = getAttribute(productRelease.getAttributes(), "app_server_role");
        if (attApp_server_role != null) {

            productInstanceDto.getAttributes().add(

                    new com.telefonica.euro_iaas.sdc.model.Attribute(attApp_server_role.getValue(), tierInstance
                            .getVM().getFqn().substring(0, tierInstance.getVM().getFqn().indexOf(".vees."))));
        }

        // Installing product with SDC
        ProductInstance productInstance = new ProductInstance();

        productInstanceDto.getVm().setFqn(tierInstance.getVM().getVmid());

        productInstance.setStatus(Status.INSTALLING);
        try {
            log.info("Installing " + productRelease.getProduct() + "-" + productRelease.getVersion());
            task = pIService.install(tierInstance.getVdc(), productInstanceDto, null, claudiaData.getUser().getToken());
            log.info("task " + task.getHref());
            log.info("task " + task.getResult());
            StringTokenizer tokens = new StringTokenizer(task.getHref(), "/");
            String id = "";

            while (tokens.hasMoreTokens()) {
                id = tokens.nextToken();
            }
            log.info("Install software in productInstance " + productInstanceDto.getProduct().getName() + " task id "
                    + id + " " + task.getHref());

            productInstance.setTaskId(id);
            tierInstance.setTaskId(id);
            tierInstanceManager.update(claudiaData, environmentInstance.getName(), tierInstance);
            sDCUtil.checkTaskStatus(task, claudiaData.getUser().getToken(), tierInstance.getVdc());

            com.telefonica.euro_iaas.sdc.model.ProductInstance pInstanceSDC = pIService.load(tierInstance.getVdc(),
                    productInstanceDto.getVm().getFqn() + "_" + productInstanceDto.getProduct().getName() + "_"
                            + productInstanceDto.getProduct().getVersion(), claudiaData.getUser().getToken());
            // Set the domain
            tierInstance.getVM().setDomain(pInstanceSDC.getVm().getDomain());
            tierInstanceManager.update(claudiaData, environmentInstance.getName(), tierInstance);

        } catch (Exception e) {
            String errorMessage = " Error invokg SDC to Install Product" + productRelease.getName() + " "
                    + productRelease.getVersion() + " " + e.getMessage();
            log.error(errorMessage);
            throw new ProductInstallatorException(errorMessage);
        }

        productInstance.setName(tierInstance.getVM().getVmid() + "_" + productRelease.getProduct() + "_"
                + productRelease.getVersion());
        productInstance.setProductRelease(productRelease);
        productInstance.setVdc(tierInstance.getVdc());

        // sDCUtil.checkTaskStatus(task, productInstance.getVdc());

        productInstance.setStatus(Status.INSTALLED);

        return productInstance;

    }

    public com.telefonica.euro_iaas.sdc.model.Attribute resolveAttributeTypeValue(
            com.telefonica.euro_iaas.sdc.model.Attribute attribute, TierInstance tierInstance,
            EnvironmentInstance environmentInstance) {

        com.telefonica.euro_iaas.sdc.model.Attribute newAtt = new com.telefonica.euro_iaas.sdc.model.Attribute();

        newAtt.setDescription(attribute.getDescription());
        newAtt.setKey(attribute.getKey());
        newAtt.setType(attribute.getType());
        newAtt.setValue(attribute.getValue());

        if (attribute.getValue().contains("(") && attribute.getValue().contains(")")) {

            String name = extractTierNameFromMacro(attribute);
            String compoundName;

            if (TYPE_IP.equals(attribute.getType())) {
                compoundName = infrastructureManager.generateVMName(environmentInstance.getBlueprintName(),
                    name, 1, environmentInstance.getVdc());
                for (TierInstance ti : environmentInstance.getTierInstances()) {
                    if (ti.getName().equals(compoundName)) {
                        newAtt.setValue(ti.getVM().getIp());
                    }
                }
            } else if (TYPE_IPALL.equals(attribute.getType())) {
                String ips = "";
                for (TierInstance ti : environmentInstance.getTierInstances()) {
                    compoundName = infrastructureManager.generateVMName(environmentInstance.getBlueprintName(), name,
                        ti.getNumberReplica(), environmentInstance.getVdc());
                    if (ti.getName().equals(compoundName)) {
                        ips = ips + ti.getVM().getIp() + ",";
                    }
                }
                ips = ips.substring(0, ips.length() - 1);
                newAtt.setValue(ips);
            } else {
                newAtt.setValue(attribute.getValue());
            }
        }

        return newAtt;
    }

    private String extractTierNameFromMacro(com.telefonica.euro_iaas.sdc.model.Attribute attribute) {

        String name = "";
        String value = attribute.getValue();
        if (TYPE_IP.equals(attribute.getType()) || TYPE_IPALL.equals(attribute.getType())) {
            if (value.indexOf("(") != -1 && value.indexOf(")") != -1) {
                name = value.substring(value.indexOf("(") + 1,
                    value.indexOf(")"));
            }
        }
        return name;
    }

    public void installArtifact(ClaudiaData claudiaData, ProductInstance productInstance, Artifact artifact)
            throws ProductInstallatorException, OpenStackException {

        log.info("Install artifact " + artifact.getName() + " in product " + artifact.getProductRelease().getProduct()
                + " for productinstance " + productInstance.getName());
        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService service = sDCClient
                .getProductInstanceService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        List<com.telefonica.euro_iaas.sdc.model.Attribute> atts = new ArrayList();

        for (Attribute att : artifact.getAttributes()) {
            com.telefonica.euro_iaas.sdc.model.Attribute attsdc = new com.telefonica.euro_iaas.sdc.model.Attribute(
                    att.getKey(), att.getValue(), att.getDescription());
            atts.add(attsdc);
        }

        com.telefonica.euro_iaas.sdc.model.Artifact sdcArtifact = new com.telefonica.euro_iaas.sdc.model.Artifact(
                artifact.getName(), atts);

        // Installing product with SDC
        productInstance.setStatus(Status.DEPLOYING_ARTEFACT);

        com.telefonica.euro_iaas.sdc.model.Task task = service.installArtifact(productInstance.getVdc(),
                productInstance.getName(), sdcArtifact, null, claudiaData.getUser().getToken());
        log.info("Deploying artefact " + artifact.getName() + "with href task " + task.getHref());

        StringTokenizer tokens = new StringTokenizer(task.getHref(), "/");
        String id = "";

        while (tokens.hasMoreTokens()) {
            id = tokens.nextToken();
        }
        log.info("Install artifact in productInstance " + productInstance.getProductRelease().getProduct()
                + " task id " + id + " " + task.getHref());

        productInstance.setTaskId(id);
        sDCUtil.checkTaskStatus(task, claudiaData.getUser().getToken(), productInstance.getVdc());

        /* How to catch an productInstallation error */
        if (task.getStatus() == com.telefonica.euro_iaas.sdc.model.Task.TaskStates.ERROR) {
            String mens = "Error installing artefact " + artifact.getName() + " in product instance "
                    + productInstance.getProductRelease().getProduct() + ". Description: " + task.getError();
            log.warn(mens);
            throw new ProductInstallatorException(mens);
        }

        productInstance.setStatus(Status.ARTEFACT_DEPLOYED);

    }

    public void uninstallArtifact(ClaudiaData claudiaData, ProductInstance productInstance, Artifact artifact)
            throws ProductInstallatorException, OpenStackException {
        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService service = sDCClient
                .getProductInstanceService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        List<com.telefonica.euro_iaas.sdc.model.Attribute> atts = new ArrayList();

        for (Attribute att : artifact.getAttributes()) {
            com.telefonica.euro_iaas.sdc.model.Attribute attsdc = new com.telefonica.euro_iaas.sdc.model.Attribute(
                    att.getKey(), att.getValue(), att.getDescription());
            atts.add(attsdc);
        }

        com.telefonica.euro_iaas.sdc.model.Artifact sdcArtifact = new com.telefonica.euro_iaas.sdc.model.Artifact(
                artifact.getName(), atts);

        // Installing product with SDC
        productInstance.setStatus(Status.UNDEPLOYING_ARTEFACT);
        com.telefonica.euro_iaas.sdc.model.Task task = service.uninstallArtifact(productInstance.getVdc(),
                productInstance.getName(), sdcArtifact, null, claudiaData.getUser().getToken());
        /* How to catch an productInstallation error */
        if (task.getStatus() == com.telefonica.euro_iaas.sdc.model.Task.TaskStates.ERROR)
            throw new ProductInstallatorException("Error uninstalling artefact " + artifact.getName()
                    + " in product instance " + productInstance.getProductRelease().getProduct() + ". Description: "
                    + task.getError());

        productInstance.setStatus(Status.ARTEFACT_UNDEPLOYED);

    }

    public void uninstall(ClaudiaData claudiaData, ProductInstance productInstance) throws ProductInstallatorException,
            OpenStackException {

        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService productService = sDCClient
                .getProductInstanceService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        try {
            productService.uninstall(productInstance.getVdc(), productInstance.getName(), null, claudiaData.getUser()
                    .getToken());
        } catch (Exception e) {
            String errorMessage = " Error invokg SDC to UnInstall Product" + productInstance.getName();
            log.error(errorMessage);
            throw new ProductInstallatorException(errorMessage);
        }

    }

    public void configure(ClaudiaData claudiaData, ProductInstance productInstance, List<Attribute> properties)
            throws ProductInstallatorException, OpenStackException {
        log.info("Configure product " + productInstance.getName() + " "
                + productInstance.getProductRelease().getProduct());
        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ProductInstanceService pIService = sDCClient
                .getProductInstanceService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        List<com.telefonica.euro_iaas.sdc.model.Attribute> arguments = new ArrayList();

        if (properties != null && properties.size() != 0) {
            for (Attribute attri : properties) {

                com.telefonica.euro_iaas.sdc.model.Attribute att = new com.telefonica.euro_iaas.sdc.model.Attribute();
                att.setKey(attri.getKey());
                att.setKey(attri.getValue());
                arguments.add(att);
            }
        }

        com.telefonica.euro_iaas.sdc.model.Task task = null;

        String name = getProductInstanceName(claudiaData, productInstance);

        // FIWARE.customers.60b4125450fc4a109f50357894ba2e28.services.deploytm.vees.contextbrokr.replicas.1_mongos_2.2.3
        // deploytm-contextbrokr-1_mongos_2.2.3
        try {
            task = pIService.configure(productInstance.getVdc(), name, null, arguments, claudiaData.getUser()
                    .getToken());
        } catch (Exception e) {
            String errorMessage = " Error invokg SDC to configure Product" + productInstance.getName() + " "
                    + e.getMessage();
            log.error(errorMessage);
            throw new ProductInstallatorException(errorMessage);
        }

        sDCUtil.checkTaskStatus(task, claudiaData.getUser().getToken(), productInstance.getVdc());

        return;

    }

    // Borrado del nodo en el ChefServer

    public void deleteNode(ClaudiaData claudiaData, String vdc, String sdcNodeName) throws ProductInstallatorException,
            OpenStackException {

        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        ChefClientService chefClientService = sDCClient.getChefClientService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        com.telefonica.euro_iaas.sdc.model.Task task = null;

        // Borrado del Nodo en el chef Server
        try {
            task = chefClientService.delete(vdc, sdcNodeName, claudiaData.getUser().getToken());
        } catch (Exception e) {
            String errorMessage = " Error invokg SDC to delete Chef Server Node " + sdcNodeName + " " + e.getMessage();
            log.error(errorMessage);
            throw new ProductInstallatorException(errorMessage);
        }

        sDCUtil.checkTaskStatus(task, claudiaData.getUser().getToken(), vdc);

        return;
    }

    // Load a node from the nodename

    public ChefClient loadNode(ClaudiaData claudiaData, String vdc, String hostname)
            throws ProductInstallatorException, EntityNotFoundException, OpenStackException {

        String sdcServerUrl = sDCUtil.getSdcUtil();

        // SDCClient client = new SDCClient();
        com.telefonica.euro_iaas.sdc.client.services.ChefClientService chefClientService = sDCClient
                .getChefClientService(sdcServerUrl, SDC_SERVER_MEDIATYPE);

        com.telefonica.euro_iaas.sdc.model.Task task = null;

        // Borrado del Nodo en el chef Server
        try {
            return chefClientService.loadByHostname(vdc, hostname, claudiaData.getUser().getToken());
        } catch (ResourceNotFoundException rnfe) {
            throw new EntityNotFoundException(ChefClient.class, rnfe.getMessage(), rnfe);
        } catch (Exception e) {
            String errorMessage = " Error invokg SDC to delete Chef Server Node " + hostname + " " + e.getMessage();
            log.error(errorMessage);
            throw new ProductInstallatorException(errorMessage);
        }
    }

    public Attribute getAttribute(Set<Attribute> attributes, String key) {
        if (attributes == null)
            return null;
        for (Attribute attribute : attributes) {
            if (attribute.getKey().equals(key))
                return attribute;
        }
        return null;
    }

    // //////////// I.O.C /////////////
    /**
     * @param sDCClient
     *            the sDCClient to set
     */
    public void setSDCClient(SDCClient sDCClient) {
        this.sDCClient = sDCClient;
    }

    /**
     * @param systemPropertiesProvider
     *            the systemPropertiesProvider to set
     */
    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    public void setProductReleaseManager(ProductReleaseManager productReleaseManager) {
        this.productReleaseManager = productReleaseManager;
    }

    public void setTierInstanceManager(TierInstanceManager tierInstanceManager) {
        this.tierInstanceManager = tierInstanceManager;
    }

    public void setSDCUtil(SDCUtil sDCUtil) {
        this.sDCUtil = sDCUtil;
    }

    public String getProductInstanceName(ClaudiaData claudiaData, ProductInstance productInstance) {
        String tierName = "";
        String productName = "";
        // Installing product with SDC
        StringTokenizer st = new StringTokenizer(productInstance.getName(), "-");

        while (st.hasMoreTokens()) {

            st.nextToken();
            tierName = st.nextToken();

            productName = st.nextToken();
        }
        String name = claudiaData.getOrg() + ".customers." + claudiaData.getVdc() + ".services."
                + claudiaData.getService() + ".vees." + tierName + ".replicas." + productName;
        return name;

    }

    public void setInfrastructureManager(InfrastructureManager infrastructureManager) {
        this.infrastructureManager = infrastructureManager;
    }

    /*
     * private Map<String, String> getHeaders(ClaudiaData claudiaData) { Map<String, String> headers = new
     * HashMap<String, String>(); headers.put("X-Auth-Token", claudiaData.getUser().getToken());
     * headers.put("Tenant-ID", claudiaData.getUser().getTenantId()); }
     */

}
