FIWARE Recipes Checking Python Script
=====================================

This is a script to check the execution of all the products (recipes) that are in the SDC Catalog by building
Environments and deploying Environment Instances through the PaaSManager API. This script has been developed in
Python_. This script uses FIWARE PaasManager and SDC Python Client.

Environment
-----------

**Prerequisites**

- `Python 2.7`__ or newer
- pip_ 6.0 or newer
- PaaSManager_
- SDC_
- `OpenStack Keystone service`_ v2 (so far, only Keystone v2 is supported for this client)

__ `Python - Downloads`_


**Installation**

All dependencies has been defined in ``requirements.txt``.
To install the last version of this client, download/clone it from the GIT PaaSManager repository (*master* branch)
and go to scripts/recipes/checking directory:


How to use it
-------------

An example of use of this client is:

::

    sudo python recipes_checking.py -u <user> -p <paasword> -t <tenant-id> -f <report_file_path> -e <environment-name>


.. REFERENCES

.. _Python: http://www.python.org/
.. _Python - Downloads: https://www.python.org/downloads/
.. _pip: https://pypi.python.org/pypi/pip
.. _PaaSManager: https://github.com/telefonicaid/fiware-paas
.. _SDC: https://github.com/telefonicaid/fiware-sdc
.. _`OpenStack Keystone service`: http://docs.openstack.org/developer/keystone/
