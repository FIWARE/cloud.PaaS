#!/bin/sh

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


# This file is used for installing and configuring
# chef and puppet inside a VM. The input parameters are:
# - chef server url
# - puppet master url
# - validation key path
#
# - install.sh should be true to ohai and should not remap
#   platform or platform versions.
#
# - remapping platform and mangling platform version numbers is
#   now the complete responsibility of the server-side endpoints
#

# Input parameters
CHEF_SERVER=$1
PUPPET_SERVER=$2
VALIDATION=$3

# We obtain the VM hostname
hostname_var=`hostname`
hostname_ip=`hostname  -I | cut -f1 -d' '`
echo "$hostname_ip $hostname_var" >> /etc/hosts
HOSTNAME_VAR=$hostname_var

# We obtain the Operative system distribution
if test -f "/etc/lsb-release" && grep -q DISTRIB_ID /etc/lsb-release; then
  platform=`grep DISTRIB_ID /etc/lsb-release | cut -d "=" -f 2 | tr '[A-Z]' '[a-z]'`
  platform_version=`grep DISTRIB_RELEASE /etc/lsb-release | cut -d "=" -f 2`
elif test -f "/etc/debian_version"; then
  platform="debian"
  platform_version=`cat /etc/debian_version`
elif test -f "/etc/redhat-release"; then
  platform=`sed 's/^\(.\+\) release.*/\1/' /etc/redhat-release | tr '[A-Z]' '[a-z]'`
  echo $platform
  platform_version=`sed 's/^.\+ release \([.0-9]\+\).*/\1/' /etc/redhat-release`
  echo $platform_version
fi

# We obtain the Operative system version

major_version=`echo $platform_version | cut -d. -f1`
case $platform in
  # FIXME: should remove this case statement completely
  "el")
    # FIXME:  "el" is deprecated, should use "redhat"
    platform_version=$major_version
    ;;
  "debian")
    # FIXME: remove client-side yolo here
    case $major_version in
      "5") platform_version="6";;  # FIXME: need to port this "reverse-yolo" into platform.rb
      "6") platform_version="6";;
      "7") platform_version="6";;
    esac
    ;;
  "freebsd")
    platform_version=$major_version
    ;;
  "sles")
    platform_version=$major_version
    ;;
  "suse")
    platform_version=$major_version
    ;;
esac

if test "x$platform_version" = "x"; then
  echo "Unable to determine platform version!"
  report_bug
  exit 1
fi

if test "x$platform" = "xsolaris2"; then
  # hack up the path on Solaris to find wget, pkgadd
  PATH=/usr/sfw/bin:/usr/sbin:$PATH
  export PATH
fi

# We obtain the chef validation key
curl -L http://repositories.testbed.fi-ware.org/webdav/user_data_script.py | python

## installing chef
mkdir /etc/chef
mkdir /var/log/chef
curl -L https://www.opscode.com/chef/install.sh | bash 
OHAI_TIME_DIR="$(find / -name ohai_time.rb)"

### Configuring Chef
sed -i 's/ohai_time Time.now.to_f/ohai_time Time.now/' ${OHAI_TIME_DIR}
echo 'log_level              :info
log_location           "/var/log/chef.log"
ssl_verify_mode        :verify_none
validation_client_name "chef-validator"
validation_key         "/etc/chef/validation.pem"
client_key             "/etc/chef/client.pem"
chef_server_url        "'${CHEF_SERVER}'"
environment            "_default"
node_name              "'${HOSTNAME_VAR}'"
json_attribs           "/etc/chef/firstboot.json"
file_cache_path        "/var/cache/chef"
file_backup_path       "/var/backups/chef"
pid_file               "/var/run/chef/client.pid"
Chef::Log::Formatter.show_time = true' > /etc/chef/client.rb

echo '{}' > /etc/chef/firstboot.json
cp $VALIDATION /etc/chef/validation.pem 
chef-client -d -i 60 -s 6 -L /var/log/chef/client.log &


## Installing puppet
### repository
echo ${platform}
if [ ${platform:0:6} == 'centos' ]; then
    echo $platform_version
    if [[ ${platform_version:0:1} = "6" ]]; then 
        rpm -ivh https://yum.puppetlabs.com/puppetlabs-release-el-6.noarch.rpm
    fi
    if [[ ${platform_version:0:1} = "7" ]]; then
        rpm -ivh https://yum.puppetlabs.com/puppetlabs-release-el-7.noarch.rpm
    fi
    if [[ ${platform_version:0:1} = "5" ]]; then
       rpm -ivh https://yum.puppetlabs.com/puppetlabs-release-el-5.noarch.rpm
    fi
    yum -y install puppet
else
    apt-get -y install puppet
fi

## Configuring puppet

echo '[main]
rundir = /var/run/puppet
logdir = /var/log/puppet
ssldir = $vardir/ssl

[agent]
classfile = $vardir/classes.txt
server = "'${PUPPET_SERVER}'"
runinterval = 60
pluginsync = True
localconfig = $vardir/localconfig' > /etc/puppet/puppet.conf

if [ ${platform:0:6} == 'centos' ]; then
    puppet agent --enable
    service puppet restart
else
     echo 'START=yes
           DAEMON_OPTS=""' > /etc/default/puppet
     puppet agent --enable
     service puppet restart
fi

