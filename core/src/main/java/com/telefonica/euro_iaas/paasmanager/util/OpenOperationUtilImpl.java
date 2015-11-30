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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;

/**
 * @author jesus.movilla
 */
public class OpenOperationUtilImpl implements OpenOperationUtil {

    /**
     * The log.
     */

    private static Logger log = LoggerFactory.getLogger(OpenOperationUtilImpl.class);

    /**
     * HTTP code for accepted requests.
     */
    private static int http_code_accepted = 202;
    /**
     * HTTP code for accepted requests.
     */
    private static int http_code_ok = 200;
    /**
     * HTTP code for created requests.
     */
    private static int http_code_created = 201;
    /**
     * HTTP code for no content response.
     */
    private static int http_code_deleted = 204;

    /**
     * HTTP code for no found..
     */
    private static int http_code_not_found = 404;

    private HttpClientConnectionManager httpConnectionManager;

    private OpenStackRegion openStackRegion;

    /**
     * The constructor.
     */
    public OpenOperationUtilImpl() {
    }

    public HttpClientConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    public void setHttpConnectionManager(HttpClientConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    /**
     * Returns an InputStream as String.
     * 
     * @param is
     *            InputStream from response
     * @return Compute Compute
     * @throws OpenStackException
     *             OCCIException
     */
    private static String convertStreamToString(InputStream is) throws OpenStackException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw new OpenStackException(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new OpenStackException(e.getMessage());
            }
        }
        return sb.toString();
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

