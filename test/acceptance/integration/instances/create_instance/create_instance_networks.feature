# -*- coding: utf-8 -*-
Feature: Create instances of environments with networks and E2E validations

    As a fi-ware user
    I want to be able to create instances of environments in a tenant using network capabilities (new,exiting,shared)
    so that I can connect to them and work with them


    @happy_path
    Scenario Outline: Deploy instance of an environment with one tier with new networks created by PaaSManager.
        Given the paas manager is up and properly configured
        And   a list of tiers has been defined with data:
                | name       | networks   |
                | <tiername> | <networks> |
        And   an environment has already been created with the previous tiers and data:
                | name      | description |
                | <envname> | descqa      |
        When  I request the creation of an instance of the environment "<envname>" using data:
                | name   | description    |
                | <name> | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the expected networks are connected to the instances

        Examples:
                | name            | envname | tiername    | networks      |
                | instancenameqa1 | nameqa1 | tiernameqa1 | netqa1        |
                | instancenameqa2 | nameqa2 | tiernameqa2 | netqa1,netqa2 |


    @happy_path
    Scenario: Deploy instance of an environment with one tier without networks (shared-net should be used)
        Given the paas manager is up and properly configured
        And   a list of tiers has been defined with data:
                | name            |
                | instancenameqa1 |
        And   an environment has already been created with the previous tiers and data:
                | name       | description |
                | nameqa1    | descqa      |
        When  I request the creation of an instance of the environment "nameqa1" using data:
                | name            | description    |
                | instancenameqa1 | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the expected networks are connected to the instances


    @env_dependant
    Scenario: Deploy instance of an environment with one tier using the shared network
        Given the paas manager is up and properly configured
        And   a list of tiers has been defined with data:
                | name            | networks    |
                | instancenameqa1 | shared-net  |
        And   an environment has already been created with the previous tiers and data:
                | name       | description |
                | nameqa1    | descqa      |
        When  I request the creation of an instance of the environment "nameqa1" using data:
                | name            | description    |
                | instancenameqa1 | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the expected networks are connected to the instances


    Scenario: Create instance of an environment with several tiers with networks
        Given the paas manager is up and properly configured
        And   a list of tiers has been defined with data:
                | name        | networks      |
                | tiernameqa1 | netqa1        |
                | tiernameqa2 | netqa2,netqa3 |
                | tiernameqa3 |               |
        And   an environment has already been created with the previous tiers and data:
                | name   | description |
                | nameqa | descqa      |
        When  I request the creation of an instance of the environment "nameqa" using data:
                | name           | description    |
                | instancenameqa | instancedescqa |
        Then  I receive an "OK" response with a task
        And   the task ends with "SUCCESS" status
        And   the expected networks are connected to the instances
