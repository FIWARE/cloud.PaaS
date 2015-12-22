PaaS Manager - User and Programmers Guide
______________________________________________

Introduction
============

Welcome the User and Programmer Guide for the PaaS Manager GE. 
This generic enabler is built on a proprietary solution using standard 
interface to communicate with and so where possible this guide points to 
the appropriate online content that has been created for this specific API. 
The online documents are being continuously updated and improved, and so 
will be the most appropriate place to get the most up to date information on using this interface.



Accessing PaaS Manager from the CLI 
===================================

The access through the CLI is made using the curl program. Curl [http://curl.haxx.se/] is a client to get documents/files from or send documents to a server, using any of the supported protocols (HTTP, HTTPS, FTP, GOPHER, DICT, TELNET, LDAP or FILE) and therefore is also usable for OpenStack Compute API. Use the curl command line tool or use libcurl from within your own programs in C. Curl is free and open software that compiles and runs under a wide variety of operating systems.

The normal operations sequence to deploying an environment and an application on top of it could be summarized in the following list:


API Authentication
------------------

All the operations in the PaaS Manager API needs to have a valid token to access it. To obtain the token, you need to have an account in FIWARE Lab (account.lab.fi-ware.org).
With the credentials (username, password and tenantName) you can obtain a valid token. From now on, we asume that the value of your tenant-id is "your-tenant-id"

Source code::

    $ curl -v -H "Content-Type: application/json" -H "Accept: application/json" -X
    POST "http://cloud.lab.fi-ware.org:4731/v2.0/tokens" -d '{"auth":{"tenantName":
    "your-tenant-id","passwordCredentials":{"username":"youruser",
    "password":"yourpassword"}}}'

You will receive the following answer, with a valid token (id).

