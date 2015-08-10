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


from qa_utils.rest_client_utils import RestClient, API_ROOT_URL_ARG_NAME, model_to_request_body,  \
    response_body_to_dict, HEADER_CONTENT_TYPE, HEADER_REPRESENTATION_XML
from qa_utils.logger_utils import get_logger

logger = get_logger("paasmanagerClient")


#URI ELEMENT
PAASMANAGER_BASE_URI = "{" + API_ROOT_URL_ARG_NAME + "}"
ENVIRONMENT_INSTANCE_RESOURCE_ROOT_URI = PAASMANAGER_BASE_URI + \
                                         "/envInst/org/FIWARE/vdc/{tenant_id}/environmentInstance"
ENVIRONMENT_INSTANCE_RESOURCE_DETAIL_URI = ENVIRONMENT_INSTANCE_RESOURCE_ROOT_URI + "/{environment_instance_name}"


# BODY ELEMENTS
ENVIRONMENT_INSTANCE_BODY_ROOT = "environmentInstanceDto"
ENVIRONMENT_INSTANCE_BODY_NAME = "blueprintName"
ENVIRONMENT_INSTANCE_BODY_DESCRIPTION = "description"

ENVIRONMENT_BODY_ROOT = "environmentDto"
ENVIRONMENT_BODY_NAME = "name"
ENVIRONMENT_BODY_DESCRIPTION = "description"

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

class EnvironmentInstanceResourceClient(RestClient):

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
            self.headers = {HEADER_CONTENT_TYPE: HEADER_REPRESENTATION_XML}
        self.headers = headers
        self.tenant_id = tenant_id
        super(EnvironmentInstanceResourceClient, self).__init__(protocol, host, port, resource=resource)

    def create_environment_instance(self, name, environment_name, tier_name, image, region_name,
                                    description="", environment_description="", keypair="", product_name="",
                                    product_version="", network_name="", subnetwork_name=""):
        """
        Create a new environment (Tenant)
        :param name: Name of the environment
        :param description: Description of the environment
        :return: 'Requests' response
        """
        logger.info("Creating new environment  instance")
        env_model = {ENVIRONMENT_INSTANCE_BODY_ROOT:
                         {
                             ENVIRONMENT_INSTANCE_BODY_NAME: name,
                             ENVIRONMENT_BODY_DESCRIPTION: description,
                             ENVIRONMENT_BODY_ROOT:
                                {
                                    ENVIRONMENT_BODY_NAME: environment_name,
                                    ENVIRONMENT_BODY_DESCRIPTION: environment_description,
                                    TIER_BODY_ROOT:
                                        {
                                            TIER_BODY_NAME: tier_name,
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
                         }
                    }
        body = model_to_request_body(env_model, self.headers[HEADER_CONTENT_TYPE])
        return self.post(ENVIRONMENT_INSTANCE_RESOURCE_ROOT_URI, body, self.headers, parameters=None,
                         tenant_id=self.tenant_id)

    def delete_environment_instance(self, name):
        """
        Delete an environemnt instance(Tenant)
        :param name: Name of the environment instance to be deleted
        :return: 'Request' response
        """
        logger.info("Deleting environment instance " + name)
        return self.delete(ENVIRONMENT_INSTANCE_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_instance_name=name)

    def get_environment_instance(self, name):
        """
        Get an environment instance(Tenant)
        :return:
        """
        logger.info("Get environment instance " + name )
        response = self.get(ENVIRONMENT_INSTANCE_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_instance_name=name)

        sr_response = response_body_to_dict(response, self.headers[HEADER_CONTENT_TYPE],
                                          xml_root_element_name=ENVIRONMENT_INSTANCE_BODY_ROOT)
        return sr_response