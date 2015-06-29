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

package com.telefonica.euro_iaas.paasmanager.claudia;

import java.util.List;

import com.telefonica.euro_iaas.paasmanager.bean.PaasManagerUser;
import com.telefonica.euro_iaas.paasmanager.exception.ClaudiaResourceNotFoundException;
import com.telefonica.euro_iaas.paasmanager.exception.ClaudiaRetrieveInfoException;
import com.telefonica.euro_iaas.paasmanager.exception.IPNotRetrievedException;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.NetworkNotRetrievedException;
import com.telefonica.euro_iaas.paasmanager.exception.OSNotRetrievedException;
import com.telefonica.euro_iaas.paasmanager.exception.VMStatusNotRetrievedException;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;
import com.telefonica.euro_iaas.paasmanager.model.dto.VM;

/**
 * @author jesus.movilla
 */
public interface ClaudiaClient {

    /**
     * Browse the selected VDC for the given organization (browseVDC).
     */
    String browseVDC(ClaudiaData claudiaData) throws ClaudiaResourceNotFoundException;

    /**
     * Browse the selected Service for the given VDC (browseService).
     */
    String browseService(ClaudiaData claudiaData) throws ClaudiaResourceNotFoundException;

    /**
     * Browse the selected VM for the given Service (browseVM).
     * 
     * @param org
     *            Organization identifier
     * @param vdc
     *            Virtual Data Center identifier
     * @param service
     *            Service identifier
     * @param vm
     *            Virtual Machine identifier
     * @return the response
     * @throws ClaudiaResourceNotFoundException
     *             if the VM does not exist
     */
    String browseVM(String org, String vdc, String service, String vm, PaasManagerUser user)
            throws ClaudiaResourceNotFoundException;

    /**
     * Deploy a service known as ServiceId for a given Org, Customer (deployService).
     */
    String deployService(ClaudiaData claudiaData, String ovf) throws InfrastructureException;

    /**
     * Deploy a VM known as ServiceId for a given Org, Customer (deployService).
     * 
     * @param org
     *            Organization identifier
     * @param vdc
     *            Virtual Data Center identifier
     * @param service
     *            Service identifier
     * @param vmName
     *            the vmName file to be deployed
     * @param vmPath
     *            the vmPath * @return the response
     * @throws InfrastructureException
     *             if there is any problem with the connection
     */
    String deployVM(String org, String vdc, String service, String vmName, PaasManagerUser user, String vmPath)
            throws InfrastructureException;

    /**
     * Undeploy a VM.
     */
    void undeployVMReplica(ClaudiaData claudiaData, TierInstance tierInstance) throws InfrastructureException;

    /**
     * Deploy a VDC for the selected organization (deployVDC).
     */
    String deployVDC(ClaudiaData claudiaData, String cpu, String mem, String disk) throws InfrastructureException;

    /**
     * Deploy a VM from claudiaData and a tier.
     */
    void deployVM(ClaudiaData claudiaData, TierInstance tierInstance, int replica, VM vm)
            throws InfrastructureException;

    /**
     * @param claudiaData
     * @param tierInstance
     * @throws InfrastructureException
     */
    void undeployVM(ClaudiaData claudiaData, TierInstance tierInstance) throws InfrastructureException;

    /**
     * @param claudiaData
     * @throws InfrastructureException
     */
    void undeployService(ClaudiaData claudiaData) throws InfrastructureException;

    /**
     * @param org
     * @param vdc
     * @param service
     * @param vmName
     * @return
     * @throws IPNotRetrievedException
     * @throws ClaudiaResourceNotFoundException
     */
    String obtainIPFromFqn(String org, String vdc, String service, String vmName, PaasManagerUser user)
            throws IPNotRetrievedException, ClaudiaResourceNotFoundException, NetworkNotRetrievedException;

    /**
     * Obtain OS.
     */
    String obtainOS(String org, String vdc, String service, String vmName, PaasManagerUser user)
            throws OSNotRetrievedException, ClaudiaResourceNotFoundException;

    /**
     * Get VApp of a Virtual Machine.
     */
    String getVApp(String org, String vdc, String service, String vmName, PaasManagerUser user)
            throws IPNotRetrievedException, ClaudiaResourceNotFoundException, NetworkNotRetrievedException,
            OSNotRetrievedException;

    /**
     * Start/Stop the scalability.
     */
    String onOffScalability(ClaudiaData claudiaData, String environmentName, boolean b) throws InfrastructureException;

    /**
     * Create Image.
     */
    // api/org/{org-id}/vdc/{vdc-id}/vapp/{vapp-id}/{vee-id}/{vm-id}/action/templatize
    // /api/org/es_tid/vdc/cc1/vapp/demorm/vees/SuperTryBea2/action/templatize
    // api/org/es_tid/vdc/cc1/vapp/demorm/veesAaUbuntu_Collectd_Bea/vmAna2/action/instantiateOvf
    String createImage(ClaudiaData claudiaData, TierInstance tierInstance) throws ClaudiaRetrieveInfoException;

    /**
     * @param org
     * @param vdc
     * @param service
     * @param vmName
     * @return
     */
    String switchVMOn(String org, String vdc, String service, String vmName, PaasManagerUser user)
            throws InfrastructureException;

    /**
     * @param vapp
     * @return
     */
    String obtainVMStatus(String vapp) throws VMStatusNotRetrievedException;

    /**
     * Get PublicIP.
     */
    List<String> getIP(ClaudiaData claudiaData, String tierName, int replica, VM vm, String region)
            throws InfrastructureException;

    /**
     * @param claudiaData
     * @param ovf
     * @return
     * @throws InfrastructureException
     */
    String deployServiceFull(ClaudiaData claudiaData, String ovf) throws InfrastructureException;

    /**
     * @param claudiaData
     * @param tierName
     * @param vm
     * @param region
     * @return
     */
    boolean existsVMReplica(ClaudiaData claudiaData, String tierName, VM vm, String region);

    /**
     * Recover all vmids associated to a certaint tenantId.
     */
    List<String> findAllVMs(ClaudiaData claudiaData, String region) throws ClaudiaResourceNotFoundException;

}
