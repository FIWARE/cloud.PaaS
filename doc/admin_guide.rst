PaaS Manager - Installation and Administration Guide
____________________________________________________


Introduction
============

This guide defines the procedure to install the different components that build
up the PaaS Manager GE, including its requirements and possible troubleshooting. The guide includes two different
ways of installing PaaS Manager: Installation from rpm or installation from source (building previously the rpm).

Requirements
============
In order to execute the PaaS Manager, it is needed to have previously installed the following software:
- PostgreSQL. 
You can find a small guide to install PostgresSQL in the next section. If you find some problems installing PostgreSQL,
please refer to the postgres official site.

PaaS Manager should be installed in a host with 2Gb RAM.

Installation from script
========================

The installation of fiware-paas can be done in the easiest way by executing the script

.. code ::

     scripts/bootstrap/centos.sh

that is in the github repository of the project.

In order to perform the installation via script, git should be installed (yum install git). 
Just clone the github repository:

.. code ::

     git clone https://github.com/telefonicaid/fiware-paas

and go to the folder

.. code ::

     cd fiware-paas/scripts/bootstrap

Assign the corresponding permissions to the script centos.sh and execute under root user

.. code ::

     ./centos.sh
     
The script will ask you the following data:
- The database name for the fiware-paas
- The postgres password of the database
- the keytone url to connect fiware-paas for the uthentication process
- the admin keystone user for the autentication process
- the admin password for the autentication process

Once the script is finished, you will have fiware-paas installed under /opt/fiware-paas/ . Please go to the Sanity Check
section in order to test the installation. This script does not insert the fiware-paas data into the keystone, so this
action has to be done manually. In order to complete the installation please refer to Configuring the PaaS Manager
in the kesytone section.

Manual Installation
===================

Requirements: Install PostgreSQL
--------------------------------
The first thing is to install and configure the requirements, in this case, the postgresql

 .. code::
 
   yum install postgresql postgresql-server postgresql-contrib

Type the following commands to install the postgresql as service and start it

.. code::

    chkconfig --add postgresql
    chkconfig postgresql on
    service postgresql initdb
    service postgresql start
    
  
Install PaaS Manager from RPM
-----------------------------
  
The PaaS Manager is packaged as RPM and stored in the rpm repository. Thus, the first thing to do is to create a file 
in /etc/yum.repos.d/fiware.repo, with the following content.

 .. code::
 
	[Fiware]
	name=FIWARE repository
	baseurl=http://repositories.testbed.fi-ware.eu/repo/rpm/x86_64/
	gpgcheck=0
	enabled=1
    
After that, you can install the PaaS Manager just doing:

.. code::

	yum install fiware-paas

or specifying the version

.. code::

	yum install fiware-paas-{version}-1.noarch

where {version} could 1.5.0

Install PaaS Manager from source
--------------------------------
Requirements: To install Paas Manager from source it is required to have the following software installed in your host
previously:

- git

- java 1.7

- maven

Here we include a small guide to install the required software. If you find any problem in the installation process,
please refer to the official site:

Install git

.. code::

   sudo yum install git

Install java 1.7

.. code::

   sudo yum install java-1.7.0-openjdk-devel

Install maven 2.5

.. code::

	sudo yum install wget
	wget http://mirrors.gigenet.com/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
	su -c "tar -zxvf apache-maven-3.2.5-bin.tar.gz -C /usr/local"
	cd /usr/local
	sudo ln -s apache-maven-3.2.5 maven

Add the following lines to the file /etc/profile.d/maven.sh

.. code::

	# Add the following lines to maven.sh
	export M2_HOME=/usr/local/maven
	export M2=$M2_HOME/bin
	PATH=$M2:$PATH

In order to check that your maven installation is OK, you shluld exit your current session with "exit" command, enter again
and type

.. code::

	mvn -version

if the system shows the current maven version installed in your host, you are ready to continue with this guide.

Now we are ready to build the PaaS Manager rpm and finally install it

