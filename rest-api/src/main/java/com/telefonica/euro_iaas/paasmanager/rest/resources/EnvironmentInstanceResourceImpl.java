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

package com.telefonica.euro_iaas.paasmanager.rest.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.telefonica.euro_iaas.paasmanager.exception.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.exception.QuotaExceededException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.async.EnvironmentInstanceAsyncManager;
import com.telefonica.euro_iaas.paasmanager.manager.async.TaskManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Environment;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.InstallableInstance.Status;
import com.telefonica.euro_iaas.paasmanager.model.Task;
import com.telefonica.euro_iaas.paasmanager.model.Task.TaskStates;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.euro_iaas.paasmanager.model.dto.EnvironmentInstanceDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.EnvironmentInstancePDto;
import com.telefonica.euro_iaas.paasmanager.model.searchcriteria.EnvironmentInstanceSearchCriteria;
import com.telefonica.euro_iaas.paasmanager.rest.auth.OpenStackAuthenticationProvider;
import com.telefonica.euro_iaas.paasmanager.rest.exception.APIException;
import com.telefonica.euro_iaas.paasmanager.rest.validation.EnvironmentInstanceResourceValidator;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;

/**
 * Default EnvironmentInstanceResource implementation.
 * 
 * @author Jesus M. Movilla
 */
@Path("/envInst/org/{org}/vdc/{vdc}/environmentInstance")
@Component
@Scope("request")
public class EnvironmentInstanceResourceImpl implements EnvironmentInstanceResource {

    public static final int ERROR_NOT_FOUND = 404;

    @Autowired
    private EnvironmentInstanceAsyncManager environmentInstanceAsyncManager;

    @Autowired
    private EnvironmentInstanceManager environmentInstanceManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private EnvironmentInstanceResourceValidator validator;

    @Autowired
    private SystemPropertiesProvider systemPropertiesProvider;

    private static Logger log = LoggerFactory.getLogger(EnvironmentInstanceResourceImpl.class);

    /**
     * @throws APIException
     */
    public Task create(String org, String vdc, EnvironmentInstanceDto environmentInstanceDto, String callback)
            throws APIException {

        log.warn("Deploy an environment instance " + environmentInstanceDto.getBlueprintName() + " from environment "
                + environmentInstanceDto.getEnvironmentDto());

        Task task = null;

        ClaudiaData claudiaData = new ClaudiaData(org, vdc, environmentInstanceDto.getBlueprintName());
        OpenStackAuthenticationProvider.addCredentialsToClaudiaData(claudiaData);
        try {
            validator.validateCreate(environmentInstanceDto, systemPropertiesProvider, claudiaData);

        } catch (InvalidEntityException e) {
            log.error("The environment instance is not valid " + e.getMessage());
            throw new APIException(e);
        } catch (QuotaExceededException e) {
            throw new APIException(e);
        }

        EnvironmentInstance environmentInstance = environmentInstanceDto.fromDto();
        Environment environment = environmentInstance.getEnvironment();
        log.info("Environment name " + environment.getName() + " " + environment.getVdc() + " " + environment.getOrg()
                + " ");
        for (Tier tier : environment.getTiers()) {
            log.info("Tier " + tier.getName() + " image " + tier.getImage());
        }
        environmentInstance.setVdc(vdc);

        environmentInstance.setStatus(Status.INIT);
        environmentInstance.setDescription(environmentInstanceDto.getDescription());
        environmentInstance.setBlueprintName(environmentInstanceDto.getBlueprintName());

        log.info("EnvironmentInstance name " + environmentInstance.getBlueprintName() + " vdc "
                + environmentInstance.getVdc() + "  description " + environmentInstance.getDescription() + "  status "
                + environmentInstance.getStatus() + " environment  " + environmentInstance.getEnvironment().getName());

        task = createTask(MessageFormat.format("Create environment {0}", environmentInstance.getBlueprintName()), vdc,
                environmentInstance.getBlueprintName());

        environmentInstance.setTaskId("" + task.getId());

        environmentInstanceAsyncManager.create(claudiaData, environmentInstance, task, callback);
        return task;
    }

