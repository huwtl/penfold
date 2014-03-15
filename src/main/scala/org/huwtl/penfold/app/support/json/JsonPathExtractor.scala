package org.huwtl.penfold.app.support.json

import org.json4s._
import org.json4s.jackson.JsonMethods._

class JsonPathExtractor {
  implicit val formats = DefaultFormats

  def extract(json: String, path: String) = {
    val parts = path.split("/")

    val extractedJson = parts.foldLeft(parse(json))((previous, current) => previous \ current.trim)

    val foundValues = extractedJson.toOption match {
      case Some(array: JArray) => array.extractOrElse[List[String]](Nil)
      case Some(_: JObject) => Nil
      case Some(JNull) => Nil
      case Some(primitive) => List(primitive.values.toString)
      case None => Nil
    }

    foundValues.map(_.toLowerCase)
  }
}
