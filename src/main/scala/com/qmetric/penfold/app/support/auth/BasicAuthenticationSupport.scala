package com.qmetric.penfold.app.support.auth

import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.BasicAuthSupport
import com.qmetric.penfold.app.AuthenticationCredentials

trait BasicAuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
  self: ScalatraBase =>

  val realm = "penfold authentication"

  protected val scentryConfig = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]

  before() {
    if (validCredentials.isDefined) {
      basicAuth
    }
  }

  protected def fromSession = {
    case id: String => User(id)
  }

  protected def toSession = {
    case usr: User => usr.id
  }

  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    validCredentials match {
      case Some(credentials) => scentry.register("Basic", app => new BasicAuthenticationStrategy(app, realm, credentials))
      case None =>
    }
  }

  protected def validCredentials: Option[AuthenticationCredentials]
}
