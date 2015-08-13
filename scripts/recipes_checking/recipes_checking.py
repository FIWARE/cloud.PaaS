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
__author__ = 'jesus.movilla'

import sys
import argparse
import xmltodict
import time
import requests
import datetime

from sdcclient.client import SDCClient
from paasmanagerclient.client import PaaSManagerClient
from utils.logger_utils import get_logger

logger = get_logger(__name__)

# HEADERS
X_AUTH_TOKEN = "X-Auth-Token"
TENANT_ID = "Tenant-Id"
ACCEPT = "Accept"
APPLICATION_JSON = "application/json"

#HTTP STATUS CODE
HTTPS_PROTOCOL ="https"
HTTP_STATUSCODE_NO_CONTENT = 204
HTTP_STATUSCODE_OK = 200

# GLANCE SERVICE
GLANCE_SERVICE_TYPE = "glance"
GLANCE_ENDPOINT_TYPE = "publicURL"
GLANCE_REQUEST_IMAGES_SDC_AWARE_URL = '/images/detail?property-sdc_aware=true&&type=fiware:utils'

# TASK BODY ELEMENTS
TASK_BODY_ROOT = "task"
TASK_BODY_HREF = "href"
TASK_BODY_STATUS = "status"
TASK_BODY_ERROR = "error"
TASK_BODY_ERROR_MAJORCODE="majorErrorCode"
TASK_BODY_ERROR_MESSAGE="message"
TASK_BODY_ERROR_MINORCODE="minorErrorCode"

#TASK STATUS
TASK_STATUS_ERROR = "ERROR"
TASK_STATUS_SUCCESS = "SUCCESS"
TASK_STATUS_RUNNING = "RUNNING"

#PRODUCTANRELEASE BODY ELEMENTS
PRODUCTANDRELEASE_BODY_ROOT = "productAndReleaseDto";
PRODUCTANDRELEASE_BODY_PRODUCT = "product";
PRODUCTANDRELEASE_BODY_PRODUCTNAME = "name";
PRODUCTANDRELEASE_BODY_PRODUCTVERSION = "version";
PRODUCTANDRELEASE_BODY_METADATAS = "metadatas";
PRODUCTANDRELEASE_BODY_METADATA_KEY = "key";
PRODUCTANDRELEASE_BODY_METADATA_VALUE = "value";
PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR = "installator";
PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR_CHEF_VALUE = "chef";
PRODUCTANDRELEASE_BODY_METADATA_IMAGE = "image";

TIME_INTERVAL_TO_DEPLOY = 60
TIME_INTERVAL_TO_DELETE = 45

def main(argv=None):
    print("Inside main")
    """
    Getting parameters
    :param argv:
    """
    parser = argparse.ArgumentParser(description='Testing product installation using paasmanager')
    parser.add_argument("-u", "--username", help='valid username', required=True)
    parser.add_argument("-p", "--password", help='valid password', required=True)
    parser.add_argument("-r", "--region_name", dest='region_name', default='Spain2', help='the name of region')
    parser.add_argument("-k", "--auth_url", dest="auth_url", default='http://cloud.lab.fiware.org:4731/v2.0',
        help='url to keystone <host or ip>:<port>')
    parser.add_argument("-t", "--tenant", dest="tenantid", help="tenant-id", default="00000000000000000000000000000001",
        required=False)
    parser.add_argument("-e", "--envName", dest='envName', default='EnvName', help='valid environment name')
    parser.add_argument("-f", "--reportfile", dest='reportfile', default='/var/log/recipes_checking_report.log',
        help='Name of the Report File')


    args = parser.parse_args()
    logger.info(args)
    print args

    # This is the file where to find the report about the tests of BlueprintInstance installation with
    # all the recipes available in the Chef-Server
    report_file = open(args.reportfile, 'w')

    check_recipes (report_file, envName= args.envName,
                     auth_url=args.auth_url,
                     tenant_id=args.tenantid,
                     user=args.username,
                     password=args.password,
                     region_name=args.region_name)