The PaaS Manager is a maven application so, we should follow following instructions:

- Download PaaS Manager code from github

.. code::

   git clone -b develop https://github.com/telefonicaid/fiware-paas

- Go to fiware-paas folder and compile, launch test and build all modules

.. code::
	
    cd fiware-paas/
    mvn clean install
   
- Create a zip with distribution in target/paas-manager-server-dist.zip

.. code::

   mvn assembly:assembly -DskipTests

- You can generate a rpm o debian packages (using profiles in pom)   for debian/ubuntu:

.. code::

   mvn install -Pdebian -DskipTests
        (created target/paas-manager-server-XXXXX.deb)

- for centOS (you need to have installed rpm-bluid. If not, please type "yum install rpm-build" )

.. code::

    mvn install -Prpm -DskipTests
        (created target/rpm/paasmanager/RPMS/noarch/paasmanager-XXXX.noarch.rpm)

Finally go to the folder where the rpm has been created (target/rpm/fiware-paas/RPMS/noarch) and execute

.. code::

	cd target/rpm/fiware-paas/RPMS/noarch
	rpm -i <rpm-name>.rpm
	
Please, be aware  that the supported installation method is the RPM package. If you use other method, some extra steps may be required. For example you would need to generate manually the certificate (See the section about "Configuring the HTTPS certificate" for more information):

.. code::

   fiware-paas/bin/generateselfsigned.sh


Configuring the database
------------------------

We need to create the paasmanager database. To do that we need to connect as postgres user to the PostgreSQL
server and set the password for user postgres using alter user as below:

.. code::

    su - postgres
    postgres$ psql postgres postgres;
    psql (8.4.13)
    Type "help" for help.
    postgres=# alter user postgres with password 'postgres';
    postgres=# create database paasmanager;
    postgres=# grant all privileges on database paasmanager to postgres;
    postgres=#\q
    exit

Edit file /var/lib/pgsql/data/pg_hba.conf and set authentication method to md5:

.. code::

    # TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
      "local" is for Unix domain socket connections only
      local   all         all                               md5
      local   all         postgres                          md5
    # IPv4 local connections:
      host    all         all         0.0.0.0/0             md5
    
Edit file /var/lib/pgsql/data/postgresql.conf and set listen addresses to 0.0.0.0:

.. code::

     listen_addresses = '0.0.0.0'
    
Reload configuration

.. code::

     service postgresql reload
 
To create the tables in the databases, just go to 

.. code::

    su - potgres
    cd /opt/fiware-paas/resources
    postgres$ psql -U postgres -d paasmanager
    Password for user postgres: <postgres-password-previously-chosen>
    postgres=# \i db-initial.sql
    postgres=# \i db-changelog.sql
    exit

Update the following columns in the table configuration_properties:

..code::

	 openstack-tcloud.keystone.url=<keystone.url>
	 paas_manager_url=https://{ip}:8443/paasmanager/rest
	 openstack-tcloud.keystone.user= <keystone.user>
	 openstack-tcloud.keystone.pass= <keystone.password>
	 openstack-tcloud.keystone.tenant=<keystone.tenant>
	 user_data_path=/opt/fiware-paas/resources/userdata

where the values between bracket <> should be found out depending on the openstack installation.
The updates of the columns are done in the following way

.. code::

 	su - potgres
    postgres$ psql -U postgres -d paasmanager
    Password for user postgres: <postgres-password-previously-chosen>
    postgres=# UPDATE configuration_properties SET value='/opt/fiware-paas/resources/userdata' where key='user_data_path'; 
    postgres=# UPDATE configuration_properties SET value='<the value>' where key='paas_manager_url';
    postgres=# UPDATE configuration_properties SET value='<the value>' where key='openstack-tcloud.keystone.user';
    postgres=# UPDATE configuration_properties SET value='<the value>' where key='openstack-tcloud.keystone.pass';
    postgres=# UPDATE configuration_properties SET value='<the value>' where key='openstack-tcloud.keystone.tenant';
    
   
