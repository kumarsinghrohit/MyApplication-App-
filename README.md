# MyApplication

## Setup to run the application on the local environment
2. Create the artifacts by running `mvn clean install` on root folder.
3. After successful build run `docker-compose up --build` command on root directory.
4. Make sure all the services are indeed up and running by using `docker-compose ps` on the root directory.

## Domain and sub-domain entries windows hosts file
* Add the following entires in `windows hosts file`. This file is situated at `c:\windows\system32\drivers\etc`
  
  ```
  127.0.0.1     keycloakdev
  127.0.0.1     elasticsearch
  127.0.0.1		localstack
  127.0.0.1		axonserver
  127.0.0.1		example.com
  127.0.0.1		apps.example.com
  127.0.0.1		auth.example.com
  127.0.0.1		store
  127.0.0.1		apps
  127.0.0.1		auth.application
  ```
## Service port mapping
| Service | Ports | Access |
|---------|-------|--------|
|**keycloak**|8180|http://keycloak.application.service:8180|
|**elasticsearch**|9200|http://elasticsearch:9200|
|**localstack (AWS S3)**|4572|http://localstack:4572|
|**localstack web UI**|8280|http://localstack:8280|

## Dissection of the docker-compose.yml file
* **nginx.application.service**: act as an reverse proxy which run on port 80. This is the single point of contact and every incoming request will for `application` will be routed through it.
* **redis.application.service**: responsible to store the http session which gets generated by spring to store the user info, that can leveraged by multiple services running on various sub-domains. 
* **manager**: sub-module to handle command side of the data that is responsible to change the app-application state. 
* **profile**: sub-module to handle the query side of the data that is responsible to view the app-application data. e.g. list of developed apps and libs for the logged in user.
* **axonserver**: aids to implement `event sourcing` and `CQRS` micro services architectural patterns by handling command, events and queries. Additionally, it also maintain the event_store database to store all the events that have happened and builds the current application's state by replaying the same.
* **keycloak.application.service**: Authentication and authorization provider.
* **elasticsearch**: Persistence storage to store application's view related data.
* **localstack**: is easy-to-use mocking framework for developing the cloud application. With this, it aids to setup `S3` of `AWS` locally.

## Setting up the localstack
1. Configure the AWS using AWS CLI. For localstack it does not require real credentials, hence add dummy infomration with default region i.e. us-east-1. Futhermore, keep Default output format blank.
  ```
  AWS Access Key ID [****************ummy]: dummy
  AWS Secret Access Key [****************ummy]: dummy
  Default region name [us-east-1]: us-east-1
  Default output format [None]:
  ```
2. It is likely for existing containers volume for localstack is already setup. use following to list the existing s3 buckets.
  ```
  aws --endpoint-url=http://localhost:4572 s3 ls or in browser use http://localstack:8280
  ```
3. If it doesn't have any buckets use following command to create a `test-bucket`.
  ```
  aws --endpoint-url=http://localhost:4572 s3 mb s3://test-bucket
  ```
4. Ensure that the bucket was created sucessfully by using command mentiond in step 2.
5. Give public read access to the `test-bucket`.
  ```
  aws --endpoint-url=http://localhost:4572 s3api put-bucket-acl --bucket <bucket-name> --acl public-read
  ```
6. Add a file and access the same to ensure public access the the bucket was given.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)
* [Thymeleaf](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-template-engines)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)

### Guides
The following guides illustrate how to use some features concretely:

* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)

