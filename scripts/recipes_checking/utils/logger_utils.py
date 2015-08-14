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


import logging
import logging.config
from xml.dom.minidom import parseString
import json
import os

HEADER_CONTENT_TYPE = u'content-type'
HEADER_REPRESENTATION_JSON = u'application/json'
HEADER_REPRESENTATION_XML = u'application/xml'
HEADER_REPRESENTATION_TEXTPLAIN = u'text/plain'

"""
Part of this code has been taken from:
 https://pdihub.hi.inet/fiware/fiware-iotqaUtils/raw/develop/iotqautils/iotqaLogger.py
"""

LOG_CONSOLE_FORMATTER = "    %(asctime)s - %(name)s - %(levelname)s - %(message)s"
LOG_FILE_FORMATTER = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"


if os.path.exists("./settings/logging.conf"):
    logging.config.fileConfig("./settings/logging.conf")


# Console logging level. By default: ERROR
logging_level = logging.ERROR


def configure_logging(level):
    """
    Configure global log level to given one
    :param level: Level (INFO | DEBUG | WARN | ERROR)
    :return:
    """

    global logging_level
    logging_level = logging.ERROR
    if "info" == level.lower():
        logging_level = logging.INFO
    elif "warn" == level.lower():
        logging_level = logging.WARNING
    elif "debug" == level.lower():
        logging_level = logging.DEBUG


def get_logger(name):
    """
    Create new logger with the given name
    :param name: Name of the logger
    :return: Logger
    """

    logger = logging.getLogger(name)
    return logger
