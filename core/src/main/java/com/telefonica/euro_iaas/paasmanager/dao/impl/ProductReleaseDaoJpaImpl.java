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

import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.telefonica.euro_iaas.paasmanager.dao.ProductReleaseDao;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.euro_iaas.paasmanager.model.Tier;
import com.telefonica.fiware.commons.dao.AbstractBaseDao;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;

@Transactional(propagation = Propagation.REQUIRED)
public class ProductReleaseDaoJpaImpl extends AbstractBaseDao<ProductRelease, String> implements ProductReleaseDao {

    public List<ProductRelease> findAll() {
        return super.findAll(ProductRelease.class);
    }

    public ProductRelease load(String name) throws EntityNotFoundException {
        return findByProductReleaseWithMetadataAndAtt(name);
    }

    public ProductRelease load(String product, String version, String tierName) throws EntityNotFoundException {
        return this.findByNameAndVdcAndTierWithMetadataAndAtt(product + "-" + version, tierName);

    }

    public ProductRelease loadProductReleaseWithMetadata(String name) throws EntityNotFoundException {

        Query query = getEntityManager().createQuery(
                "select p from ProductRelease p left join " + " fetch p.metadatas where p.name = :name");
        query.setParameter("name", name);
        ProductRelease productRelease = null;
        try {
            productRelease = (ProductRelease) query.getResultList().get(0);
        } catch (Exception e) {
            String message = " No ProductRelease found in the database with id: " + name + " Exception: "
                    + e.getMessage();
            throw new EntityNotFoundException(ProductRelease.class, "name", name);
        }
        return productRelease;
    }

    /**
     * Remove all attributes and metadata from product and return the updated productRelease
     * 
     * @param productRelease
     * @return
     */
    @Override
    public ProductRelease removeAttributesAndMetadatas(ProductRelease productRelease) {

        productRelease.setAttributes(null);
        productRelease.setMetadatas(null);

        return update(productRelease);
    }

    private ProductRelease findByProductReleaseWithMetadataAndAtt(String name) throws EntityNotFoundException {

        Query query = getEntityManager().createQuery(
                "select p from ProductRelease p left join"
                        + " fetch p.attributes as attributes left join fetch p.metadatas as metadatas "
                        + "where p.name = :name");
        query.setParameter("name", name);
        ProductRelease productRelease = null;
        try {
            productRelease = (ProductRelease) query.getResultList().get(0);
        } catch (Exception e) {
            String message = " No ProductRelease found in the database with id: " + name + " Exception: "
                    + e.getMessage();
            throw new EntityNotFoundException(ProductRelease.class, "name", name);
        }
        return productRelease;
    }

    private ProductRelease findByNameAndVdcAndTierWithMetadataAndAtt(String name, String tierName)
            throws EntityNotFoundException {
        Query query = getEntityManager()
                .createQuery(
                        "select p from ProductRelease p left join "
                                + "fetch p.attributes as attributes left join fetch p.metadatas as metadatas where p.name = :name "
                                + "and p.tierName=:tierName");
        query.setParameter("name", name);
        query.setParameter("tierName", tierName);
        ProductRelease productRelease = null;
        try {
            productRelease = (ProductRelease) query.getResultList().get(0);
        } catch (Exception e) {
            String message = " No ProductRelease found in the database with name: " + name + " and tierName "
                    + tierName;
            throw new EntityNotFoundException(Tier.class, e.getMessage(), message);
        }
        return productRelease;
    }

}
