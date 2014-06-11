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

import static com.telefonica.euro_iaas.paasmanager.util.Configuration.SDC_SERVER_MEDIATYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import com.telefonica.euro_iaas.paasmanager.exception.OpenStackException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.installator.sdc.util.SDCUtilImpl;
import com.telefonica.euro_iaas.sdc.model.Task;
import com.telefonica.euro_iaas.sdc.model.TaskError;
import com.telefonica.euro_iaas.sdc.model.Task.TaskStates;
import com.telefonica.euro_iaas.sdc.client.SDCClient;
import com.telefonica.euro_iaas.sdc.client.services.TaskService;

public class SDCUtilImplTest {
	SDCUtilImpl sdcUtilImpl ;
	OpenStackRegion openStackRegion;
	SDCClient sDCClient;
	
	@Before
	public void setUp () {
		sdcUtilImpl = new SDCUtilImpl ();
		openStackRegion = mock (OpenStackRegion.class);
		sdcUtilImpl.setOpenStackRegion(openStackRegion);
		sDCClient = mock (SDCClient.class);
		sdcUtilImpl.setSDCClient(sDCClient);
	}

    @Test
    public void testCheckTaskStatus() throws ProductInstallatorException, OpenStackException {
    	TaskService taskService = mock (TaskService.class);
    	com.telefonica.euro_iaas.sdc.model.Task task = new Task ();
    	task.setStatus(TaskStates.SUCCESS);
    	when(taskService.load(anyString(),anyString(), anyString())).thenReturn(task);
    	when(openStackRegion.getDefaultRegion(anyString())).thenReturn("region");
    	when(openStackRegion.getSdcEndPoint(anyString(), anyString())).thenReturn("http");
        
          
    	when(sDCClient.getTaskService(anyString(),anyString())).thenReturn(taskService);
    	
    	sdcUtilImpl.checkTaskStatus(task, "token", "vdc");
    	verify (sDCClient.getTaskService(anyString(),anyString()));
    
        
    }
    
    @Test(expected=ProductInstallatorException.class)
    public void testCheckTaskStatusError() throws ProductInstallatorException, OpenStackException {
    	TaskService taskService = mock (TaskService.class);
    	com.telefonica.euro_iaas.sdc.model.Task task = new Task ();
    	task.setStatus(TaskStates.ERROR);
    	TaskError err = new TaskError();
    	err.setMajorErrorCode("error");
    	err.setMessage("error");
    	task.setError(err);
    	when(taskService.load(anyString(),anyString(), anyString())).thenReturn(task);
    	when(openStackRegion.getDefaultRegion(anyString())).thenReturn("region");
    	when(openStackRegion.getSdcEndPoint(anyString(), anyString())).thenReturn("http");
        
          
    	when(sDCClient.getTaskService(anyString(),anyString())).thenReturn(taskService);
    	
    	sdcUtilImpl.checkTaskStatus(task, "token", "vdc");
    	
    	verify (sDCClient.getTaskService(anyString(),anyString()));
    	verify (openStackRegion.getSdcEndPoint(anyString(), anyString()));
        
    }
    
    @Test(expected=ProductInstallatorException.class)
    public void testCheckTaskStatusErrorToLoadUrl() throws ProductInstallatorException, OpenStackException {
    	TaskService taskService = mock (TaskService.class);
    	com.telefonica.euro_iaas.sdc.model.Task task = new Task ();
    	task.setStatus(TaskStates.ERROR);
    	
    	when(taskService.load(anyString(),anyString(), anyString())).thenReturn(task);
    	when(openStackRegion.getDefaultRegion(anyString())).thenReturn("region");
    	when(openStackRegion.getSdcEndPoint(anyString(), anyString())).thenThrow(new OpenStackException("men"));
        
          
    	when(sDCClient.getTaskService(anyString(),anyString())).thenReturn(taskService);
    	
    	sdcUtilImpl.checkTaskStatus(task, "token", "vdc");
    	
    	verify (sDCClient.getTaskService(anyString(),anyString()));
    	verify (openStackRegion.getSdcEndPoint(anyString(), anyString()));
        
    }

} 