Configure PaaS Manager application
----------------------------------  

Once the prerequisites are satisfied, you shall modify the context file at  /opt/fiware-paas/webapps/paasmanager.xml 

See the snipet bellow to know how it works:

.. code::

    <New id="paasmanager" class="org.eclipse.jetty.plus.jndi.Resource">
       <Arg>jdbc/paasmanager</Arg>
       <Arg>
           <New class="org.postgresql.ds.PGSimpleDataSource">
               <Set name="User"> {database user} </Set>
               <Set name="Password"> {database password} </Set>
               <Set name="DatabaseName"> {database name}   </Set>
               <Set name="ServerName"> {IP database hostname - localhost default} </Set>
               <Set name="PortNumber"> {port database - 5432 default}</Set>
           </New>

       </Arg>
    </New>


Configuring the PaaS Manager as service 
---------------------------------------
Once we have installed and configured the PaaS Manager, the next step is to configure it as a service. To do that just create a file in /etc/init.d/fiware-paas
with the following content

.. code::

    #!/bin/bash
    # chkconfig: 2345 20 80
    # description: Description comes here....
    # Source function library.
    . /etc/init.d/functions
    start() {
        /opt/fiware-paas/bin/jetty.sh start
    }
    stop() {
        /opt/fiware-paas/bin/jetty.sh stop
    }
    case "$1" in 
        start)
            start
        ;;
        stop)
            stop
        ;;
        restart)
            stop
            start
        ;;
        status)
            /opt/fiware-paas/bin/jetty.sh status
        ;;
        *)
            echo "Usage: $0 {start|stop|status|restart}"
    esac
    exit 0 

Now you need to execute:

.. code::

    chkconfig --add fiware-paas
    chkconfig fiware-paas on
    service fiware-paas start

Configuring the HTTPS certificate
---------------------------------

The service is configured to use HTTPS to secure the communication between clients and the server. One central point in HTTPS security is the certificate which guarantee the server identity.

Quickest solution: using a self-signed certificate
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

The service works "out of the box" against passive attacks (e.g. a sniffer) because a self-signed certificated is generated automatically when the RPM is installed. Any certificate includes a special field call "CN" (Common name) with the identity of the host: the generated certificate uses as identity the IP of the host.

