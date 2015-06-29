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

package com.telefonica.euro_iaas.paasmanager.manager.impl;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.euro_iaas.paasmanager.dao.AttributeDao;
import com.telefonica.euro_iaas.paasmanager.dao.MetadataDao;
import com.telefonica.euro_iaas.paasmanager.dao.ProductReleaseDao;
import com.telefonica.euro_iaas.paasmanager.dao.sdc.ProductReleaseSdcDao;
import com.telefonica.euro_iaas.paasmanager.exception.SdcException;
import com.telefonica.euro_iaas.paasmanager.manager.ProductReleaseManager;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.ClaudiaData;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;
import com.telefonica.fiware.commons.dao.AlreadyExistsEntityException;
import com.telefonica.fiware.commons.dao.EntityNotFoundException;
import com.telefonica.fiware.commons.dao.InvalidEntityException;

/**
 * @author jesus.movilla
 */
public class ProductReleaseManagerImpl implements ProductReleaseManager {

    private ProductReleaseDao productReleaseDao;
    private ProductReleaseSdcDao productReleaseSdcDao;
    private MetadataDao metadataDao;
    private AttributeDao attributeDao;
    private static Logger log = LoggerFactory.getLogger(ProductReleaseManagerImpl.class);

    /*
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.manager.ProductManager#load(java .lang.String)
     */
    public ProductRelease load(String name, ClaudiaData data) throws EntityNotFoundException {
        log.info("Loading product release " + name);
        ProductRelease productRelease = null;
        String product, version;
        try {
            product = name.split("-")[0];
            version = name.split("-")[1];
        } catch (Exception ex) {
            String msg = "Entity with invalid name(" + name + "): use <productName>-<version> ";
            log.warn(msg);
            throw new EntityNotFoundException(ProductRelease.class, msg, name);

        }
        try {
            log.info("Loading from sdc " + product + " " + version);

            productRelease = productReleaseDao.load(name);

        } catch (EntityNotFoundException e) {
            // Go to SDC and search. After this, introduce product in database
            log.info("The product " + name + " is not in database");
            try {
                ProductRelease pRelease = productReleaseSdcDao.load(product, version, data);
                productRelease = create(pRelease);

            } catch (EntityNotFoundException e5) {
                String msg = "No product release NOT Found in SDC neither in PaasManager: " + name;
                log.warn(msg);
                throw new EntityNotFoundException(ProductRelease.class, msg, name);
            } catch (SdcException e6) {
                String msg = "SDC failure at loading ProductRelease " + name + " " + e6.getMessage();
                log.warn(msg);
                throw new EntityNotFoundException(ProductRelease.class, msg, name);
            } catch (InvalidEntityException e7) {
                String msg = "SDC failure at loading ProductRelease " + name + " " + e7.getMessage();
                log.warn(msg);
                throw new EntityNotFoundException(ProductRelease.class, msg, name);
            } catch (AlreadyExistsEntityException e8) {
                // if already exist, we should update metadatas or attributes
                String msg = "SDC failure at loading ProductRelease " + name + " " + e8.getMessage();
                log.warn(msg);
                throw new EntityNotFoundException(ProductRelease.class, msg, name);
            }
        }

        return productRelease;
    }

