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
    response_body_to_dict, HEADER_CONTENT_TYPE, HEADER_ACCEPT, HEADER_REPRESENTATION_XML
from utils.logger_utils import get_logger

logger = get_logger(__name__)


#URI ELEMENT
PAASMANAGER_BASE_URI = "{" + API_ROOT_URL_ARG_NAME + "}"
ENVIRONMENT_RESOURCE_ROOT_URI = PAASMANAGER_BASE_URI + "/catalog/org/FIWARE/vdc/{tenant_id}/environment"
ENVIRONMENT_RESOURCE_DETAIL_URI = ENVIRONMENT_RESOURCE_ROOT_URI + "/{environment_name}"


# BODY ELEMENTS
ENVIRONMENT_BODY_ROOT = "environmentDto"
ENVIRONMENT_BODY_NAME = "name"
ENVIRONMENT_BODY_DESCRIPTION = "description"

class EnvironmentResourceClient(RestClient):

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
        super(EnvironmentResourceClient, self).__init__(protocol, host, port, resource=resource)

    def create_environment(self, name, description):
        """
        Create a new environment (Tenant)
        :param name: Name of the environment
        :param description: Description of the environment
        :return: 'Requests' response
        """
        logger.info("Creating new environment")
        env_model = {ENVIRONMENT_BODY_ROOT: {ENVIRONMENT_BODY_NAME: name,
                                             ENVIRONMENT_BODY_DESCRIPTION: description}}
        body = model_to_request_body(env_model, self.headers[HEADER_ACCEPT])

        return self.post(ENVIRONMENT_RESOURCE_ROOT_URI, body, self.headers, parameters=None,
                             tenant_id=self.tenant_id)

    def delete_environment(self, name):
        """
        Delete an environemnt (Tenant)
        :param name: Name of the environment to be deleted
        :return: 'Request' response
        """
        logger.info("Deleting environment")
        return self.delete(ENVIRONMENT_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_name=name)

    def get_environment(self, name):
        """
        Get an environment (Tenant)
        :return: A duple: The environment as a dict, , the 'Request' response
        """
        logger.info("Get environment")
        response = self.get(ENVIRONMENT_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, environment_name=name)

        dict_environment = response_body_to_dict(response, self.headers[HEADER_ACCEPT],
                                          xml_root_element_name=ENVIRONMENT_BODY_ROOT)
        return dict_environment, response