Source code::

    {
    access: {
       token: {
          expires: "2015-07-09T15:16:07Z"
          id: "756cfb31e062216544215f54447e2716"
          tenant: {
          ...
    }
	
For all the PaaS manager request, you will need to include the following header:

Source code::

    X-Auth-Token: 756cfb31e062216544215f54447e2716
    Tenant-Id: your-tenant-id

For the rest of the explanation, we are going to configure a set of variables:

Source code::

    export PAAS_MANAGER_IP =  pegasus.lab.fi-ware.org

Abstract Environment API
------------------------

Next we detail some operations that can be done in the catalogue managemente api regarding the Abstract Environments.
Abstract Environments are environments defined by the administrator. They are available for all FIWARE users.


**Get the Abstract Environment list from the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment"

This operation lists the abstract environments stored in the catalogue. The following example shows an XML response for the list Abstract Environment API operation.
	
.. code:: xml

    <environmentDtoes>
        <environmentDto>
 			<tierDtos>
				<name>orion</name>
				<flavour>2</flavour>
				<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
				<maximumNumberInstances>1</maximumNumberInstances>
				<minimumNumberInstances>1</minimumNumberInstances>
				<initialNumberInstances>1</initialNumberInstances>
 				<productReleaseDtos>
					<productName>orion</productName>
					<version>0.13.0</version>
 				</productReleaseDtos>
				<icono />
				<securityGroup />
				<keypair />
				<floatingip>false</floatingip>
				<affinity>None</affinity>
				<region>Spain</region>
 			</tierDtos>
 			<name>orion</name>
 			<description>Environment orion</description>
       </environmentDto>
       ...
    </environmentDtoes>

**Get a particular Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}"

This operation lists the abstract environments stored in the catalogue. The following example shows an XML response for the list Abstract Environment API operation.
	
.. code:: xml

    <environmentDtoes>
        <environmentDto>
 			<tierDtos>
				<name>{abstract-environment-name}</name>
				<flavour>2</flavour>
				<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
				<maximumNumberInstances>1</maximumNumberInstances>
				<minimumNumberInstances>1</minimumNumberInstances>
				<initialNumberInstances>1</initialNumberInstances>
 				<productReleaseDtos>
					<productName>orion</productName>
					<version>0.13.0</version>
 				</productReleaseDtos>
				<icono />
				<securityGroup />
				<keypair />
				<floatingip>false</floatingip>
				<affinity>None</affinity>
				<region>Spain</region>
 			</tierDtos>
 			<name>orion</name>
 			<description>Environment orion</description>
        </environmentDto>
    </environmentDtoes>

**Add an Abstract Environment to the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X POST "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment"

with the following payload

.. code:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <environmentDto>
    	<name>{abstract-environment-name}</name>
    	<description>description</description>
    </environmentDto> 

**Delete an abstract template for the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X DELETE "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}"

Abstract Tier API
-----------------

**Add an Tier to an existing Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X POST "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}/tier"

with the following payload

.. code:: xml

	<tierDto>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
		<maximumNumberInstances>1</maximumNumberInstances>
		<name>{tier-name}</name>
		<image>0dbf8aff-5dc5-4d6c-9f9c-1e6801e0b629</image>
		<flavour>2</flavour>
		<keypair>jesusmmovilla57</keypair>
		<floatingip>false</floatingip>
		<region>Trento</region>
	</tierDto> 

**Get All Tiers associated to a Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}/tier"

This operation obtains a response with the following format:

.. code:: xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
	<tierDtoes>
 		<tierDto>
			<name>{tier-name}</name>
			<flavour>2</flavour>
			<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
			<maximumNumberInstances>3</maximumNumberInstances>
			<minimumNumberInstances>1</minimumNumberInstances>
			<initialNumberInstances>1</initialNumberInstances>
 			<productReleaseDtos>
				<productName>mongodbshard</productName>
				<productDescription>mongodb shard 2.2.3</productDescription>
				<version>2.2.3</version>
 			</productReleaseDtos>
			<icono>
			   http://blog.theinit.com/wp-content/uploads/2012/03/bc358_MongoDB.png
			</icono>
			<securityGroup />
			<keypair />
			<floatingip>false</floatingip>
			<affinity>None</affinity>
			<region>Spain</region>
 		</tierDto>
	</tierDtoes>

**Get a particular Tier associated to a Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}/tier/{tier-name}"

This operation obtains a response with the following format:

.. code:: xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
 	<tierDto>
		<name>{tier-name}</name>
		<flavour>2</flavour>
		<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
		<maximumNumberInstances>3</maximumNumberInstances>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
 		<productReleaseDtos>
			<productName>mongodbshard</productName>
			<productDescription>mongodb shard 2.2.3</productDescription>
			<version>2.2.3</version>
 		</productReleaseDtos>
		<icono>
		  http://blog.theinit.com/wp-content/uploads/2012/03/bc358_MongoDB.png
        </icono>
		<securityGroup />
		<keypair />
		<floatingip>false</floatingip>
		<affinity>None</affinity>
		<region>Spain</region>
 	</tierDto>


**Update a Tier of an existing Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X PUT "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}/tier"

with the following payload

.. code:: xml

	<tierDto>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
		<maximumNumberInstances>1</maximumNumberInstances>
		<name>{tier-name}</name>
		<image>0dbf8aff-5dc5-4d6c-9f9c-1e6801e0b629</image>
		<flavour>2</flavour>
		<keypair>jesusmmovilla57</keypair>
		<floatingip>false</floatingip>
		<region>Spain</region>
	</tierDto> 


**Delete a particular Tier associated to a Abstract Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/environment
    /{abstract-environment-name}/tier/{tier-name}"


Blueprint Template/Environment API
----------------------------------

Next we detail some operations that can be done in the catalogue managemente api

**Get the blueprint template list from the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment"

This operation lists the environments stored in the catalogue. The following example shows an XML response for the list Environment API operation. It is possible to see it contains a list of tiers including products to be installed.
	
