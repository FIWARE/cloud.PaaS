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

package com.telefonica.euro_iaas.paasmanager.model.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.Metadata;
import com.telefonica.euro_iaas.paasmanager.model.ProductRelease;

/**
 * DTO to receive rest request with the product releases objects.
 * 
 * @author Jesus M. Movilla *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductReleaseDto {

    private String productName;
    private String productDescription;
    private String version;
    private Set<Attribute> attributes;
    private Set<Metadata> metatadas;

    /**
     */
    public ProductReleaseDto() {
    }

    /**
     * @param productName
     * @param version
     */
    public ProductReleaseDto(String productName, String productDescription, String version) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.version = version;
    }

    /**
     * Constructor.
     * 
     * @param productName
     *            Name of the product.
     * @param version
     *            Version of the product.
     */
    public ProductReleaseDto(String productName, String version) {
        this.productName = productName;
        this.version = version;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<Attribute> getPrivateAttributes() {
        return attributes;
    }

    public void setPrivateAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Set<Metadata> getMetatadas() {
        return metatadas;
    }

    public void setMetatadas(Set<Metadata> metatadas) {
        this.metatadas = metatadas;
    }

    /**
     * Get a Product Release from the Dto.
     * 
     * @return
     */
    public ProductRelease fromDto() {
        ProductRelease productRelease = new ProductRelease(this.getProductName(), this.getVersion());
        return productRelease;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[[ProductReleaseDto]");
        sb.append("[productName = ").append(this.productName).append("]");
        sb.append("[productDescription = ").append(this.productDescription).append("]");
        sb.append("[version = ").append(this.version).append("]");
        sb.append("[attributes = ").append(this.attributes).append("]");
        sb.append("[metadatas = ").append(this.metatadas).append("]");
        sb.append("]");
        return sb.toString();
    }

}
