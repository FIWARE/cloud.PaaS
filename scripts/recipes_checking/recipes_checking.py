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
import time
import requests
import datetime
import xmltodict

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

# IMAGE BODY ELEMENTS
IMAGE_BODY_NAME ="name"
IMAGE_BODY_ID ="id"
IMAGE_BODY_IMAGES ="images"

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


#IMAGE DICTIONARY KEYS
DICT_IMAGE_NAME ="image_name"
DICT_IMAGE_ID = "image_id"

#IMAGE_PRODUCTRELEASE DICTIONARY KEYS
DICT_IMAGE_PRODUCTRELEASE_PRODUCTRELEASE = "product_release"
DICT_IMAGE_PRODUCTRELEASE_PRODUCTNAME = "product_name"
DICT_IMAGE_PRODUCTRELEASE_PRODUCTVERSION = "product_version"

TIME_INTERVAL_TO_DEPLOY = 60
TIME_INTERVAL_TO_DELETE = 45

def main(argv=None):
    """
    Getting parameters
    :param argv:
    """
    parser = argparse.ArgumentParser(description='Testing product installation using paasmanager')
    parser.add_argument("-u", "--os-username", dest='user', help='valid username', required=True)
    parser.add_argument("-p", "--os-password", dest='password', help='valid password', required=True)
    parser.add_argument("-t", "--os-tenant-id", dest='tenant_id', help="user tenant_id", required=True)
    parser.add_argument("-r", "--os-region-name", dest='region_name', default='Spain2', help='the name of region')
    parser.add_argument("-k", "--os-auth-url", dest="auth_url", default='http://cloud.lab.fiware.org:4731/v2.0',
                        help='url to keystone <host or ip>:<port>/v2.0')
    parser.add_argument("-e", "--envName", dest='envName', default='EnvName', help='valid environment name')
    parser.add_argument("-f", "--reportfile", dest='reportfile', default='/var/log/recipes_checking_report.log',
        help='Name of the Report File')


    args = parser.parse_args()
    logger.info(args)

    # This is the file where to find the report about the tests of BlueprintInstance installation with
    # all the recipes available in the Chef-Server
    report_file = open(args.reportfile, 'w')

    check_recipes (report_file, envName= args.envName,
                     auth_url=args.auth_url,
                     tenant_id=args.tenant_id,
                     user=args.user,
                     password=args.password,
                     region_name=args.region_name)

