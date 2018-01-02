# AWS Utils [![Build Status](https://travis-ci.org/StreamSimple/awsutils.svg?branch=master)](https://travis-ci.org/StreamSimple/awsutils)

This project provides simple AWS utilities that can be used with the AWS SDK.

# Dev Setup

Please follow the instructions [here](https://github.com/StreamSimple/uber-poms/wiki/Building-Projects).

# Integration Tests

## Prereqs

In order to run integration tests you must setup the following on **AWS**:

 - Create an ssh key pair called ```dev-test```
 - Create API keys that allow you to deploy a VPC an EC2 Instance.
 
## Running

To run all the tests including integration tests do:

```
env AWS_REGION=us-west-1 AWS_ACCESS_KEY=`aws configure get aws_access_key_id` AWS_SECRET_KEY=`aws configure get aws_secret_access_key` mvn clean install -DexcludedGroups=""
```
