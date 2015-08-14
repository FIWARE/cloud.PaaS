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


from utils.rest_client_utils import RestClient, API_ROOT_URL_ARG_NAME, response_body_to_dict,\
     HEADER_CONTENT_TYPE, HEADER_ACCEPT, HEADER_REPRESENTATION_XML
from utils.logger_utils import get_logger

logger = get_logger(__name__)


#URI ELEMENT
PAASMANAGER_BASE_URI = "{" + API_ROOT_URL_ARG_NAME + "}"
TASK_RESOURCE_ROOT_URI = PAASMANAGER_BASE_URI + "/vdc/{tenant_id}/task"
TASK_RESOURCE_DETAIL_URI = TASK_RESOURCE_ROOT_URI + "/{task_id}"

#BODY ELEMENT
TASK_BODY_ROOT ="task"

class TaskResourceClient(RestClient):

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
        super(TaskResourceClient, self).__init__(protocol, host, port, resource=resource)

    def get_task(self, task_id):
        """
        Get a PaasManager Task  (Tenant)
        :param task_id: ID of the task to obtain
        :return: A duple: The corresponding task, , the 'Request' response
        """
        logger.info("Get task")
        response = self.get(TASK_RESOURCE_DETAIL_URI, headers=self.headers, parameters=None,
                           tenant_id=self.tenant_id, task_id=task_id)

        task_dict = response_body_to_dict(response, self.headers[HEADER_ACCEPT],
                                          xml_root_element_name=TASK_BODY_ROOT)
        return task_dict, response
