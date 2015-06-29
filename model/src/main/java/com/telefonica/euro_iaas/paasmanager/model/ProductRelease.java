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

package com.telefonica.euro_iaas.paasmanager.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.telefonica.euro_iaas.paasmanager.model.dto.ProductReleaseDto;

/**
 * A product release is a concrete version of a given product.
 * 
 * @author Jesus M. Movilla
 */

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "ProductRelease")
public class ProductRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    @XmlTransient
    private Long id;

    /*
     * @Id
     * @GeneratedValue(strategy = GenerationType.AUTO)
     * @XmlTransient private Long id;
     */

    @Column(nullable = false, length = 256)
    private String name;
    @Column(nullable = false, length = 256)
    private String product;
    @Column(nullable = false, length = 256)
    private String version;

    @Column(length = 2048)
    private String description;
    @Column(length = 256)
    private String tierName = "";

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Attribute> attributes = null;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Metadata> metadatas;

    // @OneToMany(cascade = CascadeType.ALL)
    // private List<Attribute> attributesPort;

    @ManyToMany
    @JoinTable(name = "productRelease_has_ooss")
    private List<OS> supportedOOSS = null;

    @Column(nullable = true)
    private Boolean withArtifact = false;

    /**
     * Constructor.
     */
    public ProductRelease() {
        attributes = new HashSet<Attribute>();
        metadatas = new HashSet<Metadata>();

    }

    /**
     * Constructor.
     * 
     * @param product
     * @param version
     */
    public ProductRelease(String product, String version) {
        this.name = product + "-" + version;
        this.product = product;
        this.version = version;
        attributes = new HashSet<Attribute>();
        metadatas = new HashSet<Metadata>();
    }

    /**
     * @param name
     * @param version
     * @param description
     * @param attributes
     */
    public ProductRelease(String name, String version, String description, Set<Attribute> attributes) {
        this.product = name;
        this.version = version;
        this.name = product + "-" + version;
        this.description = description;
        this.attributes = attributes;
        metadatas = new HashSet<Metadata>();
    }

    /**
     * Add an attribute to the product release.
     * 
     * @param attribute
     */
    public void addAttribute(Attribute attribute) {
        if (attributes == null) {
            attributes = new HashSet<Attribute>();
        }
        attributes.add(attribute);
    }

    /**
     * Add a metadata.
     * 
     * @param metadata
     */
    public void addMetadata(Metadata metadata) {
        if (metadatas == null) {
            metadatas = new HashSet<Metadata>();
        }
        metadatas.add(metadata);
    }

    /**
     * Remove a metadata .
     * 
     * @param metadata
     */
    public void deleteMetadata(Metadata metadata) {
        if (metadatas.contains(metadata)) {
            metadatas.remove(metadata);
        }
    }

    /**
     * Remove a attribute .
     * 
     * @param attribute
     */
    public void deleteAttribute(Attribute attribute) {
        if (attributes.contains(attribute)) {
            attributes.remove(attribute);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductRelease other = (ProductRelease) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    private JSONObject formatJsonArray(JSONObject productJson, String tag) {
        String stringProductJson = productJson.toString();
        if (stringProductJson.contains("\"" + tag + "\":{")) {
            String openArray = "\"" + tag + "\":[{";
            stringProductJson = stringProductJson.replace("\"" + tag + "\":{", openArray);
            String oldString = stringProductJson.split(tag)[1];
            String newString = oldString.replaceFirst("\"}", "\"}]");
            stringProductJson = stringProductJson.replace(oldString, newString);

        }
        return JSONObject.fromObject(stringProductJson);
    }

    /**
     * the json.
     * 
     * @param jsonNode
     */
    public void fromSdcJson(JSONObject jsonNode) {
        version = jsonNode.getString("version");
        product = jsonNode.getJSONObject("product").getString("name");
        name = product + "-" + version;
        if (jsonNode.containsKey("releaseNotes")) {
            description = jsonNode.getString("releaseNotes");
        }

        JSONObject productJson = jsonNode.getJSONObject("product");
        // Attributes
        if (productJson.containsKey("attributes")) {
            Set<Attribute> attributes = new HashSet<Attribute>();
            productJson = formatJsonArray(productJson, "attributes");
            JSONArray attributtesJsonArray = productJson.getJSONArray("attributes");
            for (int i = 0; i < attributtesJsonArray.size(); i++) {
                JSONObject object = attributtesJsonArray.getJSONObject(i);
                Attribute attribute = new Attribute();
                attribute.fromJson(object);
                attributes.add(attribute);
            }
            setAttributes(attributes);
        }

        // Attributes
        if (productJson.containsKey("metadatas")) {
            Set<Metadata> metadatas = new HashSet<Metadata>();
            productJson = formatJsonArray(productJson, "metadatas");
            JSONArray metadatasJsonArray = productJson.getJSONArray("metadatas");
            for (int i = 0; i < metadatasJsonArray.size(); i++) {
                JSONObject object = metadatasJsonArray.getJSONObject(i);
                Metadata metadata = new Metadata();
                metadata.fromJson(object);
                metadatas.add(metadata);
            }
            setMetadatas(metadatas);
        }

        // SSOO
        if (jsonNode.containsKey("supportedOOSS")) {
            JSONArray ssooJsonArray = jsonNode.getJSONArray("supportedOOSS");
            List<OS> ooss = new ArrayList<OS>();
            for (int i = 0; i < ssooJsonArray.size(); i++) {
                JSONObject object = ssooJsonArray.getJSONObject(i);
                OS os = new OS();
                os.fromJson(object);
                ooss.add(os);
            }
            setSupportedOOSS(ooss);
        }
    }

    /**
     * Get the attribute with the key.
     * 
     * @param key
     * @return
     */
    public Attribute getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        for (Attribute attribute : attributes) {
            if (attribute.getKey().equals(key)) {
                return attribute;
            }
        }
        return null;
    }

    /**
     * @return the attributes
     */
    public Set<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Get the metadata from the key.
     * 
     * @param key
     * @return
     */
    public Metadata getMetadata(String key) {
        if (metadatas == null) {
            return null;
        }
        for (Metadata metadata : metadatas) {
            if (metadata.getKey().equals(key)) {
                return metadata;
            }
        }
        return null;
    }

    /**
     * @return the metadatas
     */
    public Set<Metadata> getMetadatas() {
        if (metadatas == null) {
            metadatas = new HashSet<Metadata>();
        }
        return metadatas;
    }

    /**
     * @return the id
     */
    public String getName() {
        return name;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @return the supportedOOSS
     */
    public List<OS> getSupportedOOSS() {
        return supportedOOSS;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the withArtifact
     */
    public Boolean getWithArtifact() {
        return withArtifact;
    }

    /**
     * @return the tierName
     */

    public String getTierName() {
        return this.tierName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param metadatas
     *            the metadatas to set
     */
    public void setMetadatas(Set<Metadata> metadatas) {
        this.metadatas = metadatas;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @param supportedOOSS
     *            the supportedOOSS to set
     */
    public void setSupportedOOSS(List<OS> supportedOOSS) {
        this.supportedOOSS = supportedOOSS;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @param withArtifact
     *            the withArtifact to set
     */
    public void setWithArtifact(Boolean withArtifact) {
        this.withArtifact = withArtifact;
    }

    /**
     * @param tierName
     *            the tierName to set
     */
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    /**
     * the dto representation.
     * 
     * @return
     */
    public ProductReleaseDto toDto() {
        ProductReleaseDto pReleaseDto = new ProductReleaseDto();

        pReleaseDto.setProductName(getProduct());
        pReleaseDto.setVersion(getVersion());

        if (getDescription() != null) {
            pReleaseDto.setProductDescription(getDescription());
        }

        if (getAttributes() != null) {
            pReleaseDto.setPrivateAttributes(getAttributes());
        }

        return pReleaseDto;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[[ProductRelease]");
        sb.append("[id = ").append(this.id).append("]");
        sb.append("[name = ").append(this.name).append("]");
        sb.append("[product = ").append(this.product).append("]");
        sb.append("[version = ").append(this.version).append("]");
        sb.append("[description = ").append(this.description).append("]");
        sb.append("[tierName = ").append(this.tierName).append("]");
        sb.append("[attributes = ").append(this.attributes).append("]");
        sb.append("[metadatas = ").append(this.metadatas).append("]");
        sb.append("[supportedOOSS = ").append(this.supportedOOSS).append("]");
        sb.append("[withArtifact = ").append(this.withArtifact).append("]");
        sb.append("]");
        return sb.toString();
    }

}
