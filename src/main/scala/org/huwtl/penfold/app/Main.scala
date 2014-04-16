package org.huwtl.penfold.app

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._
import scala.util.Try

object Main {
  def main(args: Array[String]) {
    new Main().init().join()
  }
}

class Main() {
  def init() = {
    sys.props.getOrElseUpdate("config.file", "/usr/local/config/penfold/penfold.conf")

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val server = new Server(config.httpPort)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase(Try(getClass.getClassLoader.getResource("webapp").toExternalForm) getOrElse "src/main/webapp" )
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server
  }
}
