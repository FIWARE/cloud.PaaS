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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.Hibernate;

import com.telefonica.euro_iaas.paasmanager.model.dto.ProductReleaseDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.TierDto;

/**
 * Represents an artifact to be installed on a ProductRelease.
 * 
 * @author Jesus M. Movilla
 * @version $Id: $
 */
@Entity
public class Tier {

    public static final String NAME_FIELD = "name";
    public static final String VDC_FIELD = "vdc";
    public static final String ENVIRONMENT_NAME_FIELD = "environmentname";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 256)
    private String name = "";
    private String vdc = "";
    private String environmentname = "";

    private String flavour = "";
    private String image = "";
    private String icono = "";

    private String keypair = "";
    private String floatingip = "false";

    private String affinity = "None";

    private Integer maximumNumberInstances = new Integer(0);
    private Integer minimumNumberInstances = new Integer(0);
    private Integer initialNumberInstances = new Integer(0);

    @Column(length = 90000)
    private String payload;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tier_has_productReleases", joinColumns =
            { @JoinColumn(name = "tier_ID", nullable = false, updatable = false) },
              inverseJoinColumns = { @JoinColumn(name = "productRelease_ID", nullable = false, updatable = false) })
    private List<ProductRelease> productReleases;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tier_has_networks", joinColumns =
            { @JoinColumn(name = "tier_ID", nullable = false, updatable = false) },
              inverseJoinColumns = { @JoinColumn(name = "network_ID", nullable = false, updatable = false) })
    private Set<Network> networks;

    private String region = "";

    /**
     * Default Constructor.
     */
    public Tier() {
        networks = new HashSet<Network>();
    }

    /**
     * @arg name
     */
    public Tier(String name) {
        this.name = name;
        networks = new HashSet<Network>();
    }

    /**
     * Constructor.
     * 
     * @param name
     *            Name of the Tier.
     * @param maximumNumberInstances
     *            Maximum number of instances on that tier.
     * @param minimumNumberInstances
     *            Minimum number of instances on that tier.
     * @param initialNumberInstances
     *            Initial number of instances.
     * @param productReleases
     *            List of product to be released on that tier.
     */
    public Tier(String name, Integer maximumNumberInstances, Integer minimumNumberInstances,
            Integer initialNumberInstances, List<ProductRelease> productReleases) {
        this.name = name;
        this.maximumNumberInstances = maximumNumberInstances;
        this.minimumNumberInstances = minimumNumberInstances;
        this.initialNumberInstances = initialNumberInstances;
        this.productReleases = productReleases;
        networks = new HashSet<Network>();
    }

    /**
     * @param name
     * @param maximumNumberInstances
     * @param minimumNumberInstances
     * @param initialNumberInstances
     * @param productReleases
     * @param flavour
     * @param image
     * @param icono
     */

    public Tier(String name, Integer maximumNumberInstances, Integer minimumNumberInstances,
            Integer initialNumberInstances, List<ProductRelease> productReleases, String flavour, String image,
            String icono) {
        this.name = name;
        this.maximumNumberInstances = maximumNumberInstances;
        this.minimumNumberInstances = minimumNumberInstances;
        this.initialNumberInstances = initialNumberInstances;
        this.productReleases = productReleases;
        this.flavour = flavour;
        this.image = image;
        this.icono = icono;
        networks = new HashSet<Network>();
    }

    /**
     * Constructor.
     * 
     * @param name
     * @param maximumNumberInstances
     * @param minimumNumberInstances
     * @param initialNumberInstances
     * @param productReleases
     * @param flavour
     * @param image
     * @param icono
     * @param keypair
     * @param floatingip
     * @param payload
     */
    public Tier(String name, Integer maximumNumberInstances, Integer minimumNumberInstances,
            Integer initialNumberInstances, List<ProductRelease> productReleases, String flavour, String image,
            String icono, String keypair, String floatingip, String payload) {
        this.name = name;
        this.maximumNumberInstances = maximumNumberInstances;
        this.minimumNumberInstances = minimumNumberInstances;
        this.initialNumberInstances = initialNumberInstances;
        this.productReleases = productReleases;

        this.flavour = flavour;
        this.image = image;
        this.icono = icono;
        // this.securityGroupName = securityGroupName;
        this.keypair = keypair;
        this.floatingip = floatingip;
        this.payload = payload;
        networks = new HashSet<Network>();
    }

    /**
     * @param network
     *            the network list
     */
    public void addNetwork(Network network) {
        if (this.networks == null) {
            this.networks = new HashSet<Network>();
        }

        int count = containsNetwork(network);
        if (count != -1) {
            networks.remove(count);
        }
        networks.add(network);

    }

    /**
     * @param network
     *            the network list
     */
    public void deleteNetwork(Network network) {
        if (networks.contains(network)) {
            networks.remove(network);
        }

    }

    /**
     * Add the product release for the tier.
     * 
     * @param productRelease
     */
    public void addProductRelease(ProductRelease productRelease) {
        if (this.productReleases == null) {
            productReleases = new ArrayList<ProductRelease>();
        }
        productReleases.add(productRelease);
    }

    /**
     * It returns the position in the array list of the network.
     * 
     * @param net2
     * @return
     */
    public int containsNetwork(Network net2) {
        int i = 0;
        for (Network net : networks) {
            if (net2.getNetworkName().equals(net.getNetworkName())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        Tier other = (Tier) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }

        if (!image.equals(other.image)) {
            return false;
        }
        return true;
    }

    public String getFlavour() {
        return flavour;
    }

    public String getFloatingip() {
        return this.floatingip;
    }

    public String getIcono() {
        return this.icono;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    public String getImage() {
        return this.image;
    }

    /**
     * @return the initial_number_instances
     */
    public Integer getInitialNumberInstances() {
        return initialNumberInstances;
    }

    public String getKeypair() {
        return this.keypair;
    }

    /**
     * @return the maximum_number_instances
     */
    public Integer getMaximumNumberInstances() {
        return maximumNumberInstances;
    }

    /**
     * @return the minimum_number_instances
     */
    public Integer getMinimumNumberInstances() {
        return minimumNumberInstances;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return networks the network list
     */
    public Set<Network> getNetworks() {
        return this.networks;
    }

    public String getPayload() {
        return this.payload;
    }

    /**
     * @return the product name
     */
    public String getProductNameBalanced() {
        if (getProductReleases() != null) {
            for (ProductRelease productRelease : getProductReleases()) {
                Attribute attBalancer = productRelease.getAttribute("balancer");
                return attBalancer.getValue();
            }
        }
        return null;
    }

    /**
     * @return the productReleases
     */
    public List<ProductRelease> getProductReleases() {
        if (productReleases == null) {
            this.productReleases = new ArrayList<ProductRelease>();
        }
        return productReleases;
    }

    /*
     * public SecurityGroup getSecurityGroup() { return this.securityGroup; }
     */

    public String getVdc() {
        return vdc;
    } /*
       * (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @param network
     *            the network to remove
     */
    public void removeNetwork(Network network) {
        if (networks.contains(network)) {
            networks.remove(network);
        }
    }

    /**
     * to remove the product release.
     * 
     * @param productRelease
     */
    public void removeProductRelease(ProductRelease productRelease) {
        if (productReleases.contains(productRelease)) {
            productReleases.remove(productRelease);
        }
    }

    public void setEnviromentName(String environmentname) {
        this.environmentname = environmentname;
    }

    public String getEnviromentName() {
        return this.environmentname;
    }

    /**
     * @param flavour
     *            the flavour to set
     */
    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    public void setFloatingip(String floatingip) {
        this.floatingip = floatingip;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @param initialNumberInstances
     *            the initialNumberInstances to set
     */
    public void setInitialNumberInstances(Integer initialNumberInstances) {
        this.initialNumberInstances = initialNumberInstances;
    }

    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    /**
     * @param maximumNumberInstances
     *            the maximumNumberInstances to set
     */
    public void setMaximumNumberInstances(Integer maximumNumberInstances) {
        this.maximumNumberInstances = maximumNumberInstances;
    }

    /**
     * @param minimumNumberInstances
     *            the minimumNumberInstances to set
     */
    public void setMinimumNumberInstances(Integer minimumNumberInstances) {
        this.minimumNumberInstances = minimumNumberInstances;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param networks
     *            the network list
     */
    public void setNetworks(Set<Network> networks) {
        this.networks = networks;
    }

    /**
     * @param payload
     *            the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * @param productReleases
     *            the productReleases to set
     */
    public void setProductReleases(List<ProductRelease> productReleases) {
        this.productReleases = productReleases;
    }

    /*
     * public void setSecurityGroup(SecurityGroup securityGroup) { this.securityGroup = securityGroup; }
     */

    /**
     * Set a vdc.
     * 
     * @param vdc
     */
    public void setVdc(String vdc) {
        if (vdc == null) {
            this.vdc = "";
        } else {
            this.vdc = vdc;
        }
    }

    /**
     * To the dto entity.
     * 
     * @return
     */
    public TierDto toDto() {
        List<ProductReleaseDto> productReleasesDto = new ArrayList<ProductReleaseDto>();
        TierDto tierDto = new TierDto();
        tierDto.setName(getName());
        tierDto.setInitialNumberInstances(getInitialNumberInstances());
        tierDto.setMaximumNumberInstances(getMaximumNumberInstances());
        tierDto.setMinimumNumberInstances(getMinimumNumberInstances());
        tierDto.setIcono(getIcono());
        tierDto.setFlavour(getFlavour());
        tierDto.setImage(getImage());
        tierDto.setRegion(getRegion());
        /*
         * if (this.getSecurityGroup() != null) { tierDto.setSecurityGroup(this.getSecurityGroup().getName()); }
         */
        tierDto.setKeypair(getKeypair());
        tierDto.setFloatingip(getFloatingip());

        if (getProductReleases() != null) {
            for (ProductRelease pRelease : getProductReleases()) {

                ProductReleaseDto pReleaseDto = new ProductReleaseDto();
                pReleaseDto.setProductName(pRelease.getProduct());
                pReleaseDto.setVersion(pRelease.getVersion());
                pReleaseDto.setPrivateAttributes(pRelease.getAttributes());
                pReleaseDto.setMetatadas(pRelease.getMetadatas());

                if (pRelease.getDescription() != null) {
                    pReleaseDto.setProductDescription(pRelease.getDescription());
                }
                if (!productReleasesDto.contains(pReleaseDto)) {
                    productReleasesDto.add(pReleaseDto);
                }
            }
        }

        for (Network net : this.getNetworks()) {
            tierDto.addNetworkDto(net.toDto());
        }

        tierDto.setProductReleaseDtos(productReleasesDto);
        return tierDto;
    }

    /**
     * to json.
     * 
     * @return
     */
    public String toJson() {
        String payload = "{\"server\": " + "{\"key_name\": \"" + getKeypair() + "\", ";

        payload += "\"region\":\"" + getRegion() + "\",";
        payload += "\"flavorRef\": \"" + getFlavour() + "\", " + "\"imageRef\": \"" + getImage() + "\", "
                + "\"name\": \"" + name + "\"}}";
        return payload;

    }

    /**
     * @param network
     *            the network list
     */
    public void updateNetwork(Network network) {
        for (Network net : this.networks) {
            if (net.getNetworkName().equals(network.getNetworkName())) {
                networks.remove(net);
                networks.add(network);
            }
        }
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[[Tier]");
        sb.append("[id = ").append(this.id).append("]");
        sb.append("[name = ").append(this.name).append("]");
        sb.append("[vdc = ").append(this.vdc).append("]");
        sb.append("[environmentname = ").append(this.environmentname).append("]");
        sb.append("[flavour = ").append(this.flavour).append("]");
        sb.append("[image = ").append(this.image).append("]");
        sb.append("[icono = ").append(this.icono).append("]");
        sb.append("[keypair = ").append(this.keypair).append("]");
        sb.append("[floatingip = ").append(this.floatingip).append("]");
        sb.append("[affinity = ").append(this.affinity).append("]");
        sb.append("[maximumNumberInstances = ").append(this.maximumNumberInstances).append("]");
        sb.append("[minimumNumberInstances = ").append(this.minimumNumberInstances).append("]");
        sb.append("[initialNumberInstances = ").append(this.initialNumberInstances).append("]");
        sb.append("[payload = ").append(this.payload).append("]");

        if (Hibernate.isInitialized(this.productReleases)) {
            sb.append("[productReleases = ").append(this.productReleases).append("]");
        }
        if (Hibernate.isInitialized(this.networks)) {
            sb.append("[networks = ").append(this.networks).append("]");
        }
        sb.append("[region = ").append(this.region).append("]");
        sb.append("]");
        return sb.toString();
    }

}