def check_recipes(report_file, envName, auth_url, tenant_id, user, password, region_name):

    report_file.writelines("========================================================================================\n")
    report_file.write("Platform: " +  auth_url + ". Region: " + region_name + ". Username: " + user
                      + " Tenant-ID: " + tenant_id + "\n")
    report_file.writelines("========================================================================================\n")

    logger.info("SDC call to get the list of products available in catalog")
    print("SDC call to get the list of products available in catalog")

    #Llamar al SDC para sacar la lista de productos.
    sdc_client = SDCClient(user, password, tenant_id, auth_url, region_name)
    productandrelease_client = sdc_client.getProductAndReleaseResourceClient()
    allproductreleases = productandrelease_client.get_productandrelease()

    print str(allproductreleases['productAndReleaseDto'])
    logger.debug(str(allproductreleases['productAndReleaseDto']))
    print "There are " + str(len(allproductreleases['productAndReleaseDto'])) + " product Releases in SDC"
    logger.info("There are " + str(len(allproductreleases['productAndReleaseDto'])) + " product Releases in SDC")

    paasmanager_client = PaaSManagerClient(user, password, tenant_id, auth_url, region_name )
    glance_url = paasmanager_client.get_paasmanager_endpoint_from_keystone(region_name, GLANCE_SERVICE_TYPE,
                                                                           GLANCE_ENDPOINT_TYPE)
    logger.info("Loading image list from glance : " + glance_url + " Region: " + region_name)
    print("Loading image list from glance : " + glance_url + " Region: " + region_name)

    response_images = find_all_images_sdc_aware(glance_url, region_name, paasmanager_client.token, tenant_id)
    print (response_images)
    logger.debug(response_images)

    images = []
    for i in response_images['images']:
        object = []
        object.append(i['id'])
        object.append(i['name'])
        print "Image id: " + i['id']+ "| Image name: " + i['name']
        logger.info("Image id: " + i['id'] + "| Image name: " + i['name'])
        images.append(object)

    logger.info("Building all combinations images - product releases")
    print("Building all combinations images - product releases")

    images_productReleases = get_product_releases_images (allproductreleases, images)

    print "Product Releases to TEST in different images:"
    logger.info("Product Releases to TEST in different images:")
    for i in images_productReleases:
        print i[0] + "|" + i[1] + "|" + i[2] + "|" + i[3] + "|" + i[4]
        logger.info("image: " + i[1] + ". Product Release: " + i[3] + "-" + i[4] + "\n")
        report_file.write ("image: " + i[1] + ". Product Release: " + i[3] + "-" + i[4] + "\n")

    number_of_productrelease_images = images_productReleases.__len__()
    logger.info("there are " + str(number_of_productrelease_images) + " combinations products - images")
    print("there are " + str(number_of_productrelease_images) + " combinations products - images")
    report_file.write ("------------------------------------------------------------------------------------------- \n")
    report_file.write ("Product Releases to TEST in different images: ")
    report_file.write (" There are " + str(number_of_productrelease_images) + " combinations products - images \n")
    report_file.write ("------------------------------------------------------------------------------------------- \n")

    environment_client = paasmanager_client.getEnvironmentResourceClient()
    tier_client = paasmanager_client.getTierResourceClient()
    environment_instance_client = paasmanager_client.getEnvironmentInstanceResourceClient()
    task_client = paasmanager_client.getTaskResourceClient()

    report_file.write ("Product Releases Execution (recipes) Report: \n")
    report_file.write ("------------------------------------------- \n")
    index=0
    for image_productrelease in images_productReleases:
        product_name = image_productrelease[3]
        product_version = image_productrelease[4]
        image_id = image_productrelease[0]
        image_name = image_productrelease[1]

        env_name = envName + str(index)
        tier_name = "tierName" + env_name
        blueprint_name = env_name + "Instance"

        index = index + 1
        logger.info ("--------------------------------------------------------------------------------------")
        print ("--------------------------------------------------------------------------------------")
        logger.info ("Product: " + product_name + "-" + product_version + " with image name: " + image_name
                     + " and imageid: " + image_id)
        print ("Product: " + product_name + "-" + product_version + " with image name: " + image_name
               + " and image id: " + image_id)
        logger.info ("--------------------------------------------------------------------------------------")
        print ("--------------------------------------------------------------------------------------")

        logger.info("Create an Environment " + env_name )
        print("Create an Environment " + env_name)

        environment = environment_client.create_environment(env_name, "For testing purposes")
        if (environment.status_code != HTTP_STATUSCODE_NO_CONTENT) :
            print ("Error creating Environment " + env_name + " Description: " + environment._content)

        environment = environment_client.get_environment(env_name)
        logger.debug(str(environment))
        print str(environment)

        logger.info("Add Tier tierName" + env_name + " to the Environment " + env_name)
        print("Add Tier tierName" + env_name + " to the Environment " + env_name)
        tier = tier_client.create_tier(environment_name = env_name,
                                   name = "tierName" + env_name,
                                   product_name = product_name,
                                   product_version = product_version,
                                   image = image_id,
                                   region_name = region_name)
        tier = tier_client.get_tier(env_name, tier_name)
        logger.debug("Tier created : " + str(tier))
        print "Tier created : " + str(tier)

        logger.info("Creating Environment Instance " + blueprint_name)
        print ("Creating Environment Instance " + blueprint_name)

        initial_time = time.strftime("%H:%M:%S")
        initial_time_datetime = datetime.datetime.now()

        environment_instance_task = environment_instance_client.create_environment_instance\
        (
            name=blueprint_name,
            description="For Testing purposes",
            environment_name=env_name,
            tier_name = tier_name,
            product_name = product_name,
            product_version = product_version,
            image = image_id,
            region_name = region_name
        )

        if (environment_instance_task.status_code != HTTP_STATUSCODE_OK):
            print ("Error creating Environment Instance " + blueprint_name + " Description: "
                   + environment_instance_task._content)

        logger.info("Waiting for Environment Instance " + env_name + "Instance to be created")
        print("Waiting for Environment Instance " + env_name + "Instance to be created")
        task_url = getTaskUrl(environment_instance_task)
        task_id = paasmanager_client.get_taskid(task_url)
        task = task_client.get_task(task_id)
        task_status = task['status']

        while task_status==TASK_STATUS_RUNNING:
            time.sleep(TIME_INTERVAL_TO_DEPLOY)
            task = task_client.get_task(task_id)
            task_status = task['status']
            print "Polling every 60 seconds - Task status: " + task_status
            logger.info("Polling every " + str(TIME_INTERVAL_TO_DEPLOY) +" seconds - Task status: " + task_status)

        final_time = time.strftime("%H:%M:%S")
        final_time_datetime = datetime.datetime.now()
        interval =  final_time_datetime - initial_time_datetime

        if task_status==TASK_STATUS_SUCCESS:
            print ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to deploy in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to deploy in : " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss  \n")
            report_file.write ("Image name: " + image_name + ". Product Release: " + product_name + "-"
                               + product_version + " SUCCESS to deploy in : " + final_time_datetime.strftime("%H:%M:%S")
                               + " hh::mm:ss  \n")
        elif task_status == TASK_STATUS_ERROR:
            task_error = task[TASK_BODY_ERROR]
            major_error_desc = task_error[TASK_BODY_ERROR_MAJORCODE]
            error_message = task_error[TASK_BODY_ERROR_MESSAGE]
            minorErrorCode = task_error[TASK_BODY_ERROR_MINORCODE]
            
            print ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to deploy in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss  \n")
            print ("ERROR Major Error Description : " + str(major_error_desc))
            print ("ERROR Message : " + str(error_message))
            print ("ERROR Minor Error Code : " + str(minorErrorCode))
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to deploy in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")
            logger.info("ERROR Major Error Description : " + str(major_error_desc))
            logger.info("ERROR Message : " + str(error_message))
            logger.info("ERROR Minor Error Code : " + str(minorErrorCode))
            report_file.write ("Image name: " + image_name + ". Product Release: " + product_name + "-"
                               + product_version + " ERROR to deploy in " + final_time_datetime.strftime("%H:%M:%S")
                               + " hh::mm:ss  \n")
            report_file.write("ERROR Major Error Description : " + str(major_error_desc) + "\n")
            report_file.write("ERROR Message : " + str(error_message) + "\n")
            report_file.write("ERROR Minor Error Code : " + str(minorErrorCode) + "\n")

        logger.info("Deleting Environment Instance " + blueprint_name)
        print ("Deleting Environment Instance " + blueprint_name)
        environment_instance_task = environment_instance_client.delete_environment_instance(blueprint_name)

        logger.info("Waiting for Environment Instance " + blueprint_name + "Instance to be deleted")
        print("Waiting for Environment Instance " + blueprint_name + "Instance to be deleted")
        task_url = getTaskUrl(environment_instance_task)
        task_id = paasmanager_client.get_taskid(task_url)
        task = task_client.get_task(task_id)
        task_status = task['status']

        while task_status==TASK_STATUS_RUNNING:
            time.sleep(TIME_INTERVAL_TO_DELETE)
            task = task_client.get_task(task_id)
            task_status = task['status']
            print "Polling every " + str(TIME_INTERVAL_TO_DELETE) + " seconds - Task status: " + task_status

        if task_status==TASK_STATUS_SUCCESS:
            print ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to delete  in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to delete in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")
        elif task_status ==TASK_STATUS_ERROR:
            print ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to delete in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to delete in " + final_time_datetime.strftime("%H:%M:%S") + " hh::mm:ss \n")

        logger.info("Deleting Tier " + tier_name)
        print ("Deleting Tier " + tier_name)
        tier_client.delete_tier(env_name, tier_name)

        logger.info("Deleting Environment " + env_name)
        print ("Deleting Environment " + env_name)
        environment_client.delete_environment(env_name)

        logger.info("Environment " + env_name + " FINISHED")
        print ("Environment " + env_name + " FINISHED")

