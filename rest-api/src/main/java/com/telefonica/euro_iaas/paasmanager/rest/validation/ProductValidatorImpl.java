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
package com.telefonica.euro_iaas.paasmanager.rest.validation;

import java.util.StringTokenizer;

import com.telefonica.euro_iaas.paasmanager.exception.InvalidEnvironmentInstanceException;
import com.telefonica.euro_iaas.paasmanager.model.Attribute;
import com.telefonica.euro_iaas.paasmanager.model.dto.ProductReleaseDto;
import com.telefonica.euro_iaas.paasmanager.model.dto.TierDto;
import com.telefonica.euro_iaas.paasmanager.util.SystemPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductValidatorImpl implements ProductValidator {

    private SystemPropertiesProvider systemPropertiesProvider;
    private static Logger log = LoggerFactory.getLogger(ProductValidatorImpl.class);


    @Override
    public void validateAttributes(TierDto tierDto) throws InvalidEnvironmentInstanceException {

        for (ProductReleaseDto p : tierDto.getProductReleaseDtos()) {
            if (p.getPrivateAttributes() != null) {
                for (Attribute att : p.getPrivateAttributes()) {
                    if (att.getType() == null) {
                        att.setType("Plain");
                    }

                    checkType(att);
                    checkValue(att);

                }
            }
        }
    }

    private void checkValue(Attribute att) throws InvalidEnvironmentInstanceException {
        String msg = "Attribute value is incorrect.";
        boolean error = true;
        if (att.getValue().startsWith("IP(") && att.getValue().endsWith(")") && "IP".equals(att.getType())) {
            error = false;
        } else if (att.getValue().startsWith("IPALL(") && att.getValue().endsWith(")") && "IPALL".equals(att.getType())) {
            error = false;
        } else if ("Plain".equals(att.getType())) {
            error = false;
        }
        if (error) {
            log.warn("check value: " + att.getKey()+ " " + att.getValue() + " " + att.getType() + " " + msg);
            throw new InvalidEnvironmentInstanceException(msg);
        }
    }

    private void checkType(Attribute att) throws InvalidEnvironmentInstanceException {
        String msg = "Attribute type is incorrect.";
        String availableTypes = systemPropertiesProvider
                .getProperty(SystemPropertiesProvider.AVAILABLE_ATTRIBUTE_TYPES);

        StringTokenizer st2 = new StringTokenizer(availableTypes, "|");
        boolean error = true;
        while (st2.hasMoreElements()) {
            if (att.getType().equals(st2.nextElement())) {
                error = false;
                break;
            }
        }
        if (error) {
            log.warn("check type: " + att.getKey()+ " " + att.getValue() + " " + att.getType() + " " + msg);
            throw new InvalidEnvironmentInstanceException(msg);
        }

    }

    public void setSystemPropertiesProvider(SystemPropertiesProvider systemPropertiesProvider) {
        this.systemPropertiesProvider = systemPropertiesProvider;
    }

}
