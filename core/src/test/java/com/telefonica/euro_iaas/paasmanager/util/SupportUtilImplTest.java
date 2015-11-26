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

import com.telefonica.fiware.commons.openstack.auth.exception.OpenStackException;
import static org.mockito.Mockito.*;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SupportUtilImplTest {
    SupportServerUtilsImpl supportServerUtils;
    OpenOperationUtil openOperationUtil;

    @Before
    public void setUp() {
        supportServerUtils = new SupportServerUtilsImpl();
        openOperationUtil = mock(OpenOperationUtil.class);
        supportServerUtils.setOpenOperationUtil(openOperationUtil);

    }

    /**
     * It tests the
     * @throws OpenStackException
     */
    @Test
    public void testGetSshKey() throws OpenStackException {
        String sshKey = "ssh-rsa key";
        HttpGet httpGet = mock(HttpGet.class);

        when(openOperationUtil.createSupportGetRequest("sshkey", "application/json")).thenReturn(httpGet);

        when(openOperationUtil.executeSupportRequest(any(HttpUriRequest.class))).thenReturn(sshKey);
        String key = supportServerUtils.getSshKey("region");
        assertEquals (sshKey, key);
    }

    @Test
    public void testGetGpgKey() throws OpenStackException {
        String gpgKey = "ff";
        HttpGet httpGet = mock(HttpGet.class);

        when(openOperationUtil.createSupportGetRequest("gpgkey", "application/json")).thenReturn(httpGet);

        when(openOperationUtil.executeSupportRequest(any(HttpUriRequest.class))).thenReturn(gpgKey);
        String gpg = supportServerUtils.getGpgKey("region");
        assertEquals (gpgKey, gpg);
    }

    @Test
    public void testGetGpgKeyError() throws OpenStackException {
        HttpGet httpGet = mock(HttpGet.class);
        when(openOperationUtil.createSupportGetRequest("gpgkey", "application/json")).thenReturn(httpGet);

        when(openOperationUtil.executeSupportRequest(any(HttpUriRequest.class))).thenReturn(null);
        String gpg = supportServerUtils.getGpgKey("region");
        assertNull(gpg);

    }

}