    public ProductRelease loadFromSDCAndCreate(String name, ClaudiaData data) throws EntityNotFoundException {
        log.info("Sync product release " + name);
        ProductRelease productRelease;
        String product = name.split("-")[0];
        String version = name.split("-")[1];

        try {
            log.info("Loading from sdc " + product + " " + version);
            ProductRelease pRelease = productReleaseSdcDao.load(product, version, data);
            try {

                productRelease = productReleaseDao.load(name);

                HashSet<Attribute> attributesCopy = new HashSet<Attribute>(productRelease.getAttributes());
                HashSet<Metadata> metatadasCopy = new HashSet<Metadata>(productRelease.getMetadatas());

                productReleaseDao.removeAttributesAndMetadatas(productRelease);
                // remove
                for (Attribute attribute : attributesCopy) {
                    attributeDao.remove(attribute);
                }

                for (Metadata metadata : metatadasCopy) {
                    metadataDao.remove(metadata);
                }

                // add new attributes and metadatas
                for (Attribute attribute : pRelease.getAttributes()) {
                    Attribute newAttribute = attributeDao.create(attribute);
                    productRelease.addAttribute(newAttribute);
                }

                for (Metadata metadata : pRelease.getMetadatas()) {
                    Metadata newMetadata = metadataDao.create(metadata);
                    productRelease.addMetadata(newMetadata);
                }
                productReleaseDao.update(productRelease);
            } catch (Exception ex) {
                log.info("Product don't exist in database: creates");
                productRelease = create(pRelease);
            }
        } catch (EntityNotFoundException e5) {
            String msg = "Product release NOT found in SDC: " + name;
            log.warn(msg);
            throw new EntityNotFoundException(ProductRelease.class, msg, name);
        } catch (SdcException e6) {
            String msg = "Failure at loading ProductRelease from SDC" + name + " " + e6.getMessage();
            log.warn(msg);
            throw new EntityNotFoundException(ProductRelease.class, msg, name);
        } catch (InvalidEntityException e7) {
            String msg = "Failure creating new ProductRelease " + name + " " + e7.getMessage();
            log.warn(msg);
            throw new EntityNotFoundException(ProductRelease.class, msg, name);
        } catch (AlreadyExistsEntityException e8) {
            String msg = "Failure creating ProductRelease " + name + " " + e8.getMessage();
            log.warn(msg);
            throw new EntityNotFoundException(ProductRelease.class, msg, name);
        }

        return productRelease;
    }

    public ProductRelease create(ProductRelease productRelease) throws InvalidEntityException,
            AlreadyExistsEntityException {
        log.info("Create product release " + productRelease.getName() + " " + productRelease.getVersion());
        productRelease = productReleaseDao.create(productRelease);
        return productRelease;
    }

    /*
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.manager.ProductManager#findAll()
     */
    public List<ProductRelease> findAll() {
        // TODO Auto-generated method stub
        return productReleaseDao.findAll();
    }

    /*
     * (non-Javadoc)
     * @see com.telefonica.euro_iaas.paasmanager.manager.ProductManager#load(com.
     * telefonica.euro_iaas.sdc.model.Product, java.lang.String)
     */
    public ProductRelease load(String productName, String productVersion) throws EntityNotFoundException {
        log.info("Loading " + productName + "-" + productVersion);

        return productReleaseDao.load(productName + "-" + productVersion);
    }

    public ProductRelease loadWithMetadata(String name) throws EntityNotFoundException {
        log.info("Loading " + name);

        return productReleaseDao.loadProductReleaseWithMetadata(name);
    }

    public ProductRelease update(ProductRelease productRelease) throws InvalidEntityException {
        log.info("Updating " + productRelease.getName());

        return productReleaseDao.update(productRelease);
    }

    /**
     * @param productReleaseDao
     *            the productReleaseDao to set
     */
    public void setProductReleaseDao(ProductReleaseDao productReleaseDao) {
        this.productReleaseDao = productReleaseDao;
    }

    /**
     * @param productReleaseSdcDao
     *            the productReleaseSdcDao to set
     */
    public void setProductReleaseSdcDao(ProductReleaseSdcDao productReleaseSdcDao) {
        this.productReleaseSdcDao = productReleaseSdcDao;
    }

    /**
     * @param metadataDao
     *            the metadataDao to set
     */
    public void setMetadataDao(MetadataDao metadataDao) {
        this.metadataDao = metadataDao;
    }

    /**
     * @param attributeDao
     *            the attributeDao to set
     */
    public void setAttributeDao(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

}
