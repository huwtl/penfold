package org.huwtl.penfold.app.readstore.mongodb

import org.huwtl.penfold.readstore.Filters

case class Index(fields: List[IndexField]) {
  def suitableFor(filters: Filters) = {
    filters.keys.toSet == fields.map(_.alias).toSet
  }
}
