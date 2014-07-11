# Penfold

[![Build Status](https://travis-ci.org/qmetric/penfold.png)](https://travis-ci.org/qmetric/penfold)

Penfold is responsible for managing queues of tasks. Penfold's understanding of a task is anything that's a valid JSON object.

The primary purposes that penfold was built for:

* messaging
* job scheduling

Penfold is deployed as a standalone server, or mulitple standalone servers for a clustered environment.

Penfold enforces immutability in its implementation, and of its use of data with [CQRS and event sourcing](http://codebetter.com/gregyoung/2010/02/16/cqrs-task-based-uis-event-sourcing-agh/).

Penfold is spoken to via a Restful API, based on the media type [HAL+JSON](http://stateless.co/hal_specification.html).


## Quick start

### Installation and configuration

Prerequisites:

* [JVM](https://www.java.com/en/download/) 6+
* [Mysql server](http://www.mysql.com/)
* [MongoDB server](http://www.mongodb.org/) 

1.
Download the latest penfold JAR file from [Maven Central](http://search.maven.org/). The JAR can be found under "com.qmetric.penfold"

2.
Create a new empty database on your Mysql server

3.
Create a configuration file named "penfold.conf", and populate with:

```
penfold {

  publicUrl = "http://localhost:8080"

  httpPort = 8080

  domainJdbcConnectionPool {
    driver = com.mysql.jdbc.Driver
    url = "jdbc:mysql://localhost:3306/<EMPTY_DATABASE_NAME>"
    username = <USERNAME>
    password = <PASSWORD>
  }

  readStoreMongoDatabaseServers {
    databaseName = <DATABASE_NAME>
    servers = [
      {
        host = "127.0.0.1"
        port = 27017
      }
    ]
  }
}
```

4.
Start penfold

```
java -Dconfig.file=<CONFIG_FILE_PATH>/penfold.conf -jar penfold.jar
```

5.
Check if penfold server is running ok

```
GET: /healthcheck  HTTP 1.1
```

If penfold is healthy, then expect to receive a response with a 200 HTTP status code


## Quick play with API

A task has a status:
* waiting - task has been scheduled in the future and is waiting to become ready in its assigned queue
* ready - task is available for starting
* started - the task has been started
* closed - the task has been closed

You can view all tasks by queue and status. For the purpose of this tutorial we will use a queue named "greenback".

```
GET: /queues/greenback/waiting  HTTP 1.1
GET: /queues/greenback/ready  HTTP 1.1
GET: /queues/greenback/started  HTTP 1.1
GET: /queues/greenback/closed  HTTP 1.1
```

At this point, each of the above requests should not respond with any tasks.

Lets create a new task. Post the following data, replacing the "triggerDate" with a date a few minutes into the future:

```
POST: /tasks  HTTP 1.1

Content-Type: application/json
    
{
    "queueBinding": {
        "id": "greenback"
    },
    "triggerDate": "yyyy-MM-dd HH:mm:ss",
    "payload": {
        "customer": { 
            "id": 1,
            "name" : "bob",
            "email": "bob@email.com"
        }
    }
}
    
```

You should see a response similar to below:

```
201 Created
Content-Type: application/hal+json

{
    "_links": {
        "self": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        },
        "close": {
            "href": "http://localhost:8080/queues/greenback/closed"
        },
        "queue": {
            "href": "http://localhost:8080/queues/greenback"
        },
        "updatePayload": {
            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/1/payload"
        }
    },
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "payload": {
        "customer": {
            "id": 1,
            "name": "bob",
            "email": "bob@email.com"
        }
    },
    "queueBinding": {
        "id": "greenback"
    },
    "status": "waiting",
    "statusLastModified": "2014-07-11 16:01:47",
    "triggerDate": "2014-07-11 16:05:00",
    "version": 1
}
```


## Further documentation

TODO: create these documents

* configuration
* logging
* api
* search
* scheduling future tasks
* archiving tasks
* authentication


