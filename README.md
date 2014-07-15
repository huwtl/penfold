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


### Quick play with API

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

You should see a response similar to below. The response lists the attributes of your newly created task, including an auto generated unique task ID.

The links section of the response lists what actions and views are available for this task:
* self - link to this task resource
* updatePayload - link where requests should be sent to make changes to the task payload (PUT)
* close - link to close the task (POST)

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

Like before, send a request to view your task in the queue with waiting status.

```
GET: /queues/greenback/waiting  HTTP 1.1
```

Send another request after your task's "triggerDate" has passed to view the task as being ready.

```
GET: /queues/greenback/ready  HTTP 1.1
```

When your task is "ready", then you should see a response similar to below.

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/queues/greenback/ready"
        }
    },
    "_embedded": {
        "queue": {
            "_links": {
                "self": {
                    "href": "http://localhost:8080/queues/greenback/ready/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
                }
            },
            "taskId": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
            "_embedded": {
                "task": {
                    "_links": {
                        "self": {
                            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
                        },
                        "close": {
                            "href": "http://localhost:8080/queues/greenback/closed"
                        },
                        "start": {
                            "href": "http://localhost:8080/queues/greenback/started"
                        },
                        "updatePayload": {
                            "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/2/payload"
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
                    "status": "ready",
                    "statusLastModified": "2014-07-11 16:05:42",
                    "triggerDate": "2014-07-11 16:05:00",
                    "version": 2
                }
            }
        }
    }
}
```

Notice the new available action link "start". A task can only be started when it's "ready".
Lets start the task by sending a POST to the action link. The body of the POST should contain the unique ID of the task:

```
POST: /queues/greenback/started  HTTP 1.1

Content-Type: application/json
    
{
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02"
}
```

The task has now been started, you will notice in the POST response that the task's status has changed to "started" and a new action link "requeue" is now available (i.e. for unstarting the task).

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/queues/greenback/started/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        }
    },
    "taskId": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "_embedded": {
        "task": {
            "_links": {
                "self": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
                },
                "close": {
                    "href": "http://localhost:8080/queues/greenback/closed"
                },
                "requeue": {
                    "href": "http://localhost:8080/queues/greenback/ready"
                },
                "updatePayload": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02/3/payload"
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
            "status": "started",
            "statusLastModified": "2014-07-11 16:41:29",
            "triggerDate": "2014-07-11 16:05:00",
            "version": 3
        }
    }
}
```

Finally, assuming we've done whatever we wanted to do to the task, lets tell penfold we're done with it and close it (see "close" action link).

```
POST: /queues/greenback/closed  HTTP 1.1

Content-Type: application/json
    
{
    "id": "25cfd0f7-2266-4d6f-9a33-997fec57ed02"
}
```

The task should now be closed.

```
{
    "_links": {
        "self": {
            "href": "http://localhost:8080/queues/greenback/closed/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
        }
    },
    "taskId": "25cfd0f7-2266-4d6f-9a33-997fec57ed02",
    "_embedded": {
        "task": {
            "_links": {
                "self": {
                    "href": "http://localhost:8080/tasks/25cfd0f7-2266-4d6f-9a33-997fec57ed02"
                },
                "requeue": {
                    "href": "http://localhost:8080/queues/greenback/ready"
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
            "status": "closed",
            "statusLastModified": "2014-07-11 16:50:47",
            "triggerDate": "2014-07-11 16:05:00",
            "version": 4
        }
    }
}
```


## Further documentation

TODO: create these documents

* configuration
* logging
* api
* queue ordering
* optimistic locking
* search
* scheduling future tasks
* archiving tasks
* authentication


