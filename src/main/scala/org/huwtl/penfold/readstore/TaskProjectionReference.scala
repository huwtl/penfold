package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}

case class TaskProjectionReference(id: AggregateId, version: AggregateVersion)
