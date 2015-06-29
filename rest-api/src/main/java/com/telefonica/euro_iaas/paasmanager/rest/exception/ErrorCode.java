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

package com.telefonica.euro_iaas.paasmanager.rest.exception;

import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentInstanceException;
import com.telefonica.euro_iaas.paasmanager.exception.InvalidSecurityGroupRequestException;

/**
 * Class that contain the information related to the error code.
 */
public enum ErrorCode {

    DB_CONNECTION(20, "Could not open connection to database", "(.*)JDBCConnectionException(.*)", 500),
    HIBERNATE(10, "Problem in database backend", "(.*)org.hibernate(.*)", 500),
    ENTITY_NOT_FOUND(30, "Entity not found", "(.*)EntityNotFoundException(.*)", 404),
    ALREADY_EXIST(31, "Entity already exist", "(.*)AlreadyExistEntityException(.*)", 409),
    ALREADY_EXIST2(32, "Invalid environment", "(.*)already exists(.*)", 409),
    TIERINSTANCE_ALREADY_EXIST(33,"TierInstance Already exists", "(.*)InvalidEntityException(.*)TierInstance Already exists(.*)", 409),
    ENVIRONMENT_IN_USE(40,
            "The environment is being used by an instance",
            "(.*)InvalidEntityException: (.*)is being used(.*)",
            403),
    INVALID_NUMBER_INITIAL_VMS_IN_TIER(70, "Invalid Tier. Initial, minimum or maximun number of instances are not correct", 
            		"(.*)InvalidEntityException(.*)Error in the initial number of instances(.*)", 400),
    NAME_NO_VALID(41, "The name is not valid", "(.*)InvalidEntityException:(.*)", 400),
    OPENSTACK_ERROR_CREATINGSERVER (50, "Openstack error creating a server/assigning floating ip", "(.*)InfrastructureException(.*)Error interacting with OpenStack(.*)", 500),
    OPENSTACK_ERROR_FEDERATING_NETWORKS (51, "Error federating the networks", "(.*)InfrastructureException(.*)Error federating the networks(.*)", 500),
    OPENSTACK_ERROR_CREATING_SECGROUPS (52, "It is not possible to create the security group", "(.*)InvalidSecurityGroupRequestException(.*)", 500),
    INFRASTRUCTURE(52, "OpenStack infrastructure failure", "(.*)InfrastructureException(.*)", 500),
    INVALID_ENVIRONMENT(60, "Invalid Environment Instance", "(.*)InvalidEnvironmentInstanceException(.*)", 400),
    INVALID_INITIAL_NUMBER_REPLICAS(70, "The number of replicas is not valid",
    		"(.*)InvalidEntityException:(.*)The number of replicas is not valid(.*)",
    		400),
 	PRODUCTINSTANCE_ALREADY_EXIST (90, "ProductInstance Already Exist", "(.*)ProductInstallatorException (.*)already exists(.*)", 400),
 	ERROR_INSTALLING_A_PRODUCT (91, "Error installing a product ", "(.*)ProductInstallatorException (.*)Error installing product(.*)", 412),
    DEFAULT(500, "Internal PaasManager Server error", "(?s).*", 500);

    private final int code;
    private final String publicMessage;
    private final String pattern;
    private final int httpCode;

    private ErrorCode(Integer code, String publicMessage, String pattern, Integer httpCode) {
        this.code = code;
        this.publicMessage = publicMessage;
        this.pattern = pattern;
        this.httpCode = httpCode;
    }

    public String getPublicMessage() {
        return publicMessage;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + "(" + httpCode + "): " + publicMessage;
    }

    /**
     *
     * @param value
     * @return
     */
    public static ErrorCode find(String value) {
        if (value == null) {
            return ErrorCode.DEFAULT;
        }

        ErrorCode[] errors = ErrorCode.values();
        int i = 0;
        while (i < errors.length && !value.matches(errors[i].getPattern())) {
            i++;
        }
        return errors[i];
    }

    /**
     * Getter (pattern).
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Getter (httpCode).
     * @return
     */
    public Integer getHttpCode() {
        return this.httpCode;
    }
}
