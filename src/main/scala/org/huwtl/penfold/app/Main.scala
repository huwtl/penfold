package org.huwtl.penfold.app

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.FicusConfig._

object Main {
  def main(args: Array[String]) {

    sys.props.getOrElseUpdate("config.file", "/usr/local/config/penfold/penfold.conf")

    val config = ConfigFactory.load().as[ServerConfiguration]("penfold")

    val server = new Server(config.httpPort)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}