.. code:: xml

    <environmentDtoes>
        <environment>
            <name>{emvironment-name}</name>
            <tiers>
                <tier>
                    <initial_number_instances>1</initial_number_instances>
                    <maximum_number_instances>1</maximum_number_instances>
                    <minimum_number_instances>1</minimum_number_instances>
                    <name>{tier-id}</name>
                    <networkDto>
                    	<networkName>Internet</networkName>
                    	<subNetworkDto>
                    		<subnetName>sub-net-Internet</subnetName>
                    	</subNetworkDto>
                    </networkDto>
                    <productReleases>                  
                        <product>postgresql</product>
                        <version>0.0.3</version>
                        <withArtifact>true</withArtifact> 
                        <productType> 
                            <id>5</id>
                            <name>Database</name>  
                        </productType> 
                    </productReleases>
                    ...
               </tier>   
           </tiers>
       </environment>
       <environment>
           <name>{emvironment-name}</name>
           <tiers>
               <tier>
               ...
               </tier>
           </tiers>
       </environment>
    </environmentDtoes>


**Add a blueprint template to the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X POST "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment"

with the following payload

.. code:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <environmentDto>
        <name>{environment-name}</name>
        <description>{description of environment}</description>
        <tierDtos>
    	    <minimumNumberInstances>1</minimumNumberInstances>
    	    <initialNumberInstances>1</initialNumberInstances>
    	    <maximumNumberInstances>1</maximumNumberInstances>
    	    <name>{tier-name}</name>
    	    <networkDto>
                <networkName>{network-name}</networkName>
                    <subNetworkDto>
                	    <subnetName>{subnetwork-name}</subnetName>
                    </subNetworkDto>
            </networkDto> 
            <image>{image-id}</image>
            <flavour>{flavour of VM in number}</flavour>
            <keypair>{keypair-name}</keypair>
            <floatingip>{false/true}</floatingip>
            <region>{region-name}</region>
            <productReleaseDtos>
    		    <productName>{product-name}</productName>
    		    <version>{product-version}</version>
            </productReleaseDtos>
        </tierDtos>
    </environmentDto>

The network and region information are including also in the payload of the environment. The following lines show a example. 

.. code:: xml

    <tierDtos>
        ...
        <name>{tier-name}</name>
    	    <networkDto>
                <networkName>{network-name}</networkName>
                    <subNetworkDto>
                	    <subnetName>{subnetwork-name}</subnetName>
                    </subNetworkDto>
            </networkDto> 
    	    <image>{image-id}</image>
    	    <flavour>{flavour of VM in number}</flavour>
    	    <keypair>{keypair-name}</keypair>
    	    <floatingip>{false/true}</floatingip>
    	    <region>{region-name}</region>
    	    <productReleaseDtos>
    		    <productName>{product-name}</productName>
    		    <version>{product-version}</version>
            </productReleaseDtos> 
            ...           
    </tierDtos>  

**Delete a blueprint template from the catalogue**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X DELETE "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-id}"


Tier API
--------

**Add a Tier to an existing Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X POST "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-name}/tier"

with the following payload

.. code:: xml

	<tierDto>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
		<maximumNumberInstances>1</maximumNumberInstances>
		<networkDto>
			<networkName>Internet</networkName>
			<subNetworkDto>
				<subnetName>sub-net-Internet</subnetName>
			</subNetworkDto>
		</networkDto>
		<name>{tier-name}</name>
		<image>0dbf8aff-5dc5-4d6c-9f9c-1e6801e0b629</image>
		<flavour>2</flavour>
		<keypair>jesusmmovilla57</keypair>
		<floatingip>false</floatingip>
		<region>Trento</region>
	</tierDto> 

**Get All Tiers associated to an Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-name}/tier"

This operation obtains a response with the following format:

