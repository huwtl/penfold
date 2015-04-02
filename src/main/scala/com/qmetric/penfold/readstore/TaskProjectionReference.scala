package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.model.{AggregateVersion, AggregateId}

case class TaskProjectionReference(id: AggregateId, version: AggregateVersion)
