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


from lettuce import step, world
from lettuce_tools.dataset_utils.dataset_utils import DatasetUtils
from common_steps import sdc_product_provisioning_steps, paas_environment_provisioning
from tools.constants import NAME, DESCRIPTION, PRODUCT_FILE_NAME_FORMAT, PAAS, CONFIG_SUPPORT_KEY_FILE, \
    CONFIG_SUPPORT_USER, REGION_DEFAULT_SHAREDNET_PROPERTY, INSTALLATION_PRODUCT_DIR, MAX_CHECKS_SSH_CONNECTION,\
    SLEEP_TIME_CHECKS
from tools import http, environment_request, environment_instance_request
from tools.environment_instance_request import EnvironmentInstance
from tools.fabric_utils import FabricUtils
from tools.nova_request import get_server_id_by_partial_name, get_network_name_list, get_floating_ip
from tools.utils import raw_httplib_request_to_python_dic
import json
from nose.tools import assert_equal, assert_is_not_none, assert_equals, assert_in, assert_true
import time

dataset_utils = DatasetUtils()


@step(u'the paas manager is up and properly configured')
def the_paas_manager_is_up_and_properly_configured(step):

    pass  # Nothing to do here, the set up should be done by external means


@step(u'a list of tiers has been defined with data:')
def a_list_of_tiers_has_been_defined_with_data(step):

    world.tiers = paas_environment_provisioning.process_the_list_of_tiers(step)


@step(u'an environment has already been created with the previous tiers and data:')
def an_environment_has_already_been_created_with_the_previous_tiers_and_data(step):

    data = dataset_utils.prepare_data(step.hashes[0])
    world.env_requests.add_environment(data.get(NAME), data.get(DESCRIPTION), world.tiers)
    assert world.response.status == 204, \
        "Wrong status code received creating environment: %d. " \
        "Expected: %d. Body content: %s" % (world.response.status, 204, world.response.read())


@step(u'the product "([^"]*)" with version "([^"]*)" is created in SDC with metadatas:$')
def the_product_group1_is_created_in_sdc_with_metadatas(step, product_name, product_version):

    sdc_product_provisioning_steps.product_is_created_in_sdc_with_metadatas(step, product_name, product_version)


@step(u'I request the creation of an instance of the environment "([^"]*)" using data:')
def i_request_the_creation_of_an_instance_of_an_environment_using_data(step, env_name):

    # First, send the request to get the environment on which the instance will be based
    env_name = dataset_utils.generate_fixed_length_param(env_name)
    world.env_requests.get_environment(env_name)
    assert world.response.status == 200, \
        "Wrong status code received getting environment: %d. " \
        "Expected: %d. Body content: %s" % (world.response.status, 200, world.response.read())

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


@step(u'the product "([^"]*)" with version "([^"]*)" of tier "([^"]*)" has been installed using "(chef|puppet)"')
def the_product_is_installed(step, product_name, product_version, tier_name, installator):

    tier = None
    for tier_world in world.tiers:
        if tier_world.name == tier_name:
            tier = tier_world
    assert_is_not_none(tier, "Tier with name '{}' not found in created tier list.".format(tier_name))

    # > Get from Nova the IP of the Floating VM
    # >> Get Server list from Nova
    raw_response = world.nova_request.get_server_list()
    assert_equal(raw_response.status, 200, "Error to obtain Server list. HTTP status code is not the expected")
    server_list = raw_httplib_request_to_python_dic(raw_response)

    sub_instance_name = "{}-{}".format(world.instance_name, tier.name)
    server_id = get_server_id_by_partial_name(server_list, sub_instance_name)

    # Get Server details
    raw_response = world.nova_request.get_server_details(server_id)
    assert_equal(raw_response.status, 200, "Error to obtain Server details. HTTP status code is not the expected")
    server_details = raw_httplib_request_to_python_dic(raw_response)

    connected_networks_list = get_network_name_list(server_details)
    shared_net = world.config[PAAS][REGION_DEFAULT_SHAREDNET_PROPERTY]
    assert_in(shared_net, connected_networks_list,
              "The connected network is not the configured as Shared-Net")

    ip_internet = get_floating_ip(server_details, shared_net)
    assert_is_not_none(ip_internet, "Floating IP not found Shared-Net. Is the Internet network added to tier conf?")

    # Create new Fabric connection
    fabric_client = FabricUtils(host_name=ip_internet,
                                host_username=world.config[PAAS][CONFIG_SUPPORT_USER],
                                host_password="-",
                                host_ssh_key=world.config[PAAS][CONFIG_SUPPORT_KEY_FILE])


    file_name = PRODUCT_FILE_NAME_FORMAT.format(product_name=product_name,
                                                product_version=product_version,
                                                installator=installator)

    # Wait for software installation.
    time.sleep(SLEEP_TIME_CHECKS)

    # Retry SSH 5 times
    response = False
    for i in range(MAX_CHECKS_SSH_CONNECTION):
        try:
            response = fabric_client.file_exist(INSTALLATION_PRODUCT_DIR, file_name)
            print "Connected!"
            break
        except Exception as e:
            print "SSH Connection #%d: %s" % (i, e.message)
            time.sleep(SLEEP_TIME_CHECKS)

    assert_true(response, "Softwer is not installed. File not found: '{}/{}".format(INSTALLATION_PRODUCT_DIR,
                                                                                    file_name))
