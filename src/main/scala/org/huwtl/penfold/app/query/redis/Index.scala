package org.huwtl.penfold.app.query.redis

import org.huwtl.penfold.query.Filters

case class Index(name: String, fields: List[IndexField]) {
  def suitableFor(filters: Filters) = {
    filters.keys.map(_.toLowerCase).toSet == fields.map(_.key.toLowerCase).toSet
  }
}