def getTaskUrl (environment_instance_response):
    task_dict = xmltodict.parse(environment_instance_response._content, attr_prefix='')
    task_url = task_dict['task']['href']

    return task_url

def get_product_releases_images (allproductreleases, images):
    products_releases = []
    images_productReleases = []

    for i in allproductreleases[PRODUCTANDRELEASE_BODY_ROOT]:
        product_name = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_PRODUCTNAME]
        product_version = i[PRODUCTANDRELEASE_BODY_PRODUCTVERSION]
        product_release = product_name + "_" + product_version
        products_releases.append(product_release)
        #print ("Product Release: " +  product_release)

        if i[PRODUCTANDRELEASE_BODY_PRODUCT].get(PRODUCTANDRELEASE_BODY_METADATAS): # Checks if there are metadatas in the product
            for j in i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS]:
                product_metadatas = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS]
                #print (str (product_metadatas))
                try :
                    metadata_key = j[PRODUCTANDRELEASE_BODY_METADATA_KEY]
                    metadata_value = j[PRODUCTANDRELEASE_BODY_METADATA_VALUE]
                except TypeError:
                    metadata_key = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_KEY]
                    metadata_value = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_VALUE]
                #index = 0
                #print "metadata_key:" + str(metadata_key) + " and metadata_value:" + str(metadata_value)
                if ((metadata_key == PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR) and
                        (metadata_value == PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR_CHEF_VALUE)):
                    for k in product_metadatas:
                        try :
                            metadata_key = k[PRODUCTANDRELEASE_BODY_METADATA_KEY]
                            metadata_value = k['value']
                        except TypeError:
                            metadata_key = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_KEY]
                            metadata_value = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_VALUE]

                        if (metadata_key == PRODUCTANDRELEASE_BODY_METADATA_IMAGE) and \
                                ((metadata_value == "") or (metadata_value is None)):
                            for z in images:
                                object = []
                                object.append(z[0])
                                object.append(z[1])
                                object.append(product_release)
                                object.append(product_name)
                                object.append(product_version)
                                images_productReleases.append(object)
                        else:
                            for z in images:
                                #print ("z[0] " + str (z[0]))
                                if ((metadata_key == PRODUCTANDRELEASE_BODY_METADATA_IMAGE) and
                                        (metadata_value == z[0])):
                                    object = []
                                    object.append(z[0])
                                    object.append(z[1])
                                    object.append(product_release)
                                    object.append(product_name)
                                    object.append(product_version)
                                    images_productReleases.append(object)
    return images_productReleases

def find_all_images_sdc_aware(url_base, region, token, tenant_id):
    print( "find all images in " + region + '->' + url_base)
    logger.debug("find all images in " + region + '->' + url_base)

    url = url_base + GLANCE_REQUEST_IMAGES_SDC_AWARE_URL
    headers = {ACCEPT: APPLICATION_JSON,
               X_AUTH_TOKEN: '' + token + '',
               TENANT_ID: '' + tenant_id + ''
    }
    return sendGet(headers, url)

def sendGet(headers, url):
    if url.startswith (HTTPS_PROTOCOL):
        response = requests.get(url, headers=headers, verify=False)
    else:
        response = requests.get(url, headers=headers, verify=False)
    response_json = response.json()
    return response_json

if __name__ == "__main__":
    main(sys.argv[1:])