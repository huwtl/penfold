package org.huwtl.penfold.readstore

import org.huwtl.penfold.domain.model.{AggregateVersion, AggregateId}

case class TaskRecordReference(id: AggregateId, version: AggregateVersion)
