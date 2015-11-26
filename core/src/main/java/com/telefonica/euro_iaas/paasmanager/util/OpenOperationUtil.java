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

// import org.openstack.docs.compute.api.v1.Server;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;

/**
 * @author jesus.movilla
 */
public interface OpenOperationUtil {

    /**
     * pool name in nova *
     */
    // public static final String IPFLOATING_POOL_NAME = "fiprt1";
    /**
     * name of the json type.
     */
    String APPLICATION_JSON = "application/json";
    /**
     * name of the xml type.
     */
    String APPLICATION_XML = "application/xml";
    /**
     * name of the accept header.
     */
    String ACCEPT = "Accept";
    /**
     * name of the content-Type header.
     */
    String CONTENT_TYPE = "Content-Type";
    /**
     * name of the Authentication header.
     */
    String X_AUTH_TOKEN = "X-Auth-Token";
    /**
     * name of the resource Images.
     */
    String RESOURCE_IMAGES = "images/";

    /**
     * path for a detailed resource.
     */

    String ERROR_AUTHENTICATION_HEADERS = "Authentication Token, Tenant ID and User must be initialized...";

    /**
     * Returns a request for a NOVA DELETE petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpUriRequest createNovaDeleteRequest(String resource, String region, String token, String vdc)
            throws OpenStackException;

    /**
     * Returns a request for a NOVA GET petition.
     */
    HttpUriRequest createNovaGetRequest(String resource, String accept, String region, String token, String vdc)
            throws OpenStackException;

    /**
     * Returns a request for a NOVA POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpPost createNovaPostRequest(String resource, String payload, String content, String accept, String region,
            String token, String vdc) throws OpenStackException;

    /**
     * Returns a request for a QUANTUM DELETE petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpUriRequest createQuantumDeleteRequest(String resource, String region, String vdc, String token)
            throws OpenStackException;

    /**
     * Returns a request for a Quantum GET petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpUriRequest createQuantumGetRequest(String resource, String accept, String region, String token, String vdc)
            throws OpenStackException;

    /**
     * Returns a request for a Quantum POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpPost createQuantumPostRequest(String resource, String payload, String content, String region, String token,
            String vdc) throws OpenStackException;

    /**
     * Returns a request for a Quantum POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    HttpPut createQuantumPutRequest(String resource, String payload, String content, String region, String token,
            String vdc) throws OpenStackException;

    /**
     * Method to execute a request and get the response from NOVA.
     * 
     * @param request
     *            the request to be executed
     * @return HttpUriRequest the response from server
     * @throws OpenStackException
     */
    String executeNovaRequest(HttpUriRequest request) throws OpenStackException;


    String executeSupportRequest(HttpUriRequest request) throws OpenStackException;

    /**
     * It creates the Get request for the Support Server.
     * @param resource: the resource
     * @param accept: the acept.
     * @return
     * @throws OpenStackException
     */
    HttpUriRequest createSupportGetRequest(String resource, String accept)
        throws OpenStackException;

    /**
     * @param resourceNetwoksFederated
     * @param payload
     * @param applicationJson
     * @param token
     * @return
     * @throws OpenStackException
     */
    HttpUriRequest createJoinQuantumPostRequestRequest(String resourceNetwoksFederated, String payload,
            String applicationJson, String token) throws OpenStackException;

}
