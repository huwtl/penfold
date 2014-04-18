package org.huwtl.penfold.app.web

import org.scalatra.MultiParams
import org.huwtl.penfold.readstore.{Filters, Filter}

trait FilterParamsProvider {
  def parseFilters(params: MultiParams) = {
    val filters = params.filterKeys(_.startsWith("_")).map {
      case (key, values) => Filter(key.tail, values.map(v => if (v.isEmpty) None else Some(v)).toSet[Option[String]])
    }
    Filters(filters.toList)
  }
}
