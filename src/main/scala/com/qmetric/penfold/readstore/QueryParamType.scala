package com.qmetric.penfold.readstore

sealed trait QueryParamType {
  val name: String
}

object QueryParamType {
  case object StringType extends QueryParamType {
    override val name = "string"
  }
  case object NumericType extends QueryParamType {
    override val name = "numeric"
  }

  def from(str: String): QueryParamType = {
    str.toLowerCase match {
      case StringType.name => StringType
      case NumericType.name => NumericType
      case _ => throw new IllegalArgumentException(s"unknown query param type: ${str}")
    }
  }
}