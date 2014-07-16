package com.qmetric.penfold.readstore

import com.qmetric.penfold.domain.model.Status

sealed trait SortOrder {
  val name: String
}

object SortOrder {
  case object Asc extends SortOrder {
    override val name = "asc"
  }
  case object Desc extends SortOrder {
    override val name = "desc"
  }

  def from(str: String): SortOrder = {
    str.toLowerCase match {
      case Asc.name => Asc
      case Desc.name => Desc
      case unknown => throw new IllegalArgumentException(s"unknown sort order $unknown should be one of [asc, desc]")
    }
  }
}

case class SortOrderMapping(private val mappings: Map[Status, SortOrder]) {
  def sortOrderFor(status: Status) = mappings.getOrElse(status, SortOrder.Desc)
}