The IP used in the certificate should be the public IP (i.e. the floating IP). The script which generates the certificate knows the public IP asking to an Internet service (http://ifconfig.me/ip). Usually this obtains the floating IP of the server, but of course it wont work without a direct connection to Internet.

If you need to regenerate a self-signed certificate with a different IP address (or better, a convenient configured hostname), please run:

.. code::

    /opt/fiware-paas/bin/generateselfsigned.sh myhost.mydomain.org

By the way, the self-signed certificate is at /etc/keystorejetty. This file wont be overwritten although you reinstall the package. The same way, it wont be removed automatically if you uninstall de package.

Advanced solution: using certificates signed by a CA
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

Although a self-signed certificate works against passive attack, it is not enough by itself to prevent active attacks, 
specifically a "man in the middle attack" where an attacker try to impersonate the server. Indeed, any browser warns 
user against self-signed certificates. To avoid these problems, a certificate conveniently signed by a CA may be used.

If you need a certificate signed by a CA, the more cost effective and less intrusive practice when an organization has 
several services is to use a wildcard certificate, that is, a common certificate among all the servers of a DNS domain. 
Instead of using an IP or hostname in the CN, an expression as ".fiware.org " is used.

This solution implies:

* The service must have a DNS name in the domain specified in the wildcard certificate. For example, if the domain is ".fiware.org" a valid name may be "paasmanager.fiware.org".
* The clients should use this hostname instead of the IP
* The file /etc/keystorejetty must be replaced with another one generated from the wildcard certificate, the corresponding private key and other certificates signing the wild certificate.

It's possible that you already have a wild certificate securing your portal, but Apache server uses a different file format. A tool is provided to import a wildcard certificate, a private key and a chain of certificates, into /etc/keystorejetty:

.. code::

    # usually, on an Apache installation, the certificate files are at /etc/ssl/private
    /opt/fiware-paas/bin/importcert.sh key.pem cert.crt chain.crt

If you have a different configuration, for example your organization has got its own PKI, please refer to: http://docs.codehaus.org/display/JETTY/How%2bto%2bconfigure%2bSSL


Configuring the PaaS Manager in the keystone
--------------------------------------------
The FIWARE keystone is a endpoint catalogue which collects all the endpoint of the different services

Sanity check procedures
=======================

Sanity check procedures
-----------------------
The Sanity Check Procedures are the steps that a System Administrator will take to verify that an installation is ready to be tested. This is therefore a preliminary set of tests to ensure that obvious or basic malfunctioning is fixed before proceeding to unit tests, integration tests and user validation.

End to End testing
------------------
Although one End to End testing must be associated to the Integration Test, we can show here a quick testing to check that everything is up and running. It involves to obtain the product information storaged in the catalogue. With it, we test that the service is running and the database configure correctly.

.. code ::

    http://{PaaSManagerIP}:{port}/paasmanager/rest

The request to test it in the testbed should be

 .. code::

     curl -v -k -H 'Access-Control-Request-Method: GET' -H 'Content-Type: application xml' 
	 -H 'Accept: application/xml' -H 'X-Auth-Token: 5d035c3a29be41e0b7007383bdbbec57' 
	 -H 'Tenant-Id: 60b4125450fc4a109f50357894ba2e28' 
	 -X GET 'https://{PaaSManagerIP}:8443/paasmanager/rest/catalog/org/FIWARE/environment'

the option -k should be included in the case you have not changed the security configuration of PaaS Manager.

Whose result is the PaaS Manager API documentation.

List of Running Processes
-------------------------
Due to the PaaS Manager basically is running over the Tomcat, the list of processes must be only the Jetty and PostgreSQL. If we execute the following command:

.. code::

     ps -ewF | grep 'postgres\|jetty' | grep -v grep

It should show something similar to the following:

  .. code::

    postgres  1327     1  0 58141  9256   0 08:26 ?        00:00:00 /usr/bin/postgres -D /var/lib/pgsql/data -p 5432
	postgres  1328  1327  0 48078  1696   0 08:26 ?        00:00:00 postgres: logger process
	postgres  1330  1327  0 58166  3980   0 08:26 ?        00:00:00 postgres: checkpointer process
	postgres  1331  1327  0 58141  2068   0 08:26 ?        00:00:00 postgres: writer process
	postgres  1332  1327  0 58141  1808   0 08:26 ?        00:00:00 postgres: wal writer process
	postgres  1333  1327  0 58349  3172   0 08:26 ?        00:00:00 postgres: autovacuum launcher process
	postgres  1334  1327  0 48110  2052   0 08:26 ?        00:00:00 postgres: stats collector process
	root     14054     1  4 598402 811464 0 09:35 ?        00:00:22 java -Xmx1024m -Xms1024m -Djetty.state=/opt/fiware-paas/jetty.state -Djetty.home=/opt/fiware-paas -Djetty.base=/opt/fiware-paas -Djava.io.tmpdir=/tmp -jar /opt/fiware-paas/start.jar jetty-logging.xml jetty-started.xml
	postgres 14114  1327  0 58414  3956   0 09:36 ?        00:00:00 postgres: postgres paasmanager 127.0.0.1(48012) idle
	postgres 14117  1327  0 58449  3772   0 09:36 ?        00:00:00 postgres: postgres paasmanager 127.0.0.1(48013) idle
	postgres 14118  1327  0 58449  3776   0 09:36 ?        00:00:00 postgres: postgres paasmanager 127.0.0.1(48014) idle


Network interfaces Up & Open
----------------------------
Taking into account the results of the ps commands in the previous section, we take the PID in order to know the information about the network interfaces up & open. To check the ports in use and listening, execute the command:
  
.. code::

    netstat -p -a | grep $PID

Where $PID is the PID of Java process obtained at the ps command described before, in the previous case 14054 jetty and 1327 (postgresql). 
The expected results for the postgres process must be something like this output:

.. code::

	tcp6       0      0 [::]:pcsync-https       [::]:*                  LISTEN      14054/java
	tcp6       0      0 localhost:48017         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48015         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48027         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48016         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48022         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48023         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48029         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48013         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48012         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48019         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48028         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48014         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48020         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48024         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48031         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48021         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48018         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48026         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48030         localhost:postgres      ESTABLISHED 14054/java
	tcp6       0      0 localhost:48025         localhost:postgres      ESTABLISHED 14054/java
	unix  2      [ ]         STREAM     CONNECTED     71542    14054/java
	unix  3      [ ]         STREAM     CONNECTED     71480    14054/java

and the following output for the jetty process:

.. code::

	tcp        0      0 localhost:postgres      0.0.0.0:*               LISTEN      1327/postgres
	tcp6       0      0 localhost:postgres      [::]:*                  LISTEN      1327/postgres
	udp6       0      0 localhost:53966         localhost:53966         ESTABLISHED 1327/postgres
	unix  2      [ ACC ]     STREAM     LISTENING     19508    1327/postgres        /tmp/.s.PGSQL.5432
	unix  2      [ ACC ]     STREAM     LISTENING     19506    1327/postgres        /var/run/postgresql/.s.PGSQL.5432

Databases
---------
The last step in the sanity check, once that we have identified the processes and ports is to check the different databases that have to be up and accept queries. Fort he first one, if we execute the following commands:

.. code::

    psql -U postgres -d paasmanager

For obtaining the tables in the database, just use

.. code::

    paasmanager=# \dt

     Schema|                Name                     | Type  |  Owner

    ---------+---------------------------------------+-------+----------
    public  | applicationinstance                   | tabla | postgres
    public  | applicationrelease                    | tabla | postgres
    public  | applicationrelease_applicationrelease | tabla | postgres
    public  | applicationrelease_artifact           | tabla | postgres
    ...

Diagnosis Procedures
====================

The Diagnosis Procedures are the first steps that a System Administrator will take to locate the source of an error in a GE.
Once the nature of the error is identified with these tests, the system admin will very often have to resort to more
concrete and specific testing to pinpoint the exact point of error and a possible solution. Such specific testing is out of the scope of this section.


Resource availability
---------------------

The resource availability should be at least 1Gb of RAM and 6GB of Hard disk in order to prevent enabler's bad performance.
This means that bellow these thresholds the enabler is likely to experience problems or bad performance.

Resource consumption
--------------------

State the amount of resources that are abnormally high or low. This applies to RAM,
CPU and I/O. For this purpose we have differentiated between:

- Low usage, in which we check the resources that the Tomcat requires in order to load the PaaS Manager.
- High usage, in which we send 100 concurrent accesses to the PaaS Manager.


The results were obtained with a top command execution over the following machine configuration:

     |       Name          | Type                |
     ----------------------+----------------------
     |   Type Machine      |   Virtual Machine   |
     |   CPU 	           |   1 core @ 2,4Ghz   |
     |   RAM 	           |   1,4GB             |
     |   HDD 	           |   9,25GB            |
     |   Operating System  |   CentOS 6.3        |



The results of requirements both RAM, CPU and I/O to HDD is shown in the following table:

     | Resource Consumption   | Low UsageType     | High Usage       |
     -------------------------+---------------------------------------
     |   RAM                  | 1GB ~ 63%         | 3GB ~ 78%        |
     |   CPU 	              | 0,8% of a 2400MHz | 90% of a 2400MHZ |
     |   I/O HDD 	          |   6GB            | 6GB