    /**
     * Returns a request for a NOVA DELETE petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpUriRequest createNovaDeleteRequest(String resource, String region, String token, String vdc)
            throws OpenStackException {
        HttpUriRequest request;

        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        String novaUrl = openStackRegion.getNovaEndPoint(region);
        log.debug("novaUrl" + novaUrl);
        request = new HttpDelete(novaUrl + vdc + "/" + resource);

        // request.setHeader(OpenStackConstants.CONTENT_TYPE,
        // OpenStackConstants.APPLICATION_JSON);
        log.debug("NOVA DELETE url " + request.getURI().toString());
        log.debug("NOVA token " + token);
        request.setHeader(ACCEPT, APPLICATION_JSON);
        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * Returns a request for a NOVA GET petition.
     */
    public HttpUriRequest createNovaGetRequest(String resource, String accept, String region, String token, String vdc)
            throws OpenStackException {
        HttpUriRequest request;

        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        String novaUrl = openStackRegion.getNovaEndPoint(region);
        request = new HttpGet(novaUrl + vdc + "/" + resource);

        // request.setHeader(OpenStackConstants.CONTENT_TYPE,
        // OpenStackConstants.APPLICATION_XML);
        log.debug("NOVA GET url " + request.getURI().toString());
        log.debug("NOVA token " + token);
        request.setHeader(ACCEPT, accept);
        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * Returns a request for a NOVA POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpPost createNovaPostRequest(String resource, String payload, String content, String accept,
            String region, String token, String vdc) throws OpenStackException {
        HttpPost request;

        // Check that the authtoken, tenant and user was initialized
        // previously.
        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.info("Payload " + payload);

        String novaUrl = openStackRegion.getNovaEndPoint(region);

        request = new HttpPost(novaUrl + vdc + "/" + resource);

        try {
            request.setEntity(new StringEntity(payload));

        } catch (NullPointerException e) {
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new OpenStackException(ex.getMessage());
        }

        log.debug("NOVA POST url " + request.getURI().toString());
        log.debug("NOVA token " + token);

        request.setHeader(CONTENT_TYPE, content);
        request.setHeader(ACCEPT, accept);
        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * Returns a request for a QUANTUM DELETE petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpUriRequest createQuantumDeleteRequest(String resource, String region, String vdc, String token)
            throws OpenStackException {
        HttpUriRequest request;

        // Check that the authtoken, tenant and user was initialized
        // previously.
        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        String quantumUrl = null;
        quantumUrl = openStackRegion.getQuantumEndPoint(region);
        request = new HttpDelete(quantumUrl + resource);

        // request.setHeader(OpenStackConstants.CONTENT_TYPE, OpenStackConstants.APPLICATION_JSON);
        request.setHeader(ACCEPT, APPLICATION_JSON);
        log.debug(X_AUTH_TOKEN + " " + token);
        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * Returns a request for a Quantum GET petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpUriRequest createQuantumGetRequest(String resource, String accept, String region, String token,
            String vdc) throws OpenStackException {
        HttpUriRequest request;

        // Check that the auth token, tenant and user was initialized previously.
        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        String quantumUrl = openStackRegion.getQuantumEndPoint(region);
        log.debug("quantumUrl for region " + region + " " + quantumUrl);
        request = new HttpGet(quantumUrl + resource);

        request.setHeader(ACCEPT, accept);
        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * Returns a request for a Quantum POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpPost createQuantumPostRequest(String resource, String payload, String content, String region,
            String token, String vdc) throws OpenStackException {
        log.debug("createQuantumPostRequest " + resource);
        HttpPost request;

        // Check that the authtoken, tenant and user was initialized
        // previously.
        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.info("Payload " + payload);

        String quantumUrl = openStackRegion.getQuantumEndPoint(region);
        request = new HttpPost(quantumUrl + resource);

        try {

            request.setEntity(new StringEntity(payload));

        } catch (NullPointerException e) {
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new OpenStackException(ex.getMessage());
        }

        request.setHeader(ACCEPT, APPLICATION_JSON);

        request.setHeader(CONTENT_TYPE, content);
        log.debug("Content " + content);

        request.setHeader(X_AUTH_TOKEN, token);
        log.debug("user.getToken() " + token);

        return request;
    }

    /**
     * Returns a request for a Quantum POST petition.
     * 
     * @param resource
     *            the target resource
     * @return HttpUriRequest the request
     */
    public HttpPut createQuantumPutRequest(String resource, String payload, String content, String region,
            String token, String vdc) throws OpenStackException {
        HttpPut request;

        // Check that the authtoken, tenant and user was initialized
        // previously.
        try {
            checkParam(vdc, token);
        } catch (OpenStackException ex) {
            java.util.logging.Logger.getLogger(OpenOperationUtilImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.info("Payload " + payload);

        String quantumUrl = openStackRegion.getQuantumEndPoint(region);
        request = new HttpPut(quantumUrl + resource);

        try {

            request.setEntity(new StringEntity(payload));

        } catch (NullPointerException e) {
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new OpenStackException(ex.getMessage());
        }

        request.setHeader(ACCEPT, APPLICATION_JSON);

        request.setHeader(CONTENT_TYPE, content);

        request.setHeader(X_AUTH_TOKEN, token);

        return request;
    }

    /**
     * It obtains the request for Support Server.
     * @param resource
     * @param accept
     * @return
     * @throws OpenStackException
     */
    public HttpUriRequest createSupportGetRequest(String resource, String accept)
        throws OpenStackException {
        HttpUriRequest request;

        String supportUrl = openStackRegion.getSupportEndPoint();
        log.debug("supportUrl for region " + supportUrl);
        request = new HttpGet(supportUrl + resource);

        request.setHeader(ACCEPT, accept);
        return request;
    }

    /**
     * Method to execute a request and get the response from NOVA.
     * 
     * @param request
     *            the request to be executed
     * @return HttpUriRequest the response from server
     * @throws OpenStackException
     */
    public String executeNovaRequest(HttpUriRequest request) throws OpenStackException {
        log.debug("executeNovaRequest " + request.getURI().toString());
        String[] newHeaders = null;
        // Where the response is located. 0 for json, 1 for XML (it depends on
        // the \n)
        int responseLocation = 0;

        CloseableHttpClient httpClient = getHttpClient();

        if (request.containsHeader(ACCEPT) && request.getFirstHeader(ACCEPT).getValue().equals(APPLICATION_XML)) {
            responseLocation = 1;
        }
        HttpResponse response = null;

        try {
            response = httpClient.execute(request);
            log.debug("Status : " + response.getStatusLine().getStatusCode());
            // if (response.getEntity() != null) {
            if ((response.getStatusLine().getStatusCode() != http_code_deleted)) {

                InputStream is = response.getEntity().getContent();
                String result = convertStreamToString(is);
                log.debug("Result " + result);

                is.close();

                if ((response.getStatusLine().getStatusCode() == http_code_ok)
                        || (response.getStatusLine().getStatusCode() == http_code_accepted)
                        || (response.getStatusLine().getStatusCode() == http_code_created)) {

                    newHeaders = result.split("\n");
                } else {
                    log.debug(" HttpResponse " + response.getStatusLine().getStatusCode());
                    if (result.indexOf("badRequest") != -1 || result.indexOf("itemNotFound") != -1) {
                        int i = result.indexOf("message");
                        int j = result.lastIndexOf("message");

                        String error = result.substring(i + "message".length() + 1, j - 2);
                        log.info("Error in the request, message: " + error);
                        throw new OpenStackException(error);
                    }

                    throw new OpenStackException(result);
                }

                response.getEntity().getContent().close();
            } else {
                return response.getStatusLine().getReasonPhrase();
            }

        } catch (Exception e) {
            log.warn("Error in the request, message:" + e.getMessage());
            if (response.getStatusLine().getStatusCode() == http_code_accepted) {
                return response.getStatusLine().getReasonPhrase();
            } else {
                throw new OpenStackException(e.getMessage());
            }

        }

        if (response.containsHeader("Location")
                && response.getFirstHeader("Location").getValue().contains(RESOURCE_IMAGES)) {

            return response.getFirstHeader("Location").getValue();
        }

        return newHeaders[responseLocation];
    }

    /**
     * Method to execute a request and get the response from NOVA.
     *
     * @param request
     *            the request to be executed
     * @return HttpUriRequest the response from server
     * @throws OpenStackException
     */
    public String executeSupportRequest(HttpUriRequest request) {
        log.debug("executeSupportRequest " + request.getURI().toString());
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        String result = null;

        try {
            response = httpClient.execute(request);
            log.debug("Status : " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == http_code_ok) {
                InputStream is = response.getEntity().getContent();
                result = convertStreamToString(is);
                log.debug("Result " + result);

                is.close();
                response.getEntity().getContent().close();
            }

        } catch (Exception e) {
            log.warn("Error in the request, message:" + response.getStatusLine().getStatusCode() +
                " : " + e.getMessage());
        }
        return result;
    }

    protected CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setConnectionManager(httpConnectionManager).build();
    }

    public OpenStackRegion getOpenStackRegion() {
        return openStackRegion;
    }

    public void setOpenStackRegion(OpenStackRegion openStackRegion) {
        this.openStackRegion = openStackRegion;
    }

    public HttpUriRequest createJoinQuantumPostRequestRequest(String resource, String payload, String applicationJson,
            String token) throws OpenStackException {
        log.debug("createJoinQuantumPostRequestRequest " + resource);
        HttpPost request;

        log.info("Payload " + payload);

        String quantumUrl = openStackRegion.getFederatedQuantumEndPoint();
        request = new HttpPost(quantumUrl + resource);

        try {

            request.setEntity(new StringEntity(payload));

        } catch (NullPointerException e) {
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            throw new OpenStackException(ex.getMessage());
        }

        request.setHeader(ACCEPT, APPLICATION_JSON);
        request.setHeader(X_AUTH_TOKEN, token);
        log.debug("user.getToken() " + token);

        return request;
    }

}
