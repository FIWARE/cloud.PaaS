#!/usr/bin/env python
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


from setuptools import setup
from setuptools import find_packages


setup(name='python-paasmanagerclient',
      version='0.0.2',
      description='PaaSManager Client',
      author='jfernandez',
      url='https://github.com/telefonicaid/fiware-paas/python-paasmanagerclient',
      packages=find_packages(),
      install_requires=['requests', 'xmldict', 'xmltodict', 'python-keystoneclient==1.3.0'])
