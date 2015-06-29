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

from lettuce import step, world
from lettuce_tools.dataset_utils.dataset_utils import DatasetUtils
from tools import http, environment_request, environment_instance_request
from tools.environment_instance import EnvironmentInstance
import json
from tools.constants import NAME, DESCRIPTION, METADATA_NID_NOVA_KEY, METADATA_NID, REGION_DEFAULT_SHAREDNET_PROPERTY,\
    PAAS
from common_steps import sdc_product_provisioning_steps, paas_environment_provisioning
from tools.utils import raw_httplib_request_to_python_dic
from nose.tools import assert_equal, assert_is_not_none, assert_equals, assert_in
from tools.nova_request import get_server_id_by_partial_name, get_metadata_value, get_ports_from_rules,\
    get_network_name_list

dataset_utils = DatasetUtils()


def _check_metadata_for_tier_in_nova(tier_name, metadata_key, metadata_value):
    # Request the list of servers to Nova
    raw_response = world.nova_request.get_server_list()
    assert_equal(raw_response.status, 200, "Error to obtain the server list. HTTP status code is not the expected")

    body_response = raw_httplib_request_to_python_dic(raw_response)
    sub_instance_name = "{}-{}".format(world.instance_name, tier_name)
    server_id = get_server_id_by_partial_name(body_response, sub_instance_name)
    assert_is_not_none(server_id, "Server has not been found with sub-name " + sub_instance_name)

    # Request the server details by server_id
    raw_response = world.nova_request.get_server_details(server_id)
    assert_equal(raw_response.status, 200, "Error to obtain server details. HTTP status code is not the expected")

    body_response = raw_httplib_request_to_python_dic(raw_response)
    nid_metadata_value = get_metadata_value(body_response, metadata_key)
    assert_equals(str(nid_metadata_value), str(metadata_value))


@step(u'the paas manager is up and properly configured')
def the_paas_manager_is_up_and_properly_configured(step):
    pass  # Nothing to do here, the set up should be done by external means


@step(u'a list of tiers has been defined with data:')
def a_list_of_tiers_has_been_defined_with_data(step):
    world.tiers = paas_environment_provisioning.process_the_list_of_tiers(step)


@step(u'an environment has already been created with data:')
def an_environment_has_already_been_created_with_data(step):
    data = dataset_utils.prepare_data(step.hashes[0])
    world.env_requests.add_environment(data.get(NAME), data.get(DESCRIPTION))


@step(u'an environment has already been created with the previous tiers and data:')
def an_environment_has_already_been_created_with_the_previous_tiers_and_data(step):
    data = dataset_utils.prepare_data(step.hashes[0])
    world.env_requests.add_environment(data.get(NAME), data.get(DESCRIPTION), world.tiers)
    assert world.response.status == 204,\
    "Wrong status code received creating environment: %d. Expected: %d. Body content: %s"\
    % (world.response.status, 204, world.response.read())


@step(u'an instance of the environment "([^"]*)" has already been created using data:')
def an_instance_of_the_environment_has_already_been_created_using_data(step, env_name):
    i_request_the_creation_of_an_instance_of_an_environment_using_data(step, env_name)
    the_task_ends_with_status(step, "SUCCESS")


@step(u'I request the creation of an instance of the environment "([^"]*)" using data:')
def i_request_the_creation_of_an_instance_of_an_environment_using_data(step, env_name):
    # First, send the request to get the environment on which the instance will be based
    env_name = dataset_utils.generate_fixed_length_param(env_name)
    world.env_requests.get_environment(env_name)
    assert world.response.status == 200,\
    "Wrong status code received getting environment: %d. Expected: %d. Body content: %s"\
    % (world.response.status, 200, world.response.read())
    environment = environment_request.process_environment(json.loads(world.response.read()))
    # Then, create the instance
    data = dataset_utils.prepare_data(step.hashes[0])
    world.instance_name = data.get(NAME)
    instance = EnvironmentInstance(world.instance_name, data.get(DESCRIPTION), environment)
    world.inst_requests.add_instance(instance)


@step(u'I receive an? "([^"]*)" response(?: with a task)?')
def i_receive_a_response_of_type(step, response_type):
    status_code = http.status_codes[response_type]
    environment_instance_request.check_add_instance_response(world.response, status_code)


@step(u'the task ends with "([^"]*)" status')
def the_task_ends_with_status(step, status):
    environment_instance_request.check_task_status(world.task_data, status)