.. code:: xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
	<tierDtoes>
 		<tierDto>
			<name>{tier-name}</name>
			<flavour>2</flavour>
			<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
			<maximumNumberInstances>3</maximumNumberInstances>
			<minimumNumberInstances>1</minimumNumberInstances>
			<initialNumberInstances>1</initialNumberInstances>
 			<networkDto>
				<networkName>Internet</networkName>
			 	<subNetworkDto>
					<subnetName>sub-net-Internet</subnetName>
				</subNetworkDto>
			</networkDto>
 			<productReleaseDtos>
				<productName>mongodbshard</productName>
				<productDescription>mongodb shard 2.2.3</productDescription>
				<version>2.2.3</version>
 			</productReleaseDtos>
			<icono>
			    http://blog.theinit.com/wp-content/uploads/2012/03/bc358_MongoDB.png
			</icono>
			<securityGroup />
			<keypair />
			<floatingip>false</floatingip>
			<affinity>None</affinity>
			<region>Spain</region>
 		</tierDto>
	</tierDtoes>

**Get a particular Tier associated to an Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-name}/tier/{tier-name}"

This operation obtains a response with the following format:

.. code:: xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
 	<tierDto>
		<name>{tier-name}</name>
		<flavour>2</flavour>
		<image>dbefb2d6-2221-46e2-a11c-b466e2503da5</image>
		<maximumNumberInstances>3</maximumNumberInstances>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
 		<networkDto>
			<networkName>Internet</networkName>
			<subNetworkDto>
				<subnetName>sub-net-Internet</subnetName>
			</subNetworkDto>
		</networkDto>
 		<productReleaseDtos>
			<productName>mongodbshard</productName>
			<productDescription>mongodb shard 2.2.3</productDescription>
			<version>2.2.3</version>
 		</productReleaseDtos>
		<icono>
		    http://blog.theinit.com/wp-content/uploads/2012/03/bc358_MongoDB.png
		</icono>
		<securityGroup />
		<keypair />
		<floatingip>false</floatingip>
		<affinity>None</affinity>
		<region>Spain</region>
 	</tierDto>


**Update a Tier of an existing Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X PUT "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-name}/tier"

with the following payload

.. code:: xml

	<tierDto>
		<minimumNumberInstances>1</minimumNumberInstances>
		<initialNumberInstances>1</initialNumberInstances>
		<maximumNumberInstances>1</maximumNumberInstances>
		<name>{tier-name}</name>
		<networkDto>
			<networkName>Internet</networkName>
			<subNetworkDto>
				<subnetName>sub-net-Internet</subnetName>
			</subNetworkDto>
		</networkDto>
		<image>0dbf8aff-5dc5-4d6c-9f9c-1e6801e0b629</image>
		<flavour>2</flavour>
		<keypair>jesusmmovilla57</keypair>
		<floatingip>false</floatingip>
		<region>Spain</region>
	</tierDto> 


**Delete a particular Tier associated to an Environment**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /{your-tenant-id}/environment/{environment-name}/tier/{tier-name}"

BluePrint/Environment Instance Provisioning API
-----------------------------------------------

**Deploy a Blueprint Instance**

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id" 
    -X POST "https://PAAS_MANAGER_IP:8443/paasmanager/rest/envInst/org/FIWARE/vdc
    /{your-tenant-id}/environmentInstance"

where "your-tenant-id" is the tenant-id in this guide. The payload of this request can be as follows:

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <environmentInstanceDto>
	    <blueprintName>{environmentinstance-name}</blueprintName>
	    <description>{description of environmentinstance}</description>
	    <environmentDto>
		    <name>{environment-name}</name>
		    <description>{description of environmet}</description>
		    <tierDtos>
			    <name>{tier-name}</name>
			    <flavour>{flavour of the VM}</flavour>
			    <image>{image-id of the image to create the VM}</image>
			    <maximumNumberInstances>1</maximumNumberInstances>
			    <minimumNumberInstances>1</minimumNumberInstances>
			    <initialNumberInstances>1</initialNumberInstances>
			    <networkDto>
				    <networkName>{network-name}</networkName>
			    </networkDto>
			    <icono></icono>
			    <securityGroup>{security-group-name}</securityGroup>
			    <keypair>{keypair-name}</keypair>
			    <floatingip>{true/false}</floatingip>
			    <affinity>None</affinity>
			    <region>{region-name where to deploy}</region>
		    </tierDtos>
	    </environmentDto>
    </environmentInstanceDto>
    