def check_recipes(report_file, envName, auth_url, tenant_id, user, password, region_name):

    report_file.writelines("========================================================================================\n")
    report_file.write("Platform: " +  auth_url + ". Region: " + region_name + ". Username: " + user
                      + " Tenant-ID: " + tenant_id + "\n")
    report_file.writelines("========================================================================================\n")

    logger.info("SDC call to get the list of products available in catalog")

    #Llamar al SDC para sacar la lista de productos.
    sdc_client = SDCClient(user, password, tenant_id, auth_url, region_name)
    productandrelease_client = sdc_client.getProductAndReleaseResourceClient()
    allproductreleases,_ = productandrelease_client.get_allproductandrelease()

    logger.debug(str(allproductreleases['productAndReleaseDto']))
    logger.info("There are " + str(len(allproductreleases['productAndReleaseDto'])) + " product Releases in SDC")
    report_file.writelines("There are " + str(len(allproductreleases['productAndReleaseDto']))
                           + " product Releases in SDC")

    paasmanager_client = PaaSManagerClient(user, password, tenant_id, auth_url, region_name )
    glance_url = paasmanager_client.get_paasmanager_endpoint_from_keystone(region_name, GLANCE_SERVICE_TYPE,
                                                                           GLANCE_ENDPOINT_TYPE)
    logger.info("Loading image list from glance : " + glance_url + " Region: " + region_name)
    report_file.writelines("Loading image list from glance : " + glance_url + " Region: " + region_name  + "\n")
    report_file.write ("------------------------------------------------------------------------------------------- \n")

    response_images = find_all_images_sdc_aware(glance_url, region_name, paasmanager_client.token, tenant_id)
    logger.debug(response_images)

    images = []
    for i in response_images[IMAGE_BODY_IMAGES]:
        image_name = i[IMAGE_BODY_NAME]
        image_id = i[IMAGE_BODY_ID]
        image_dict = {DICT_IMAGE_NAME: image_name, DICT_IMAGE_ID: image_id}
        logger.info("Image id: " + image_dict['image_id']+ "| Image name: " +  image_dict['image_name']  + "\n")
        report_file.writelines("Image id: " + image_dict['image_id']+ "| Image name: " +  image_dict['image_name'] + "\n")
        images.append(image_dict)
    report_file.write ("------------------------------------------------------------------------------------------- \n")

    logger.info("Building all combinations images - product releases")

    images_productReleases = get_product_releases_images (allproductreleases, images)

    logger.info("Product Releases to TEST in different images:")
    for i in images_productReleases:
        logger.info("image: " + i[DICT_IMAGE_NAME] + ". Product Release: " + i['product_release'] + "\n")
        report_file.write ("image: " + i[DICT_IMAGE_NAME] + ". Product Release: " + i['product_release'] + "\n")

    number_of_productrelease_images = images_productReleases.__len__()
    logger.info("there are " + str(number_of_productrelease_images) + " combinations products - images")
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
        product_name = image_productrelease[DICT_IMAGE_PRODUCTRELEASE_PRODUCTNAME]
        product_version = image_productrelease[DICT_IMAGE_PRODUCTRELEASE_PRODUCTVERSION]
        image_id = image_productrelease[DICT_IMAGE_ID]
        image_name = image_productrelease[DICT_IMAGE_NAME]

        env_name = envName + str(index)
        tier_name = "tierName" + env_name
        blueprint_name = env_name + "Instance"

        index = index + 1
        logger.info ("--------------------------------------------------------------------------------------")
        logger.info ("Product: " + product_name + "-" + product_version + " with image name: " + image_name
                     + " and imageid: " + image_id)
        logger.info ("--------------------------------------------------------------------------------------")

        logger.info("Create an Environment " + env_name )

        environment = environment_client.create_environment(env_name, "For testing purposes")
        if (environment.status_code != HTTP_STATUSCODE_NO_CONTENT) :
            logger.info ("Error creating Environment " + env_name + " Description: " + environment._content)

        environment_dict, _ = environment_client.get_environment(env_name)
        logger.debug(str(environment_dict))

        logger.info("Add Tier tierName" + env_name + " to the Environment " + env_name)
        tier = tier_client.create_tier(environment_name = env_name,
                                   name = "tierName" + env_name,
                                   product_name = product_name,
                                   product_version = product_version,
                                   image = image_id,
                                   region_name = region_name)
        tier_dict, _ = tier_client.get_tier(env_name, tier_name)
        logger.debug("Tier created : " + str(tier_dict))

        logger.info("Creating Environment Instance " + blueprint_name)

        initial_time_deploy = time.strftime("%H:%M:%S")
        initial_time_deploy_datetime = datetime.datetime.now()

        environment_instance_task_dict, environment_instance_response = \
            environment_instance_client.create_environment_instance (name=blueprint_name,
                                                                     description="For Testing purposes",
                                                                     environment_name=env_name,
                                                                     environment_description = "For Testing purposes env",
                                                                     tier_name = tier_name,
                                                                     product_name = product_name,
                                                                     product_version = product_version,
                                                                     image = image_id,
                                                                     region_name = region_name)

        if (environment_instance_response.status_code != HTTP_STATUSCODE_OK):
            logger.info ("Error creating Environment Instance " + blueprint_name + " Description: "
                   + environment_instance_response._content)

        logger.info("Waiting for Environment Instance " + env_name + "Instance to be created")
        task_url = getTaskUrl(environment_instance_task_dict)
        task_id = paasmanager_client.get_taskid(task_url)
        task, _ = task_client.get_task(task_id)
        task_status = task[TASK_BODY_STATUS]

        while task_status==TASK_STATUS_RUNNING:
            time.sleep(TIME_INTERVAL_TO_DEPLOY)
            task, _ = task_client.get_task(task_id)
            task_status = task[TASK_BODY_STATUS]
            logger.info("Polling every " + str(TIME_INTERVAL_TO_DEPLOY) +" seconds - Task status: " + task_status)

        final_time_deploy = time.strftime("%H:%M:%S")
        final_time_deploy_datetime = datetime.datetime.now()
        interval_deploy =  final_time_deploy_datetime - initial_time_deploy_datetime

        if task_status==TASK_STATUS_SUCCESS:
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to deploy in  " + str(interval_deploy.seconds) + " seconds  \n")
            report_file.write ("Image name: " + image_name + ". Product Release: " + product_name + "-"
                               + product_version + " SUCCESS to deploy in " + str(interval_deploy.seconds)
                               + " seconds  \n")
        elif task_status == TASK_STATUS_ERROR:
            task_error = task[TASK_BODY_ERROR]
            major_error_desc = task_error[TASK_BODY_ERROR_MAJORCODE]
            error_message = task_error[TASK_BODY_ERROR_MESSAGE]
            minorErrorCode = task_error[TASK_BODY_ERROR_MINORCODE]

            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to deploy in " + str(interval_deploy.seconds) + " seconds \n")
            logger.info("ERROR Major Error Description : " + str(major_error_desc))
            logger.info("ERROR Message : " + str(error_message))
            logger.info("ERROR Minor Error Code : " + str(minorErrorCode))
            report_file.write ("Image name: " + image_name + ". Product Release: " + product_name + "-"
                               + product_version + " ERROR to deploy in " + str(interval_deploy.seconds)
                               + " seconds  \n")
            report_file.write("ERROR Major Error Description : " + str(major_error_desc) + "\n")
            report_file.write("ERROR Message : " + str(error_message) + "\n")
            report_file.write("ERROR Minor Error Code : " + str(minorErrorCode) + "\n")

        logger.info("Deleting Environment Instance " + blueprint_name)

        initial_time_delete = time.strftime("%H:%M:%S")
        initial_time_delete_datetime = datetime.datetime.now()

        environment_instance_task_dict, environment_instance_response = \
            environment_instance_client.delete_environment_instance(blueprint_name)

        logger.info("Waiting for Environment Instance " + blueprint_name + "Instance to be deleted")
        task_url = getTaskUrl(environment_instance_task_dict)
        task_id = paasmanager_client.get_taskid(task_url)
        task, _ = task_client.get_task(task_id)
        task_status = task[TASK_BODY_STATUS]

        while task_status==TASK_STATUS_RUNNING:
            time.sleep(TIME_INTERVAL_TO_DELETE)
            task, _ = task_client.get_task(task_id)
            task_status = task[TASK_BODY_STATUS]
            logger.info("Polling every " + str(TIME_INTERVAL_TO_DELETE) + " seconds - Task status: " + task_status)

        final_time_delete = time.strftime("%H:%M:%S")
        final_time_delete_datetime = datetime.datetime.now()
        interval_delete =  final_time_delete_datetime - initial_time_delete_datetime

        if task_status==TASK_STATUS_SUCCESS:
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " SUCCESS to delete in " + str(interval_delete.seconds) + " seconds \n")
        elif task_status ==TASK_STATUS_ERROR:
            logger.info ("Image name: " + image_name + ". Product Release: " + product_name + "-" + product_version +
                " ERROR to delete in " + str(interval_delete.seconds) + " seconds \n")

        logger.info("Deleting Tier " + tier_name)
        tier_client.delete_tier(env_name, tier_name)

        logger.info("Deleting Environment " + env_name)
        environment_client.delete_environment(env_name)

        logger.info("Environment " + env_name + " FINISHED")