    /**
	 *
	 */
    public List<EnvironmentInstancePDto> findAll(Integer page, Integer pageSize, String orderBy, String orderType,
            List<Status> status, String vdc) {

        EnvironmentInstanceSearchCriteria criteria = new EnvironmentInstanceSearchCriteria();

        criteria.setVdc(vdc);
        criteria.setStatus(status);

        if (page != null && pageSize != null) {
            criteria.setPage(page);
            criteria.setPageSize(pageSize);
        }
        if (!StringUtils.isEmpty(orderBy)) {
            criteria.setOrderBy(orderBy);
        }
        if (!StringUtils.isEmpty(orderType)) {
            criteria.setOrderBy(orderType);
        }

        List<EnvironmentInstance> environmentInstances = environmentInstanceManager.findByCriteria(criteria);

        List<EnvironmentInstancePDto> envInstancesDto = new ArrayList<EnvironmentInstancePDto>();
        for (int i = 0; i < environmentInstances.size(); i++) {
            envInstancesDto.add(environmentInstances.get(i).toPDtos());
        }
        return envInstancesDto;
    }

    /**
     * 
     */
    public EnvironmentInstancePDto load(String vdc, String name) {
        EnvironmentInstanceSearchCriteria criteria = new EnvironmentInstanceSearchCriteria();
        criteria.setVdc(vdc);
        criteria.setEnviromentName(name);

        List<EnvironmentInstance> environmentInstances = environmentInstanceManager.findByCriteria(criteria);

        if (environmentInstances == null || environmentInstances.size() == 0) {
            throw new WebApplicationException(new EntityNotFoundException(Environment.class, "EnvironmeniInstance "
                    + name + " not found", ""), ERROR_NOT_FOUND);
        } else {
            // EnvironmentInstancePDto envInstanceDto = envInstances.get(0).toPDto();
            EnvironmentInstancePDto envInstanceDto = environmentInstances.get(0).toPDto();
            return envInstanceDto;
        }

        /*
         * try { EnvironmentInstance envInstance = environmentInstanceManager.load( vdc, name); // return
         * envInstance.toDto(); log.info(envInstance.toPDto()); return envInstance.toPDto(); } catch
         * (EntityNotFoundException e) { throw new WebApplicationException(e.getCause(), 404); }
         */
    }

    /**
     * Delete a specific environment instance.
     * 
     * @param org
     *            The organization that contains the environment instance.
     * @param vdc
     *            The vdc of the environment instance.
     * @param name
     *            The name of the instance.
     * @param callback
     *            if not empty, contains the url where the result of the async operation will be sent
     * @return The task to follow the execution of the operation.
     * @throws APIException
     *             Any exception launched during the process.
     */
    public Task destroy(String org, String vdc, String name, String callback) throws APIException {

        log.info("Destroy environment instance " + name + " vdc " + vdc);
        EnvironmentInstance environmentInstance = null;
        try {
            environmentInstance = environmentInstanceManager.load(vdc, name);
        } catch (EntityNotFoundException e) {
            log.warn("Not found " + e.getMessage());
            throw new APIException(e);
        }

        ClaudiaData claudiaData = new ClaudiaData(org, vdc, name);

        OpenStackAuthenticationProvider.addCredentialsToClaudiaData(claudiaData);

        Task task = createTask(MessageFormat.format("Destroying EnvironmentInstance {0} ", name), vdc, name);
        environmentInstanceAsyncManager.destroy(claudiaData, environmentInstance, task, callback);
        return task;
    }

    /**
     * createTask.
     * 
     * @param description
     * @param vdc
     * @return
     */
    private Task createTask(String description, String vdc, String environment) {
        Task task = new Task(TaskStates.RUNNING);
        task.setDescription(description);
        task.setVdc(vdc);
        task.setEnvironment(environment);
        return taskManager.createTask(task);
    }

    /**
     * @param validator
     *            the validator to set
     */
    public void setValidator(EnvironmentInstanceResourceValidator validator) {
        this.validator = validator;
    }

    /**
     * @param systemPropertiesProvider
     *            the systemPropertiesProvider to set
     */
    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

    public void setEnvironmentInstanceAsyncManager(EnvironmentInstanceAsyncManager environmentInstanceAsyncManager) {
        this.environmentInstanceAsyncManager = environmentInstanceAsyncManager;
    }

    public void setEnvironmentInstanceManager(EnvironmentInstanceManager environmentInstanceManager) {
        this.environmentInstanceManager = environmentInstanceManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

}
