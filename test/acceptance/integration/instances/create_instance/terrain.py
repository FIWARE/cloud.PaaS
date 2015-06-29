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

from lettuce import world, after, before
from tools import terrain_steps
from tools.http import get_token
from tools import environment_request, environment_instance_request
from tools.environment_request import EnvironmentRequest
from tools.environment_instance_request import EnvironmentInstanceRequest
from tools.constants import PAAS, KEYSTONE_URL, PAASMANAGER_URL, TENANT, USER,\
    PASSWORD, VDC, SDC_URL, NOVA_URL
from tools.nova_request import NovaRequest
from tools.product_sdc_request import ProductSdcRequest


@before.all
def before_all():
    """ Hook: Before all features. It will config common requisites for TCs execution """
    # Get Auth Token
    world.auth_token = get_token(world.config[PAAS][KEYSTONE_URL] + '/tokens', world.config[PAAS][TENANT],
                                 world.config[PAAS][USER],
                                 world.config[PAAS][PASSWORD])


@before.each_feature
def before_each_feature(feature):
    world.env_requests = EnvironmentRequest(world.config[PAAS][KEYSTONE_URL],
        world.config[PAAS][PAASMANAGER_URL],
        world.config[PAAS][TENANT],
        world.config[PAAS][USER],
        world.config[PAAS][PASSWORD],
        world.config[PAAS][VDC],
        world.config[PAAS][SDC_URL])

    world.inst_requests = EnvironmentInstanceRequest(world.config[PAAS][KEYSTONE_URL],
        world.config[PAAS][PAASMANAGER_URL],
        world.config[PAAS][TENANT],
        world.config[PAAS][USER],
        world.config[PAAS][PASSWORD],
        world.config[PAAS][VDC],
        world.config[PAAS][SDC_URL])

    world.product_sdc_request = ProductSdcRequest(world.config[PAAS][KEYSTONE_URL],
                                                  world.config[PAAS][SDC_URL],
                                                  world.config[PAAS][TENANT],
                                                  world.config[PAAS][USER],
                                                  world.config[PAAS][PASSWORD],
                                                  world.config[PAAS][VDC])

    world.nova_request = NovaRequest(world.config[PAAS][NOVA_URL],
                                     world.config[PAAS][TENANT],
                                     world.config[PAAS][USER],
                                     world.config[PAAS][PASSWORD],
                                     world.config[PAAS][VDC],
                                     world.auth_token)

    # Init vars
    world.product_and_release_list = list()
    world.product_installator = 'chef'


@before.each_scenario
def before_each_scenario(scenario):
    """ Lettuce Hook. Will be executed before each scenario. Init global scenario vars. """
    world.product_list_with_attributes = list()
    world.paas_product_list_with_attributes = list()

    # Create product in SDC to be used by this feature
    terrain_steps.init_products_in_sdc()


@after.outline
def after_outline(param1, param2, param3, param4):
    """ Hook: Will be executed after each Scenario Outline. Same behaviour as 'after_each_scenario'"""
    after_each_scenario(None)


@after.each_scenario
def after_each_scenario(scenario):
    # Delete the environments created in the scenario.
    environment_instance_request.delete_created_instances()
    environment_request.delete_created_environments()

    # Remove SDC product data
    for product_and_release in world.product_and_release_list:
        world.product_sdc_request.delete_product_and_release(product_and_release['product_name'],
                                                             product_and_release['product_release'])


@before.outline
def before_outline(param1, param2, param3, param4):
    """ Hook: Will be executed before each Scenario Outline. Same behaviour as 'before_each_scenario'"""
    before_each_scenario(None)


@after.each_feature
def after_feature(feature):
    """Hook: Will be executed after the feature"""
    # Remove SDC product data
    for product_and_release in world.product_and_release_list:
        world.product_sdc_request.delete_product_and_release(product_and_release['product_name'],
                                                             product_and_release['product_release'])
