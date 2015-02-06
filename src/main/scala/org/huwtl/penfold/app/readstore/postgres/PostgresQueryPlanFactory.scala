package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore.{Filter, Filters}

class PostgresQueryPlanFactory {
  def create(filters: Filters) = {
    PostgresQueryPlan(filters.all.map(transform))
  }

  private def transform(filter: Filter) = {
    PostgresRestrictionField(filter.key, filter)
  }
}
