# -*- coding: utf-8 -*-

# Copyright 2015 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of FIWARE project.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
# You may obtain a copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.
#
# For those usages not covered by the Apache version 2.0 License please
# contact with opensource@tid.es


from utils.rest_client_utils import RestClient, API_ROOT_URL_ARG_NAME, model_to_request_body,  \
    response_body_to_dict, delete_element_when_value_none, HEADER_CONTENT_TYPE, HEADER_ACCEPT, HEADER_REPRESENTATION_XML
from utils.logger_utils import get_logger

logger = get_logger(__name__)


#URI ELEMENT
PAASMANAGER_BASE_URI = "{" + API_ROOT_URL_ARG_NAME + "}"
TIER_RESOURCE_ROOT_URI = PAASMANAGER_BASE_URI + "/catalog/org/FIWARE/vdc/{tenant_id}/environment/{environment_name}/tier"
TIER_RESOURCE_DETAIL_URI = TIER_RESOURCE_ROOT_URI + "/{tier_name}"

# BODY ELEMENTS
TIER_BODY_ROOT = "tierDto"
TIER_BODY_INITIAL_INSTANCES = "initialNumberInstances"
TIER_BODY_MAXIMUM_INSTANCES = "maximumNumberInstances"
TIER_BODY_MINIMUM_INSTANCES = "minimumNumberInstances"
TIER_BODY_NAME = "name"
TIER_BODY_IMAGE = "image"
TIER_BODY_FLAVOUR = "flavour"
TIER_BODY_KEYPAIR = "keypair"
TIER_BODY_FLOATINGIP = "floatingip"
TIER_BODY_REGION = "region"
TIER_BODY_PRODUCTRELEASE = "productReleaseDtos"
TIER_BODY_PRODUCTRELEASE_NAME = "productName"
TIER_BODY_PRODUCTRELEASE_VERSION = "version"
TIER_BODY_NETWORK = "networkDto"
TIER_BODY_NETWORK_NAME = "networkName"
TIER_BODY_SUBNETWORK = "subNetworkDto"
TIER_BODY_SUBNETWORK_NAME = "subnetName"

class TierResourceClient(RestClient):

    def __init__(self, protocol, host, port, tenant_id, resource=None, headers=None):
        """
        Class constructor. Inits default attributes.
        :param protocol: Connection protocol (HTTP | HTTPS)
        :param host: Host
        :param port: Port
        :param tenant_id: TenantID
        :param resource: Base URI resource
        :param headers: HTTP Headers
        :return: None
        """
        if headers is None:
            self.headers = {HEADER_CONTENT_TYPE: HEADER_REPRESENTATION_XML,
                            HEADER_ACCEPT: HEADER_REPRESENTATION_XML}
        self.headers = headers
        self.tenant_id = tenant_id
        super(TierResourceClient, self).__init__(protocol, host, port, resource=resource)

    def create_tier(self, environment_name, name, image, region_name, keypair=None, product_name=None,
                    product_version=None, network_name=None, subnetwork_name=None):
        """
        Add a Tier to an already existing Environment (Tenant)
        :param name: Name of the environment
        :param image: image id to deploy a VM
        :param region: region where to deploy
        :return: 'Requests' response
        """
        logger.info("Add tier to environment")
        tier_model =    {TIER_BODY_ROOT:
                            {
                                TIER_BODY_NAME: name,
                                TIER_BODY_INITIAL_INSTANCES: "1",
                                TIER_BODY_MAXIMUM_INSTANCES: "1",
                                TIER_BODY_MINIMUM_INSTANCES: "1",
                                TIER_BODY_IMAGE: image,
                                TIER_BODY_FLAVOUR: "2",
                                TIER_BODY_KEYPAIR: keypair,
                                TIER_BODY_FLOATINGIP: "False",
                                TIER_BODY_REGION: region_name,
                                TIER_BODY_PRODUCTRELEASE :
                                    {
                                        TIER_BODY_PRODUCTRELEASE_NAME : product_name,
                                        TIER_BODY_PRODUCTRELEASE_VERSION : product_version
                                    },
                                TIER_BODY_NETWORK :
                                    {
                                        TIER_BODY_NETWORK_NAME : network_name,
                                        TIER_BODY_SUBNETWORK :
                                            {
                                                TIER_BODY_SUBNETWORK_NAME: subnetwork_name
                                            }
                                    }
                            }
                        }

        #Removing keys whose values are None
        delete_element_when_value_none(tier_model)

        body = model_to_request_body(tier_model, self.headers[HEADER_ACCEPT])

        return self.post(TIER_RESOURCE_ROOT_URI, body, self.headers, parameters=None,
                                            tenant_id=self.tenant_id,environment_name=environment_name)

    def delete_tier(self, environment_name, name):
        """
        Delete a Tier (Tenant)
        :param environment_name: Name of the environment to which tier belongs
        :param name: Name of the tier to be deleted
        :return: 'Request' response
        """
        logger.info("Deleting environment")
        return self.delete(TIER_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_name=environment_name, tier_name=name)

    def get_tier(self, environment_name, name):
        """
        Get a Tier of a Environment  (Tenant)
        :param environment_name: Name of the environment to which tier belongs
        :param name: Name of the tier to be deleted
        :return: Aduple: the corresponding Tier as a dict, , the 'Request' response
        """
        logger.info("Get tier")
        response = self.get(TIER_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_name=environment_name, tier_name=name)

        dict_tier = response_body_to_dict(response, self.headers[HEADER_ACCEPT],
                                          xml_root_element_name=TIER_BODY_ROOT)
        return dict_tier, response
