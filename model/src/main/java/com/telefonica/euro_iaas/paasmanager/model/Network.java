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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.telefonica.euro_iaas.paasmanager.model.dto.NetworkDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.SubNetworkDto;

/**
 * A network.
 * 
 * @author Henar Munoz
 */

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "Network")
public class Network {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    @XmlTransient
    private Long id;

    private String name;

    private String region;

    private String vdc;

    private boolean federatedNetwork = false;

    private String federatedRange = "0";

    @OneToMany(cascade = { CascadeType.ALL })
    private Set<SubNetwork> subNets;

    /**
     * Constructor.
     */
    public Network() {
        subNets = new HashSet<SubNetwork>();
    }

    /**
     * @param networkName
     */
    public Network(String name, String vdc, String region) {
        this.name = name;
        this.vdc = vdc;
        this.region = region;
        subNets = new HashSet<SubNetwork>();
    }

    /**
     * It adds a subnet to the network.
     * 
     * @param subNet
     * @return
     */
    public void addSubNet(SubNetwork subNet) {
        if (subNets == null) {
            subNets = new HashSet<SubNetwork>();
        }
        subNets.add(subNet);
    }

    /**
     * It does a copy of the collection.
     * 
     * @return
     */
    public Set<SubNetwork> cloneSubNets() {
        Set<SubNetwork> subNetAux = new HashSet<SubNetwork>();
        for (SubNetwork subNet2 : getSubNets()) {
            subNetAux.add(subNet2);
        }
        return subNetAux;
    }

    /**
     * It deletes a subnet to the network.
     * 
     * @param subNet
     * @return
     */
    public void deleteSubNet(SubNetwork subNet) {
        if (subNets.contains(subNet)) {
            subNets.remove(subNet);
        }
    }

    /**
     * It updates a subnet to the network.
     * 
     * @param subNet
     * @return
     */
    public void updateSubNet(SubNetwork subNet) {
        if (subNets.contains(subNet)) {
            subNets.remove(subNet);
        }
        subNets.add(subNet);
    }

    /**
     * @param subNet
     * @return
     */
    public boolean contains(SubNetwork subNet) {
        return subNets.contains(subNet);
    }

    /**
     * @return the networkName
     */
    public String getNetworkName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public void setNetworkName(String name) {
        this.name = name;
    }

    public String getVdc() {
        return vdc;
    }

    public void setVdc(String vdc) {
        this.vdc = vdc;
    }

    public void setFederatedNetwork(boolean federatedNetwork) {
        this.federatedNetwork = federatedNetwork;
    }

    /**
     * Get the federated network.
     * 
     * @return
     */
    public boolean getfederatedNetwork() {
        return this.federatedNetwork;
    }

    public void setFederatedRange(String range) {
        this.federatedRange = range;
    }

    public String getFederatedRange() {
        return this.federatedRange;
    }

    /**
     * It gets the subnets.
     * 
     * @return List<SubNetwork>
     */
    public Set<SubNetwork> getSubNets() {
        return this.subNets;
    }

    /**
     * It add the subnet collection.
     * 
     * @param subNets
     */
    public void setSubNets(Set<SubNetwork> subNets) {
        this.subNets = subNets;
    }

    /**
     * the dto entity.
     * 
     * @return
     */
    public NetworkDto toDto() {
        NetworkDto networkDto = new NetworkDto(this.name);
        for (SubNetwork subnet : this.getSubNets()) {
            SubNetworkDto subNetDto = subnet.toDto();
            networkDto.addSubNetworkDto(subNetDto);
        }
        return networkDto;
    }

    /**
     * the network instance.
     * 
     * @return
     */
    public NetworkInstance toNetworkInstance() {
        NetworkInstance networkInstance = new NetworkInstance(this.name, vdc, region);
        networkInstance.setFederatedNetwork(this.getfederatedNetwork());
        networkInstance.setFederatedRange(this.getFederatedRange());
        for (SubNetwork subnet : this.getSubNets()) {
            SubNetworkInstance subNetInstance = subnet.toInstance(vdc, region);
            networkInstance.addSubNet(subNetInstance);
        }
        return networkInstance;
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
        Network other = (Network) obj;
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

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[[Network]");
        sb.append("[id = ").append(this.id).append("]");
        sb.append("[name = ").append(this.name).append("]");
        sb.append("[region = ").append(this.region).append("]");
        sb.append("[vdc = ").append(this.vdc).append("]");
        sb.append("[federatedNetwork = ").append(this.federatedNetwork).append("]");
        sb.append("[federatedRange = ").append(this.federatedRange).append("]");
        sb.append("[subNets = ").append(this.subNets).append("]");
        sb.append("]");
        return sb.toString();
    }

}