def getTaskUrl (environment_instance_task_dict):
    return environment_instance_task_dict[TASK_BODY_HREF]

def get_product_releases_images (allproductreleases, images):
    images_productReleases = []

    for i in allproductreleases[PRODUCTANDRELEASE_BODY_ROOT]:
        product_name = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_PRODUCTNAME]
        product_version = i[PRODUCTANDRELEASE_BODY_PRODUCTVERSION]
        product_release = product_name + "_" + product_version

        if i[PRODUCTANDRELEASE_BODY_PRODUCT].get(PRODUCTANDRELEASE_BODY_METADATAS): # Checks if there are metadatas in the product
            for j in i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS]:
                product_metadatas = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS]
                try :
                    metadata_key = j[PRODUCTANDRELEASE_BODY_METADATA_KEY]
                    metadata_value = j[PRODUCTANDRELEASE_BODY_METADATA_VALUE]
                except TypeError:
                    metadata_key = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_KEY]
                    metadata_value = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_VALUE]

                if ((metadata_key == PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR) and
                        (metadata_value == PRODUCTANDRELEASE_BODY_METADATA_INSTALLATOR_CHEF_VALUE)):
                    for k in product_metadatas:
                        try :
                            metadata_key = k[PRODUCTANDRELEASE_BODY_METADATA_KEY]
                            metadata_value = k[PRODUCTANDRELEASE_BODY_METADATA_VALUE]
                        except TypeError:
                            metadata_key = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_KEY]
                            metadata_value = i[PRODUCTANDRELEASE_BODY_PRODUCT][PRODUCTANDRELEASE_BODY_METADATAS][PRODUCTANDRELEASE_BODY_METADATA_VALUE]

                        if (metadata_key == PRODUCTANDRELEASE_BODY_METADATA_IMAGE) and \
                                ((metadata_value == "") or (metadata_value is None)):
                            for z in images:
                                image_id = z[DICT_IMAGE_ID]
                                image_name = z[DICT_IMAGE_NAME]
                                image_productRelease = { DICT_IMAGE_ID : image_id,
                                                         DICT_IMAGE_NAME : image_name,
                                                         DICT_IMAGE_PRODUCTRELEASE_PRODUCTRELEASE : product_release,
                                                         DICT_IMAGE_PRODUCTRELEASE_PRODUCTNAME: product_name,
                                                         DICT_IMAGE_PRODUCTRELEASE_PRODUCTVERSION: product_version
                                                        }
                                images_productReleases.append(image_productRelease)
                        else:
                            for z in images:
                                if ((metadata_key == PRODUCTANDRELEASE_BODY_METADATA_IMAGE) and
                                        (z[DICT_IMAGE_ID]  in metadata_value)):
                                    image_id = z[DICT_IMAGE_ID]
                                    image_name = z[DICT_IMAGE_NAME]
                                    image_productRelease = {    DICT_IMAGE_ID : image_id,
                                                                DICT_IMAGE_NAME : image_name,
                                                                DICT_IMAGE_PRODUCTRELEASE_PRODUCTRELEASE : product_release,
                                                                DICT_IMAGE_PRODUCTRELEASE_PRODUCTNAME: product_name,
                                                                DICT_IMAGE_PRODUCTRELEASE_PRODUCTVERSION: product_version
                                                            }
                                    images_productReleases.append(image_productRelease)
    return images_productReleases

def find_all_images_sdc_aware(url_base, region, token, tenant_id):
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
