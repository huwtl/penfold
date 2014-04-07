package org.huwtl.penfold.app.web

import org.scalatra.Params
import org.huwtl.penfold.readstore.{Filters, Filter}

trait FilterParamsProvider {
  def parseFilters(params: Params) = {
    val filters = params.filterKeys(_.startsWith("_")).collect {
      case param if !param._2.isEmpty => Filter(param._1.tail.toLowerCase, param._2)
    }
    Filters(filters.toList)
  }
}
