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

package com.telefonica.euro_iaas.paasmanager.manager.async.impl;

import static com.telefonica.euro_iaas.paasmanager.util.Configuration.ENVIRONMENT_INSTANCE_PATH;
import static com.telefonica.euro_iaas.paasmanager.util.Configuration.ENVIRONMENT_PATH;
import static com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider.PAAS_MANAGER_URL;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;
import com.telefonica.euro_iaas.paasmanager.dao.TierDao;
import com.telefonica.euro_iaas.paasmanager.exception.IPNotRetrievedException;
import com.telefonica.euro_iaas.paasmanager.exception.InfrastructureException;
import com.telefonica.euro_iaas.paasmanager.exception.NotUniqueResultException;
import com.telefonica.euro_iaas.paasmanager.exception.ProductInstallatorException;
import com.telefonica.euro_iaas.paasmanager.exception.TaskNotFoundException;
import com.telefonica.euro_iaas.paasmanager.manager.EnvironmentInstanceManager;
import com.telefonica.euro_iaas.paasmanager.manager.SecurityGroupManager;
import com.telefonica.euro_iaas.paasmanager.manager.async.EnvironmentInstanceAsyncManager;
import com.telefonica.euro_iaas.paasmanager.manager.async.TaskManager;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.EnvironmentInstance;
import com.telefonica.euro_iaas.paasmanager.model.Task;
import com.telefonica.euro_iaas.paasmanager.model.Task.TaskStates;
import com.telefonica.euro_iaas.paasmanager.model.TaskError;
import com.telefonica.euro_iaas.paasmanager.model.TaskReference;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import com.telefonica.euro_iaas.paasmanager.util.TaskNotificator;

public class EnvironmentInstanceAsyncManagerImpl implements EnvironmentInstanceAsyncManager {

    private static Logger log = LoggerFactory.getLogger(EnvironmentInstanceAsyncManagerImpl.class.getName());

    private EnvironmentInstanceManager environmentInstanceManager;
    private TaskManager taskManager;
    private SystemPropertiesProvider propertiesProvider;
    private TaskNotificator taskNotificator;
    @Async