@step(u'the product installator to be used is "([^"]*)"')
def the_installator_to_be_used_is_group1(step, installator):
    world.product_installator = installator


@step(u'the product "([^"]*)" with version "([^"]*)" is created in SDC$')
def the_product_group1_is_created_in_sdc(step, product_name, product_version):
    world.product_sdc_request.create_product_and_release(product_name, product_version)


@step(u'the product "([^"]*)" with version "([^"]*)" is created in SDC with attributes:$')
def the_product_group1_is_created_in_sdc_with_attributes(step, product_name, product_version):
    sdc_product_provisioning_steps.product_is_created_in_sdc_with_attributes(step, product_name, product_version)
    paas_environment_provisioning.process_following_instance_attributes(step, product_name)


@step(u'the product "([^"]*)" with version "([^"]*)" is created in SDC with metadatas:$')
def the_product_group1_is_created_in_sdc_with_metadatas(step, product_name, product_version):
    sdc_product_provisioning_steps.product_is_created_in_sdc_with_metadatas(step, product_name, product_version)


@step(u'the created instances have not NID metadata')
def the_created_instances_have_not_nid_metadata(step):
    # For each tier, check NID value
    for tier in world.tiers:
        _check_metadata_for_tier_in_nova(tier.name, METADATA_NID_NOVA_KEY, None)


@step(u'the created instances have the correct NID metadata value')
def the_created_instances_have_not_nid_metadata(step):
    # For each tier, check NID value
    for tier in world.tiers:
        # Get the last product installed
        last_product_name = None
        for tier_product in tier.products:
            last_product_name = tier_product.product

        # Look for test product data in the world and get the nid value
        metadata_value = None
        if last_product_name is not None:
            for test_product in world.product_list_with_attributes:
                if test_product['name'] == last_product_name:
                    # Look for metadata with key=nid
                    for metadata in test_product['metadatas']:
                        if metadata['key'] == METADATA_NID:
                            metadata_value = metadata['value']
                            break
                    break

        print "NID value expected:", metadata_value
        _check_metadata_for_tier_in_nova(tier.name, METADATA_NID_NOVA_KEY, metadata_value)


@step(u'the created security group has rules with "(TCP|UDP)" ports "([^"]*)"')
def the_created_sec_group_has_rules(step, protocol, open_ports):
    open_ports_list = []
    if "" != open_ports:
        open_ports_list = open_ports.split(' ')

    # Add default port '22'
    if protocol == 'TCP':
        open_ports_list.append('22')

    # Get Sec. Group from Nova
    raw_response = world.nova_request.get_security_group_list()
    assert_equal(raw_response.status, 200, "Error to obtain Sec. Groups list. HTTP status code is not the expected")

    body_response = raw_httplib_request_to_python_dic(raw_response)
    rules_tcp_ports_list = get_ports_from_rules(body_response, world.instance_name, protocol=protocol)

    # Check expected ports
    assert_equals(len(open_ports_list), len(rules_tcp_ports_list))
    for expected_port in open_ports_list:
        assert_in(expected_port, rules_tcp_ports_list)


@step(u'the expected networks are connected to the instances')
def the_networks_are_connected_to_instance(step):

    # Get Server list from Nova
    raw_response = world.nova_request.get_server_list()
    assert_equal(raw_response.status, 200, "Error to obtain Server list. HTTP status code is not the expected")
    server_list = raw_httplib_request_to_python_dic(raw_response)

    # For each tier, check Networks
    for tier in world.tiers:
        # Get Server id
        sub_instance_name = "{}-{}".format(world.instance_name, tier.name)
        server_id = get_server_id_by_partial_name(server_list, sub_instance_name)

        # Get Server details
        raw_response = world.nova_request.get_server_details(server_id)
        assert_equal(raw_response.status, 200, "Error to obtain Server details. HTTP status code is not the expected")
        server_details = raw_httplib_request_to_python_dic(raw_response)

        connected_networks_list = get_network_name_list(server_details)

        if tier.networks is None or tier.networks == []:
            assert_equal(len(connected_networks_list), 1,
                         "The number of connected networks is not the expected one")
            assert_equal(connected_networks_list[0], world.config[PAAS][REGION_DEFAULT_SHAREDNET_PROPERTY],
                         "The connected network is not the configured as Shared-Net")
        else:
            assert_equal(len(connected_networks_list), len(tier.networks),
                         "The number of connected networks is not the expected one")
            for network in tier.networks:
                assert_in(network.network_name, connected_networks_list,
                          "The network '%s' is not connected to the instance".format(network.network_name))
