.. _Top:
=============================
FIWARE PaaS Manager | Pegasus
=============================

| |Build Status| |Coverage Status| |StackOverflow|

.. contents:: :local:

Introduction
============

This is the code repository for FIWARE Pegasus, the reference implementation
of the PaaS Manager GE.

This project is part of FIWARE_. Check also the
`FIWARE Catalogue - PaaS Manager GE`_.

Any feedback on this documentation is highly welcome, including bugs, typos
or things you think should be included but aren't. You can use `FIWARE PaaS Manager - GitHub issues`_
to provide feedback.

For documentation previous to release 4.4.2 please check the manuals at FIWARE
public wiki:

- `FIWARE PaaS Manager - Installation and Administration Guide`_
- `FIWARE PaaS Manager - User and Programmers Guide`_

Top_


GEi overall description
=======================
The PaaS Manager GE provides a
new layer over the IaaS layer (Openstack) in the aim of easing the task of deploying applications on a Cloud infrastructure.
Therefore, it orchestrates the provisioning of the required virtual resources at IaaS level, and then, the installation and configuration
of the whole software stack of the application by the SDC GE ( (see `FIWARE SDC`_), taking into account the underlying virtual infrastructure.
It provides a flexible mechanism to perform the deployment, enabling multiple deployment architectures:
everything in a single VM or server, several VMs or servers, or elastic architectures based on load balancers and different software tiers.

Top_


Why to get it
=============

PaaS Manager GE  is the orchestration platform to be used in the
FIWARE Cloud ecosystem to deploy not just insfrastructure  but also software on top
of that.

-   **Full Openstack integrated solution**
    The PaaS Manager is fully integrated with the Opesntack services (nova for computation, neutron for networking and glance
    for image catalog.

-   **Asynchronous interface**

    Asynchronous interface with polling mechanism to obtain information about the deployment status.

-   **Decoupling the management  and provisioning**

    Decoupling the management of the catalogue (specifications of what can be deployed)
    and the management of the inventory (instances of what has been already deployed).
    In addition, decoupling the management of environments from the management of applications,
    since there could be uses cases where the users of those functionalities could be different ones.

Top_


Build and Install
=================

The recommended procedure is to install using RPM packages in CentOS 6.x as it is explained in
the `following document <doc/admin_guide.rst#install-paas-manager-from-rpm>`_
. If you are interested in building
from sources, check `this document <doc/admin_guide.rst#install-paas-manager-from-source#>`_.


Requirements
------------

- System resources: see `these recommendations
  <doc/admin_guide.rst#Resource availability>`_.
- Operating systems: CentOS (RedHat), being CentOS 6.5 the
  reference operating system.


Installation
------------

Using FIWARE package repository (recommended)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Refer to the documentation of your Linux distribution to set up the URL of the
repository where FIWARE packages are available (and update cache, if needed)

::

    http://repositories.testbed.fiware.org/repo/rpm/x86_64

Then, use the proper tool to install the packages

::

    $ sudo yum install fiware-paas

and the latest version will be installed. In order to install a specific version

::

    $ sudo yum install fiware-paas-{version}-1.noarch

where {version} being the specific version to be installed


Upgrading from a previous version
---------------------------------

Unless explicitly stated, no migration steps are required to upgrade to a
newer version of the PaaS Manager components:

- When using the package repositories, just follow the same directions
  described in the Installation_ section (the ``install`` subcommand also
  performs upgrades).
- When upgrading from downloaded package files, use ``rpm -U`` in CentOS

Upgrading database
~~~~~~~~~~~~~~~~~~
In case the database needs to be upgrade, the script db-changelog.sql should
be execute. To do that, it just needed to execute

::

    psql -U postgres -d $db_name << EOF
    \i db-changelog.sql

Using installation script
~~~~~~~~~~~~~~~~~~~~~~~~~
The installation of fiware-paas can be done in the easiest way by executing the script

::

  scripts/bootstrap/centos.sh

The script will ask you the following data to configure the configuration properties:

- The database name for the fiware-paas
- The postgres password of the database
- the keystone url to connect fiware-paas for the authentication process
- the admin keystone user for the authentication process
- the admin password for the authentication process

Top_


Running
=======

As explained in the `GEi overall description`_ section, there are a variety of
elements involved in the PaaS Manager architecture, apart from those components
provided by this PaaS Manager GE as the Software Deployment and Configuration and
OpenStack services. Please
refer to their respective documentation for instructions to run them.


In order to start the PaaS Manager service, as it is based on a
web application on top of jetty, just you should run

::

    $ service fiware-paas start

Then, to stop the service, run

::

    $ service fiware-paas stop

We can also force a service restart

::

    $ service fiware-paas restart


Configuration file
------------------

The configuration of PaaS Manager is in configuration_properties table in the database.
There, it is required to configure

::

    $ openstack-tcloud.keystone.url: This is the url where the keystone-proxy is deployed
    $ openstack-tcloud.keystone.user: the admin user
    $ openstack-tcloud.keystone.password: the admin password
    $ openstack-tcloud.keystone.tenant: the admin tenant
    $ paas_manager_url: the final url, mainly https://paas-ip:8443/paasmanager

In addition, to configue the PaaS Manager application inside the webserver, it is needed to change the context file.
To do that, change paasmanager.xml found in distribution file and store it in folder $PAASMANAGER_HOME/webapps/

::

  <New id="sdc" class="org.eclipse.jetty.plus.jndi.Resource">
    <Arg>jdbc/paasmanager</Arg>
    <Arg>
        <New class="org.postgresql.ds.PGSimpleDataSource">
            <Set name="User"> <database user> </Set>
            <Set name="Password"> <database password> </Set>
            <Set name="DatabaseName"> <database name>   </Set>
            <Set name="ServerName"> <IP/hostname> </Set>
            <Set name="PortNumber">5432</Set>
        </New>
    </Arg>
  </New>

Checking status
---------------

In order to check the status of the service, use the following command
(no special privileges required):

::

    $ service fiware-paas status

Top_


API Overview
============
The PaaS Manager offers a REST API, which can be used for both
managing deploying virtual infrastructure and install software
on top of it.

For instance, it is possible to obtain the template list in the catalogue

::

    $ curl -v -H "Content-Type: application/json" -H "Accept: application/xml"
    -H "X-Auth-Token: your-token-id" -H "Tenant-Id: your-tenant-id"
    -X GET "https://pegasus.lab.fi-ware.org:8443/paasmanager/rest/catalog/org/FIWARE/environment"

Please have a look at the API Reference Documentation section bellow and at the programmer guide.

API Reference Documentation
---------------------------

- `FIWARE PaaS Manager v1 (Apiary) <https://jsapi.apiary.io/apis/fiwarepaas/reference.html>`_

Top_


Testing
=======

Unit tests
----------

The ``test`` target for each module in the PaaS Manager is used for running the unit tests in both components of
PaaS Manager GE. To execute the unit tests you just need to execute

::

    mvn test

Please have a look at the section `building from source code
<doc/admin-guide.rst#install-paas-from-source>`_ in order to get more
information about how to prepare the environment to run the
unit tests.


Acceptance tests
----------------

In the following path you will find a set of tests related to the
end-to-end funtionalities.

- `PaaS Manager Aceptance Tests <https://github.com/telefonicaid/fiware-paas/tree/develop/test>`_

To execute the acceptance tests, go to the test/acceptance folder of the project and run

::

  lettuce_tools --tags=-skip.

This command will execute all acceptance tests (see available params with the -h option)

End to End testing
------------------
Although one End to End testing must be associated to the Integration Test, we can show
here a quick testing to check that everything is up and running. It involves to obtain
the product information storaged in the catalogue. With it, we test that the service
is running and the database configure correctly

::

   https://{PaaS Manager\_IP}:{port}/paasmanager/rest

The request to test it in the testbed should be

::

    curl -v -k -H 'Access-Control-Request-Method: GET' -H 'Content-Type: application xml'
    -H 'Accept: application/xml' -H 'X-Auth-Token: 5d035c3a29be41e0b7007383bdbbec57'
    -H 'Tenant-Id: 60b4125450fc4a109f50357894ba2e28'
    -X GET 'https://localhost:8443/paasmanager/rest/catalog/org/FIWARE/environment'

the option -k should be included in the case you have not changed the security configuration of PaaS Manager. The result should be the product catalog.

If you obtain a 401 as a response, please check the admin credentials and the connectivity from the PaaS Manager machine
to the keystone (openstack-tcloud.keystone.url in configuration_properties table)

Top_


Advanced topics
===============

- `Installation and administration <doc/installation-guide.rst>`_

  * `Software requirements <doc/admin-guide.rst#requirements>`_
  * `Building from sources <doc/admin-guide.rst/#install-paas-from-source>`_
  * `Resources & I/O Flows <doc/admin-guide.rst#resource-availability>`_

- `User and programmers guide <doc/user_guide.rst>`_

Top_


Support
=======

Ask your thorough programming questions using `stackoverflow`_ and your general questions on `FIWARE Q&A`_.
In both cases please use the tag `fiware-pegasus`

Top_


License
=======

\(c) 2013-2015 Telefónica I+D, Apache License 2.0

Top_


.. REFERENCES

.. _FIWARE: http://www.fiware.org
.. _FIWARE Catalogue - PaaS Manager GE: http://catalogue.fiware.org/enablers/paas-manager-pegasus
.. _FIWARE PaaS Manager - GitHub issues: https://github.com/telefonicaid/fiware-paas/issues/new
.. _FIWARE PaaS Manager - User and Programmers Guide: https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/PaaS_Manager_-_User_and_Programmers_Guide
.. _FIWARE PaaS Manager - Installation and Administration Guide: https://forge.fiware.org/plugins/mediawiki/wiki/fiware/index.php/PaaS_Manager_-_Installation_and_Administration_Guide
.. _FIWARE PaaS Manager - Apiary: https://jsapi.apiary.io/apis/fiwarepaas/reference.html
.. _FIWARE SDC: https://github.com/telefonicaid/fiware-sdc
.. _stackoverflow: http://stackoverflow.com/questions/ask
.. _`FIWARE Q&A`: https://ask.fiware.org


.. IMAGES

.. |Build Status| image::  https://travis-ci.org/telefonicaid/fiware-paas.svg
   :target: https://travis-ci.org/telefonicaid/fiware-paas
.. |Coverage Status| image:: https://coveralls.io/repos/telefonicaid/fiware-paas/badge.png?branch=develop
   :target: https://coveralls.io/r/telefonicaid/fiware-paas
.. |StackOverflow| image:: http://b.repl.ca/v1/help-stackoverflow-orange.png
   :target: https://stackoverflow.com/questions/tagged/fiware-pegasus
