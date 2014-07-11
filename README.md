# Penfold

[![Build Status](https://travis-ci.org/qmetric/penfold.png)](https://travis-ci.org/qmetric/penfold)

Penfold is responsible for managing queues of user defined tasks. A task is anything that can be represented by valid JSON.
A queue is an ordered queue of tasks.

Penfold is deployed as a standalone server, or mulitple standalone servers for a clustered environment.

Penfold enforces immutability in its implementation and of its data with [CQRS and event sourcing](http://codebetter.com/gregyoung/2010/02/16/cqrs-task-based-uis-event-sourcing-agh/).

Penfold is spoken to via a Restful API, based on the media type [HAL+JSON](http://stateless.co/hal_specification.html).

The primary purposes that penfold was built for:

* messaging
* job scheduling


## Quick start

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





