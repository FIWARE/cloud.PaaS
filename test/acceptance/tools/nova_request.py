# -*- coding: utf-8 -*-
# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of FI-WARE project.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
# You may obtain a copy of the License at:
#
#        http://www.apache.org/licenses/LICENSE-2.0
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

__author__ = 'jfernandez'

import http


class NovaRequest:

    def __init__(self, nova_url, tenant, user, password, vdc, auth_token):
        """
        Constructor. Initializes class attributes.
        :param nova_url: Nova URL
        :param tenant: Fiware tenant name
        :param user: Fiware User name
        :param password: Fiware Password
        :param vdc: TenantId
        :param auth_token: Valid Auth Token (Keystone)
        """
        self.nova_url = nova_url

        self.vdc = vdc

        self.user = user
        self.password = password
        self.tenant = tenant

        self.default_headers = {'Accept': 'application/json',
                                'X-Auth-Token': auth_token,
                                'Tenant-Id': self.tenant}

    def __get__(self, url):
        """
        Executes a get request to Nova service
        :param url: Full URL to GET
        :return: HTTPlib request
        """
        return http.get(url, self.default_headers)

    def get_flavour_list(self):
        """
        Gets the list of available flavors
        :return: HTTPlib request
        """
        url = "{}/{}".format(self.nova_url, 'flavors')
        return self.__get__(url)

    def get_server_list(self):
        """
        Gets the list of launched servers
        :return: HTTPlib request
        """
        url = "{}/{}".format(self.nova_url, 'servers')
        return self.__get__(url)

    def get_server_details(self, server_id):
        """
        Gets full details about a server
        :return: HTTPlib request
        """
        url = "{}/{}/{}".format(self.nova_url, 'servers', server_id)
        return self.__get__(url)

    def get_security_group_list(self):
        """
        Gets the list of security groups
        :return: HTTPlib request
        """
        url = "{}/{}".format(self.nova_url, 'os-security-groups')
        return self.__get__(url)


def get_number_of_flavors(body_response):
    """
    Returns the number of images in the list.
    :param body_response: Parsed response (Python dic). List of flavors
    :return: Length of the list
    """
    return len(body_response['flavors'])


def get_first_flavor_in_list(body_response, name_filter=None):
    flavor = None
    if name_filter is not None:
        for flavor in body_response['flavors']:
            if name_filter in flavor['name']:
                flavor = flavor['id']
                break
    else:
        if len(body_response['flavors']) != 0:
            flavor = body_response['flavors'][0]['id']

    return flavor


def get_server_id_by_partial_name(body_response_server_list, partial_server_name):
    """
    Looks for server Id in the server list by server name
    :param body_response_server_list: Parsed response (python dic). List of deployed instances
    :param partial_name: The name of the server to find (or a substring)
    :return: Server ID
    """
    server_id = None
    for server in body_response_server_list['servers']:
        if partial_server_name in server['name']:
            server_id = server['id']
            break

    return server_id


def get_metadata_value(body_response_server_details, metadata_key):
    """
    Retrieves the metadata value from instance details
    :param body_response_server_details: Parsed response (python dic). Server details data
    :param metadata_key: The key of the metadata to be retrieved
    :return: Metadata value with that key
    """
    metadata_value = None
    if metadata_key in body_response_server_details['server']['metadata']:
        metadata_value = body_response_server_details['server']['metadata'][metadata_key]

    return metadata_value


def get_security_group_rules(body_response_sec_group_list, sec_group_name):
    """
    Retrieve security group rules list
    :param body_response_sec_group_list: Parsed response (python dic). List of sec. groups
    :param sec_group_name: Name of Sec. Group to look for
    :return: Return the first sec. group with sec_group_name IN name.
    """
    rules_list = None
    for sec_group in body_response_sec_group_list:
        if sec_group_name in sec_group['name']:
            rules_list = sec_group['rules']
            break

    return rules_list


def get_ports_from_rules(body_response_sec_group_list, sec_group_name, protocol='TCP'):
    """
    Retrieve the list of TCP open ports
    :param body_response_sec_group_list: Parsed response (python dic). List of sec. groups
    :param sec_group_name: Name of Sec. Group to look for
    :param protocol: TCP or UDP
    :return: List of TCP ports (list of strings)
    """
    port_list = []
    for sec_group in body_response_sec_group_list['security_groups']:
        if sec_group_name in sec_group['name']:
            for rule in sec_group['rules']:
                if rule['ip_protocol'] == protocol.lower():
                    port = str(rule['from_port'])
                    to_port = str(rule['to_port'])
                    port = port + "-" + to_port if to_port != port else port
                    port_list.append(port)

    return port_list


def get_network_name_list(body_response_server_details):
    """
    Retrieve the list of network names where this server is connected
    :param body_response_server_details: (dic) Parsed response. Server details data
    :return: (list) The list of linked networks to the server
    """
    network_list = list()
    for address in body_response_server_details['server']['addresses']:
        network_list.append(address)

    return network_list


def get_floating_ip(body_response_server_details, network_name):
    """
    Retrieve the first floating IP from the given network attached to VM
    :param body_response_server_details: (dic) Parsed response. Server details data
    :param network_name (String): Name of the network where floating IP is allocated.
    :return: (String) The floating IP. None if floating IP not found in the given network (VM)
    """
    floating_ip = None
    addresses = body_response_server_details['server']['addresses']
    for address in addresses:
        if address == network_name:
            for net in addresses[address]:
                if net['OS-EXT-IPS:type'] == "floating":
                    floating_ip = net['addr']
                    break

    return floating_ip
