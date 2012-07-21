package com.hlewis.rabbit_scheduler.app

import org.scalatra._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.Extraction._
import scalate.ScalateSupport
import com.hlewis.rabbit_scheduler.jobstore.RedisJobstore
import com.hlewis.rabbit_scheduler.jobstore.RedisJobstore

case class User(name: String, age: Int, lst: List[Entry])

case class Entry(data: Int, str: String)

class MyScalatraServlet extends ScalatraServlet with ScalateSupport {

  val jobstore = new RedisJobstore()

  get("/hello") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say
        <a href="hello-scalate">hello to Scalate</a>
        .
      </body>
    </html>
  }

  get("/json") {
    implicit val formats = net.liftweb.json.DefaultFormats
    //val json = List(1, 2, 3)

    val user = new User("huw", 32, List(Entry(1, "abc"), Entry(2, "empty")))
    //pretty(render(decompose(user)))
    compact(render(decompose(user)))
    //compact(render())
  }

  get("/") {
    "pong"
  }

  get("/redis-hash-test/:key/:value") {
    jobstore.add(params("key"), params("value"))
    "added"
  }

  //  get("/users/:id") {
  //    params("id") match {
  //      case "1" => compact(render(("user" -> ("name" -> "John") ~ ("age" -> 30))))
  //    }
  //  }
  //
  //  post("/users") {
  //    implicit val formats = DefaultFormats
  //    case class User(name : String, age : Integer)
  //    val user = parse(request.body).extract[User]
  //    write(new Integer(1))
  //  }

  notFound {
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
