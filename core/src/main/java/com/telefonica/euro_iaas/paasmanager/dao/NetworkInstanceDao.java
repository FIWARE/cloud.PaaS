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

package com.telefonica.euro_iaas.paasmanager.dao;

import com.telefonica.fiware.commons.dao.BaseDAO;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.model.TierInstance;

import java.util.List;


/**
 * Defines the methods needed to persist Network interface objects.
 * 
 * @author Henar Munoz
 */
public interface NetworkInstanceDao extends BaseDAO<NetworkInstance, String> {

    /**
     * It obtains the networkInstance from DB.
     * @param name
     * @param vdc
     * @param region
     * @return
     * @throws EntityNotFoundException
     */
    NetworkInstance load(String name, String vdc, String region) throws EntityNotFoundException ;

    /**
     * It obtains the tierinstances which have network instance associated.
     * @param name
     * @param vdc
     * @param region
     * @return
     * @throws EntityNotFoundException
     */
    public List<TierInstance> findTierInstanceUsedByNetwork(String name, String vdc, String region)
        throws EntityNotFoundException;

}