The response obatined should be:

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <task href="https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /your-tenant-id/task/{task-id}" startTime="2012-11-08T09:13:18.311+01:00"
    status="RUNNING">
        <description>Deploy environment {environment-name}</description>
        <vdc>your-tenant-id</vdc>
    </task>

Given the URL obtained in the href in the Task, it is possible to monitor the operation status (you can check Task Management). Once the environment has been deployed, 
the task status should be SUCCESS. 

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <task href="https://PAAS_MANAGER_IP:8443/paasmanager/rest/catalog/org/FIWARE/vdc
    /your-tenant-id/task/{task-id}" startTime="2012-11-08T09:13:19.567+01:00"
    status="SUCCESS">
        <description>Deploy environment {environment-name}</description>
        <vdc>your-tenant-id</vdc>
    </task>


**Get information about Blueprint Instances deployed**	

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id" 
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/envInst/org/FIWARE/vdc
    /your-tenant-id/environmentInstance"

The Response obtained includes all the blueprint instances deployed

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <environmentInstanceDtoes>
        <environmentInstance>
            <environmentInstanceName>{environmentInstance-id</environmentInstanceName>
            <vdc>your-tenant-id</vdc>
            <environment>
                <name>{environment-name}</name>
                <tiers>
                    <tier>
                    <initial_number_instances>1</initial_number_instances>
                    <maximum_number_instances>1</maximum_number_instances>
                    <minimum_number_instances>1</minimum_number_instances>
                    <name>{tier-id}</name>               
                    <productReleases>                  
                        <product>postgresql</product>
                        <version>0.0.3</version>
                        <withArtifact>true</withArtifact> 
                        <productType> 
                            <id>5</id>
                            <name>Database</name>  
                        </productType> 
                    </productReleases>                     ...
                    </tier>   
                </tiers>
            </environment>        
            <tierInstances>
                <id>35</id>
                <date>2012-10-31T09:24:45.298Z</date>  
                <name>tomcat-</name>       
                <status>INSTALLED</status>       
                <vdc>your-tenant-id</vdc>       
                <tier>
                    <name>{tier-id}</name>               
                </tier>   
                <productInstances>
                    <id>33</id>   
                    <date>2012-10-31T09:14:33.192Z</date>  
                    <name>postgresql</name>         
                    <status>INSTALLED</status>    
                    <vdc>your-tenant-id</vdc>  
                    <productRelease>  
                        <product>postgresql</product>  
                        <version>0.0.3</version> 
                    </productRelase>
                    <vm>
                        <fqn>vmfqn</fqn> 
                        <hostname>rehos456544</hostname> 
                        <ip>109.231.70.77</ip> 
                   </vm>
           </tierInstances>
       </environmentInstance>
    </environmentInstanceDtoes>

**Get details of a certain Blueprint Instance**	

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id" 
    -X GET "https://PAAS_MANAGER_IP:8443/paasmanager/rest/envInst/org/FIWARE/vdc
    /your-tenant-id/environmentInstance/{BlueprintInstance-id}"
	
This operation does not require any payload in the request and provides a BlueprintInstance XML response. 

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <environmentInstancePDto>
        <environmentInstanceName>{environmentinstance-name}</environmentInstanceName>
        <vdc>{tenant-id}</vdc>
        <description>{description of environmentinstance}</description>
        <status>{status of the environment installation}</status>
        <blueprintName>{blueprint-name}</blueprintName>
        <taskId>{task-id of the execution}</taskId>
        <tierDto>
            <name>{tier-name}</name>
            <flavour>{flavour of the vm}</flavour>
            <image>{image-id}</image>
            <maximumNumberInstances>1</maximumNumberInstances>
            <minimumNumberInstances>1</minimumNumberInstances>
            <initialNumberInstances>1</initialNumberInstances>
 			<productReleaseDtos>
				<productName>{product-name}</productName>
				<version>{product-version}</version>
 			</productReleaseDtos>
 			<icono />
 			<securityGroup>{securityGroup-name}</securityGroup>
 			<keypair>{keypair-name}</keypair>
 			<floatingip>{true/false}</floatingip>
 			<region>{region-name}</region>
 			<tierInstancePDto>
				<tierInstanceName>{tierinstance-name}</tierInstanceName>
				<status>{status of the tierinstallation}</status>
				<taskId>{task id of tier installation execution}</taskId>
 				<productInstanceDtos>
 					<productReleaseDto>
						<productName>{product-name}</productName>
						<version>{product-version}</version>
 					</productReleaseDto>
					<name>{productInstance-name}</name>
					<taskId>{task id of product installation}</taskId>
 				</productInstanceDtos>
 				<vm>
					<domain>{domain of vm}</domain>
					<fqn>{fqn of vm}</fqn>
					<hostname>{hostname}</hostname>
					<ip>{ip}</ip>
					<id>{nova-host-id}</id>
 				</vm>
 			</tierInstancePDto>
        </tierDto>
    </environmentInstancePDto>
 
**Undeploy a Blueprint Instance**	

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id" 
    -X DELETE "https://PAAS_MANAGER_IP:8443/paasmanager/rest/envInst/org/FIWARE/vdc
    /{your-tenant-id}/environmentInstance/{BlueprintInstance-id}"

This operation does not require a request body and returns the details of a generated task. 

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <task href="https://PAAS_MANAGER_IP:8443/paasmanager/rest/vdc/{your-tenant-id}
    /task/{task-id}" startTime="2012-11-08T09:45:44.020+01:00" status="RUNNING">
        <description>Uninstall environment</description>
        <vdc>your-tenant-id</vdc>
    </task>

With the URL obtained in the href in the Task, it is possible to monitor the operation status (you can checkTask Management). Once the environment has been undeployed, the task status should be SUCCESS. 

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <task href="https://PAAS_MANAGER_IP:8443/paasmanager/rest/vdc/{your-tenant-id}
    /task/{task-id}" startTime="2012-11-08T09:13:19.567+01:00" status="SUCCESS">
        <description>Undeploy environment {emvironment-name}</description>
        <vdc>your-tenant-id</vdc>
    </task>

Task Management
--------------- 

**Get a specific task**	

Source code::

    $ curl -v -H "Content-Type: application/xml" -H "Accept: application/xml" -H
    "X-Auth-Token: 756cfb31e062216544215f54447e2716" -H "Tenant-Id: your-tenant-id"
    -X DELETE "http://pegasus.lab.fi-ware.org:8080/paasmanager/rest/vdc
    /your-tenant-id/task/{task-id}"
	
This operation recovers the status of a task created previously. It does not need any request body and the response body in XML would be the following. 

.. code:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <task
        href="http:/130.206.80.112:8080/sdc/rest/vdc/{your-tenant-id}/task/{task-id}"
        startTime="2012-11-08T09:13:18.311+01:00" status="SUCCESS">
        <description>Install product tomcat in  VM rhel-5200ee66c6</description>
        <vdc>your-tenant-id</vdc>
    </task>


The value of the status attribute could be one of the following: 

=========  ====================================
Value      Description 
=========  ====================================
QUEUED     The task is queued for execution.   
PENDING    The task is pending for approval.   
RUNNING    The task is currently running.      
SUCCESS    The task is completed successfully.  
ERROR      The task is finished but it failed.  
CANCELLED  The task has been cancelled by user.  
=========  ====================================
