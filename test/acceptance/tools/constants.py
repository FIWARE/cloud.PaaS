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
# Names of the properties in the {environment}-properties.json files

# Configuration values
ENVIRONMENT = "environment"
ENVIRONMENT_TESTFILES = "test_files"
NAME = "name"
LOGS_PATH = "logs_path"
PAAS = "paas"
KEYSTONE_URL = "keystone_url"
SDC_URL = "sdc_url"
PAASMANAGER_URL = "paasmanager_url"
GLANCE_URL = "glance_url"
NOVA_URL = "nova_url"
VDC = "vdc"
TENANT = "tenant"
USER = "user"
PASSWORD = "password"
TENANT_ALT = "tenant_alt"
VDC_ALT = "vdc_alt"
USER_ALT = "user_alt"
PASSWORD_ALT = "password_alt"
TIER_IMAGE = "tier_image"
TIER_NUM_MIN = "tier_num_min"
TIER_NUM_MAX = "tier_num_max"
TIER_NUM_INITIAL = "tier_num_initial"
TIER_FLAVOUR = "tier_flavour"
TIER_KEYPAIR = "tier_keypair"
TIER_FLOATINGIP = "tier_floatingip"
TIER_REGION = "tier_region"
REGION_DEFAULT_SHAREDNET_PROPERTY = "region_default_sharednet"
CONFIG_SUPPORT_USER = "support_user"
CONFIG_SUPPORT_KEY_FILE = "support_key_file"


# METADATA constants
METADATA_NID = "nid"
METADATA_NID_NOVA_KEY = "nid"

# Auxiliary constants used in .feature files or interfaces
# Request values
NAME = "name"
DESCRIPTION = "description"
PRODUCTS = "products"
NETWORKS = "networks"
TIER_REQUEST_IMAGE = "image"
TIER_REQUEST_NUM_MIN = "minimumNumberInstances"
TIER_REQUEST_NUM_MAX = "maximumNumberInstances"
TIER_REQUEST_NUM_INITIAL = "initialNumberInstances"
TIER_REQUEST_FLAVOUR = "flavour"
TIER_REQUEST_KEYPAIR = "keypair"
TIER_REQUEST_FLOATINGIP = "floatingip"
TIER_REQUEST_REGION = "region"

# E2E Testing
MAX_CHECKS_SSH_CONNECTION = 5
SLEEP_TIME_CHECKS = 5  # seconds
PRODUCT_FILE_NAME_FORMAT = u'{product_name}_{product_version}_{installator}'
INSTALLATION_PRODUCT_DIR = u'/tmp'
