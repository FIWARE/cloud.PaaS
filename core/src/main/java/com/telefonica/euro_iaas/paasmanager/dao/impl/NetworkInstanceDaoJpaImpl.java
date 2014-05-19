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

package com.telefonica.euro_iaas.paasmanager.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.telefonica.euro_iaas.commons.dao.AbstractBaseDao;
import com.telefonica.euro_iaas.commons.dao.EntityNotFoundException;
import com.telefonica.euro_iaas.paasmanager.dao.NetworkInstanceDao;
import com.telefonica.euro_iaas.paasmanager.model.NetworkInstance;
import com.telefonica.euro_iaas.paasmanager.util.OpenStackUtilImpl;

/**
 * @author Henar Munoz
 */
@Transactional(propagation = Propagation.REQUIRED)
public class NetworkInstanceDaoJpaImpl extends AbstractBaseDao<NetworkInstance, String> implements NetworkInstanceDao {

	private static Logger log = Logger.getLogger(NetworkInstanceDaoJpaImpl.class);
    /**
     * k7 find all networks.
     * 
     * @return network list
     */
    public List<NetworkInstance> findAll() {
        return super.findAll(NetworkInstance.class);
    }

    /**
     * It load the netowkr interface from its name.
     * 
     * @param name
     *            of the network instance
     */
    public NetworkInstance load(String name) throws EntityNotFoundException {
       return null;

    }
    
    public NetworkInstance load(String name, String vdc, String region) throws EntityNotFoundException {
        return findByNetworkInstanceName(name, vdc, region);

     }

    /**
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.dao.TierDao#findByTierId(java.lang .String)
     */
    private NetworkInstance findByNetworkInstanceName(String name) throws EntityNotFoundException {
        Query query = getEntityManager().createQuery(
                "select p from NetworkInstance p left join " + "fetch p.subNets where p.name = :name");
        query.setParameter("name", name);
        NetworkInstance networkInstance = null;
        try {
            networkInstance = (NetworkInstance) query.getSingleResult();
        } catch (NoResultException e) {
            String message = " No NetworkInstance found in the database with id: " + name + " Exception: "
                    + e.getMessage();
            log.debug (message);
            throw new EntityNotFoundException(NetworkInstance.class, "name", name);
        }
        return networkInstance;
    }
    
    /**
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.dao.TierDao#findByTierId(java.lang .String)
     */
    private NetworkInstance findByNetworkInstanceName(String name, String vdc, String region) throws EntityNotFoundException {
        Query query = getEntityManager().createQuery(
                "select p from NetworkInstance p left join " + "fetch p.subNets where p.name = :name and p.vdc = :vdc and p.region = :region");
        query.setParameter("name", name);
        query.setParameter("vdc", vdc);
        query.setParameter("region", region);
        NetworkInstance networkInstance = null;
        try {
            networkInstance = (NetworkInstance) query.getSingleResult();
        } catch (NoResultException e) {
            String message = " No NetworkInstance found in the database with id: " + name + " vdc " + vdc + " region " + region + " Exception: "
                    + e.getMessage();
            log.debug (message);
            throw new EntityNotFoundException(NetworkInstance.class, "name", name);
        }
        return networkInstance;
    }

}
