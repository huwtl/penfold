package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.readstore.Filter

case class PostgresQueryPlan(restrictionFields: List[PostgresRestrictionField])

case class PostgresRestrictionField(path: String, filter: Filter)