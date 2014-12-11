package org.huwtl.penfold.app.web

import java.lang.IllegalArgumentException

case class ContentTypeWithCommandType(contentType: Option[String]) {

  private val contentTypeRegex = "application/json.*;.*domain-command=([a-zA-Z]+).*".r

  private val contentTypeError = "Content-Type header should include domain-command parameter, i.e. application/json;domain-command=COMMAND_TYPE"

  private val contentTypeOrBlank = contentType.getOrElse("")

  require(expectedDomainCommandContentType, contentTypeError)

  val extractedCommandType = {
    contentTypeRegex findFirstIn contentTypeOrBlank match {
      case Some(contentTypeRegex(commandType)) => commandType
      case None => throw new IllegalArgumentException(contentTypeError)
    }
  }

  private def expectedDomainCommandContentType = (contentTypeRegex findFirstMatchIn contentTypeOrBlank).isDefined
}
