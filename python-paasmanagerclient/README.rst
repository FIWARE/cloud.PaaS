FIWARE PaaSManager Python Client
================================

This is a client for Pegasus PaaSManager API. This API client has been developed in Python_. This client uses
the OpenStack Keystone service for authorization and service endpoint management.

Environment
-----------

**Prerequisites**

- `Python 2.7`__ or newer
- pip_ 6.0 or newer
- Additional libs that are required before installing dependencies: Python Development Tools (python-devel),
zlib-devel, bzip2-devel, openssl-devel, ncurses-devel, sqlite-devel, gcc
- PaaSManager_
- `OpenStack Keystone service`_ v2 (so far, only Keystone v2 is supported for this client)

__ `Python - Downloads`_


**Installation**

All dependencies has been defined in ``requirements.txt``.
To install the last version of this client, download it from the GIT PaaSManager repository (*master* branch)
and install it, using following command:

::

    pip install -e "-e git+https://github.com/telefonicaid/fiware-paas.git@master#egg=python-paasmanagerclient&subdirectory=python-paasmanagerclient"


Developed operations
---------------------

Following operations are already implemented:

**Environment API Resource**

- Create new environment
- Delete environment


Python API
----------

An example of use of this client is:

::

    from paasmanager_client.client import PaaSManagerClient
    paasmanager_client = PaaSManagerClient(tenant_id, username, password, region_name, auth_url)

    env_name = "QAEnv"
    response = paasmanager_client.getEnvironmentResourceClient().create_environment(env_name,
                                                                                    "For testing purposes")
    assertTrue(response.ok, "ERROR creating environment {}. Response: {}".format(env_name, str(response.content)))


.. REFERENCES

.. _Python: http://www.python.org/
.. _Python - Downloads: https://www.python.org/downloads/
.. _pip: https://pypi.python.org/pypi/pip
.. _PaaSMaager: https://github.com/telefonicaid/fiware-paas
.. _`OpenStack Keystone service`: http://docs.openstack.org/developer/keystone/
