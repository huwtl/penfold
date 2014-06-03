package com.qmetric.penfold.app.support.auth

import org.scalatra.auth.strategy.BasicAuthStrategy
import org.scalatra.ScalatraBase
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.qmetric.penfold.app.AuthenticationCredentials

class BasicAuthenticationStrategy(protected override val app: ScalatraBase, realm: String, validCredentials: AuthenticationCredentials) extends BasicAuthStrategy[User](app, realm) {
  override protected def getUserId(user: User)(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    user.id
  }

  override protected def validate(userName: String, password: String)(implicit request: HttpServletRequest, response: HttpServletResponse) = {
    if (userName == validCredentials.username && password == validCredentials.password) Some(User(userName)) else None
  }
}

case class User(id: String)