package org.huwtl.penfold.app.readstore.redis

import org.huwtl.penfold.readstore.Filters

case class Index(name: String, fields: List[IndexField]) {
  def suitableFor(filters: Filters) = {
    filters.keys.map(_.toLowerCase).toSet == fields.map(_.key.toLowerCase).toSet
  }
}
