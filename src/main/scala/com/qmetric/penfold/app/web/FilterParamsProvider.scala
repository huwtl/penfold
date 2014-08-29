package com.qmetric.penfold.app.web

import org.scalatra.MultiParams
import com.qmetric.penfold.readstore.{Filter, Filters}
import com.qmetric.penfold.app.support.json.ObjectSerializer

trait FilterParamsProvider {
  val objectSerializer = new ObjectSerializer

  def parseFilters(params: MultiParams) = {
    params.get("q") match {
      case Some(values) => Filters(values.headOption.map(objectSerializer.deserialize[List[Filter]]).getOrElse(Nil))
      case None => Filters(Nil)
    }
  }
}