    public void create(ClaudiaData claudiaData, EnvironmentInstance environmentInstance, Task task, String callback) {
        log.debug ("Create enviornment instance");
        try {
            environmentInstance = environmentInstanceManager.load(claudiaData.getVdc(), claudiaData.getService());
            updateSuccessTask(task, environmentInstance);
            log.info("The Environment Instance " + environmentInstance.getBlueprintName() + " is ALREADY in the system");
        } catch (TaskNotFoundException tnfe) {
            String errorMsg = "Unable to update task: " + tnfe.getTask().getHref() + ". Description: "
                    + tnfe.getMessage();
            log.error(errorMsg);
            updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, tnfe);
        } catch (EntityNotFoundException e) {
            try {
                environmentInstance = environmentInstanceManager.create(claudiaData, environmentInstance);
                updateSuccessTask(task, environmentInstance);
                log.info("The Environment Instance " + environmentInstance.getName()
                        + " has been CORRECTLY provisioned");
            } catch (EntityNotFoundException enf) {
                String errorMsg = "The Environment " + environmentInstance.getBlueprintName() + " is not in the System";
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, enf);
            } catch (InvalidEntityException iee) {
                String errorMsg = "The Environment " + environmentInstance.getBlueprintName() + " is Invalid";
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, iee);
            } catch (AlreadyExistsEntityException aee) {
                String errorMsg = "The Environment " + environmentInstance.getBlueprintName() + " already exists";
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, aee);
            } catch (InfrastructureException ie) {
                String errorMsg = "Infrastructure error " + ie.getLocalizedMessage();
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, ie);
            } catch (IPNotRetrievedException ipe) {
                String errorMsg = " The ip of a VM could not be retrieved ";
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, ipe);
            } catch (ProductInstallatorException pie) {
                String errorMsg = "Error installing a product. Description:" + pie.getMessage();
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, pie);
            } catch (TaskNotFoundException tne) {
                String errorMsg = "Unable to update task: " + tne.getTask().getHref() + ". Description: "
                        + tne.getMessage();
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, tne);
            } catch (Exception e2) {
                String errorMsg = "Unexpected error creating environment: " + environmentInstance + ". Description:"
                        + e2.getMessage();
                log.error(errorMsg);
                updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, e2);
            } finally {
                notifyTask(callback, task);
            }
        } finally {
            notifyTask(callback, task);
        }
    }

    /*
     * @Override public EnvironmentInstance load(String name) throws EntityNotFoundException { return
     * environmentInstanceManager.load(name); }
     */

    @Async
    public void destroy(ClaudiaData claudiaData, EnvironmentInstance environmentInstance, Task task, String callback) {
        try {
            environmentInstanceManager.destroy(claudiaData, environmentInstance);
            updateSuccessTask(task, environmentInstance);
            log.info("The Environment Instance " + environmentInstance.getName() + " has been CORRECTLY destroyed");
        } catch (InvalidEntityException e) {
            String errorMsg = "InvalidEntity at destroying  environmentInstance: " + environmentInstance.getName()
                    + ". Description:" + e.getMessage();
            log.error(errorMsg);
            updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error destroying  environmentInstance: " + environmentInstance.getName()
                    + ". Description:" + e.getMessage();
            log.error(errorMsg);
            updateErrorTaskOnInstall(environmentInstance, claudiaData.getVdc(), task, errorMsg, e);
        } finally {
            notifyTask(callback, task);
        }
    }

    /*
     * Update the task with necessary information when the task is success.
     */
    private void updateSuccessTask(Task task, EnvironmentInstance environmentInstance) throws TaskNotFoundException {
        Task loadedTask;
        String path = MessageFormat.format(ENVIRONMENT_INSTANCE_PATH, environmentInstance.getVdc(),
                environmentInstance.getName());

        String aiResource = propertiesProvider.getProperty(PAAS_MANAGER_URL) + path;

        // vdc

        try {
            loadedTask = taskManager.load(task.getId());
        } catch (EntityNotFoundException e) {
            throw new TaskNotFoundException(e.getMessage(), task);
        }

        // Uncommented - Could not commit JPA transaction; nested exception is
        // javax.persistence.RollbackException: Transaction marked as
        // rollbackOnly
        // loadedTask.setResult(new TaskReference(piResource));
        loadedTask.setEndTime(new Date());
        loadedTask.setStatus(TaskStates.SUCCESS);
        taskManager.updateTask(loadedTask);
    }

    /*
     * Update the error task in install operation. The installation is a bit different to the other operations because
     * the instance could exist or not so this method shall do this check.
     */
    private void updateErrorTaskOnInstall(EnvironmentInstance environmentInstance, String vdc, Task task,
            String errorMsg, Throwable e) {
        updateErrorTask(environmentInstance, vdc, task, errorMsg, e);
    }

    /*
     * Update the task with necessary information when the task is wrong and the product instance exists in the system.
     */
    private void updateErrorTask(EnvironmentInstance environmentInstance, String vdc, Task task, String message,
            Throwable t) {

        String path = MessageFormat.format(ENVIRONMENT_PATH, environmentInstance.getBlueprintName());

        String aiResource = propertiesProvider.getProperty(PAAS_MANAGER_URL) + path;

        task.setResult(new TaskReference(aiResource));
        updateErrorTask(task, message, t);
    }

    /*
     * Update the task with necessary information when the task is wrong.
     */
    private void updateErrorTask(Task task, String message, Throwable t) {
        TaskError error = new TaskError(message);
        error.setMajorErrorCode(t.getMessage());
        error.setMinorErrorCode(t.getClass().getSimpleName());
        task.setEndTime(new Date());
        task.setStatus(TaskStates.ERROR);
        task.setError(error);
        taskManager.updateTask(task);
        log.error("An error occurs while executing an environment action. See task " + task.getHref()
                + " for more information " + t.getMessage());
    }

    private void notifyTask(String url, Task task) {
        if (!StringUtils.isEmpty(url)) {
            taskNotificator.notify(url, task);
        }
    }

    // ////////// I.O.C ////////////
    /**
     * @param taskManager
     *            the taskManager to set
     */
    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * @param propertiesProvider
     *            the propertiesProvider to set
     */
    public void setPropertiesProvider(SystemPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    /**
     * @param taskNotificator
     *            the taskNotificator to set
     */
    public void setTaskNotificator(TaskNotificator taskNotificator) {
        this.taskNotificator = taskNotificator;
    }

    /**
     * @param environmentInstanceManager
     *            the environmentInstanceManager to set
     */
    public void setEnvironmentInstanceManager(EnvironmentInstanceManager environmentInstanceManager) {
        this.environmentInstanceManager = environmentInstanceManager;
    }
}
