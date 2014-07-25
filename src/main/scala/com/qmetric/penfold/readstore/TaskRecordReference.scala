package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.model.{AggregateVersion, AggregateId}

case class TaskRecordReference(id: AggregateId, version: AggregateVersion)
