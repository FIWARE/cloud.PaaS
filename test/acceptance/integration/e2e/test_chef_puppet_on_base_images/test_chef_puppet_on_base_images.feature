# -*- coding: utf-8 -*-
Feature: Deploy and install products using Chef and Puppet on VMs with each base image

    As a fi-ware user
    I want to be able to create templates using all base images
    so that I can install products on each one with Chef and Puppet when blueprint is deployed.


    @skip @bug @CLAUDIA-5337 @happy_path @env_dependant @experimenation
    Scenario Outline: Deploy blueprints and install a Chef product with each base image.
        Given the paas manager is up and properly configured
        And   the product "testing_paas_product" with version "0.0.1" is created in SDC with metadatas:
                | key         | value   |
                | installator | chef    |
        And   a list of tiers has been defined with data:
                | name        | region      | image       | networks | products                   |
                | <tiername>  | Trento      | <image_id>  | Internet | testing_paas_product=0.0.1 |
        And   an environment has already been created with the previous tiers and data:
                | name        | description |
                | <envname>   | descqa      |
        When  I request the creation of an instance of the environment "<envname>" using data:
                | name   | description    |
                | <name> | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the product "testing_paas_product" with version "0.0.1" of tier "<tiername>" has been installed using "chef"
        Examples:
            | name            | envname | tiername    | image_id                             |
            # ubuntu14.04_rc
            | instancenameqa1 | nameqa1 | tiernameqa1 | 67d301f0-28c6-49da-a236-031e8beca5d0 |
            # ubuntu12.04_rc
            | instancenameqa2 | nameqa2 | tiernameqa2 | cff5380b-7108-424f-9392-64bc9e0d1158 |
            # debian7_rc
            | instancenameqa3 | nameqa3 | tiernameqa3 | 97238b6c-ae87-4941-b007-8077478e1646 |
            # centos7_rc
            | instancenameqa4 | nameqa4 | tiernameqa4 | 3e2822e9-ed9a-4608-938a-9185ec777448 |
            # centos6_rc
            | instancenameqa5 | nameqa5 | tiernameqa5 | 3c10a376-3062-4974-bd95-bc3116cc07ff |


    @skip @bug @CLAUDIA-5337 @happy_path @env_dependant @experimenation
    Scenario Outline: Deploy blueprints and install a Puppet product with each base image.
        Given the paas manager is up and properly configured
        And   the product "testing_paas_product_puppet" with version "0.0.1" is created in SDC with metadatas:
                | key         | value   |
                | installator | puppet  |
        And   a list of tiers has been defined with data:
                | name        | region      | image       | networks | products                          |
                | <tiername>  | Trento      | <image_id>  | Internet | testing_paas_product_puppet=0.0.1 |
        And   an environment has already been created with the previous tiers and data:
                | name        | description |
                | <envname>   | descqa      |
        When  I request the creation of an instance of the environment "<envname>" using data:
                | name   | description    |
                | <name> | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the product "testing_paas_product_puppet" with version "0.0.1" of tier "<tiername>" has been installed using "puppet"
        Examples:
            | name            | envname | tiername    | image_id                             |
            # ubuntu14.04_rc
            | instancenameqa1 | nameqa1 | tiernameqa1 | 67d301f0-28c6-49da-a236-031e8beca5d0 |
            # ubuntu12.04_rc
            | instancenameqa2 | nameqa2 | tiernameqa2 | cff5380b-7108-424f-9392-64bc9e0d1158 |
            # debian7_rc
            | instancenameqa3 | nameqa3 | tiernameqa3 | 97238b6c-ae87-4941-b007-8077478e1646 |
            # centos7_rc
            | instancenameqa4 | nameqa4 | tiernameqa4 | 3e2822e9-ed9a-4608-938a-9185ec777448 |
            # centos6_rc
            | instancenameqa5 | nameqa5 | tiernameqa5 | 3c10a376-3062-4974-bd95-bc3116cc07ff |
