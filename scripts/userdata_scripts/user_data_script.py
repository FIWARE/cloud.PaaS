#!/usr/bin/python

# Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U
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

#
# This file obtain the validation chef key from the
# the user data. This information is defined in the
# chefkey resource
#

import yaml
import urllib2

def get_validation_key(userdata):
    data = yaml.load(userdata)
    if not 'chefkey' in data:
        return None
    data = data['chefkey']
    print data
    f = open('/etc/validation.pem','w')
    f.write(data)
    f.close()

def get_user_data():
    try:
        h =  urllib2.urlopen('http://169.254.169.254/openstack/latest/user_data',None,30)
    except Exception, e:
        return None, None
    if h.getcode() != 200:
        return None, None

    return get_validation_key(h)

get_user_data()
