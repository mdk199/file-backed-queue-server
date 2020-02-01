# File Backed Queue Server

## Overview

* This project provides a file backed queue which is accessible through RestFul Services and Client API. 
* Enqueue and Dequeue operations are supported through RestFul Service.
* Using Client API makes it easy to access queue through different configurations like retry mechanism, sync and async access, polling queue for new entries, etc...

## Frameworks and Libraries

* Maven as a project and dependency management tool
* Spring Boot for configuring and running restful web service
* Logback for console and file based logging
* Spring Fox (Swagger 2) for API Documentation and manual testing
* Apache Commons IO for easy file read/write and access

## Rest API Documentation

**Link to Swagger:** http://localhost:3456/swagger-ui.html

* POST /queue/enqueue
   * Use this method to enqueue an element on queue server
   * Data can be passed as a message body
* GET /queue/dequeue
   * Use this method to dequeue an element from queue server

## How to start queue server on Local Machine

* Open pom.xml file in IDE (Eclipse/IntelliJ)
* Resolve maven dependencies
* Open QueueServerApplication class
* Run main method from this class to start queue server
* Go to http://localhost:3456/swagger-ui.html
* Click on queue-controller
* Perform relevant operations on queue from that screen to test the server
* To enable debug logging add JVM Parameter _-Dlogging.config=classpath:logback-debug.xml_