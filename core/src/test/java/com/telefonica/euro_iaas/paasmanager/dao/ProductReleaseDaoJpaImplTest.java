/**
 * (c) Copyright 2013 Telefonica, I+D. Printed in Spain (Europe). All Rights Reserved.<br>
 * The copyright to the software program(s) is property of Telefonica I+D. The program(s) may be used and or copied only
 * with the express written consent of Telefonica I+D or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */
package com.telefonica.euro_iaas.paasmanager.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.telefonica.euro_iaas.paasmanager.dao.ProductReleaseDao;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;

/**
 * @author jesus.movilla
 *
 */
public class ProductReleaseDaoJpaImplTest extends AbstractJpaDaoTest {

    private ProductReleaseDao productReleaseDao;

    @Test
    public void testProductReleasesNotAttributes() throws Exception {

        ProductRelease productTomcat = new ProductRelease("mysql", "2", "tomcat 7", null);

        productTomcat = productReleaseDao.create(productTomcat);
        assertNotNull(productTomcat);
        assertEquals(productTomcat.getProduct(), "mysql");
        assertEquals(productTomcat.getVersion(), "2");

        List<ProductRelease> productReleases = productReleaseDao.findAll();
        assertNotNull(productReleases);

        ProductRelease productRelease = productReleaseDao.load("mysql-2");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "mysql");
        assertEquals(productRelease.getVersion(), "2");

    }

    @Test
    public void testProductReleasesWithAttributes() throws Exception {

        List<ProductRelease> productReleases = productReleaseDao.findAll();
        assertNotNull(productReleases);

        int number = productReleases.size();

        List<Attribute> attproduct = new ArrayList<Attribute>();
        attproduct.add(new Attribute("product", "product", "product"));

        ProductRelease productproduct = new ProductRelease("product", "0.1", "product 0.1", attproduct);

        productproduct = productReleaseDao.create(productproduct);
        assertNotNull(productproduct);
        assertEquals(productproduct.getProduct(), "product");
        assertEquals(productproduct.getVersion(), "0.1");

        productReleases = productReleaseDao.findAll();
        assertNotNull(productReleases);
        assertEquals(productReleases.size(), number + 1);

        ProductRelease productRelease = productReleaseDao.load("product-0.1");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "product");
        assertEquals(productRelease.getVersion(), "0.1");
        assertEquals(productRelease.getAttributes().size(), 1);

    }
    
    
    
    @Test
    public void testProductReleasesWithMetadata() throws Exception {

        Metadata metproduct = new Metadata("product", "product", "product");     

        ProductRelease productproduct = new ProductRelease("product2", "0.1");
        productproduct.addMetadata(metproduct);

        productproduct = productReleaseDao.create(productproduct);
        assertNotNull(productproduct);
        assertEquals(productproduct.getProduct(), "product2");
        assertEquals(productproduct.getVersion(), "0.1");
        assertEquals(productproduct.getMetadatas().size(), 1);

        ProductRelease productRelease = productReleaseDao.load("product2-0.1");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "product2");
        assertEquals(productRelease.getVersion(), "0.1");
        assertEquals(productRelease.getMetadatas().size(), 1);

    }
    
    @Test
    public void testProductReleasesWithMetadataAndAttributes() throws Exception {

        Metadata metproduct = new Metadata("product", "product", "product");
        Attribute attribute = new Attribute("product", "product", "product");
        
        ProductRelease productproduct = new ProductRelease("product2", "0.1");
        productproduct.addMetadata(metproduct);
        productproduct.addAttribute(attribute);

        productproduct = productReleaseDao.create(productproduct);
        assertNotNull(productproduct);
        assertEquals(productproduct.getProduct(), "product2");
        assertEquals(productproduct.getVersion(), "0.1");
        assertEquals(productproduct.getMetadatas().size(), 1);
        assertEquals(productproduct.getAttributes().size(), 1);
        
        ProductRelease productRelease = productReleaseDao.load("product2-0.1");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "product2");
        assertEquals(productRelease.getVersion(), "0.1");
        assertEquals(productRelease.getMetadatas().size(), 1);
        assertEquals(productRelease.getAttributes().size(), 1);

    }
    @Test
    public void testProductReleasesWithAttributes2() throws Exception {

        Attribute att = new Attribute("product", "product", "product");

        ProductRelease productproduct = new ProductRelease("product3", "0.3");
        productproduct.addAttribute(att);

        productproduct = productReleaseDao.create(productproduct);
        assertNotNull(productproduct);
        assertEquals(productproduct.getProduct(), "product3");
        assertEquals(productproduct.getVersion(), "0.3");
        assertEquals(productproduct.getAttributes().size(), 1);
        assertEquals(productproduct.getMetadatas().size(), 0);


        ProductRelease productRelease = productReleaseDao.load("product3-0.3");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "product3");
        assertEquals(productRelease.getVersion(), "0.3");
        assertEquals(productRelease.getAttributes().size(), 1);

    }
    
    @Test
    public void testProductReleasesWithEmptyAttributes() throws Exception {

        ProductRelease productproduct = new ProductRelease("product4", "0.1");

        productproduct = productReleaseDao.create(productproduct);
        assertNotNull(productproduct);
        assertEquals(productproduct.getProduct(), "product4");
        assertEquals(productproduct.getVersion(), "0.1");
        assertEquals(productproduct.getMetadatas().size(), 0);
        assertEquals(productproduct.getAttributes().size(), 0);

        ProductRelease productRelease = productReleaseDao.load("product4-0.1");
        assertNotNull(productRelease);
        assertEquals(productRelease.getProduct(), "product4");
        assertEquals(productRelease.getVersion(), "0.1");
        assertEquals(productRelease.getAttributes().size(), 0);

    }
    
    /**
     * @param productReleaseDao
     *            the productReleaseDao to set
     */
    public void setProductReleaseDao(ProductReleaseDao productReleaseDao) {
        this.productReleaseDao = productReleaseDao;
    }
    
}